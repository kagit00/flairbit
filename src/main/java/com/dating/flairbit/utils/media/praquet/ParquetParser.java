package com.dating.flairbit.utils.media.praquet;

import com.dating.flairbit.config.factory.ResponseFactory;
import com.dating.flairbit.dto.enums.NodeType;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.utils.media.csv.HeaderNormalizer;
import com.dating.flairbit.utils.media.csv.RowValidator;
import com.dating.flairbit.utils.media.csv.ValueSanitizer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@Slf4j
public class ParquetParser {

    private static final ThreadLocal<Map<String, String>> METADATA_MAP_THREAD_LOCAL =
            ThreadLocal.withInitial(HashMap::new);

    private static final ConcurrentHashMap<String, Map<String, Integer>> SCHEMA_CACHE = new ConcurrentHashMap<>();

    public static <T> Flux<T> parseInStream(InputStream inputStream, ResponseFactory<T> factory) {
        return Flux.create(sink -> {
            ParquetReader<GenericRecord> reader = null;
            Path tempFile = null;
            try {
                tempFile = Files.createTempFile("parquet-import-", ".parquet");
                try (OutputStream tempOut = Files.newOutputStream(tempFile)) {
                    inputStream.transferTo(tempOut);
                }

                reader = AvroParquetReader.<GenericRecord>builder(
                        HadoopInputFile.fromPath(new org.apache.hadoop.fs.Path(tempFile.toString()), new Configuration())
                ).build();

                Schema schema = null;
                Map<String, Integer> fieldMap = null;
                int rowIndex = 0;

                GenericRecord record;
                while ((record = reader.read()) != null && !sink.isCancelled()) {
                    rowIndex++;

                    if (schema == null) {
                        schema = record.getSchema();
                        fieldMap = validateSchema(schema);
                    }

                    T parsed = safelyParseRecord(fieldMap, record, rowIndex, factory);
                    if (parsed != null) {
                        sink.next(parsed);
                    }

                    // Log progress checkpoint with memory usage
                    if (rowIndex % 1_000_000 == 0) {
                        long memUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
                        log.info("Processed {} rows. Memory used: {} MB", rowIndex, memUsedMB);
                    }
                }
                sink.complete();
            } catch (IOException e) {
                log.error("Error parsing Parquet: {}", e.getMessage(), e);
                sink.error(new RuntimeException("Error parsing Parquet: " + e.getMessage(), e));
            } catch (Exception e) {
                log.error("Unexpected error during Parquet parsing: {}", e.getMessage(), e);
                sink.error(new RuntimeException("Unexpected error: " + e.getMessage(), e));
            } finally {
                cleanup(reader, tempFile);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    private static void cleanup(ParquetReader<GenericRecord> reader, Path tempFile) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Error closing Parquet reader: {}", e.getMessage());
            }
        }
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.error("Error deleting temporary file {}: {}", tempFile, e.getMessage());
            }
        }
    }

    private static Map<String, Integer> validateSchema(Schema schema) {
        // Use schema fingerprint as key for caching
        String schemaFingerprint = schema.toString();
        return SCHEMA_CACHE.computeIfAbsent(schemaFingerprint, k -> {
            Map<String, Integer> fieldMap = new HashMap<>();
            List<Schema.Field> fields = schema.getFields();
            for (int i = 0; i < fields.size(); i++) {
                fieldMap.put(fields.get(i).name(), i);
            }

            if (!fieldMap.containsKey(HeaderNormalizer.FIELD_GROUP_ID) || !fieldMap.containsKey(HeaderNormalizer.FIELD_REFERENCE_ID)) {
                log.error("Parquet schema missing required fields: groupId={}, referenceId={}",
                        fieldMap.containsKey(HeaderNormalizer.FIELD_GROUP_ID) ? "present" : "missing",
                        fieldMap.containsKey(HeaderNormalizer.FIELD_REFERENCE_ID) ? "present" : "missing");
                throw new RuntimeException("Parquet must contain groupId and referenceId fields");
            }
            return fieldMap;
        });
    }

    /**
     * Safely parses a single generic record, catching and logging any errors.
     */
    private static <T> T safelyParseRecord(Map<String, Integer> fieldMap, GenericRecord record, int rowIndex, ResponseFactory<T> factory) {
        try {
            return processRecord(fieldMap, record, rowIndex, factory);
        } catch (Exception e) {
            log.warn("Skipping row {} due to parsing error: {}. Record: {}", rowIndex, e.getMessage(), record);
            return null;
        }
    }

    private static <T> T processRecord(Map<String, Integer> fieldMap, GenericRecord record, int rowIndex, ResponseFactory<T> factory) {
        Map<String, String> metadata = METADATA_MAP_THREAD_LOCAL.get();
        metadata.clear();

        Map<String, String> specialFields = extractSpecialFields(fieldMap, record, rowIndex, metadata);

        String referenceId = specialFields.get(HeaderNormalizer.FIELD_REFERENCE_ID);
        String groupId = specialFields.get(HeaderNormalizer.FIELD_GROUP_ID);
        NodeType type = specialFields.get(HeaderNormalizer.FIELD_TYPE) != null
                ? parseNodeType(specialFields.get(HeaderNormalizer.FIELD_TYPE), rowIndex)
                : NodeType.USER;

        if (isRequiredFieldMissing(referenceId, groupId, rowIndex, record)) {
            return null;
        }

        return factory.createResponse(type, referenceId, metadata, groupId);
    }

    private static Map<String, String> extractSpecialFields(Map<String, Integer> fieldMap, GenericRecord record, int rowIndex, Map<String, String> metadata) {
        Map<String, String> specialFields = new HashMap<>();
        specialFields.put(HeaderNormalizer.FIELD_REFERENCE_ID, null);
        specialFields.put(HeaderNormalizer.FIELD_GROUP_ID, null);
        specialFields.put(HeaderNormalizer.FIELD_TYPE, null);

        for (Map.Entry<String, Integer> entry : fieldMap.entrySet()) {
            String fieldName = entry.getKey();
            Object value = record.get(fieldName);
            String strValue = value != null ? ValueSanitizer.sanitize(value.toString()) : null;

            if (isSpecialField(fieldName)) {
                specialFields.put(fieldName, strValue);
            } else {
                metadata.put(fieldName, strValue);
            }
        }
        return specialFields;
    }

    private static boolean isSpecialField(String fieldName) {
        return fieldName.equals(HeaderNormalizer.FIELD_TYPE) ||
                fieldName.equals(HeaderNormalizer.FIELD_REFERENCE_ID) ||
                fieldName.equals(HeaderNormalizer.FIELD_GROUP_ID);
    }

    private static NodeType parseNodeType(String value, int rowIndex) {
        if (value == null || value.isEmpty()) return null;
        try {
            return NodeType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Row {}: Unknown NodeType [{}]. Skipping type assignment.", rowIndex, value);
            return null;
        }
    }

    private static boolean isRequiredFieldMissing(String referenceId, String groupId, int rowIndex, GenericRecord record) {
        if (!RowValidator.isValid(referenceId, groupId)) {
            log.warn("Row {}: Missing required fields (reference_id={}, group_id={}). Record: {}",
                    rowIndex, referenceId, groupId, record);
            return true;
        }
        return false;
    }
}
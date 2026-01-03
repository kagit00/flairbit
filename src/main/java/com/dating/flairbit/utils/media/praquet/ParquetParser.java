package com.dating.flairbit.utils.media.praquet;

import com.dating.flairbit.config.factory.ResponseFactory;
import com.dating.flairbit.dto.enums.NodeType;
import com.dating.flairbit.utils.media.csv.HeaderNormalizer;
import com.dating.flairbit.utils.media.csv.RowValidator;
import com.dating.flairbit.utils.media.csv.ValueSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.LocalInputFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import static java.nio.file.StandardOpenOption.CREATE;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


@Slf4j
public class ParquetParser {


    public static <T> Flux<T> parseInStream(InputStream inputStream, ResponseFactory<T> factory) {
        // Step 1: Offload the blocking IO (File Copying) to a dedicated thread pool
        return Mono.fromCallable(() -> copyToTempFile(inputStream))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(tempFile ->
                        processParquetFile(tempFile, factory)
                                .subscribeOn(Schedulers.boundedElastic())
                );
    }

    private static Path copyToTempFile(InputStream inputStream) throws IOException {
        Path tempFile = Files.createTempFile("parquet-import-", ".parquet");
        try (OutputStream out = Files.newOutputStream(tempFile, CREATE)) {
            inputStream.transferTo(out);
        }

        return tempFile;
    }

    private static <T> Flux<T> processParquetFile(Path tempFile, ResponseFactory<T> factory) {
        // Step 2: Use Flux.using to manage the Lifecycle of the Reader AND the File deletion
        return Flux.using(
                () -> {
                    ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(
                            new LocalInputFile(tempFile) // Ensure LocalInputFile implements InputFile correctly
                    ).build();
                    return Tuples.of(reader, tempFile);
                },
                tuple -> readRows(tuple.getT1(), factory),
                tuple -> cleanup(tuple.getT1(), tuple.getT2())
        );
    }

    private static <T> Flux<T> readRows(ParquetReader<GenericRecord> reader, ResponseFactory<T> factory) {
        return Flux.generate(
                GeneratorState::new,
                (state, sink) -> {
                    try {

                        GenericRecord record = reader.read();

                        if (record == null) {
                            sink.complete();
                            return state;
                        }

                        state.rowIndex++;

                        // Validate schema on first record only
                        if (state.fieldMap == null) {
                            state.fieldMap = parseAndValidateSchema(record.getSchema());
                        }

                        T parsed = safelyParseRecord(state.fieldMap, record, state.rowIndex, factory);
                        if (parsed != null) {
                            sink.next(parsed);
                        }

                        // Log progress
                        if (state.rowIndex % 1_000_000 == 0) {
                            logProgress(state.rowIndex);
                        }

                    } catch (Exception e) {
                        sink.error(e);
                    }
                    return state;
                }
        );
    }

    private static void cleanup(ParquetReader<GenericRecord> reader, Path tempFile) {
        try {
            if (reader != null) reader.close();
        } catch (IOException e) {
            log.error("Error closing Parquet reader", e);
        }

        try {
            if (tempFile != null) Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.error("Error deleting temp file: {}", tempFile, e);
        }
    }

    private static Map<String, Integer> parseAndValidateSchema(Schema schema) {
        Map<String, Integer> fieldMap = new HashMap<>();
        List<Schema.Field> fields = schema.getFields();
        for (int i = 0; i < fields.size(); i++) {
            fieldMap.put(fields.get(i).name(), i);
        }

        boolean hasGroupId = fieldMap.containsKey(HeaderNormalizer.FIELD_GROUP_ID);
        boolean hasRefId = fieldMap.containsKey(HeaderNormalizer.FIELD_REFERENCE_ID);

        if (!hasGroupId || !hasRefId) {
            log.error("Parquet schema missing required fields. GroupId: {}, RefId: {}", hasGroupId, hasRefId);
            throw new IllegalArgumentException("Parquet must contain groupId and referenceId fields");
        }
        return fieldMap;
    }

    private static <T> T safelyParseRecord(Map<String, Integer> fieldMap, GenericRecord record, long rowIndex, ResponseFactory<T> factory) {
        try {
            // New Map per record. Safe for downstream async processing.
            Map<String, String> metadata = new HashMap<>();

            Map<String, String> specialFields = extractSpecialFields(fieldMap, record, metadata);

            String referenceId = specialFields.get(HeaderNormalizer.FIELD_REFERENCE_ID);
            String groupId = specialFields.get(HeaderNormalizer.FIELD_GROUP_ID);
            String typeStr = specialFields.get(HeaderNormalizer.FIELD_TYPE);

            NodeType type = (typeStr != null) ? parseNodeType(typeStr, rowIndex) : NodeType.USER;

            if (isRequiredFieldMissing(referenceId, groupId, rowIndex, record)) {
                return null;
            }

            return factory.createResponse(type, referenceId, metadata, groupId);
        } catch (Exception e) {
            log.warn("Skipping row {} due to parsing error: {}. Record: {}", rowIndex, e.getMessage(), record);
            return null;
        }
    }

    private static Map<String, String> extractSpecialFields(Map<String, Integer> fieldMap, GenericRecord record, Map<String, String> metadataTarget) {
        Map<String, String> specialFields = new HashMap<>();

        for (Map.Entry<String, Integer> entry : fieldMap.entrySet()) {
            String fieldName = entry.getKey();
            Object rawValue = record.get(entry.getValue()); // Access by index is faster than name
            String strValue = rawValue != null ? ValueSanitizer.sanitize(rawValue.toString()) : null;

            if (isSpecialField(fieldName)) {
                specialFields.put(fieldName, strValue);
            } else {
                metadataTarget.put(fieldName, strValue);
            }
        }
        return specialFields;
    }

    private static boolean isSpecialField(String fieldName) {
        return HeaderNormalizer.FIELD_TYPE.equals(fieldName) ||
                HeaderNormalizer.FIELD_REFERENCE_ID.equals(fieldName) ||
                HeaderNormalizer.FIELD_GROUP_ID.equals(fieldName);
    }

    private static NodeType parseNodeType(String value, long rowIndex) {
        if (value == null || value.isEmpty()) return null;
        try {
            return NodeType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Row {}: Unknown NodeType [{}]. Skipping type assignment.", rowIndex, value);
            return null;
        }
    }

    private static boolean isRequiredFieldMissing(String referenceId, String groupId, long rowIndex, GenericRecord record) {
        if (!RowValidator.isValid(referenceId, groupId)) {
            log.warn("Row {}: Missing required fields (id={}, group={})", rowIndex, referenceId, groupId);
            return true;
        }
        return false;
    }

    private static void logProgress(long count) {
        long memUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        log.info("Processed {} rows. Memory used: {} MB", count, memUsedMB);
    }

    private static class GeneratorState {
        long rowIndex = 0;
        Map<String, Integer> fieldMap;
    }
}
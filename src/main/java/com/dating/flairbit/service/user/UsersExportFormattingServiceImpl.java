package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.service.MinioUploadService;
import com.dating.flairbit.utils.media.csv.CsvExporter;
import com.dating.flairbit.utils.media.csv.HeaderNormalizer;
import com.dating.flairbit.utils.media.csv.UserFieldsExtractor;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import com.opencsv.CSVWriter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;


@Slf4j
@Component
public class UsersExportFormattingServiceImpl implements UsersExportFormattingService {

    private final MeterRegistry meterRegistry;
    private final RetryTemplate retryTemplate;
    private final ThreadPoolTaskExecutor exportExecutor;
    private final MinioUploadService minioUploadService;

    @Value("${export.base-dir}")
    private String baseDir;

    @Value("${export.batch-size:1000}")
    private int batchSize;

    @Value("${export.minio.bucket:flairbit-exports}")
    private String bucketName;

    public UsersExportFormattingServiceImpl(
            MeterRegistry meterRegistry,
            RetryTemplate retryTemplate,
            @Qualifier("usersExportExecutor") ThreadPoolTaskExecutor exportExecutor,
            MinioUploadService minioUploadService
    ) {
        this.meterRegistry = meterRegistry;
        this.retryTemplate = retryTemplate;
        this.exportExecutor = exportExecutor;
        this.minioUploadService = minioUploadService;
    }

    @Override
    public CompletableFuture<ExportedFile> exportCsv(List<UserExportDTO> userDtos, String groupId, UUID domainId) {
        return CompletableFuture.supplyAsync(() -> {

            List<UserFieldsExtractor.UserView> userViews = userDtos.stream()
                    .filter(dto -> groupId.equalsIgnoreCase(dto.groupId()))
                    .map(dto -> new UserFieldsExtractor.UserView(
                            RequestMakerUtility.buildUserFromUserExportDTO(dto),
                            RequestMakerUtility.buildProfileFromUserExportDto(dto)
                    ))
                    .toList();

            if (userViews.isEmpty()) {
                log.info("No valid user views for group '{}'", groupId);
                return null;
            }

            return retryTemplate.execute(context -> {
                try {
                    long startTime = System.nanoTime();
                    Path fullPath = createFilePath(groupId, domainId);

                    try (Writer writer = new OutputStreamWriter(
                            new GZIPOutputStream(Files.newOutputStream(fullPath)),
                            StandardCharsets.UTF_8
                    )) {
                        CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '"', "\n");

                        List<CsvExporter.FieldExtractor<UserFieldsExtractor.UserView>> extractors = UserFieldsExtractor.fieldExtractors();
                        String[] headers = new String[extractors.size() + 1];
                        headers[0] = HeaderNormalizer.FIELD_GROUP_ID;
                        for (int i = 0; i < extractors.size(); i++) {
                            headers[i + 1] = extractors.get(i).header();
                        }

                        csvWriter.writeNext(headers);
                        for (UserFieldsExtractor.UserView userView : userViews) {
                            csvWriter.writeNext(CsvExporter.mapEntityToCsvRow(userView, groupId, extractors), false);
                        }
                    }

                    String fileName = fullPath.getFileName().toString();
                    String objectName = String.format("%s/%s/%s", domainId, groupId, fileName);
                    minioUploadService.upload(fullPath.toString(), objectName);

                    String remoteUrl = String.format("%s/%s/%s/%s", baseDir, domainId, groupId, fileName).replaceAll("(?<!:)//", "/");
                    log.info("Uploaded CSV to MinIO: bucket={}, object={}", bucketName, fileName);

                    long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                    meterRegistry.timer("users_export_csv_duration", "groupId", groupId).record(durationMs, TimeUnit.MILLISECONDS);
                    meterRegistry.counter("users_export_csv_processed", "groupId", groupId).increment(userViews.size());
                    log.info("----------{} {}", fullPath, remoteUrl);

                    return new ExportedFile(null, fullPath.getFileName().toString(),
                            "application/gzip", groupId, domainId, remoteUrl);

                } catch (IOException e) {
                    log.error("Error exporting CSV for group '{}': {}", groupId, e.getMessage());
                    meterRegistry.counter("users_export_csv_failures", "groupId", groupId).increment();
                    throw new InternalServerErrorException("Failed to export CSV: " + e.getMessage());
                }
            });
        }, exportExecutor);
    }

    @Override
    public List<String> extractEligibleUsernames(List<UserExportDTO> userDtos, String groupId) {
        return userDtos.stream()
                .filter(dto -> groupId.equalsIgnoreCase(dto.groupId()))
                .map(UserExportDTO::username)
                .distinct()
                .toList();
    }

    private synchronized Path createFilePath(String groupId, UUID domainId) throws IOException {
        Path localTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "flairbit-exports", domainId.toString(), groupId);
        Files.createDirectories(localTempDir);

        String fileName = groupId + "_users_batch_" + UUID.randomUUID() + ".csv.gz";
        Path localFilePath = localTempDir.resolve(fileName);

        log.info("Temporary export file created at: {}", localFilePath);

        return localFilePath;
    }

}

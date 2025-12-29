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
            if (userDtos == null || userDtos.isEmpty()) {
                return null;
            }

            // Perform logic inside retry template
            try {
                return retryTemplate.execute(context -> {
                    long startTime = System.nanoTime();
                    Path localPath = createFilePath(groupId, domainId);

                    try {
                        // 1. Generate CSV
                        writeCsvToFile(userDtos, localPath, groupId);

                        // 2. Prepare MinIO paths
                        String fileName = localPath.getFileName().toString();
                        String objectName = String.format("%s/%s/%s", domainId, groupId, fileName);

                        minioUploadService.upload(localPath.toString(), objectName);

                        // 4. Finalize
                        String remoteUrl = String.format("%s/%s/%s/%s", baseDir, domainId, groupId, fileName)
                                .replaceAll("(?<!:)//", "/");

                        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                        meterRegistry.timer("users_export_csv_duration", "groupId", groupId).record(durationMs, TimeUnit.MILLISECONDS);

                        log.info("Export/Upload complete for group '{}'. URL: {}", groupId, remoteUrl);

                        return new ExportedFile(null, fileName, "application/gzip", groupId, domainId, remoteUrl);

                    } catch (Exception e) {
                        log.error("Batch attempt {} failed for group '{}': {}", context.getRetryCount(), groupId, e.getMessage());
                        throw new RuntimeException("Export failed", e);
                    }
                });
            } catch (IOException e) {
                throw new InternalServerErrorException("Export failed for groupId " + groupId + " and domainId " + domainId + " " + e.getMessage());
            }
        }, exportExecutor);
    }

    private void writeCsvToFile(List<UserExportDTO> userDtos, Path path, String groupId) throws IOException {
        try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(path)), StandardCharsets.UTF_8)) {
            CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '"', "\n");
            List<CsvExporter.FieldExtractor<UserFieldsExtractor.UserView>> extractors = UserFieldsExtractor.fieldExtractors();

            // Header
            String[] headers = new String[extractors.size() + 1];
            headers[0] = HeaderNormalizer.FIELD_GROUP_ID;
            for (int i = 0; i < extractors.size(); i++) headers[i + 1] = extractors.get(i).header();
            csvWriter.writeNext(headers);

            // Data
            for (UserExportDTO dto : userDtos) {
                UserFieldsExtractor.UserView view = new UserFieldsExtractor.UserView(
                        RequestMakerUtility.buildUserFromUserExportDTO(dto),
                        RequestMakerUtility.buildProfileFromUserExportDto(dto)
                );
                csvWriter.writeNext(CsvExporter.mapEntityToCsvRow(view, groupId, extractors), false);
            }
        }
    }

    @Override
    public List<String> extractEligibleUsernames(List<UserExportDTO> userDtos, String groupId) {
        return userDtos.stream()
                .filter(dto -> groupId.equalsIgnoreCase(dto.groupId()))
                .map(UserExportDTO::username)
                .distinct()
                .toList();
    }

    private Path createFilePath(String groupId, UUID domainId) throws IOException {
        Path localTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "flairbit-exports", domainId.toString(), groupId);
        Files.createDirectories(localTempDir);
        String fileName = groupId + "_users_batch_" + UUID.randomUUID() + ".csv.gz";
        return localTempDir.resolve(fileName);
    }
}
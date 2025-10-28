package com.dating.flairbit.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MinioUploadService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioUploadService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Uploads a local file to the configured MinIO bucket.
     *
     * @param localPath  the path of the local file to upload
     * @param objectName the object name to use in MinIO
     */
    public void upload(String localPath, String objectName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created new bucket: {}", bucketName);
            }

            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(localPath)
                            .build()
            );
            log.info("Uploaded file '{}' to bucket '{}'", objectName, bucketName);
        } catch (Exception e) {
            log.error("Failed to upload file {}: {}", localPath, e.getMessage());
        }
    }
}

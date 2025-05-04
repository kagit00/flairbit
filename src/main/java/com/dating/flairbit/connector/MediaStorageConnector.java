package com.dating.flairbit.connector;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dating.flairbit.config.MediaStorageProperties;
import com.dating.flairbit.dto.MediaUploadRequest;
import com.dating.flairbit.dto.MediaUploadResponse;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class MediaStorageConnector implements ThirdPartyConnector<MediaUploadRequest, MediaUploadResponse> {

    private final Cloudinary cloudinary;
    private final MediaStorageProperties properties;

    @Override
    public boolean supports(String integrationKey) {
        return "storage.cloud".equalsIgnoreCase(integrationKey);
    }

    @Override
    public MediaUploadResponse call(@Valid MediaUploadRequest request) {
        MultipartFile file = request.getFile();
        validateFile(file);

        try {
            log.info("Uploading file {} (type: {}, size: {}) to Cloudinary",
                    file.getOriginalFilename(), file.getContentType(), file.getSize());

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", properties.getUploadFolder(),
                    "resource_type", determineType(file),
                    "public_id", DefaultValuesPopulator.getUid(),
                    "use_filename", true
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().uploadLarge(
                    file.getBytes(),
                    uploadParams
            );

            log.info("Files uploaded successfully.");

            return MediaUploadResponse.builder()
                    .publicId((String) uploadResult.get("public_id"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .build();
        } catch (IOException e) {
            log.error("upload failed for file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new InternalServerErrorException("Failed to upload file to Cloudinary" + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during Cloudinary upload: {}", e.getMessage());
            throw new InternalServerErrorException("Unexpected upload error" + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }
        if (file.getSize() > properties.getMaxFileSize()) {
            throw new BadRequestException("File size exceeds limit: " + properties.getMaxFileSize() / (1024 * 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedTypes().contains(contentType)) {
            throw new BadRequestException("Unsupported file type: " + contentType);
        }
    }

    private String determineType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("video")) {
                return "video";
            }
            if (contentType.startsWith("image")) {
                return "image";
            }
        }
        throw new BadRequestException("Cannot determine file type for: " + file.getOriginalFilename());
    }
}


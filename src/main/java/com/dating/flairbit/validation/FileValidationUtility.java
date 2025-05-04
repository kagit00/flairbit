package com.dating.flairbit.validation;

import com.dating.flairbit.dto.enums.ReelType;
import com.dating.flairbit.exceptions.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public final class FileValidationUtility {

    private FileValidationUtility() {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    public static void validateFile(MultipartFile file, ReelType reelType, int maxFileSize) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        if (contentType == null || filename == null) {
            throw new BadRequestException("Invalid file metadata");
        }

        if (file.getSize() > (long) maxFileSize * 1024 * 1024) {
            throw new BadRequestException("File size exceeds " + maxFileSize + "MB");
        }

        if (reelType == ReelType.PRIMARY) {
            if (!contentType.startsWith("image/") || !filename.matches(".*\\.(jpg|jpeg|png)")) {
                throw new BadRequestException("Only .jpg/.jpeg/.png allowed for PRIMARY");
            }
        } else if (!contentType.startsWith("video/") || !filename.matches(".*\\.(mp4|mov)")) {
            throw new BadRequestException("Only .mp4/.mov allowed for " + reelType);
        }
    }
}
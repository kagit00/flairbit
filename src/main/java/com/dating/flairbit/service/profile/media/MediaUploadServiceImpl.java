package com.dating.flairbit.service.profile.media;

import com.dating.flairbit.dto.MediaUploadResponse;
import com.dating.flairbit.dto.enums.ReelType;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.MediaFile;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.MediaFileRepository;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.ProfileUtils;
import com.dating.flairbit.validation.FileValidationUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MediaUploadServiceImpl implements MediaUploadService{
    private final MediaFileRepository mediaFileRepository;
    private final UserService userService;
    private final ProfileProcessor profileProcessor;
    @Value("${file.upload.max-size}")
    private  int maxFileSize;
    @Value("${reels.upload.max-count}")
    private int maxReels;


    @Override
    @CacheEvict(value = {"profileCache"}, key = "#email")
    public void processUploading(String email, MultipartFile file, MediaUploadResponse response, ReelType reelType, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);

        if (Objects.isNull(profile)) throw new BadRequestException("No profile found with intent");

        FileValidationUtility.validateFile(file, reelType, maxFileSize);

        MediaFile mediaFile = mediaFileRepository.findByProfileAndReelType(profile, reelType)
                .map(existing -> updateExistingMedia(existing, file, response))
                .orElseGet(() -> createNewMedia(file, response, profile, reelType));

        mediaFileRepository.save(mediaFile);
    }

    private MediaFile updateExistingMedia(MediaFile existing, MultipartFile file, MediaUploadResponse response) {
        existing.setOriginalFileName(file.getOriginalFilename());
        existing.setFileType(file.getContentType());
        existing.setFileSize(file.getSize());
        existing.setFilePath(response.getSecureUrl());
        return existing;
    }

    private MediaFile createNewMedia(MultipartFile file, MediaUploadResponse response, Profile profile, ReelType reelType) {
        if (mediaFileRepository.countByProfile(profile) >= maxReels) {
            throw new BadRequestException("Maximum " + maxReels + " reels allowed");
        }
        return MediaFile.builder()
                .originalFileName(file.getOriginalFilename()).fileType(file.getContentType())
                .fileSize(file.getSize()).filePath(response.getSecureUrl())
                .profile(profile).reelType(reelType)
                .build();
    }
}

package com.dating.flairbit.service.profile.media;

import com.dating.flairbit.dto.MediaUploadResponse;
import com.dating.flairbit.dto.enums.ReelType;
import org.springframework.web.multipart.MultipartFile;

public interface MediaUploadService {
    void processUploading(String email, MultipartFile file, MediaUploadResponse response, ReelType reelType, String intent);
}

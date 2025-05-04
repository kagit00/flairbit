package com.dating.flairbit.controller.profile.media;

import com.dating.flairbit.connector.ThirdPartyConnectorDispatcher;
import com.dating.flairbit.dto.MediaUploadRequest;
import com.dating.flairbit.dto.MediaUploadResponse;
import com.dating.flairbit.dto.enums.ReelType;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.service.profile.media.MediaUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/users/{email}/media")
@RequiredArgsConstructor
public class MediaUploadController {
    private final ThirdPartyConnectorDispatcher dispatcher;
    private final MediaUploadService mediaUploadService;
    @Value("${reels.upload.max-count}")
    private int maxReels;

    @PostMapping("/upload/{intent}")
    @Transactional
    public ResponseEntity<Void> upload(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable("email") String email,
            @RequestParam("reelTypes") ReelType[] reelTypes,
            @PathVariable String intent) {
        if (files.length == 0 || files.length > maxReels) {
            throw new BadRequestException("Must provide 1 to " + maxReels + " files");
        }

        if (files.length != reelTypes.length) {
            throw new BadRequestException("Number of files must match number of reel types");
        }

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            ReelType reelType = reelTypes[i];
            MediaUploadRequest request = MediaUploadRequest.builder().file(file).build();
            MediaUploadResponse response = dispatcher.dispatch("storage.cloud", request);
            mediaUploadService.processUploading(email, file, response, reelType, intent);
        }

        return ResponseEntity.ok().build();
    }

}

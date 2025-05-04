package com.dating.flairbit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaUploadRequest {
    @NotNull(message = "File cannot be null")
    private MultipartFile file;
}

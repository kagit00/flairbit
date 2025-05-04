package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.ReelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class MediaFileRequest {
    private String originalFileName;
    private String fileType;
    private ReelType reelType;
    private Long fileSize;
    private String filePath;
    private int displayOrder;
}
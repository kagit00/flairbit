package com.dating.flairbit.dto;

import com.dating.flairbit.dto.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFileResponse {
    private String url;
    private MediaType type;
    private int displayOrder;
    private LocalDateTime uploadedAt;
}

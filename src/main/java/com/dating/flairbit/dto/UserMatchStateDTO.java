package com.dating.flairbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserMatchStateDTO {
    private boolean sentToMatchingService = false;
    private boolean profileComplete = false;
    private boolean readyForMatching = false;
    private String intent;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalDateTime lastMatchedAt;
    private String groupId;
}

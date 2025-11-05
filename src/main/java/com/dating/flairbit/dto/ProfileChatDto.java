package com.dating.flairbit.dto;

import com.dating.flairbit.models.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileChatDto {
    private UUID id;
    private String displayName;

    public static ProfileChatDto from(Profile p) {
        ProfileChatDto dto = new ProfileChatDto();
        dto.setId(p.getId());
        dto.setDisplayName(p.getDisplayName());
        return dto;
    }
}
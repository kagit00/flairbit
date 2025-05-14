package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.models.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UsersExportFormattingService {
    CompletableFuture<ExportedFile> exportCsv(List<UserExportDTO> userDtos, String groupId, UUID domainId);
    List<String> extractEligibleUsernames(List<UserExportDTO> userDtos, String groupIds);
}

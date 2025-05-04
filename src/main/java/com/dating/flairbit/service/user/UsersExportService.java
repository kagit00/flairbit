package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.models.User;

import java.util.List;
import java.util.UUID;

public interface UsersExportService {
    ExportedFile exportCsv(List<User> users, String groupId, UUID domainId);
    List<String> extractEligibleUsernames(List<User> users, String groupId);
}

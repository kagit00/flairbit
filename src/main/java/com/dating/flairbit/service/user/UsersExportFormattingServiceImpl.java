package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.models.*;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import com.dating.flairbit.utils.media.csv.UserFieldsExtractor;

import com.dating.flairbit.utils.media.csv.CsvExporter;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UsersExportFormattingServiceImpl implements UsersExportFormattingService {

    @Override
    @Transactional
    public ExportedFile exportCsv(List<UserExportDTO> userDtos, String groupId, UUID domainId) {
        List<UserFieldsExtractor.UserView> userViews = userDtos.stream()
                .filter(dto -> groupId.equalsIgnoreCase(dto.getGroupId()) && dto.getReadyForMatching())
                .map(dto -> {
                    User user = RequestMakerUtility.buildUserFromUserExportDTO(dto);
                    Profile profile = RequestMakerUtility.buildProfileFromUserExportDto(dto);
                    return new UserFieldsExtractor.UserView(user, profile);
                })
                .toList();

        String csv = CsvExporter.exportToCsvString(userViews, groupId, UserFieldsExtractor.fieldExtractors());

        return new ExportedFile(
                csv.getBytes(StandardCharsets.UTF_8),
                groupId + "_users.csv",
                "text/csv",
                groupId,
                domainId
        );
    }

    @Override
    @Transactional
    public List<String> extractEligibleUsernames(List<UserExportDTO> userDtos, String groupId) {
        return userDtos.stream()
                .filter(dto -> groupId.equalsIgnoreCase(dto.getGroupId()) && dto.getReadyForMatching())
                .map(dto -> StringConcatUtil.concatWithSeparator("_", dto.getUsername(), dto.getIntent().toLowerCase()))
                .distinct()
                .toList();
    }
}


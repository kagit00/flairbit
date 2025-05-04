package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.models.UserMatchState;
import com.dating.flairbit.utils.media.csv.UserFieldsExtractor;

import com.dating.flairbit.utils.media.csv.CsvExporter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UsersExportServiceImpl implements UsersExportService {

    @Override
    public ExportedFile exportCsv(List<User> users, String groupId, UUID domainId) {
        List<UserFieldsExtractor.UserView> userViews = users.stream()
                .flatMap(user -> {
                    if (user.getProfiles() == null) return Stream.empty();
                    return user.getProfiles().stream()
                            .filter(p -> {
                                UserMatchState matchState = p.getUserMatchState();
                                return matchState != null &&
                                        groupId.equals(matchState.getGroupId()) &&
                                        matchState.isReadyForMatching();
                            })
                            .map(profile -> new UserFieldsExtractor.UserView(user, profile));
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
    public List<String> extractEligibleUsernames(List<User> users, String groupId) {
        return users.stream()
                .flatMap(user -> {
                    if (user.getProfiles() == null) return Stream.empty();
                    return user.getProfiles().stream()
                            .filter(p -> {
                                UserMatchState matchState = p.getUserMatchState();
                                return matchState != null &&
                                        groupId.equals(matchState.getGroupId()) &&
                                        matchState.isReadyForMatching();
                            })
                            .map(profile -> profile.getUser().getUsername());
                })
                .distinct()
                .toList();
    }

}


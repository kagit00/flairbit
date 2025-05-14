package com.dating.flairbit.utils.request;

import com.dating.flairbit.dto.*;
import com.dating.flairbit.dto.NodesTransferJobExchange;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.dto.enums.InteractionType;
import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.dto.enums.NodeType;
import com.dating.flairbit.models.*;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.time.LocalDate;
import java.util.*;


@UtilityClass
@Slf4j
public final class RequestMakerUtility {

    public static MatchSuggestionsImportJob createMatchSuggestionsImportJob(String groupId, JobStatus status, int total, int processed) {
        return MatchSuggestionsImportJob.builder()
                .groupId(groupId).status(status)
                .totalRows(total).processedRows(processed)
                .startedAt(DefaultValuesPopulator.getCurrentTimestamp())
                .build();
    }

    public static List<MatchSuggestion> convertResponsesToMatchSuggestions(List<MatchSuggestionDTO> responses, String groupId) {
        List<MatchSuggestion> result = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            MatchSuggestionDTO res = responses.get(i);

            try {
                MatchSuggestion matchSuggestion = MatchSuggestion.builder()
                        .groupId(groupId)
                        .participantId(res.getParticipantId())
                        .matchedParticipantId(res.getMatchedParticipantId())
                        .compatibilityScore(res.getCompatibilityScore())
                        .matchSuggestionType(res.getMatchSuggestionType())
                        .build();

                matchSuggestion.setCreatedAt(DefaultValuesPopulator.getCurrentTimestamp());
                result.add(matchSuggestion);
            } catch (Exception e) {
                log.error("Error transforming MatchSuggestion at index {}: {}", i, e.getMessage(), e);
            }
        }
        log.info("{} valid Match Suggestions out of {}", result.size(), responses.size());
        return result;
    }

    public static NodeExchange buildCostBasedNodes(String groupId, String filePath, String fileName, String contentType, UUID domainId) {
        return NodeExchange.builder()
                .groupId(groupId).filePath(filePath).fileName(fileName).contentType(contentType).domainId(domainId)
                .build();
    }

    public static NodeExchange buildNonCostBasedNodesPayload(String groupId, List<String> usernames, UUID domainId) {
        return NodeExchange.builder()
                .groupId(groupId).referenceIds(usernames).type(NodeType.USER).domainId(domainId)
                .build();
    }

    public static NodesTransferJobExchange buildImportJobReq(UUID jobId, String groupId, String status, int processedRows, int totalRows,
                                                             List<String> successList, List<String> failedList, String domainId) {
        return NodesTransferJobExchange.builder()
                .domainId(UUID.fromString(domainId)).groupId(groupId)
                .jobId(jobId).successList(successList).failedList(failedList)
                .processed(processedRows).status(status).total(totalRows)
                .build();
    }

    public static ReelInteraction getReelInteraction(User user, String interactionType, MediaFile reel) {
        return ReelInteraction.builder()
                .reel(reel).user(user).interactionType(InteractionType.valueOf(interactionType))
                .createdAt(DefaultValuesPopulator.getCurrentTimestamp())
                .build();
    }

    public static UserMatchStateDTO build(String intent, String groupId, String gender, LocalDate dob) {
        return  UserMatchStateDTO.builder()
                .intent(intent)
                .gender(gender)
                .dateOfBirth(dob)
                .groupId(groupId)
                .sentToMatchingService(false)
                .profileComplete(false)
                .build();
    }

    public static List<UserExportDTO> transformToUserExportDTO(List<Object[]> results) {
        return results.stream().map(result ->
                new UserExportDTO((UUID) result[0], (String) result[1], (UUID) result[2], (String) result[3], (String) result[4],
                        (String) result[5], (Boolean) result[6], (Boolean) result[7], (String) result[8], (Boolean) result[9],
                        (String) result[10], (Integer) result[11], (Integer) result[12], (String) result[13], (Boolean) result[14],
                        (String) result[15], (String) result[16], (String) result[17], ((java.sql.Date) result[18]).toLocalDate(), (String) result[19],
                        (Boolean) result[20], (String) result[21]
                )
        ).toList();
    }

    public static User buildUserFromUserExportDTO(UserExportDTO dto) {
        return User.builder()
                .id(dto.userId())
                .username(dto.username())
                .build();
    }

    public static Profile buildProfileFromUserExportDto(UserExportDTO dto) {
        return Profile.builder()
                .id(dto.profileId())
                .displayName(dto.displayName())
                .bio(dto.bio())
                .location(
                        Location.builder()
                                .city(dto.city())
                                .build()
                ).lifestyle(
                        Lifestyle.builder()
                                .smokes(dto.smokes())
                                .drinks(dto.drinks())
                                .religion(dto.religion())
                                .build()
                ).preferences(
                        Preferences.builder()
                                .wantsKids(dto.wantsKids())
                                .preferredGenders(new HashSet<>(Arrays.asList(dto.preferredGenders().split(","))))
                                .preferredMinAge(dto.preferredMinAge())
                                .preferredMaxAge(dto.preferredMaxAge())
                                .openToLongDistance(dto.openToLongDistance())
                                .build()
                ).education(
                        Education.builder()
                                .fieldOfStudy(dto.fieldOfStudy())
                                .build()
                ).profession(
                        Profession.builder()
                                .industry(dto.industry())
                                .build()
                ).userMatchState(UserMatchState.builder()
                        .gender(dto.gender())
                        .dateOfBirth(dto.dateOfBirth())
                        .intent(dto.intent())
                        .readyForMatching(dto.readyForMatching())
                        .build()
                ).build();
    }

    public static FileSystemMultipartFile fromPayload(MatchSuggestionsExchange payload) {
        return new FileSystemMultipartFile(payload.getFilePath(), payload.getFileName(), payload.getContentType());
    }

}

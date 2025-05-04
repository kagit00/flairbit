package com.dating.flairbit.utils.request;

import com.dating.flairbit.dto.*;
import com.dating.flairbit.dto.NodesTransferJobExchange;
import com.dating.flairbit.dto.enums.InteractionType;
import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.dto.enums.NodeType;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.models.*;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.GZIPOutputStream;

@Slf4j
public final class RequestMakerUtility {
    private static final long MAX_INPUT_SIZE = 500_000_000; // 500 MB
    private static final long MAX_JSON_SIZE = 50_000_000; // 50 MB

    private RequestMakerUtility() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

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
                        .build();

                matchSuggestion.setCreatedAt(DefaultValuesPopulator.getCurrentTimestamp());
                result.add(matchSuggestion);
            } catch (Exception e) {
                log.error("Error transforming MatchSuggestion at index {}: {}", i, e.getMessage(), e);
            }
        }
        log.info("{} valid MatchInfo out of {}", result.size(), responses.size());
        return result;
    }

    public static NodeExchange buildCostBasedNodes(String groupId, byte[] fileContent, String fileName, String contentType, UUID domainId) {
        if (fileContent.length > MAX_INPUT_SIZE) {
            throw new BadRequestException("Input fileContent too large: " + fileContent.length + " bytes");
        }

        byte[] compressedContent = compress(fileContent);
        String base64Content = Base64.getEncoder().encodeToString(compressedContent);

        NodeExchange message = NodeExchange.builder()
                .groupId(groupId)
                .fileContent(base64Content)
                .fileName(fileName)
                .contentType(contentType)
                .domainId(domainId)
                .build();

        String json = BasicUtility.stringifyObject(message);
        if (json.length() > MAX_JSON_SIZE) {
            throw new IllegalStateException("Serialized NodeExportMessage too large: " + json.length() + " bytes");
        }

        return message;
    }

    private static byte[] compress(byte[] data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream)) {
            gzip.write(data);
            gzip.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to compress data: " + e.getMessage());
        }
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
}

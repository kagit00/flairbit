package com.dating.flairbit.service.importjob;


import com.dating.flairbit.dto.MatchSuggestionsExchange;
import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.models.MatchSuggestionsImportJob;
import com.dating.flairbit.repo.MatchSuggestionsImportJobRepository;
import com.dating.flairbit.service.match.MatchSuggestionsImportJobService;
import com.dating.flairbit.utils.FileValidationUtility;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import com.dating.flairbit.validation.MatchExportValidator;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Slf4j
@RequiredArgsConstructor
@Service
public class ImportJobServiceImpl implements ImportJobService {

    private final MatchSuggestionsImportJobRepository matchSuggestionsImportJobRepository;
    private final MatchSuggestionsImportJobService matchSuggestionsImportJobService;
    private static final int BATCH_SIZE = 10000;
    private final MinioClient minioClient;

    @Override
    @Transactional
    public CompletableFuture<Void> startMatchesImport(MatchSuggestionsExchange payload) {
        if (!MatchExportValidator.isValidPayload(payload)) {
            return CompletableFuture.failedFuture(new BadRequestException("Invalid payload for matches export"));
        }

        return CompletableFuture.supplyAsync(() -> {
            UUID jobId = initiateNodesImport(payload);
            log.info("Received file for group '{}': name={} path={}", payload.getGroupId(), payload.getFileName(), payload.getFilePath());
            MultipartFile file = RequestMakerUtility.resolvePayload(payload, minioClient);
            FileValidationUtility.validateInput(file, payload.getGroupId());
            matchSuggestionsImportJobService.processImportedMatchSuggestions(jobId, file, payload.getGroupId(), BATCH_SIZE);
            return (Void) null;
        }).exceptionally(throwable -> {
            log.error("Failed to start matches import", throwable);
            throw new InternalServerErrorException(throwable.getMessage());
        });
    }

    private UUID initiateNodesImport(MatchSuggestionsExchange payload) {
        MatchSuggestionsImportJob job = RequestMakerUtility.createMatchSuggestionsImportJob(
                payload.getGroupId(), JobStatus.PENDING, 0, 0);
        matchSuggestionsImportJobRepository.save(job);
        return job.getId();
    }
}
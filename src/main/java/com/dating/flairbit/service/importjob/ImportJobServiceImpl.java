package com.dating.flairbit.service.importjob;

import com.dating.flairbit.dto.BytesMultipartFile;
import com.dating.flairbit.dto.FileSystemMultipartFile;
import com.dating.flairbit.dto.MatchSuggestionsExchange;
import com.dating.flairbit.dto.NodeExchange;
import com.dating.flairbit.dto.enums.JobStatus;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.models.MatchSuggestionsImportJob;
import com.dating.flairbit.processor.MatchSuggestionsImportJobProcessor;
import com.dating.flairbit.repo.MatchSuggestionsImportJobRepository;
import com.dating.flairbit.service.match.MatchSuggestionsImportJobService;
import com.dating.flairbit.utils.FileValidationUtility;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import com.dating.flairbit.validation.MatchExportValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobServiceImpl implements ImportJobService {

    private final MatchSuggestionsImportJobRepository matchSuggestionsImportJobRepository;
    private final MatchSuggestionsImportJobService matchSuggestionsImportJobService;
    private static final int BATCH_SIZE = 10000;


    @Override
    @Transactional
    public void startMatchesImport(MatchSuggestionsExchange payload) {
        if (!MatchExportValidator.isValidPayload(payload)) throw new BadRequestException("Not valid payload for matches export");

        try {
            UUID jobId = initiateNodesImport(payload);
            log.info("Received file for group '{}': name={} path={}", payload.getGroupId(), payload.getFileName(), payload.getFilePath());
            MultipartFile file = RequestMakerUtility.fromPayload(payload);

            FileValidationUtility.validateInput(file, payload.getGroupId());
            matchSuggestionsImportJobService.processImportedMatchSuggestions(jobId, file, payload.getGroupId(), BATCH_SIZE);

        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    private UUID initiateNodesImport(MatchSuggestionsExchange  payload) {
        MatchSuggestionsImportJob job = RequestMakerUtility.createMatchSuggestionsImportJob(
                payload.getGroupId(),
                JobStatus.PENDING,
                0,
                0
        );
        matchSuggestionsImportJobRepository.save(job);
        return job.getId();
    }
}

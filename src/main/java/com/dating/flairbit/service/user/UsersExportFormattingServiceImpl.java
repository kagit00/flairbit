package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.ExportedFile;
import com.dating.flairbit.dto.db.UserExportDTO;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.models.*;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import com.dating.flairbit.utils.basic.StringConcatUtil;
import com.dating.flairbit.utils.media.csv.HeaderNormalizer;
import com.dating.flairbit.utils.media.csv.UserFieldsExtractor;

import com.dating.flairbit.utils.media.csv.CsvExporter;
import com.dating.flairbit.utils.request.RequestMakerUtility;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Service
@Slf4j
public class UsersExportFormattingServiceImpl implements UsersExportFormattingService {

    @Override
    @Transactional
    public ExportedFile exportCsv(List<UserExportDTO> userDtos, String groupId, UUID domainId) {
        List<UserFieldsExtractor.UserView> userViews = userDtos.stream()
                .filter(dto -> groupId.equalsIgnoreCase(dto.groupId()) && dto.readyForMatching())
                .map(dto -> {
                    User user = RequestMakerUtility.buildUserFromUserExportDTO(dto);
                    Profile profile = RequestMakerUtility.buildProfileFromUserExportDto(dto);
                    return new UserFieldsExtractor.UserView(user, profile);
                })
                .toList();

        try {
            String baseDir = "e:/users/";
            Files.createDirectories(Paths.get(baseDir, domainId.toString(), groupId));
            String fileName = groupId + "_users_batch_" + DefaultValuesPopulator.getUid() + ".csv.gz";
            Path fullPath = Paths.get(baseDir, domainId.toString(), groupId, fileName);

            try (Writer writer = new OutputStreamWriter(
                    new GZIPOutputStream(Files.newOutputStream(fullPath)),
                    StandardCharsets.UTF_8
            )) {
                CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '"', "\n");

                List<CsvExporter.FieldExtractor<UserFieldsExtractor.UserView>> extractors = UserFieldsExtractor.fieldExtractors();
                String[] headers = new String[extractors.size() + 1];
                headers[0] = HeaderNormalizer.FIELD_GROUP_ID;
                for (int i = 0; i < extractors.size(); i++) {
                    headers[i + 1] = extractors.get(i).header();
                }

                csvWriter.writeNext(headers);
                for (UserFieldsExtractor.UserView userView : userViews) {
                    csvWriter.writeNext(CsvExporter.mapEntityToCsvRow(userView, groupId, extractors), false);
                }

                log.info("Exported {} user rows to {}", userViews.size(), fullPath);
            }

            return new ExportedFile(null, fileName, "application/gzip", groupId, domainId, fullPath.toString());

        } catch (IOException e) {
            log.error("Error writing compressed user CSV for group '{}'", groupId, e);
            throw new InternalServerErrorException("Failed to export to disk: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<String> extractEligibleUsernames(List<UserExportDTO> userDtos, String groupId) {
        return userDtos.stream()
                .filter(dto -> groupId.equalsIgnoreCase(dto.groupId()) && dto.readyForMatching())
                .map(dto -> StringConcatUtil.concatWithSeparator("_", dto.username(), dto.intent().toLowerCase()))
                .distinct()
                .toList();
    }
}


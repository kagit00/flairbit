package com.dating.flairbit.service.profile.education;

import com.dating.flairbit.dto.EducationRequest;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.Education;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.EducationRepository;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class EducationUpdateServiceImpl implements EducationUpdateService {
    private final UserService userService;
    private final EducationRepository educationRepository;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional
    public void createOrUpdateEducation(String email, EducationRequest educationRequest, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);

        if (Objects.isNull(profile)) throw new BadRequestException("No profile found with intent: " + intent);
        if (ObjectUtils.allNull(educationRequest)) throw new BadRequestException("Education request cannot be empty.");

        Education education = !Objects.isNull(profile.getEducation())
                ? profile.getEducation()
                : Education.builder().createdAt(DefaultValuesPopulator.getCurrentTimestamp()).build();

        if (!StringUtils.equalsIgnoreCase(educationRequest.getDegree(), education.getDegree())) {
            education.setDegree(educationRequest.getDegree());
        }

        if (!StringUtils.equalsIgnoreCase(educationRequest.getFieldOfStudy(), education.getFieldOfStudy())) {
            education.setFieldOfStudy(educationRequest.getFieldOfStudy());
        }

        if (!StringUtils.equalsIgnoreCase(educationRequest.getInstitution(), education.getInstitution())) {
            education.setInstitution(educationRequest.getInstitution());
        }

        if (!Objects.equals(educationRequest.getGraduationYear(), education.getGraduationYear())) {
            education.setGraduationYear(educationRequest.getGraduationYear());
        }

        education.setUpdatedAt(DefaultValuesPopulator.getCurrentTimestamp());
        education.setProfile(profile);
        educationRepository.save(education);
    }
}

package com.dating.flairbit.service.profile.profession;

import com.dating.flairbit.dto.ProfessionRequest;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.Profession;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.ProfessionRepository;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfessionUpdateServiceImpl implements ProfessionUpdateService {
    private final UserService userService;
    private final ProfessionRepository professionRepository;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional
    public void createOrUpdateProfession(String email, ProfessionRequest request, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);

        if (ObjectUtils.allNull(request)) throw new BadRequestException("Profession request cannot be empty.");
        if (Objects.isNull(profile)) throw new BadRequestException("No profile with intent : " + intent);

        Profession profession = Objects.isNull(profile.getProfession())
                ? Profession.builder().createdAt(DefaultValuesPopulator.getCurrentTimestamp()).build()
                : profile.getProfession();

        profile.setProfession(profession);

        if (!request.getJobTitle().equalsIgnoreCase(profession.getJobTitle())) {
            profession.setJobTitle(request.getJobTitle());
        }

        if (!request.getCompany().equalsIgnoreCase(profession.getCompany())) {
            profession.setCompany(request.getCompany());
        }

        if (!request.getIndustry().equalsIgnoreCase(profession.getIndustry())) {
            profession.setIndustry(request.getIndustry());
        }

        profession.setProfile(profile);
        profession.setUpdatedAt(DefaultValuesPopulator.getCurrentTimestamp());
        professionRepository.save(profession);
    }
}

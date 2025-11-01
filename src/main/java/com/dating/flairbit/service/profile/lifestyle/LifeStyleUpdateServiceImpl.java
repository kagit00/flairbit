package com.dating.flairbit.service.profile.lifestyle;

import com.dating.flairbit.dto.LifestyleRequest;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.Lifestyle;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.LifeStyleRepository;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class LifeStyleUpdateServiceImpl implements LifeStyleUpdateService {
    private final UserService userService;
    private final LifeStyleRepository lifeStyleRepository;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional
    @CacheEvict(value = {"profileCache"}, key = "#email")
    public void createOrUpdateLifestyle(String email, LifestyleRequest request, String intent) {
        if (ObjectUtils.allNull(request)) throw new BadRequestException("Lifestyle request cannot be empty.");

        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);

        if (Objects.isNull(profile)) throw new BadRequestException("no profile found with intent: " + intent);

        Lifestyle lifestyle = !Objects.isNull(profile.getLifestyle())
                ? profile.getLifestyle()
                : Lifestyle.builder().createdAt(DefaultValuesPopulator.getCurrentTimestamp()).build();

        if (!Objects.equals(request.getDrinks(), lifestyle.getDrinks())) {
            lifestyle.setDrinks(request.getDrinks());
        }

        if (!Objects.equals(request.getSmokes(), lifestyle.getSmokes())) {
            lifestyle.setSmokes(request.getSmokes());
        }

        if (!StringUtils.equalsIgnoreCase(request.getReligion(), lifestyle.getReligion())) {
            lifestyle.setReligion(request.getReligion());
        }

        lifestyle.setProfile(profile);
        lifestyle.setUpdatedAt(DefaultValuesPopulator.getCurrentTimestamp());
        lifeStyleRepository.save(lifestyle);
    }

}

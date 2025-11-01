package com.dating.flairbit.service.profile.location;

import com.dating.flairbit.dto.LocationRequest;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.Location;
import com.dating.flairbit.models.Profile;
import com.dating.flairbit.models.User;
import com.dating.flairbit.processor.ProfileProcessor;
import com.dating.flairbit.repo.LocationRepository;
import com.dating.flairbit.service.user.UserService;
import com.dating.flairbit.utils.ProfileUtils;
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
@Slf4j
@RequiredArgsConstructor
public class LocationUpdateServiceImpl implements LocationUpdateService {
    private final UserService userService;
    private final LocationRepository locationRepository;
    private final ProfileProcessor profileProcessor;

    @Override
    @Transactional
    @CacheEvict(value = {"profileCache"}, key = "#email")
    public void createOrUpdateLocation(String email, LocationRequest request, String intent) {
        User user = userService.getUserByEmail(email);
        Profile profile = profileProcessor.getProfile(user, intent);

        if (Objects.isNull(profile)) throw new BadRequestException("No profile found with intent: " + intent);
        if (ObjectUtils.allNull(request)) throw new BadRequestException("Location request cannot be empty.");

        Location location = !Objects.isNull(profile.getLocation())
                ? profile.getLocation()
                : Location.builder().createdAt(DefaultValuesPopulator.getCurrentTimestamp()).build();

        if (!StringUtils.equalsIgnoreCase(request.getCity(), location.getCity())) {
            location.setCity(request.getCity());
        }

        if (!StringUtils.equalsIgnoreCase(request.getCountry(), location.getCountry())) {
            location.setCountry(request.getCountry());
        }

        if (!Objects.equals(location.getLatitude(), request.getLatitude())) {
            location.setLatitude(request.getLatitude());
        }

        if (!Objects.equals(location.getLongitude(), request.getLongitude())) {
            location.setLongitude(request.getLongitude());
        }

        location.setProfile(profile);
        location.setUpdatedAt(DefaultValuesPopulator.getCurrentTimestamp());
        locationRepository.save(location);
    }
}

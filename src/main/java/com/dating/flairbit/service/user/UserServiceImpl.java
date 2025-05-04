package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.*;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.exceptions.InternalServerErrorException;
import com.dating.flairbit.models.*;
import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;


    @Override
    @Transactional
    @Cacheable(value = "userCache", key = "#email + '_'", unless = "#result == null")
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new BadRequestException("User doesnt exist by email: " + email)
        );
    }

    @Override
    public UserResponse updateUserByUsername(String username, UserRequest userRequest) {
        return null;
    }


    @Override
    @CacheEvict(value = "userCache", allEntries = true)
    public void deleteUserByUserId(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.getRoles().clear();
        userRepository.delete(user);
    }

    @Override
    @CacheEvict(value = "userCache", allEntries = true)
    public void updateNotificationEnabled(Notification notification) {
        boolean isNotificationEnabled = notification.getIsNotificationEnabled();
        UUID userId = notification.getUserId();

        User user = userRepository.findById(userId).orElseThrow(() -> new InternalServerErrorException("User not found"));

        if (user.isNotificationEnabled() != isNotificationEnabled) {
            user.setNotificationEnabled(isNotificationEnabled);
            userRepository.save(user);
        }
    }
}

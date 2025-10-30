package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.UserRequest;
import com.dating.flairbit.dto.UserResponse;
import com.dating.flairbit.models.Notification;
import com.dating.flairbit.models.User;

import java.util.UUID;


public interface UserService {
    User getUserByEmail(String email);
    UserResponse updateUserByUsername(String username, UserRequest user);
    void deleteUserByUserId(UUID userId);
    void updateNotificationEnabled(Notification notification);
}

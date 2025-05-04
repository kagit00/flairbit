package com.dating.flairbit.service.user;

import com.dating.flairbit.dto.UserRequest;
import com.dating.flairbit.dto.UserResponse;
import com.dating.flairbit.models.Notification;
import com.dating.flairbit.models.User;

import java.util.UUID;

/**
 * Service interface for managing user-related operations such as registration,
 * retrieval, update, deletion, and notification settings.
 */
public interface UserService {



    User getUserByEmail(String email);

    /**
     * Updates an existing user's details identified by username.
     *
     * @param username the username of the user to update
     * @param user the updated user data
     * @return the response containing the updated user's data
     */
    UserResponse updateUserByUsername(String username, UserRequest user);

    /**
     * Deletes a user from the system by their user ID.
     *
     * @param userId the UUID of the user to delete
     */
    void deleteUserByUserId(UUID userId);

    /**
     * Updates a user's notification preferences.
     *
     * @param notification the notification settings to apply
     */
    void updateNotificationEnabled(Notification notification);
}

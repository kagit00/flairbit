package com.dating.flairbit.controller.user;

import com.dating.flairbit.dto.NoContent;
import com.dating.flairbit.models.Notification;
import com.dating.flairbit.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Tag(name = "User API", description = "Operations related to User")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Delete User By Id",
            description = "Requires either JWT or OAuth2 for authentication",
            security = {
                    @SecurityRequirement(name = "JWT"),
                    @SecurityRequirement(name = "OAuth2")
            })
    @Transactional
    @DeleteMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteUserById(@PathVariable("userId") String userId) {
        userService.deleteUserByUserId(UUID.fromString(userId));
        return new ResponseEntity<>(new NoContent(HttpStatus.OK, "User successfully deleted."), HttpStatus.OK);
    }

    @Operation(summary = "Update Notification Enabled",
            description = "Requires either JWT or OAuth2 for authentication",
            security = {
                    @SecurityRequirement(name = "JWT"),
                    @SecurityRequirement(name = "OAuth2")
            })
    @Transactional
    @PutMapping(value = "/notification", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateNotificationEnabled(@RequestBody Notification notification) {
        userService.updateNotificationEnabled(notification);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

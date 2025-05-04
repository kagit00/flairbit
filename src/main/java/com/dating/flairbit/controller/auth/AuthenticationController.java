package com.dating.flairbit.controller.auth;

import com.dating.flairbit.dto.*;
import com.dating.flairbit.service.auth.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Operations related to user authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@Valid @RequestBody RequestOtpRequest request) {
        authenticationService.requestOtp(request);
        return ResponseEntity.ok("OTP sent successfully to " + request.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authenticationService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }
}



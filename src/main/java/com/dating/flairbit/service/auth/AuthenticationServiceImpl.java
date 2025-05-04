package com.dating.flairbit.service.auth;

import com.dating.flairbit.dto.AuthResponse;
import com.dating.flairbit.dto.OtpEntry;
import com.dating.flairbit.dto.RequestOtpRequest;
import com.dating.flairbit.dto.UserDTO;
import com.dating.flairbit.models.Role;
import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.RoleRepository;
import com.dating.flairbit.repo.UserRepository;
import com.dating.flairbit.security.JwtUtils;
import com.dating.flairbit.utils.basic.BasicUtility;
import com.dating.flairbit.utils.basic.DefaultValuesPopulator;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dating.flairbit.utils.auth.AuthUtility.generateOtp;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Duration OTP_REQUEST_INTERVAL = Duration.ofSeconds(30);
    private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
    private final RoleRepository roleRepository;
    private final Cache<String, OtpEntry> otpCache;
    private final Cache<String, AtomicInteger> rateLimitCache;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Override
    public void requestOtp(RequestOtpRequest request) {
        try {
            String email = request.getEmail().trim().toLowerCase();
            String username = BasicUtility.generateUsernameFromEmail(email);

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .username(username)
                        .notificationEnabled(request.isNotificationEnabled())
                        .build();

                Role userRole = DefaultValuesPopulator.populateDefaultUserRoles(roleRepository);
                newUser.getRoles().add(userRole);

                return userRepository.save(newUser);
            });

            if (!Objects.isNull(user.getId()) && !user.isNotificationEnabled()) {
                throw new IllegalStateException("Notifications are disabled for this user.");
            }

            AtomicInteger requestCount = rateLimitCache.get(email, k -> new AtomicInteger(0));
            if (requestCount.incrementAndGet() > 3) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many OTP requests. Try again later.");
            }

            OtpEntry existing = otpCache.getIfPresent(email);
            if (existing != null && Duration.between(existing.getGeneratedAt(), Instant.now()).compareTo(OTP_REQUEST_INTERVAL) < 0) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "OTP already sent recently. Please wait before trying again.");
            }

            String otp = generateOtp();
            otpCache.put(email, new OtpEntry(otp, Instant.now()));
            //sendOtpEmail(email, otp);
            log.info("OTP sent to {} {}", email, otp);
        } catch (Exception e) {
            log.error("Failed to request OTP", e);
            throw e;
        }
    }


    @Override
    public AuthResponse verifyOtp(String email, String otp) {
        OtpEntry entry = otpCache.getIfPresent(email);

        if (entry == null || !entry.getOtp().equals(otp)) throw new BadCredentialsException("Invalid or expired OTP.");

        if (Duration.between(entry.getGeneratedAt(), Instant.now()).compareTo(OTP_VALIDITY) > 0) {
            otpCache.invalidate(email);
            throw new BadCredentialsException("OTP expired.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        otpCache.invalidate(email);
        String token = jwtUtils.generateToken(user);

        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getUsername());
        return new AuthResponse(token, userDTO);
    }


    private void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Login OTP");
        message.setText("Your OTP is: " + otp + "\nIt will expire in 5 minutes.\n\nIf you didn't request this, please ignore.");
        mailSender.send(message);
    }
}
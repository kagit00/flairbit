package com.dating.flairbit.service.auth;

import com.dating.flairbit.dto.AuthResponse;
import com.dating.flairbit.dto.LogInRequest;
import com.dating.flairbit.dto.LogInResponse;
import com.dating.flairbit.dto.RequestOtpRequest;
import com.dating.flairbit.exceptions.BadRequestException;
import com.dating.flairbit.models.User;
import com.dating.flairbit.repo.UserRepository;
import com.dating.flairbit.security.JwtUtils;
import com.dating.flairbit.utils.auth.AuthUtility;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


public interface AuthenticationService {
    void requestOtp(RequestOtpRequest request);
    AuthResponse verifyOtp(String email, String otp);
}


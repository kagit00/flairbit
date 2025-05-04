package com.dating.flairbit.utils.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Random;


public final class AuthUtility {

    private AuthUtility() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    public static void logOut(HttpServletResponse response) {
        removeCookie(response, "GOOGLE_OAUTH2_TOKEN");
        removeCookie(response, "OAUTH2_TOKEN_EXPIRY");
    }

    private static void removeCookie(HttpServletResponse response, String cookieName) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

}

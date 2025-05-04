package com.dating.flairbit.dto;

import java.io.Serializable;
import java.time.Instant;

public class OtpEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String otp;
    private final Instant generatedAt;

    public OtpEntry(String otp, Instant generatedAt) {
        this.otp = otp;
        this.generatedAt = generatedAt;
    }

    public String getOtp() {
        return otp;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
package com.dating.flairbit.processor;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class ServiceTokenVerifier {

    @Value("${chat-service.name:chat-service}") private String chatServiceName;
    @Value("${service.name:FlairBit}") private String serviceName;
    private final RSAKey chatServicePublicKey;

    public ServiceTokenVerifier (RSAKey chatServicePublicKey) {
        this.chatServicePublicKey = chatServicePublicKey;
    }


    public JWTClaimsSet verifyAndExtractClaims(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            JWSVerifier verifier = new RSASSAVerifier(chatServicePublicKey.toRSAPublicKey());
            if (!jwt.verify(verifier)) throw new SecurityException("Invalid JWT signature");


            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date now = new Date();
            if (claims.getExpirationTime().before(now)) throw new SecurityException("Token expired");

            if (!chatServiceName.equals(claims.getIssuer())) throw new SecurityException("Invalid token issuer");
            if (!serviceName.equals(claims.getSubject())) throw new SecurityException("Invalid subject");

            return claims;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify token", e);
        }
    }
}

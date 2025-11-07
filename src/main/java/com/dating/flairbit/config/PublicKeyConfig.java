package com.dating.flairbit.config;

import com.nimbusds.jose.jwk.RSAKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class PublicKeyConfig {

    @Bean
    public RSAKey chatServicePublicKey(@Value("${service.auth.public-key}") Resource resource) throws Exception {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(resource.getInputStream()))) {
            Object parsedObject = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (parsedObject instanceof SubjectPublicKeyInfo publicKeyInfo) {
                RSAPublicKey publicKey = (RSAPublicKey) converter.getPublicKey(publicKeyInfo);
                return new RSAKey.Builder(publicKey)
                        .keyID("df40f3f2-7d94-4361-88b8-d6f3a942036b")
                        .build();
            } else {
                throw new IllegalStateException("Unsupported PEM format for public key: " + parsedObject.getClass());
            }
        }
    }
}

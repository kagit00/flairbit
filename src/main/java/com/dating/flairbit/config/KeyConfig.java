package com.dating.flairbit.config;

import com.nimbusds.jose.jwk.RSAKey;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

@Configuration
public class KeyConfig {

    @Bean
    public RSAKey flairbitKeyPair(@Value("${service.auth.private-key}") Resource resource) throws Exception {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(resource.getInputStream()))) {
            Object parsedObject = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            RSAPrivateKey privateKey;
            RSAPublicKey publicKey;

            if (parsedObject instanceof org.bouncycastle.openssl.PEMKeyPair pemKeyPair) {
                KeyPair keyPair = converter.getKeyPair(pemKeyPair);
                privateKey = (RSAPrivateKey) keyPair.getPrivate();
                publicKey = (RSAPublicKey) keyPair.getPublic();
            } else if (parsedObject instanceof PrivateKeyInfo privateKeyInfo) {
                privateKey = (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
                RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
                publicKey = (RSAPublicKey) keyFactory.generatePublic(publicSpec);
            } else {
                throw new IllegalStateException("Unsupported key format: " + parsedObject.getClass());
            }

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("flairbit-key-1")
                    .build();
        }
    }
}

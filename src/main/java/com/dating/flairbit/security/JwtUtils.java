package com.dating.flairbit.security;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;


import java.util.*;

@Service
public class JwtUtils {

    private final RSAKey flairbitKeyPair;

    public JwtUtils(RSAKey flairbitKeyPair) {
        this.flairbitKeyPair = flairbitKeyPair;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Set<String> roles = new HashSet<>();
        userDetails.getAuthorities().forEach(a -> roles.add(a.getAuthority()));
        claims.put("roles", roles);

        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        try {
            long now = System.currentTimeMillis();
            Date issueTime = new Date(now);
            Date expirationTime = new Date(now + 3600000); // 1 hour

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer("FlairBit")
                    .issueTime(issueTime)
                    .expirationTime(expirationTime)
                    .claim("roles", claims.get("roles"))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(flairbitKeyPair.getKeyID()).build(),
                    claimsSet
            );

            signedJWT.sign(new RSASSASigner(flairbitKeyPair.toPrivateKey()));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("JWT signing failed", e);
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            return jwt.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            jwt.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(flairbitKeyPair.toRSAPublicKey()));
            String username = jwt.getJWTClaimsSet().getSubject();
            Date expiration = jwt.getJWTClaimsSet().getExpirationTime();
            return username.equals(userDetails.getUsername()) && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

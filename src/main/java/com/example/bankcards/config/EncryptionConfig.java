package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

@Configuration
public class EncryptionConfig {
    @Value("${card.encryption.algorithm}")
    private String algorithm;

    @Value("${card.encryption.secret}")
    private String secret;


    public String getAlgorithm() {
        return algorithm;
    }

    public Key getKey() {
        return new SecretKeySpec(secret.getBytes(), algorithm);
    }
}

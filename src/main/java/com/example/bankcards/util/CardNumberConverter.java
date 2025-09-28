package com.example.bankcards.util;

import com.example.bankcards.config.EncryptionConfig;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.util.Base64;

@Component
@Converter
public class CardNumberConverter implements AttributeConverter<String, String> {
    private EncryptionConfig encryptionConfig;

    @Autowired
    public void setEncryptionConfig(EncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        try{
            Cipher cipher = Cipher.getInstance(encryptionConfig.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, encryptionConfig.getKey());
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }
        catch (Exception e){
            throw new RuntimeException("Could not encrypt data", e);
        }

    }

    @Override
    public String convertToEntityAttribute(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(encryptionConfig.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, encryptionConfig.getKey());
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        }
        catch (Exception e){
            throw new RuntimeException("Could not decrypt data", e);
        }
    }
}

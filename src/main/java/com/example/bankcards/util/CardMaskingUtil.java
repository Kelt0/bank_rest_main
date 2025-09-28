package com.example.bankcards.util;

public class CardMaskingUtil {

    private static final String MASK_CHUNK = "**** ";

    public static String maskCardNumber(String fullCardNumber){
        if (fullCardNumber == null || fullCardNumber.length() < 4) {
            return MASK_CHUNK.repeat(3) + "0000";
        }

        String clearNumber = fullCardNumber.replaceAll("\\s+", "");
        String lastFour = clearNumber.substring(clearNumber.length()-4);
        String maskedPart = MASK_CHUNK.repeat(3);

        return (maskedPart + lastFour).trim();
    }
}

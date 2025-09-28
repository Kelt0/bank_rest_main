package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CardCreationRequest {
    @NotNull(message = "Owner ID must be specified")
    private Long ownerId;

    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 19, message = "Card number must be 16 to 19 characters long")
    private String cardNumber;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Expiry date format must be MM/YY")
    private String expiryDate;

    public CardCreationRequest(Long ownerId, String cardNumber, String expiryDate) {
        this.ownerId = ownerId;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}

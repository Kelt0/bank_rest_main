package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferRequest {

    @NotNull(message = "Source card ID must be specified.")
    private Long sourceCardId;

    @NotNull(message = "Target card ID must be specified.")
    private Long targetCardId;

    @NotNull(message = "Amount is required.")
    @Positive(message = "Amount must be a positive value greater than zero.")
    private BigDecimal amount;

    public TransferRequest(Long sourceCardId, Long targetCardId, BigDecimal amount) {
        this.sourceCardId = sourceCardId;
        this.targetCardId = targetCardId;
        this.amount = amount;
    }

    public Long getSourceCardId() {
        return sourceCardId;
    }

    public void setSourceCardId(Long sourceCardId) {
        this.sourceCardId = sourceCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getTargetCardId() {
        return targetCardId;
    }

    public void setTargetCardId(Long targetCardId) {
        this.targetCardId = targetCardId;
    }
}

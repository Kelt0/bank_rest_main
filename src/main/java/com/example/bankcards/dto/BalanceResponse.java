package com.example.bankcards.dto;

import java.math.BigDecimal;

public class BalanceResponse {
    BigDecimal balance;

    public BalanceResponse(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

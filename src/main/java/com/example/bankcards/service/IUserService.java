package com.example.bankcards.service;

import java.math.BigDecimal;

public interface IUserService {
    void deleteUser(Long userId);
    BigDecimal getUserBalance(Long userId);
}

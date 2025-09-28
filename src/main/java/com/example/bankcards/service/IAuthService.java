package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;

public interface IAuthService {
    String authenticateUser(LoginRequest loginRequest);
}

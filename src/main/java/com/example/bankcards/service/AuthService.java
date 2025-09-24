package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager AUTHENTICATION_MANAGER;
    private final JwtUtil JWT_UTIL;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.AUTHENTICATION_MANAGER = authenticationManager;
        this.JWT_UTIL = jwtUtil;
    }

    public String authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = AUTHENTICATION_MANAGER.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        return JWT_UTIL.generateToken(authentication);
    }
}

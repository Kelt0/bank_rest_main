package com.example.bankcards.service;

import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    private static final Long USER_ID = 42L;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void deleteUser_CallsRepositoryDelete() {
        userService.deleteUser(USER_ID);
        verify(userRepository, times(1)).deleteById(USER_ID);
    }

    @Test
    void getUserBalance_ReturnsValue_WhenPresent() {
        when(userRepository.getUserBalance(USER_ID)).thenReturn(Optional.of(new BigDecimal("123.45")));

        BigDecimal result = userService.getUserBalance(USER_ID);

        assertEquals(new BigDecimal("123.45"), result);
        verify(userRepository, times(1)).getUserBalance(USER_ID);
    }

    @Test
    void getUserBalance_Throws_WhenUserNotFound() {
        when(userRepository.getUserBalance(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserBalance(USER_ID));
        verify(userRepository, times(1)).getUserBalance(USER_ID);
    }
}

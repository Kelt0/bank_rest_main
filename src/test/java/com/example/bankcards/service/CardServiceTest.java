package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CardServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardService cardService;

    private final Long USER_ID = 100L;
    private final Long OTHER_USER_ID = 101L;

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        cardService = new CardService(cardRepository, userRepository);
    }

    private Card createCard(Long cardId, Long ownerId, BigDecimal balance, Card.CardStatus status) {
        Card card = new Card();
        card.setId(cardId);
        card.setBalance(balance);
        card.setStatus(status);

        User owner = new User();
        owner.setId(ownerId);
        card.setOwner(owner);
        return card;
    }


    @Test
    void transferMoney_Success() throws Exception {
        Card sourceCard = createCard(1L, USER_ID, new BigDecimal("200.00"), Card.CardStatus.ACTIVE);
        Card targetCard = createCard(2L, OTHER_USER_ID, new BigDecimal("50.00"), Card.CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(targetCard));

        cardService.transferMoney(USER_ID, 1L, 2L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), sourceCard.getBalance());
        assertEquals(new BigDecimal("150.00"), targetCard.getBalance());

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferMoney_InsufficientFunds_ThrowsException() {
        Card sourceCard = createCard(1L, USER_ID, new BigDecimal("50.00"), Card.CardStatus.ACTIVE);
        Card targetCard = createCard(2L, OTHER_USER_ID, new BigDecimal("50.00"), Card.CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(targetCard));

        assertThrows(InsufficientFundsException.class, () ->
                cardService.transferMoney(USER_ID, 1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transferMoney_AccessDenied_ThrowsException() {
        Card sourceCard = createCard(1L, OTHER_USER_ID, new BigDecimal("200.00"), Card.CardStatus.ACTIVE);
        Card targetCard = createCard(2L, USER_ID, new BigDecimal("50.00"), Card.CardStatus.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(targetCard));

        assertThrows(AccessDeniedException.class, () ->
                cardService.transferMoney(USER_ID, 1L, 2L, new BigDecimal("10.00")));
    }


    @Test
    void setCardStatus_BlockCard_Success() {
        Card card = createCard(5L, USER_ID, BigDecimal.ZERO, Card.CardStatus.ACTIVE);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        cardService.setCardStatus(5L, "BLOCKED");

        assertEquals(Card.CardStatus.BLOCKED, card.getStatus());

        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void deleteCard_CardExists_Success() {
        when(cardRepository.existsById(3L)).thenReturn(true);

        cardService.deleteCard(3L);

        verify(cardRepository, times(1)).deleteById(3L);
    }

    @Test
    void deleteCard_CardNotFound_ThrowsException() {
        when(cardRepository.existsById(99L)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () ->
                cardService.deleteCard(99L));
    }
}

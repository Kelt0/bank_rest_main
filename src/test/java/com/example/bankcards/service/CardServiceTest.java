package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardService;
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
    private CardService cardService;

    private final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        cardRepository = Mockito.mock(CardRepository.class);

        UserRepository userRepository = Mockito.mock(UserRepository.class);
        cardService = new CardService(cardRepository, userRepository);
    }

    @Test
    void transferMoney_Success() {
        Card sourceCard = createActiveCard(1L, USER_ID, new BigDecimal("200.00"));
        Card targetCard = createActiveCard(2L, USER_ID, new BigDecimal("50.00"));

        when(cardRepository.findByIdAndOwnerId(1L, USER_ID)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndOwnerId(2L, USER_ID)).thenReturn(Optional.of(targetCard));

        cardService.transferMoney(USER_ID, 1L, 2L, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), sourceCard.getBalance());
        assertEquals(new BigDecimal("150.00"), targetCard.getBalance());

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferMoney_InsufficientFunds_ThrowsException() {
        Card sourceCard = createActiveCard(1L, USER_ID, new BigDecimal("50.00"));
        Card targetCard = createActiveCard(2L, USER_ID, new BigDecimal("50.00"));

        when(cardRepository.findByIdAndOwnerId(1L, USER_ID)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndOwnerId(2L, USER_ID)).thenReturn(Optional.of(targetCard));

        assertThrows(InsufficientFundsException.class, () ->
                cardService.transferMoney(USER_ID, 1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transferMoney_AccessDenied_ThrowsException() {
        Card sourceCard = createActiveCard(1L, USER_ID + 1, new BigDecimal("200.00"));
        Card targetCard = createActiveCard(2L, USER_ID, new BigDecimal("50.00"));

        when(cardRepository.findByIdAndOwnerId(1L, USER_ID + 1)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findByIdAndOwnerId(2L, USER_ID)).thenReturn(Optional.of(targetCard));

        assertThrows(AccessDeniedException.class, () ->
                cardService.transferMoney(USER_ID, 1L, 2L, new BigDecimal("10.00")));
    }


    @Test
    void setCardStatus_BlockCard_Success() {
        Card card = createActiveCard(5L, USER_ID, BigDecimal.ZERO);
        when(cardRepository.updateCardStatus(5L, Card.CardStatus.BLOCKED))
                .thenAnswer(invocation -> {
                    card.setStatus(Card.CardStatus.BLOCKED);
                    return 1;
                });

        cardService.setCardStatus(5L, Card.CardStatus.BLOCKED);

        assertEquals(Card.CardStatus.BLOCKED, card.getStatus());

        verify(cardRepository, times(1)).updateCardStatus(card.getId(), Card.CardStatus.BLOCKED);
    }

    @Test
    void deleteCard_CardExists_Success() {
        when(cardRepository.existsById(3L)).thenReturn(true);

        cardService.deleteCard(3L);

        verify(cardRepository, times(1)).deleteById(3L);
    }

    private Card createActiveCard(Long cardId, Long ownerId, BigDecimal balance) {
        Card card = new Card();
        card.setId(cardId);
        card.setBalance(balance);
        card.setStatus(Card.CardStatus.ACTIVE);

        User owner = new User();
        owner.setId(ownerId);
        card.setOwner(owner);
        return card;
    }
}

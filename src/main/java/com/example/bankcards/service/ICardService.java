package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ICardService {
    CardResponse createCard(CardCreationRequest request);
    void setCardStatus(Long cardId, Card.CardStatus status);
    void deleteCard(Long cardId);
    void transferMoney(Long userId, Long sourceCardId, Long targetCardId, BigDecimal amount);
    Page<CardResponse> findMyCards(Long userId, Pageable pageable);
    Page<CardResponse> findAllCards(Pageable pageable);
}

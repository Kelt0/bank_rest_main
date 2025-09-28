package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskingUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class CardService {
    private final CardRepository CARD_REPOSITORY;
    private final UserRepository USER_REPOSITORY;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        CARD_REPOSITORY = cardRepository;
        USER_REPOSITORY = userRepository;
    }

    @Transactional
    public CardResponse createCard(CardCreationRequest request) {

        User owner = USER_REPOSITORY.findById(request.getOwnerId())
                .orElseThrow(() -> new CardNotFoundException("Owner not found."));

        Card newCard = new Card();
        newCard.setOwner(owner);
        newCard.setCardNumber(request.getCardNumber());
        newCard.setBalance(BigDecimal.ZERO);
        newCard.setStatus(Card.CardStatus.ACTIVE);
        newCard.setExpiryDate(LocalDate.parse(request.getExpiryDate()));

        Card savedCard = CARD_REPOSITORY.save(newCard);
        return mapToCardResponse(savedCard);
    }

    @Transactional
    public void setCardStatus(Long cardId, Card.CardStatus status) {

        int modified = CARD_REPOSITORY.updateCardStatus(cardId, status);
        if (modified == 0) {
            throw new CardNotFoundException("Card with ID " + cardId + " not found.");
        }
    }

    @Transactional
    public void deleteCard(Long cardId) {
        CARD_REPOSITORY.deleteById(cardId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        USER_REPOSITORY.deleteById(userId);
    }

    @Transactional
    public void transferMoney(Long userId, Long sourceCardId, Long targetCardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Card sourceCard = CARD_REPOSITORY.findByIdAndOwnerId(sourceCardId, userId)
                .orElseThrow(() -> new AccessDeniedException("Source card not found or does not belong to the current user."));

        Card targetCard = CARD_REPOSITORY.findByIdAndOwnerId(targetCardId, userId)
                .orElseThrow(() -> new AccessDeniedException("Target card not found or does not belong to the current user."));

        if (sourceCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new IllegalStateException("Source card is not active.");
        }

        if (sourceCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds.");
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        targetCard.setBalance(targetCard.getBalance().add(amount));

        CARD_REPOSITORY.save(sourceCard);
        CARD_REPOSITORY.save(targetCard);
    }

    public CardResponse mapToCardResponse(Card card) {
        CardResponse cardResponse = new CardResponse();

        String fullDecryptedNumber = card.getCardNumber();
        String maskedNumber = CardMaskingUtil.maskCardNumber(fullDecryptedNumber);
        cardResponse.setMaskedCardNumber(maskedNumber);
        cardResponse.setBalance(card.getBalance());
        cardResponse.setStatus(card.getStatus().name());
        cardResponse.setOwnerId(card.getOwner().getId());
        cardResponse.setId(card.getId());
        cardResponse.setExpiryDate(card.getExpiryDate());

        return cardResponse;
    }

    public Page<CardResponse> findMyCards(Long userId, Pageable pageable) {
        return CARD_REPOSITORY.findAllByOwnerId(userId, pageable)
                .map(this::mapToCardResponse);
    }

    public Page<CardResponse> findAllCards(Pageable pageable) {
        return CARD_REPOSITORY.findAll(pageable).map(this::mapToCardResponse);
    }

    public BigDecimal balanceView(Long cardId) {
       User user = USER_REPOSITORY.findById(cardId).orElseThrow(() -> new UsernameNotFoundException("User not found."));

       return user.getCards().stream()
               .filter(card -> card.getStatus() == Card.CardStatus.ACTIVE)
               .map(Card::getBalance)
               .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

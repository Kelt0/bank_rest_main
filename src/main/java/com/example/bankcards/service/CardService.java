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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


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

        Card savedCard = CARD_REPOSITORY.save(newCard);
        return mapToCardResponse(savedCard);
    }

    @Transactional
    public void setCardStatus(Long cardId, String status) {

        Card card = CARD_REPOSITORY.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with ID " + cardId + " not found."));

        try {
            Card.CardStatus newStatus = Card.CardStatus.valueOf(status.toUpperCase());
            card.setStatus(newStatus);
            CARD_REPOSITORY.save(card);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status + ". Must be ACTIVE or BLOCKED.");
        }
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!CARD_REPOSITORY.existsById(cardId)) {
            throw new CardNotFoundException("Card with ID " + cardId + " not found for deletion.");
        }
        CARD_REPOSITORY.deleteById(cardId);
    }

    @Transactional
    public void transferMoney(Long userId, Long sourceCardId, Long targetCardId, BigDecimal amount) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Card sourceCard = CARD_REPOSITORY.findById(sourceCardId).orElseThrow(() -> new CardNotFoundException("Source card not found."));
        Card targetCard = CARD_REPOSITORY.findById(targetCardId).orElseThrow(() -> new CardNotFoundException("Target card not found."));

        if(!sourceCard.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Source card does not belong to the current user.");
        }
        if(sourceCard.getStatus() != Card.CardStatus.ACTIVE) {throw new IllegalStateException("Source card is not active.");}
        if(sourceCard.getBalance().compareTo(amount) < 0){throw new InsufficientFundsException("Insufficient funds.");}

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        targetCard.setBalance(targetCard.getBalance().add(amount));

        CARD_REPOSITORY.save(sourceCard);
        CARD_REPOSITORY.save(targetCard);
    }

    public CardResponse mapToCardResponse(Card card) {
        CardResponse cardResponse = new CardResponse();

        String fullDecryptedNumber = card.getEncryptedCardNumber();
        String maskedNumber = CardMaskingUtil.maskCardNumber(fullDecryptedNumber);
        cardResponse.setMaskedCardNumber(maskedNumber);

        return cardResponse;
    }

    public List<CardResponse> findMyCards(Long userId, Pageable pageable) {
        List<Card> cards = CARD_REPOSITORY.findAllByOwnerId(userId, pageable).getContent();

        return cards.stream()
                .map(this::mapToCardResponse)
                .collect(Collectors.toList());
    }
}

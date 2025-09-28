package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.ICardService;
import com.example.bankcards.util.CardMaskingUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class CardService implements ICardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CardResponse createCard(CardCreationRequest request) {

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new CardNotFoundException("Owner not found."));

        Card newCard = new Card();
        newCard.setOwner(owner);
        newCard.setCardNumber(request.getCardNumber());
        newCard.setBalance(BigDecimal.ZERO);
        newCard.setStatus(Card.CardStatus.ACTIVE);
        newCard.setExpiryDate(LocalDate.parse(request.getExpiryDate()));

        Card savedCard = cardRepository.save(newCard);
        return mapToCardResponse(savedCard);
    }

    @Override
    @Transactional
    public void setCardStatus(Long cardId, Card.CardStatus status) {

        int modified = cardRepository.updateCardStatus(cardId, status);
        if (modified == 0) {
            throw new CardNotFoundException("Card with ID " + cardId + " not found.");
        }
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    @Override
    @Transactional
    public void transferMoney(Long userId, Long sourceCardId, Long targetCardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Card sourceCard = cardRepository.findByIdAndOwnerId(sourceCardId, userId)
                .orElseThrow(() -> new AccessDeniedException("Source card not found or does not belong to the current user."));

        Card targetCard = cardRepository.findByIdAndOwnerId(targetCardId, userId)
                .orElseThrow(() -> new AccessDeniedException("Target card not found or does not belong to the current user."));

        if (sourceCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new IllegalStateException("Source card is not active.");
        }

        if (sourceCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds.");
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        targetCard.setBalance(targetCard.getBalance().add(amount));

        cardRepository.save(sourceCard);
        cardRepository.save(targetCard);
    }

    private CardResponse mapToCardResponse(Card card) {
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

    @Override
    public Page<CardResponse> findMyCards(Long userId, Pageable pageable) {
        return cardRepository.findAllByOwnerId(userId, pageable)
                .map(this::mapToCardResponse);
    }

    @Override
    public Page<CardResponse> findAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::mapToCardResponse);
    }
}

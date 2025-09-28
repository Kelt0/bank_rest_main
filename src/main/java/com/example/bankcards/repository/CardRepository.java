package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.text.Bidi;
import java.util.Optional;


public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    Optional<Card> findByIdAndOwnerId(Long cardId, Long ownerId);

    @Modifying
    @Query("UPDATE Card c SET c.status = :status WHERE c.id = :cardId")
    int updateCardStatus(Long cardId, Card.CardStatus status);
}

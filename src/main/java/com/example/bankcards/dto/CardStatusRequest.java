package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;

public record CardStatusRequest(
        Card.CardStatus status
) {
}

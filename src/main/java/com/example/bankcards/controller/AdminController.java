package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final CardService CARD_SERVICE;

    public AdminController(CardService cardService) {
        this.CARD_SERVICE = cardService;
    }

    @GetMapping("/users")
    public Page<CardResponse> getAllCards(Pageable pageable) {
        return CARD_SERVICE.findAllCards(pageable);
    }

    @PostMapping("/cards")
    public CardResponse created(@Valid @RequestBody CardCreationRequest request){
        return CARD_SERVICE.createCard(request);
    }

    @PatchMapping("/cards/{cardId}/status")
    public ResponseEntity<Void> setCardStatus(@PathVariable Long cardId, @RequestBody CardStatusRequest request) {
        CARD_SERVICE.setCardStatus(cardId, request.status());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId){
        CARD_SERVICE.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void>  deleteUser(@PathVariable Long userId){
        CARD_SERVICE.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


}

package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final CardService CARD_SERVICE;

    public AdminController(CardService cardService) {
        this.CARD_SERVICE = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> created(@Valid @RequestBody CardCreationRequest request){
        CardResponse response = CARD_SERVICE.createCard(request);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @PutMapping("/{cardId}/status")
    public ResponseEntity<Void> setCardStatus(@PathVariable Long cardId, @RequestParam String status){
        CARD_SERVICE.setCardStatus(cardId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@PathVariable Long cardId){
        CARD_SERVICE.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}

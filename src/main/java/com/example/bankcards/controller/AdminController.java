package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CardStatusRequest;
import com.example.bankcards.service.ICardService;
import com.example.bankcards.service.IUserService;
import com.example.bankcards.service.impl.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ICardService cardService;
    private final IUserService userService;

    public AdminController(ICardService cardService, IUserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping("/cards")
    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardService.findAllCards(pageable);
    }

    @PostMapping("/cards")
    public CardResponse created(@Valid @RequestBody CardCreationRequest request){
        return cardService.createCard(request);
    }

    @PatchMapping("/cards/{cardId}/status")
    public ResponseEntity<Void> setCardStatus(@PathVariable Long cardId, @RequestBody CardStatusRequest request) {
        cardService.setCardStatus(cardId, request.status());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId){
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


}

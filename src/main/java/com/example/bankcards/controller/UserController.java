package com.example.bankcards.controller;

import com.example.bankcards.dto.BalanceResponse;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/user/cards")
@PreAuthorize("hasRole('USER')")
public class UserController {
    private final CardService CARD_SERVICE;
    public UserController(CardService cardService) {
        this.CARD_SERVICE = cardService;
    }

    @GetMapping
    public Page<CardResponse> getMyCards(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        return CARD_SERVICE.findMyCards(userId, pageable);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long cardId) {
        BigDecimal totalBalance = CARD_SERVICE.balanceView(cardId);
        return ResponseEntity.ok(new BalanceResponse(totalBalance));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(@Valid  @RequestBody TransferRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        CARD_SERVICE.transferMoney(userId,
                request.getSourceCardId(),
                request.getTargetCardId(),
                request.getAmount());

        return ResponseEntity.ok().build();
    }
}

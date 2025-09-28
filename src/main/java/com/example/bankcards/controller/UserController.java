package com.example.bankcards.controller;

import com.example.bankcards.dto.BalanceResponse;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.ICardService;
import com.example.bankcards.service.IUserService;
import com.example.bankcards.service.impl.CardService;
import com.example.bankcards.service.impl.UserService;
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
    private final ICardService cardService;
    private final IUserService userService;

    public UserController(ICardService cardService, IUserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping
    public Page<CardResponse> getMyCards(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        return cardService.findMyCards(userId, pageable);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long cardId) {
        BigDecimal totalBalance = userService.getUserBalance(cardId);
        return ResponseEntity.ok(new BalanceResponse(totalBalance));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(@Valid  @RequestBody TransferRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        cardService.transferMoney(userId,
                request.getSourceCardId(),
                request.getTargetCardId(),
                request.getAmount());

        return ResponseEntity.ok().build();
    }
}

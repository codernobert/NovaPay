package com.digitalwallet.controller;

import com.digitalwallet.dto.WalletBalanceResponse;
import com.digitalwallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{walletNumber}/balance")
    public Mono<ResponseEntity<WalletBalanceResponse>> getWalletBalance(
            @PathVariable String walletNumber,
            Authentication authentication) {
        return walletService.getWalletBalance(walletNumber)
                .map(ResponseEntity::ok)
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    @GetMapping("/user/{userId}")
    public Flux<WalletBalanceResponse> getUserWallets(@PathVariable Long userId) {
        return walletService.getUserWallets(userId)
                .map(wallet -> WalletBalanceResponse.builder()
                        .walletId(wallet.getId())
                        .walletNumber(wallet.getWalletNumber())
                        .balance(wallet.getBalance())
                        .currency(wallet.getCurrency())
                        .status(wallet.getStatus())
                        .dailyLimit(wallet.getDailyLimit())
                        .availableBalance(wallet.getBalance())
                        .lastUpdated(wallet.getUpdatedAt())
                        .build());
    }
}
package com.digitalwallet.service;

import com.digitalwallet.dto.WalletBalanceResponse;
import com.digitalwallet.exception.InsufficientBalanceException;
import com.digitalwallet.exception.WalletNotFoundException;
import com.digitalwallet.model.Wallet;
import com.digitalwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final AuditService auditService;

    public Mono<Wallet> getWalletByNumber(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(
                        "Wallet not found: " + walletNumber)));
    }

    public Mono<Wallet> getActiveWalletById(Long walletId) {
        return walletRepository.findActiveWalletById(walletId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(
                        "Active wallet not found with ID: " + walletId)));
    }

    public Flux<Wallet> getUserWallets(Long userId) {
        return walletRepository.findActiveWalletsByUserId(userId);
    }

    public Mono<WalletBalanceResponse> getWalletBalance(String walletNumber) {
        return getWalletByNumber(walletNumber)
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

    @Transactional
    public Mono<Wallet> creditWallet(Long walletId, BigDecimal amount, Long performedBy) {
        return getActiveWalletById(walletId)
                .flatMap(wallet -> {
                    String oldBalance = wallet.getBalance().toString();

                    return walletRepository.creditWallet(walletId, amount)
                            .flatMap(rowsAffected -> {
                                if (rowsAffected == 0) {
                                    return Mono.error(new RuntimeException("Failed to credit wallet"));
                                }
                                return walletRepository.findById(walletId);
                            })
                            .flatMap(updatedWallet ->
                                    auditService.logWalletAction(walletId, "WALLET_CREDITED",
                                                    performedBy, oldBalance, updatedWallet.getBalance().toString())
                                            .thenReturn(updatedWallet)
                            );
                })
                .doOnSuccess(wallet -> log.info("Wallet credited: {} - Amount: {}", walletId, amount))
                .doOnError(error -> log.error("Failed to credit wallet: {}", walletId, error));
    }

    @Transactional
    public Mono<Wallet> debitWallet(Long walletId, BigDecimal amount, Long performedBy) {
        return getActiveWalletById(walletId)
                .flatMap(wallet -> {
                    if (wallet.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new InsufficientBalanceException(
                                "Insufficient balance in wallet: " + wallet.getWalletNumber()));
                    }

                    String oldBalance = wallet.getBalance().toString();

                    return walletRepository.debitWallet(walletId, amount)
                            .flatMap(rowsAffected -> {
                                if (rowsAffected == 0) {
                                    return Mono.error(new InsufficientBalanceException(
                                            "Insufficient balance or wallet update failed"));
                                }
                                return walletRepository.findById(walletId);
                            })
                            .flatMap(updatedWallet ->
                                    auditService.logWalletAction(walletId, "WALLET_DEBITED",
                                                    performedBy, oldBalance, updatedWallet.getBalance().toString())
                                            .thenReturn(updatedWallet)
                            );
                })
                .doOnSuccess(wallet -> log.info("Wallet debited: {} - Amount: {}", walletId, amount))
                .doOnError(error -> log.error("Failed to debit wallet: {}", walletId, error));
    }

    public Mono<Boolean> validateWalletStatus(Long walletId) {
        return walletRepository.findById(walletId)
                .map(wallet -> "ACTIVE".equals(wallet.getStatus()))
                .defaultIfEmpty(false);
    }
}
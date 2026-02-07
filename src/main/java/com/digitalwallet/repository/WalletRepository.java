package com.digitalwallet.repository;

import com.digitalwallet.model.Wallet;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
public interface WalletRepository extends ReactiveCrudRepository<Wallet, Long> {

    Mono<Wallet> findByWalletNumber(String walletNumber);

    Flux<Wallet> findByUserId(Long userId);

    @Query("SELECT * FROM wallets WHERE user_id = :userId AND status = 'ACTIVE'")
    Flux<Wallet> findActiveWalletsByUserId(Long userId);

    @Query("SELECT * FROM wallets WHERE id = :walletId AND status = 'ACTIVE'")
    Mono<Wallet> findActiveWalletById(Long walletId);

    @Modifying
    @Query("UPDATE wallets SET balance = balance + :amount, updated_at = CURRENT_TIMESTAMP WHERE id = :walletId")
    Mono<Integer> creditWallet(Long walletId, BigDecimal amount);

    @Modifying
    @Query("UPDATE wallets SET balance = balance - :amount, updated_at = CURRENT_TIMESTAMP WHERE id = :walletId AND balance >= :amount")
    Mono<Integer> debitWallet(Long walletId, BigDecimal amount);

    @Query("SELECT SUM(balance) FROM wallets WHERE currency = :currency")
    Mono<BigDecimal> getTotalBalanceByCurrency(String currency);

    Mono<Boolean> existsByWalletNumber(String walletNumber);
}
package com.digitalwallet.repository;

import com.digitalwallet.model.Transfer;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransferRepository extends ReactiveCrudRepository<Transfer, Long> {

    Mono<Transfer> findByTransferReference(String transferReference);

    Flux<Transfer> findBySourceWalletId(Long sourceWalletId);

    Flux<Transfer> findByDestinationWalletId(Long destinationWalletId);

    @Query("SELECT * FROM transfers WHERE source_wallet_id = :walletId OR destination_wallet_id = :walletId ORDER BY created_at DESC LIMIT :limit")
    Flux<Transfer> findTransfersByWalletId(Long walletId, int limit);

    @Query("SELECT * FROM transfers WHERE status = :status ORDER BY created_at DESC")
    Flux<Transfer> findByStatus(String status);

    @Query("SELECT SUM(amount) FROM transfers WHERE source_wallet_id = :walletId " +
            "AND status = 'COMPLETED' AND created_at >= :startDate AND created_at < :endDate")
    Mono<BigDecimal> getTotalDebitForPeriod(Long walletId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(*) FROM transfers WHERE source_wallet_id = :walletId " +
            "AND status IN ('PENDING', 'PROCESSING')")
    Mono<Long> countPendingTransfersByWallet(Long walletId);

    Mono<Boolean> existsByTransferReference(String transferReference);
}
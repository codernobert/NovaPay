package com.digitalwallet.repository;

import com.digitalwallet.model.LedgerEntry;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
public interface LedgerEntryRepository extends ReactiveCrudRepository<LedgerEntry, Long> {

    Flux<LedgerEntry> findByTransferId(Long transferId);

    Flux<LedgerEntry> findByWalletId(Long walletId);

    @Query("SELECT * FROM ledger_entries WHERE wallet_id = :walletId ORDER BY created_at DESC LIMIT :limit")
    Flux<LedgerEntry> findRecentEntriesByWalletId(Long walletId, int limit);

    @Query("SELECT SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE -amount END) " +
            "FROM ledger_entries WHERE wallet_id = :walletId")
    Mono<BigDecimal> calculateWalletBalance(Long walletId);

    @Query("SELECT * FROM ledger_entries WHERE wallet_id = :walletId AND entry_type = :entryType " +
            "ORDER BY created_at DESC")
    Flux<LedgerEntry> findByWalletIdAndEntryType(Long walletId, String entryType);
}
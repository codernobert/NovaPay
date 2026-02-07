package com.digitalwallet.service;

import com.digitalwallet.dto.ReconciliationReport;
import com.digitalwallet.model.Wallet;
import com.digitalwallet.repository.LedgerEntryRepository;
import com.digitalwallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditService auditService;

    public Mono<ReconciliationReport> runDailyReconciliation() {
        log.info("Starting daily reconciliation process");

        return walletRepository.findAll()
                .flatMap(this::reconcileWallet)
                .collectList()
                .flatMap(this::generateReport)
                .flatMap(report ->
                        auditService.logAction("RECONCILIATION", report.getReconciliationId(),
                                        "RECONCILIATION_RUN", null, null, report.getSummary())
                                .thenReturn(report)
                )
                .doOnSuccess(report -> log.info("Reconciliation completed: {}", report.getSummary()))
                .doOnError(error -> log.error("Reconciliation failed", error));
    }

    private Mono<ReconciliationReport.WalletDiscrepancy> reconcileWallet(Wallet wallet) {
        return ledgerEntryRepository.calculateWalletBalance(wallet.getId())
                .defaultIfEmpty(BigDecimal.ZERO)
                .map(ledgerBalance -> {
                    BigDecimal walletBalance = wallet.getBalance();
                    BigDecimal difference = walletBalance.subtract(ledgerBalance);

                    if (difference.compareTo(BigDecimal.ZERO) != 0) {
                        log.warn("Discrepancy found for wallet {}: Wallet Balance = {}, Ledger Balance = {}, Difference = {}",
                                wallet.getWalletNumber(), walletBalance, ledgerBalance, difference);
                    }

                    return ReconciliationReport.WalletDiscrepancy.builder()
                            .walletId(wallet.getId())
                            .walletNumber(wallet.getWalletNumber())
                            .walletBalance(walletBalance)
                            .ledgerBalance(ledgerBalance)
                            .difference(difference)
                            .build();
                });
    }

    private Mono<ReconciliationReport> generateReport(List<ReconciliationReport.WalletDiscrepancy> discrepancies) {
        BigDecimal totalWalletBalance = discrepancies.stream()
                .map(ReconciliationReport.WalletDiscrepancy::getWalletBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLedgerBalance = discrepancies.stream()
                .map(ReconciliationReport.WalletDiscrepancy::getLedgerBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscrepancy = totalWalletBalance.subtract(totalLedgerBalance);

        List<ReconciliationReport.WalletDiscrepancy> walletsWithDiscrepancy = discrepancies.stream()
                .filter(d -> d.getDifference().compareTo(BigDecimal.ZERO) != 0)
                .toList();

        String status = walletsWithDiscrepancy.isEmpty() ? "SUCCESS" : "DISCREPANCIES_FOUND";
        String summary = String.format(
                "Reconciliation completed. Total wallets: %d, Wallets with discrepancies: %d, Total discrepancy: %s",
                discrepancies.size(), walletsWithDiscrepancy.size(), totalDiscrepancy
        );

        return Mono.just(ReconciliationReport.builder()
                .reconciliationId(System.currentTimeMillis())
                .reconciliationDate(LocalDateTime.now())
                .status(status)
                .totalWalletBalance(totalWalletBalance)
                .totalLedgerBalance(totalLedgerBalance)
                .discrepancy(totalDiscrepancy)
                .walletDiscrepancies(walletsWithDiscrepancy)
                .summary(summary)
                .build());
    }

    public Mono<ReconciliationReport.WalletDiscrepancy> reconcileSingleWallet(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .flatMap(this::reconcileWallet)
                .doOnSuccess(result -> log.info("Single wallet reconciliation completed for: {}", walletNumber));
    }
}
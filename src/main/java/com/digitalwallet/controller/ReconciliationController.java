package com.digitalwallet.controller;

import com.digitalwallet.dto.ReconciliationReport;
import com.digitalwallet.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/run")
    public Mono<ResponseEntity<ReconciliationReport>> runReconciliation() {
        return reconciliationService.runDailyReconciliation()
                .map(ResponseEntity::ok)
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.internalServerError().build())
                );
    }

    @GetMapping("/wallet/{walletNumber}")
    public Mono<ResponseEntity<ReconciliationReport.WalletDiscrepancy>> reconcileWallet(
            @PathVariable String walletNumber) {
        return reconciliationService.reconcileSingleWallet(walletNumber)
                .map(ResponseEntity::ok)
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }
}
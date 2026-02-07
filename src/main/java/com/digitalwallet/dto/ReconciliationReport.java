package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationReport {

    private Long reconciliationId;
    private LocalDateTime reconciliationDate;
    private String status;
    private BigDecimal totalWalletBalance;
    private BigDecimal totalLedgerBalance;
    private BigDecimal discrepancy;
    private List<WalletDiscrepancy> walletDiscrepancies;
    private String summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletDiscrepancy {
        private Long walletId;
        private String walletNumber;
        private BigDecimal walletBalance;
        private BigDecimal ledgerBalance;
        private BigDecimal difference;
    }
}
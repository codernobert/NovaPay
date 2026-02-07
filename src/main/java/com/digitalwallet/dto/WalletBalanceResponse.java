package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceResponse {

    private Long walletId;
    private String walletNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
    private BigDecimal dailyLimit;
    private BigDecimal availableBalance;
    private LocalDateTime lastUpdated;
}
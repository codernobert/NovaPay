package com.digitalwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallets")
public class Wallet {
    
    @Id
    private Long id;
    
    private Long userId;
    private String walletNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
    private String walletType;
    private BigDecimal dailyLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum Status {
        ACTIVE, INACTIVE, FROZEN, CLOSED
    }
    
    public enum WalletType {
        STANDARD, PREMIUM, BUSINESS
    }
}
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
@Table("transfers")
public class Transfer {

    @Id
    private Long id;

    private String transferReference;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String transferType;
    private String description;
    private Long initiatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED
    }

    public enum TransferType {
        P2P, DEPOSIT, WITHDRAWAL, REFUND
    }
}
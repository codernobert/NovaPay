package com.digitalwallet.event;

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
public class TransferEvent {

    private Long transferId;
    private String transferReference;
    private TransferEventType eventType;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Long initiatedBy;
    private LocalDateTime timestamp;
    private String message;
}
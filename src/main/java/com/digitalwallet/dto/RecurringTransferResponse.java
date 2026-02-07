package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransferResponse {
    
    private Long recurringTransferId;
    private String sourceWalletNumber;
    private String destinationWalletNumber;
    private Long savingsGoalId;
    private String savingsGoalName;
    private BigDecimal amount;
    private String currency;
    private String frequency;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private LocalTime executionTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private LocalDateTime lastExecutedAt;
    private String status;
    private Integer executionCount;
    private Integer maxExecutions;
    private String description;
    private LocalDateTime createdAt;
}
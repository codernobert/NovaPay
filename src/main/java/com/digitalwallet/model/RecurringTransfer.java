package com.digitalwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recurring_transfers")
public class RecurringTransfer {
    
    @Id
    private Long id;
    
    private Long userId;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private Long savingsGoalId; // Optional: link to savings goal
    private BigDecimal amount;
    private String currency;
    private String frequency; // DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY
    private Integer dayOfWeek; // For WEEKLY (1=Monday, 7=Sunday)
    private Integer dayOfMonth; // For MONTHLY (1-28)
    private LocalTime executionTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private LocalDateTime lastExecutedAt;
    private String status;
    private Integer executionCount;
    private Integer maxExecutions; // Optional: stop after N executions
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum Status {
        ACTIVE, PAUSED, COMPLETED, CANCELLED, FAILED
    }
    
    public enum Frequency {
        DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
    }
}
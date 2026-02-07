package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransferRequest {
    
    @NotBlank(message = "Source wallet number is required")
    private String sourceWalletNumber;
    
    @NotBlank(message = "Destination wallet number is required")
    private String destinationWalletNumber;
    
    private Long savingsGoalId; // Optional: link to savings goal
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotBlank(message = "Frequency is required")
    private String frequency; // DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY
    
    private Integer dayOfWeek; // For WEEKLY (1=Monday, 7=Sunday)
    
    private Integer dayOfMonth; // For MONTHLY (1-28)
    
    private LocalTime executionTime; // Default: 09:00:00
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate; // Optional
    
    private Integer maxExecutions; // Optional: stop after N executions
    
    private String description;
}
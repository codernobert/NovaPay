package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalResponse {
    
    private Long goalId;
    private String goalName;
    private String description;
    private String savingsWalletNumber;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String currency;
    private BigDecimal progressPercentage;
    private LocalDate targetDate;
    private String status;
    private Integer daysRemaining;
    private BigDecimal amountNeeded;
    private BigDecimal suggestedMonthlyContribution;
    private LocalDateTime createdAt;
    private LocalDateTime achievedAt;
}
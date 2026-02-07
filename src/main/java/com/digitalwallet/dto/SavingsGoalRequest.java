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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalRequest {
    
    @NotBlank(message = "Goal name is required")
    private String goalName;
    
    private String description;
    
    @NotNull(message = "Savings wallet number is required")
    private String savingsWalletNumber;
    
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "1.0", message = "Target amount must be at least 1.0")
    private BigDecimal targetAmount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Target date is required")
    private LocalDate targetDate;
}
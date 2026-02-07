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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("savings_goals")
public class SavingsGoal {
    
    @Id
    private Long id;
    
    private Long userId;
    private Long savingsWalletId;
    private String goalName;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String currency;
    private LocalDate targetDate;
    private String status;
    private BigDecimal progressPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime achievedAt;
    
    public enum Status {
        ACTIVE, PAUSED, ACHIEVED, CANCELLED, EXPIRED
    }
}
package com.digitalwallet.service;

import com.digitalwallet.dto.SavingsGoalRequest;
import com.digitalwallet.dto.SavingsGoalResponse;
import com.digitalwallet.model.SavingsGoal;
import com.digitalwallet.model.Wallet;
import com.digitalwallet.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingsGoalService {
    
    private final SavingsGoalRepository savingsGoalRepository;
    private final WalletService walletService;
    private final AuditService auditService;
    
    @Transactional
    public Mono<SavingsGoalResponse> createSavingsGoal(SavingsGoalRequest request, Long userId) {
        log.info("Creating savings goal for user: {}", userId);
        
        return walletService.getWalletByNumber(request.getSavingsWalletNumber())
                .flatMap(wallet -> validateGoalCreation(request, wallet, userId))
                .flatMap(wallet -> {
                    SavingsGoal goal = SavingsGoal.builder()
                            .userId(userId)
                            .savingsWalletId(wallet.getId())
                            .goalName(request.getGoalName())
                            .description(request.getDescription())
                            .targetAmount(request.getTargetAmount())
                            .currentAmount(BigDecimal.ZERO)
                            .currency(request.getCurrency())
                            .targetDate(request.getTargetDate())
                            .status(SavingsGoal.Status.ACTIVE.name())
                            .progressPercentage(BigDecimal.ZERO)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    return savingsGoalRepository.save(goal);
                })
                .flatMap(savedGoal -> 
                    auditService.logAction("SAVINGS_GOAL", savedGoal.getId(), "GOAL_CREATED", 
                            userId, null, "Created goal: " + savedGoal.getGoalName())
                            .thenReturn(savedGoal)
                )
                .map(this::buildGoalResponse)
                .doOnSuccess(response -> log.info("Savings goal created: {}", response.getGoalId()))
                .doOnError(error -> log.error("Failed to create savings goal", error));
    }
    
    private Mono<Wallet> validateGoalCreation(SavingsGoalRequest request, Wallet wallet, Long userId) {
        if (!wallet.getUserId().equals(userId)) {
            return Mono.error(new IllegalArgumentException("Wallet does not belong to user"));
        }
        
        if (!"ACTIVE".equals(wallet.getStatus())) {
            return Mono.error(new IllegalStateException("Savings wallet is not active"));
        }
        
        if (!wallet.getCurrency().equals(request.getCurrency())) {
            return Mono.error(new IllegalArgumentException("Currency mismatch with savings wallet"));
        }
        
        if (request.getTargetDate().isBefore(LocalDate.now())) {
            return Mono.error(new IllegalArgumentException("Target date must be in the future"));
        }
        
        if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Target amount must be greater than zero"));
        }
        
        return Mono.just(wallet);
    }
    
    public Flux<SavingsGoalResponse> getUserGoals(Long userId) {
        return savingsGoalRepository.findByUserId(userId)
                .map(this::buildGoalResponse);
    }
    
    public Flux<SavingsGoalResponse> getActiveUserGoals(Long userId) {
        return savingsGoalRepository.findActiveGoalsByUserId(userId)
                .map(this::buildGoalResponse);
    }
    
    public Mono<SavingsGoalResponse> getGoalById(Long goalId) {
        return savingsGoalRepository.findById(goalId)
                .map(this::buildGoalResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("Savings goal not found")));
    }
    
    @Transactional
    public Mono<SavingsGoalResponse> contributeToGoal(Long goalId, BigDecimal amount, Long userId) {
        log.info("Contributing {} to goal: {}", amount, goalId);
        
        return savingsGoalRepository.findById(goalId)
                .switchIfEmpty(Mono.error(new RuntimeException("Savings goal not found")))
                .flatMap(goal -> {
                    if (!goal.getUserId().equals(userId)) {
                        return Mono.error(new IllegalArgumentException("Goal does not belong to user"));
                    }
                    
                    if (!"ACTIVE".equals(goal.getStatus())) {
                        return Mono.error(new IllegalStateException("Goal is not active"));
                    }
                    
                    BigDecimal newAmount = goal.getCurrentAmount().add(amount);
                    
                    return savingsGoalRepository.updateProgress(goalId, newAmount)
                            .flatMap(updated -> savingsGoalRepository.findById(goalId))
                            .flatMap(updatedGoal -> {
                                // Check if goal is achieved
                                if (updatedGoal.getCurrentAmount().compareTo(updatedGoal.getTargetAmount()) >= 0) {
                                    return savingsGoalRepository.markAsAchieved(goalId)
                                            .flatMap(marked -> savingsGoalRepository.findById(goalId))
                                            .flatMap(achievedGoal -> 
                                                auditService.logAction("SAVINGS_GOAL", goalId, "GOAL_ACHIEVED", 
                                                        userId, null, "Goal achieved: " + achievedGoal.getGoalName())
                                                        .thenReturn(achievedGoal)
                                            );
                                }
                                return Mono.just(updatedGoal);
                            })
                            .flatMap(updatedGoal -> 
                                auditService.logAction("SAVINGS_GOAL", goalId, "CONTRIBUTION_MADE", 
                                        userId, goal.getCurrentAmount().toString(), newAmount.toString())
                                        .thenReturn(updatedGoal)
                            );
                })
                .map(this::buildGoalResponse)
                .doOnSuccess(response -> log.info("Contribution successful. New balance: {}", 
                        response.getCurrentAmount()));
    }
    
    @Transactional
    public Mono<Void> updateGoalStatus(Long goalId, String status, Long userId) {
        return savingsGoalRepository.findById(goalId)
                .flatMap(goal -> {
                    if (!goal.getUserId().equals(userId)) {
                        return Mono.error(new IllegalArgumentException("Goal does not belong to user"));
                    }
                    
                    goal.setStatus(status);
                    goal.setUpdatedAt(LocalDateTime.now());
                    return savingsGoalRepository.save(goal);
                })
                .flatMap(updatedGoal -> 
                    auditService.logAction("SAVINGS_GOAL", goalId, "STATUS_CHANGED", 
                            userId, null, "Status changed to: " + status)
                )
                .then();
    }
    
    public Mono<SavingsGoalResponse> pauseGoal(Long goalId, Long userId) {
        return updateGoalStatus(goalId, SavingsGoal.Status.PAUSED.name(), userId)
                .then(getGoalById(goalId));
    }
    
    public Mono<SavingsGoalResponse> resumeGoal(Long goalId, Long userId) {
        return updateGoalStatus(goalId, SavingsGoal.Status.ACTIVE.name(), userId)
                .then(getGoalById(goalId));
    }
    
    public Mono<SavingsGoalResponse> cancelGoal(Long goalId, Long userId) {
        return updateGoalStatus(goalId, SavingsGoal.Status.CANCELLED.name(), userId)
                .then(getGoalById(goalId));
    }
    
    private SavingsGoalResponse buildGoalResponse(SavingsGoal goal) {
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate());
        BigDecimal amountNeeded = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        
        // Calculate suggested monthly contribution
        BigDecimal suggestedMonthly = BigDecimal.ZERO;
        if (daysRemaining > 0 && amountNeeded.compareTo(BigDecimal.ZERO) > 0) {
            long monthsRemaining = daysRemaining / 30;
            if (monthsRemaining > 0) {
                suggestedMonthly = amountNeeded.divide(BigDecimal.valueOf(monthsRemaining), 
                        2, RoundingMode.HALF_UP);
            }
        }
        
        return SavingsGoalResponse.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .description(goal.getDescription())
                .savingsWalletNumber("****") // Masked for security
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .currency(goal.getCurrency())
                .progressPercentage(progressPercentage)
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .daysRemaining((int) daysRemaining)
                .amountNeeded(amountNeeded.max(BigDecimal.ZERO))
                .suggestedMonthlyContribution(suggestedMonthly)
                .createdAt(goal.getCreatedAt())
                .achievedAt(goal.getAchievedAt())
                .build();
    }
}
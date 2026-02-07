package com.digitalwallet.repository;

import com.digitalwallet.model.SavingsGoal;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface SavingsGoalRepository extends ReactiveCrudRepository<SavingsGoal, Long> {
    
    Flux<SavingsGoal> findByUserId(Long userId);
    
    @Query("SELECT * FROM savings_goals WHERE user_id = :userId AND status = 'ACTIVE'")
    Flux<SavingsGoal> findActiveGoalsByUserId(Long userId);
    
    @Query("SELECT * FROM savings_goals WHERE savings_wallet_id = :walletId AND status = 'ACTIVE'")
    Flux<SavingsGoal> findActiveGoalsByWalletId(Long walletId);
    
    @Query("SELECT * FROM savings_goals WHERE status = 'ACTIVE' AND target_date < :date")
    Flux<SavingsGoal> findOverdueGoals(LocalDate date);
    
    @Modifying
    @Query("UPDATE savings_goals SET current_amount = :amount, " +
           "progress_percentage = (:amount / target_amount * 100), " +
           "updated_at = CURRENT_TIMESTAMP WHERE id = :goalId")
    Mono<Integer> updateProgress(Long goalId, BigDecimal amount);
    
    @Modifying
    @Query("UPDATE savings_goals SET status = 'ACHIEVED', achieved_at = CURRENT_TIMESTAMP, " +
           "updated_at = CURRENT_TIMESTAMP WHERE id = :goalId")
    Mono<Integer> markAsAchieved(Long goalId);
    
    @Query("SELECT COUNT(*) FROM savings_goals WHERE user_id = :userId AND status = 'ACHIEVED'")
    Mono<Long> countAchievedGoals(Long userId);
}
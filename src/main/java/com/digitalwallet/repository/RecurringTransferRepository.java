package com.digitalwallet.repository;

import com.digitalwallet.model.RecurringTransfer;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface RecurringTransferRepository extends ReactiveCrudRepository<RecurringTransfer, Long> {
    
    Flux<RecurringTransfer> findByUserId(Long userId);
    
    @Query("SELECT * FROM recurring_transfers WHERE user_id = :userId AND status = 'ACTIVE'")
    Flux<RecurringTransfer> findActiveByUserId(Long userId);
    
    @Query("SELECT * FROM recurring_transfers WHERE savings_goal_id = :goalId AND status = 'ACTIVE'")
    Flux<RecurringTransfer> findByGoalId(Long goalId);
    
    @Query("SELECT * FROM recurring_transfers WHERE status = 'ACTIVE' " +
           "AND next_execution_date <= :date ORDER BY next_execution_date")
    Flux<RecurringTransfer> findDueForExecution(LocalDate date);
    
    @Modifying
    @Query("UPDATE recurring_transfers SET next_execution_date = :nextDate, " +
           "last_executed_at = :executedAt, execution_count = execution_count + 1, " +
           "updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateAfterExecution(Long id, LocalDate nextDate, LocalDateTime executedAt);
    
    @Modifying
    @Query("UPDATE recurring_transfers SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
    Mono<Integer> updateStatus(Long id, String status);
    
    @Query("SELECT COUNT(*) FROM recurring_transfers WHERE user_id = :userId AND status = 'ACTIVE'")
    Mono<Long> countActiveByUserId(Long userId);
}
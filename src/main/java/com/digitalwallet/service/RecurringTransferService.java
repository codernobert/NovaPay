package com.digitalwallet.service;

import com.digitalwallet.dto.RecurringTransferRequest;
import com.digitalwallet.dto.RecurringTransferResponse;
import com.digitalwallet.dto.TransferRequest;
import com.digitalwallet.model.RecurringTransfer;
import com.digitalwallet.model.SavingsGoal;
import com.digitalwallet.model.Wallet;
import com.digitalwallet.repository.RecurringTransferRepository;
import com.digitalwallet.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransferService {
    
    private final RecurringTransferRepository recurringTransferRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final WalletService walletService;
    private final TransferService transferService;
    private final SavingsGoalService savingsGoalService;
    private final AuditService auditService;
    
    @Transactional
    public Mono<RecurringTransferResponse> createRecurringTransfer(RecurringTransferRequest request, Long userId) {
        log.info("Creating recurring transfer for user: {}", userId);
        
        return validateRecurringTransfer(request, userId)
                .flatMap(wallets -> {
                    Wallet sourceWallet = wallets[0];
                    Wallet destinationWallet = wallets[1];
                    
                    LocalDate nextExecution = calculateNextExecutionDate(
                            request.getStartDate(),
                            request.getFrequency(),
                            request.getDayOfWeek(),
                            request.getDayOfMonth()
                    );
                    
                    RecurringTransfer recurring = RecurringTransfer.builder()
                            .userId(userId)
                            .sourceWalletId(sourceWallet.getId())
                            .destinationWalletId(destinationWallet.getId())
                            .savingsGoalId(request.getSavingsGoalId())
                            .amount(request.getAmount())
                            .currency(request.getCurrency())
                            .frequency(request.getFrequency())
                            .dayOfWeek(request.getDayOfWeek())
                            .dayOfMonth(request.getDayOfMonth())
                            .executionTime(request.getExecutionTime() != null ? 
                                    request.getExecutionTime() : LocalTime.of(9, 0))
                            .startDate(request.getStartDate())
                            .endDate(request.getEndDate())
                            .nextExecutionDate(nextExecution)
                            .status(RecurringTransfer.Status.ACTIVE.name())
                            .executionCount(0)
                            .maxExecutions(request.getMaxExecutions())
                            .description(request.getDescription())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    return recurringTransferRepository.save(recurring);
                })
                .flatMap(savedRecurring -> 
                    auditService.logAction("RECURRING_TRANSFER", savedRecurring.getId(), 
                            "RECURRING_CREATED", userId, null, 
                            "Created recurring transfer: " + savedRecurring.getFrequency())
                            .thenReturn(savedRecurring)
                )
                .flatMap(this::buildRecurringResponse)
                .doOnSuccess(response -> log.info("Recurring transfer created: {}", response.getRecurringTransferId()))
                .doOnError(error -> log.error("Failed to create recurring transfer", error));
    }
    
    private Mono<Wallet[]> validateRecurringTransfer(RecurringTransferRequest request, Long userId) {
        return walletService.getWalletByNumber(request.getSourceWalletNumber())
                .zipWith(walletService.getWalletByNumber(request.getDestinationWalletNumber()))
                .flatMap(wallets -> {
                    Wallet source = wallets.getT1();
                    Wallet destination = wallets.getT2();
                    
                    if (!source.getUserId().equals(userId)) {
                        return Mono.error(new IllegalArgumentException("Source wallet does not belong to user"));
                    }
                    
                    if (!source.getCurrency().equals(destination.getCurrency())) {
                        return Mono.error(new IllegalArgumentException("Currency mismatch"));
                    }
                    
                    if (!source.getCurrency().equals(request.getCurrency())) {
                        return Mono.error(new IllegalArgumentException("Currency does not match wallets"));
                    }
                    
                    if (request.getStartDate().isBefore(LocalDate.now())) {
                        return Mono.error(new IllegalArgumentException("Start date must be today or in the future"));
                    }
                    
                    if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
                        return Mono.error(new IllegalArgumentException("End date must be after start date"));
                    }
                    
                    // Validate savings goal if provided
                    if (request.getSavingsGoalId() != null) {
                        return savingsGoalRepository.findById(request.getSavingsGoalId())
                                .flatMap(goal -> {
                                    if (!goal.getUserId().equals(userId)) {
                                        return Mono.error(new IllegalArgumentException("Savings goal does not belong to user"));
                                    }
                                    if (!goal.getSavingsWalletId().equals(destination.getId())) {
                                        return Mono.error(new IllegalArgumentException("Destination wallet does not match savings goal"));
                                    }
                                    return Mono.just(new Wallet[]{source, destination});
                                });
                    }
                    
                    return Mono.just(new Wallet[]{source, destination});
                });
    }
    
    private LocalDate calculateNextExecutionDate(LocalDate startDate, String frequency, 
                                                 Integer dayOfWeek, Integer dayOfMonth) {
        LocalDate baseDate = startDate.isBefore(LocalDate.now()) ? LocalDate.now() : startDate;
        
        return switch (frequency) {
            case "DAILY" -> baseDate;
            case "WEEKLY" -> {
                if (dayOfWeek == null) dayOfWeek = baseDate.getDayOfWeek().getValue();
                DayOfWeek targetDay = DayOfWeek.of(dayOfWeek);
                yield baseDate.with(TemporalAdjusters.nextOrSame(targetDay));
            }
            case "BIWEEKLY" -> {
                if (dayOfWeek == null) dayOfWeek = baseDate.getDayOfWeek().getValue();
                DayOfWeek targetDay = DayOfWeek.of(dayOfWeek);
                LocalDate nextWeek = baseDate.with(TemporalAdjusters.nextOrSame(targetDay));
                yield nextWeek.isBefore(baseDate.plusDays(7)) ? nextWeek.plusWeeks(1) : nextWeek;
            }
            case "MONTHLY" -> {
                if (dayOfMonth == null) dayOfMonth = Math.min(baseDate.getDayOfMonth(), 28);
                int targetDay = Math.min(dayOfMonth, baseDate.lengthOfMonth());
                yield baseDate.getDayOfMonth() <= targetDay ? 
                        baseDate.withDayOfMonth(targetDay) : 
                        baseDate.plusMonths(1).withDayOfMonth(Math.min(targetDay, baseDate.plusMonths(1).lengthOfMonth()));
            }
            case "QUARTERLY" -> {
                if (dayOfMonth == null) dayOfMonth = 1;
                int targetDay = Math.min(dayOfMonth, 28);
                yield baseDate.plusMonths(3).withDayOfMonth(targetDay);
            }
            default -> baseDate;
        };
    }
    
    // Scheduled job to execute recurring transfers - runs every hour
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void processRecurringTransfers() {
        log.info("Processing recurring transfers scheduled task");
        
        recurringTransferRepository.findDueForExecution(LocalDate.now())
                .flatMap(this::executeRecurringTransfer)
                .subscribe(
                        result -> log.info("Recurring transfer executed: {}", result),
                        error -> log.error("Error executing recurring transfer", error),
                        () -> log.info("Completed processing recurring transfers")
                );
    }
    
    @Transactional
    private Mono<String> executeRecurringTransfer(RecurringTransfer recurring) {
        log.info("Executing recurring transfer: {}", recurring.getId());
        
        return walletService.getActiveWalletById(recurring.getSourceWalletId())
                .zipWith(walletService.getActiveWalletById(recurring.getDestinationWalletId()))
                .flatMap(wallets -> {
                    Wallet source = wallets.getT1();
                    Wallet destination = wallets.getT2();
                    
                    // Create a one-time transfer
                    TransferRequest transferRequest = TransferRequest.builder()
                            .sourceWalletNumber(source.getWalletNumber())
                            .destinationWalletNumber(destination.getWalletNumber())
                            .amount(recurring.getAmount())
                            .currency(recurring.getCurrency())
                            .description("Recurring transfer: " + recurring.getDescription())
                            .build();
                    
                    return transferService.initiateTransfer(transferRequest, recurring.getUserId());
                })
                .flatMap(transferResponse -> {
                    // Update contribution to savings goal if linked
                    if (recurring.getSavingsGoalId() != null) {
                        return savingsGoalService.contributeToGoal(
                                recurring.getSavingsGoalId(), 
                                recurring.getAmount(), 
                                recurring.getUserId()
                        ).thenReturn(transferResponse);
                    }
                    return Mono.just(transferResponse);
                })
                .flatMap(transferResponse -> {
                    // Calculate next execution date
                    LocalDate nextDate = calculateNextExecutionDate(
                            recurring.getNextExecutionDate().plusDays(1),
                            recurring.getFrequency(),
                            recurring.getDayOfWeek(),
                            recurring.getDayOfMonth()
                    );
                    
                    // Update recurring transfer
                    return recurringTransferRepository.updateAfterExecution(
                            recurring.getId(), 
                            nextDate, 
                            LocalDateTime.now()
                    ).thenReturn(recurring);
                })
                .flatMap(updatedRecurring -> {
                    // Check if we should stop (end date or max executions reached)
                    return recurringTransferRepository.findById(updatedRecurring.getId())
                            .flatMap(refreshed -> {
                                boolean shouldStop = false;
                                
                                if (refreshed.getEndDate() != null && 
                                    refreshed.getNextExecutionDate().isAfter(refreshed.getEndDate())) {
                                    shouldStop = true;
                                }
                                
                                if (refreshed.getMaxExecutions() != null && 
                                    refreshed.getExecutionCount() >= refreshed.getMaxExecutions()) {
                                    shouldStop = true;
                                }
                                
                                if (shouldStop) {
                                    return recurringTransferRepository.updateStatus(
                                            refreshed.getId(), 
                                            RecurringTransfer.Status.COMPLETED.name()
                                    ).thenReturn("Recurring transfer completed: " + refreshed.getId());
                                }
                                
                                return Mono.just("Recurring transfer executed successfully: " + refreshed.getId());
                            });
                })
                .doOnSuccess(result -> log.info("Recurring transfer execution result: {}", result))
                .onErrorResume(error -> {
                    log.error("Failed to execute recurring transfer: {}", recurring.getId(), error);
                    return recurringTransferRepository.updateStatus(
                            recurring.getId(), 
                            RecurringTransfer.Status.FAILED.name()
                    ).thenReturn("Recurring transfer failed: " + recurring.getId());
                });
    }
    
    public Flux<RecurringTransferResponse> getUserRecurringTransfers(Long userId) {
        return recurringTransferRepository.findByUserId(userId)
                .flatMap(this::buildRecurringResponse);
    }
    
    public Flux<RecurringTransferResponse> getActiveRecurringTransfers(Long userId) {
        return recurringTransferRepository.findActiveByUserId(userId)
                .flatMap(this::buildRecurringResponse);
    }
    
    public Mono<RecurringTransferResponse> pauseRecurringTransfer(Long recurringId, Long userId) {
        return updateRecurringStatus(recurringId, RecurringTransfer.Status.PAUSED.name(), userId);
    }
    
    public Mono<RecurringTransferResponse> resumeRecurringTransfer(Long recurringId, Long userId) {
        return updateRecurringStatus(recurringId, RecurringTransfer.Status.ACTIVE.name(), userId);
    }
    
    public Mono<RecurringTransferResponse> cancelRecurringTransfer(Long recurringId, Long userId) {
        return updateRecurringStatus(recurringId, RecurringTransfer.Status.CANCELLED.name(), userId);
    }
    
    private Mono<RecurringTransferResponse> updateRecurringStatus(Long recurringId, String status, Long userId) {
        return recurringTransferRepository.findById(recurringId)
                .flatMap(recurring -> {
                    if (!recurring.getUserId().equals(userId)) {
                        return Mono.error(new IllegalArgumentException("Recurring transfer does not belong to user"));
                    }
                    
                    return recurringTransferRepository.updateStatus(recurringId, status)
                            .flatMap(updated -> recurringTransferRepository.findById(recurringId));
                })
                .flatMap(updatedRecurring -> 
                    auditService.logAction("RECURRING_TRANSFER", recurringId, "STATUS_CHANGED", 
                            userId, null, "Status changed to: " + status)
                            .thenReturn(updatedRecurring)
                )
                .flatMap(this::buildRecurringResponse);
    }
    
    private Mono<RecurringTransferResponse> buildRecurringResponse(RecurringTransfer recurring) {
        Mono<String> goalNameMono = Mono.just("");
        
        if (recurring.getSavingsGoalId() != null) {
            goalNameMono = savingsGoalRepository.findById(recurring.getSavingsGoalId())
                    .map(SavingsGoal::getGoalName)
                    .defaultIfEmpty("");
        }
        
        return Mono.zip(
                walletService.getActiveWalletById(recurring.getSourceWalletId()),
                walletService.getActiveWalletById(recurring.getDestinationWalletId()),
                goalNameMono
        ).map(tuple -> RecurringTransferResponse.builder()
                .recurringTransferId(recurring.getId())
                .sourceWalletNumber(tuple.getT1().getWalletNumber())
                .destinationWalletNumber(tuple.getT2().getWalletNumber())
                .savingsGoalId(recurring.getSavingsGoalId())
                .savingsGoalName(tuple.getT3())
                .amount(recurring.getAmount())
                .currency(recurring.getCurrency())
                .frequency(recurring.getFrequency())
                .dayOfWeek(recurring.getDayOfWeek())
                .dayOfMonth(recurring.getDayOfMonth())
                .executionTime(recurring.getExecutionTime())
                .startDate(recurring.getStartDate())
                .endDate(recurring.getEndDate())
                .nextExecutionDate(recurring.getNextExecutionDate())
                .lastExecutedAt(recurring.getLastExecutedAt())
                .status(recurring.getStatus())
                .executionCount(recurring.getExecutionCount())
                .maxExecutions(recurring.getMaxExecutions())
                .description(recurring.getDescription())
                .createdAt(recurring.getCreatedAt())
                .build());
    }
}
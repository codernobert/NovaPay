package com.digitalwallet.service;

import com.digitalwallet.model.AuditLog;
import com.digitalwallet.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public Mono<AuditLog> logAction(String entityType, Long entityId, String action,
                                    Long performedBy, String oldValue, String newValue) {
        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(LocalDateTime.now())
                .build();

        return auditLogRepository.save(auditLog)
                .doOnSuccess(auditLogEntry -> log.info("Audit log created: {} - {} - {}", entityType, entityId, action))
                .doOnError(error -> log.error("Failed to create audit log", error));
    }

    public Mono<AuditLog> logTransferAction(Long transferId, String action, Long performedBy, String details) {
        return logAction("TRANSFER", transferId, action, performedBy, null, details);
    }

    public Mono<AuditLog> logWalletAction(Long walletId, String action, Long performedBy,
                                          String oldBalance, String newBalance) {
        return logAction("WALLET", walletId, action, performedBy, oldBalance, newBalance);
    }

    public Mono<AuditLog> logUserAction(Long userId, String action, Long performedBy) {
        return logAction("USER", userId, action, performedBy, null, null);
    }
}
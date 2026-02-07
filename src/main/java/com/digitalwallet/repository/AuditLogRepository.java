package com.digitalwallet.repository;

import com.digitalwallet.model.AuditLog;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AuditLogRepository extends ReactiveCrudRepository<AuditLog, Long> {

    Flux<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    Flux<AuditLog> findByPerformedBy(Long performedBy);

    @Query("SELECT * FROM audit_logs WHERE entity_type = :entityType " +
            "AND entity_id = :entityId ORDER BY created_at DESC LIMIT :limit")
    Flux<AuditLog> findRecentAuditLogs(String entityType, Long entityId, int limit);

    @Query("SELECT * FROM audit_logs WHERE action = :action ORDER BY created_at DESC")
    Flux<AuditLog> findByAction(String action);
}
package com.digitalwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_logs")
public class AuditLog {

    @Id
    private Long id;

    private String entityType;
    private Long entityId;
    private String action;
    private Long performedBy;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    public enum Action {
        CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT,
        TRANSFER_INITIATED, TRANSFER_COMPLETED, TRANSFER_FAILED,
        WALLET_CREDITED, WALLET_DEBITED, RECONCILIATION_RUN
    }
}
package com.ticketguard.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private String action;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public AuditLog() {}

    public AuditLog(Long id, Long adminId, String action, String tableName, Long recordId, LocalDateTime createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.action = action;
        this.tableName = tableName;
        this.recordId = recordId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Builder
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private Long id;
        private Long adminId;
        private String action;
        private String tableName;
        private Long recordId;
        private LocalDateTime createdAt;

        public AuditLogBuilder id(Long id) { this.id = id; return this; }
        public AuditLogBuilder adminId(Long adminId) { this.adminId = adminId; return this; }
        public AuditLogBuilder action(String action) { this.action = action; return this; }
        public AuditLogBuilder tableName(String tableName) { this.tableName = tableName; return this; }
        public AuditLogBuilder recordId(Long recordId) { this.recordId = recordId; return this; }
        public AuditLogBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AuditLog build() {
            return new AuditLog(id, adminId, action, tableName, recordId, createdAt);
        }
    }
}

package com.ticketguard.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Report() {}

    public Report(Long id, String reportName, User generatedBy, String reportType, LocalDateTime createdAt) {
        this.id = id;
        this.reportName = reportName;
        this.generatedBy = generatedBy;
        this.reportType = reportType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public User getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(User generatedBy) { this.generatedBy = generatedBy; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Builder
    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public static class ReportBuilder {
        private Long id;
        private String reportName;
        private User generatedBy;
        private String reportType;
        private LocalDateTime createdAt;

        public ReportBuilder id(Long id) { this.id = id; return this; }
        public ReportBuilder reportName(String reportName) { this.reportName = reportName; return this; }
        public ReportBuilder generatedBy(User generatedBy) { this.generatedBy = generatedBy; return this; }
        public ReportBuilder reportType(String reportType) { this.reportType = reportType; return this; }
        public ReportBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Report build() {
            return new Report(id, reportName, generatedBy, reportType, createdAt);
        }
    }
}

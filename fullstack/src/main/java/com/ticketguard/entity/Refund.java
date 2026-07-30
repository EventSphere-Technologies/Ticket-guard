package com.ticketguard.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "refund_amount", nullable = false)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    private String reason;

    // Constructors
    public Refund() {}

    public Refund(Long id, Payment payment, BigDecimal refundAmount, RefundStatus refundStatus, LocalDateTime refundDate, String reason) {
        this.id = id;
        this.payment = payment;
        this.refundAmount = refundAmount;
        this.refundStatus = refundStatus;
        this.refundDate = refundDate;
        this.reason = reason;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }

    public LocalDateTime getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDateTime refundDate) { this.refundDate = refundDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @PrePersist
    protected void onCreate() {
        refundDate = LocalDateTime.now();
        if (refundStatus == null) {
            refundStatus = RefundStatus.IN_PROGRESS;
        }
    }

    public enum RefundStatus {
        IN_PROGRESS, COMPLETED, FAILED
    }

    // Builder
    public static RefundBuilder builder() {
        return new RefundBuilder();
    }

    public static class RefundBuilder {
        private Long id;
        private Payment payment;
        private BigDecimal refundAmount;
        private RefundStatus refundStatus;
        private LocalDateTime refundDate;
        private String reason;

        public RefundBuilder id(Long id) { this.id = id; return this; }
        public RefundBuilder payment(Payment payment) { this.payment = payment; return this; }
        public RefundBuilder refundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; return this; }
        public RefundBuilder refundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; return this; }
        public RefundBuilder refundDate(LocalDateTime refundDate) { this.refundDate = refundDate; return this; }
        public RefundBuilder reason(String reason) { this.reason = reason; return this; }

        public Refund build() {
            return new Refund(id, payment, refundAmount, refundStatus, refundDate, reason);
        }
    }
}

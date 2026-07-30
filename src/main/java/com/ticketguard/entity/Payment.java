package com.ticketguard.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    // Constructors
    public Payment() {}

    @SuppressWarnings("java:S107")
    public Payment(Long id, Booking booking, String paymentMethod, String transactionId, BigDecimal amount, PaymentStatus paymentStatus, LocalDateTime paymentTime, String gatewayResponse) {
        this.id = id;
        this.booking = booking;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentTime = paymentTime;
        this.gatewayResponse = gatewayResponse;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    @PrePersist
    protected void onCreate() {
        paymentTime = LocalDateTime.now();
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
    }

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING
    }

    // Builder
    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long id;
        private Booking booking;
        private String paymentMethod;
        private String transactionId;
        private BigDecimal amount;
        private PaymentStatus paymentStatus;
        private LocalDateTime paymentTime;
        private String gatewayResponse;

        public PaymentBuilder id(Long id) { this.id = id; return this; }
        public PaymentBuilder booking(Booking booking) { this.booking = booking; return this; }
        public PaymentBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public PaymentBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public PaymentBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentBuilder paymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public PaymentBuilder paymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; return this; }
        public PaymentBuilder gatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; return this; }

        public Payment build() {
            return new Payment(id, booking, paymentMethod, transactionId, amount, paymentStatus, paymentTime, gatewayResponse);
        }
    }
}

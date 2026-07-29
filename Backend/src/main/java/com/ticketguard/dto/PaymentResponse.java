package com.ticketguard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long paymentId;
    private Long bookingId;
    private String transactionId;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime paymentTime;
    private String qrCode;
    private String message;

    // Constructors
    public PaymentResponse() {}

    @SuppressWarnings("java:S107")
    public PaymentResponse(Long paymentId, Long bookingId, String transactionId, BigDecimal amount, String paymentStatus, LocalDateTime paymentTime, String qrCode, String message) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentTime = paymentTime;
        this.qrCode = qrCode;
        this.message = message;
    }

    // Getters and Setters
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // Builder
    public static PaymentResponseBuilder builder() {
        return new PaymentResponseBuilder();
    }

    public static class PaymentResponseBuilder {
        private Long paymentId;
        private Long bookingId;
        private String transactionId;
        private BigDecimal amount;
        private String paymentStatus;
        private LocalDateTime paymentTime;
        private String qrCode;
        private String message;

        public PaymentResponseBuilder paymentId(Long paymentId) { this.paymentId = paymentId; return this; }
        public PaymentResponseBuilder bookingId(Long bookingId) { this.bookingId = bookingId; return this; }
        public PaymentResponseBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public PaymentResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public PaymentResponseBuilder paymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public PaymentResponseBuilder paymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; return this; }
        public PaymentResponseBuilder qrCode(String qrCode) { this.qrCode = qrCode; return this; }
        public PaymentResponseBuilder message(String message) { this.message = message; return this; }

        public PaymentResponse build() {
            return new PaymentResponse(paymentId, bookingId, transactionId, amount, paymentStatus, paymentTime, qrCode, message);
        }
    }
}

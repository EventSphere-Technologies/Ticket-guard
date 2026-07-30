package com.ticketguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String upiId;
    
    private String cardHolder;
    
    private String cardLastFour;

    // Constructors
    public PaymentRequest() {}

    public PaymentRequest(Long bookingId, String paymentMethod, String upiId, String cardHolder, String cardLastFour) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.upiId = upiId;
        this.cardHolder = cardHolder;
        this.cardLastFour = cardLastFour;
    }

    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
}

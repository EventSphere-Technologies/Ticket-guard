package com.ticketguard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "card_holder")
    private String cardHolder;

    @Column(name = "card_last_four")
    private String cardLastFour;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    // Constructors
    public PaymentMethod() {}

    public PaymentMethod(Long id, User user, String cardHolder, String cardLastFour, PaymentType paymentType, String upiId, boolean isDefault) {
        this.id = id;
        this.user = user;
        this.cardHolder = cardHolder;
        this.cardLastFour = cardLastFour;
        this.paymentType = paymentType;
        this.upiId = upiId;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCardHolder() { return cardHolder; }
    public void setCardHolder(String cardHolder) { this.cardHolder = cardHolder; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public enum PaymentType {
        CARD, UPI
    }

    // Builder
    public static PaymentMethodBuilder builder() {
        return new PaymentMethodBuilder();
    }

    public static class PaymentMethodBuilder {
        private Long id;
        private User user;
        private String cardHolder;
        private String cardLastFour;
        private PaymentType paymentType;
        private String upiId;
        private boolean isDefault;

        public PaymentMethodBuilder id(Long id) { this.id = id; return this; }
        public PaymentMethodBuilder user(User user) { this.user = user; return this; }
        public PaymentMethodBuilder cardHolder(String cardHolder) { this.cardHolder = cardHolder; return this; }
        public PaymentMethodBuilder cardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; return this; }
        public PaymentMethodBuilder paymentType(PaymentType paymentType) { this.paymentType = paymentType; return this; }
        public PaymentMethodBuilder upiId(String upiId) { this.upiId = upiId; return this; }
        public PaymentMethodBuilder isDefault(boolean isDefault) { this.isDefault = isDefault; return this; }

        public PaymentMethod build() {
            return new PaymentMethod(id, user, cardHolder, cardLastFour, paymentType, upiId, isDefault);
        }
    }
}

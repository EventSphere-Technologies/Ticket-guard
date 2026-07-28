package com.ticketguard.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_fraud_logs")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AiFraudLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "risk_score", nullable = false)
    private double riskScore;

    @Column(nullable = false)
    private String reason;

    @Column(name = "action_taken")
    private String actionTaken;

    @Column(name = "keystroke_velocity")
    private Double keystrokeVelocity;

    @Column(name = "mouse_movement_entropy")
    private Double mouseMovementEntropy;

    @Column(name = "time_spent_seconds")
    private Double timeSpentSeconds;

    @Column(name = "is_bot")
    private Boolean isBot;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public AiFraudLog() {}

    public AiFraudLog(Long id, User user, Booking booking, double riskScore, String reason, String actionTaken, Double keystrokeVelocity, Double mouseMovementEntropy, Double timeSpentSeconds, Boolean isBot, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.booking = booking;
        this.riskScore = riskScore;
        this.reason = reason;
        this.actionTaken = actionTaken;
        this.keystrokeVelocity = keystrokeVelocity;
        this.mouseMovementEntropy = mouseMovementEntropy;
        this.timeSpentSeconds = timeSpentSeconds;
        this.isBot = isBot;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public Double getKeystrokeVelocity() { return keystrokeVelocity; }
    public void setKeystrokeVelocity(Double keystrokeVelocity) { this.keystrokeVelocity = keystrokeVelocity; }

    public Double getMouseMovementEntropy() { return mouseMovementEntropy; }
    public void setMouseMovementEntropy(Double mouseMovementEntropy) { this.mouseMovementEntropy = mouseMovementEntropy; }

    public Double getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Double timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public Boolean getIsBot() { return isBot; }
    public void setIsBot(Boolean isBot) { this.isBot = isBot; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Builder
    public static AiFraudLogBuilder builder() {
        return new AiFraudLogBuilder();
    }

    public static class AiFraudLogBuilder {
        private Long id;
        private User user;
        private Booking booking;
        private double riskScore;
        private String reason;
        private String actionTaken;
        private Double keystrokeVelocity;
        private Double mouseMovementEntropy;
        private Double timeSpentSeconds;
        private Boolean isBot;
        private LocalDateTime createdAt;

        public AiFraudLogBuilder id(Long id) { this.id = id; return this; }
        public AiFraudLogBuilder user(User user) { this.user = user; return this; }
        public AiFraudLogBuilder booking(Booking booking) { this.booking = booking; return this; }
        public AiFraudLogBuilder riskScore(double riskScore) { this.riskScore = riskScore; return this; }
        public AiFraudLogBuilder reason(String reason) { this.reason = reason; return this; }
        public AiFraudLogBuilder actionTaken(String actionTaken) { this.actionTaken = actionTaken; return this; }
        public AiFraudLogBuilder keystrokeVelocity(Double keystrokeVelocity) { this.keystrokeVelocity = keystrokeVelocity; return this; }
        public AiFraudLogBuilder mouseMovementEntropy(Double mouseMovementEntropy) { this.mouseMovementEntropy = mouseMovementEntropy; return this; }
        public AiFraudLogBuilder timeSpentSeconds(Double timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; return this; }
        public AiFraudLogBuilder isBot(Boolean isBot) { this.isBot = isBot; return this; }
        public AiFraudLogBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AiFraudLog build() {
            return new AiFraudLog(id, user, booking, riskScore, reason, actionTaken, keystrokeVelocity, mouseMovementEntropy, timeSpentSeconds, isBot, createdAt);
        }
    }
}

package com.ticketguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false)
    private TicketStatus ticketStatus;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    // Constructors
    public Ticket() {}

    public Ticket(Long id, Booking booking, String ticketNumber, String qrCode, TicketStatus ticketStatus, LocalDateTime generatedAt) {
        this.id = id;
        this.booking = booking;
        this.ticketNumber = ticketNumber;
        this.qrCode = qrCode;
        this.ticketStatus = ticketStatus;
        this.generatedAt = generatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public TicketStatus getTicketStatus() { return ticketStatus; }
    public void setTicketStatus(TicketStatus ticketStatus) { this.ticketStatus = ticketStatus; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        if (ticketStatus == null) {
            ticketStatus = TicketStatus.ACTIVE;
        }
    }

    public enum TicketStatus {
        ACTIVE, USED, CANCELLED
    }

    // Builder
    public static TicketBuilder builder() {
        return new TicketBuilder();
    }

    public static class TicketBuilder {
        private Long id;
        private Booking booking;
        private String ticketNumber;
        private String qrCode;
        private TicketStatus ticketStatus;
        private LocalDateTime generatedAt;

        public TicketBuilder id(Long id) { this.id = id; return this; }
        public TicketBuilder booking(Booking booking) { this.booking = booking; return this; }
        public TicketBuilder ticketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; return this; }
        public TicketBuilder qrCode(String qrCode) { this.qrCode = qrCode; return this; }
        public TicketBuilder ticketStatus(TicketStatus ticketStatus) { this.ticketStatus = ticketStatus; return this; }
        public TicketBuilder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }

        public Ticket build() {
            return new Ticket(id, booking, ticketNumber, qrCode, ticketStatus, generatedAt);
        }
    }
}

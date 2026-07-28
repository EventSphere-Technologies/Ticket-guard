package com.ticketguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seat_reservations")
public class SeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatLayout seat;

    @Column(name = "reservation_start", nullable = false)
    private LocalDateTime reservationStart;

    @Column(name = "reservation_expiry", nullable = false)
    private LocalDateTime reservationExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    // Constructors
    public SeatReservation() {}

    public SeatReservation(Long id, User user, Event event, SeatLayout seat, LocalDateTime reservationStart, LocalDateTime reservationExpiry, ReservationStatus status) {
        this.id = id;
        this.user = user;
        this.event = event;
        this.seat = seat;
        this.reservationStart = reservationStart;
        this.reservationExpiry = reservationExpiry;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public SeatLayout getSeat() { return seat; }
    public void setSeat(SeatLayout seat) { this.seat = seat; }

    public LocalDateTime getReservationStart() { return reservationStart; }
    public void setReservationStart(LocalDateTime reservationStart) { this.reservationStart = reservationStart; }

    public LocalDateTime getReservationExpiry() { return reservationExpiry; }
    public void setReservationExpiry(LocalDateTime reservationExpiry) { this.reservationExpiry = reservationExpiry; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public enum ReservationStatus {
        LOCKED, EXPIRED, CONFIRMED
    }

    // Builder
    public static SeatReservationBuilder builder() {
        return new SeatReservationBuilder();
    }

    public static class SeatReservationBuilder {
        private Long id;
        private User user;
        private Event event;
        private SeatLayout seat;
        private LocalDateTime reservationStart;
        private LocalDateTime reservationExpiry;
        private ReservationStatus status;

        public SeatReservationBuilder id(Long id) { this.id = id; return this; }
        public SeatReservationBuilder user(User user) { this.user = user; return this; }
        public SeatReservationBuilder event(Event event) { this.event = event; return this; }
        public SeatReservationBuilder seat(SeatLayout seat) { this.seat = seat; return this; }
        public SeatReservationBuilder reservationStart(LocalDateTime reservationStart) { this.reservationStart = reservationStart; return this; }
        public SeatReservationBuilder reservationExpiry(LocalDateTime reservationExpiry) { this.reservationExpiry = reservationExpiry; return this; }
        public SeatReservationBuilder status(ReservationStatus status) { this.status = status; return this; }

        public SeatReservation build() {
            return new SeatReservation(id, user, event, seat, reservationStart, reservationExpiry, status);
        }
    }
}

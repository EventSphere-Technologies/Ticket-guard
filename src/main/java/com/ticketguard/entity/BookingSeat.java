package com.ticketguard.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats")
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatLayout seat;

    @Column(name = "seat_price", nullable = false)
    private BigDecimal seatPrice;

    // Constructors
    public BookingSeat() {}

    public BookingSeat(Long id, Booking booking, SeatLayout seat, BigDecimal seatPrice) {
        this.id = id;
        this.booking = booking;
        this.seat = seat;
        this.seatPrice = seatPrice;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public SeatLayout getSeat() { return seat; }
    public void setSeat(SeatLayout seat) { this.seat = seat; }

    public BigDecimal getSeatPrice() { return seatPrice; }
    public void setSeatPrice(BigDecimal seatPrice) { this.seatPrice = seatPrice; }

    // Builder
    public static BookingSeatBuilder builder() {
        return new BookingSeatBuilder();
    }

    public static class BookingSeatBuilder {
        private Long id;
        private Booking booking;
        private SeatLayout seat;
        private BigDecimal seatPrice;

        public BookingSeatBuilder id(Long id) { this.id = id; return this; }
        public BookingSeatBuilder booking(Booking booking) { this.booking = booking; return this; }
        public BookingSeatBuilder seat(SeatLayout seat) { this.seat = seat; return this; }
        public BookingSeatBuilder seatPrice(BigDecimal seatPrice) { this.seatPrice = seatPrice; return this; }

        public BookingSeat build() {
            return new BookingSeat(id, booking, seat, seatPrice);
        }
    }
}

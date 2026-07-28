package com.ticketguard.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@Entity
@Table(name = "seat_layouts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SeatLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "row_name", nullable = false)
    private String rowName;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    // Constructors
    public SeatLayout() {}

    public SeatLayout(Long id, Venue venue, String rowName, Integer seatNumber, SeatType seatType, BigDecimal price, SeatStatus status) {
        this.id = id;
        this.venue = venue;
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.price = price;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public String getRowName() { return rowName; }
    public void setRowName(String rowName) { this.rowName = rowName; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public SeatType getSeatType() { return seatType; }
    public void setSeatType(SeatType seatType) { this.seatType = seatType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public enum SeatType {
        REGULAR, VIP, BALCONY
    }

    public enum SeatStatus {
        AVAILABLE, BOOKED, LOCKED
    }

    // Builder
    public static SeatLayoutBuilder builder() {
        return new SeatLayoutBuilder();
    }

    public static class SeatLayoutBuilder {
        private Long id;
        private Venue venue;
        private String rowName;
        private Integer seatNumber;
        private SeatType seatType;
        private BigDecimal price;
        private SeatStatus status;

        public SeatLayoutBuilder id(Long id) { this.id = id; return this; }
        public SeatLayoutBuilder venue(Venue venue) { this.venue = venue; return this; }
        public SeatLayoutBuilder rowName(String rowName) { this.rowName = rowName; return this; }
        public SeatLayoutBuilder seatNumber(Integer seatNumber) { this.seatNumber = seatNumber; return this; }
        public SeatLayoutBuilder seatType(SeatType seatType) { this.seatType = seatType; return this; }
        public SeatLayoutBuilder price(BigDecimal price) { this.price = price; return this; }
        public SeatLayoutBuilder status(SeatStatus status) { this.status = status; return this; }

        public SeatLayout build() {
            return new SeatLayout(id, venue, rowName, seatNumber, seatType, price, status);
        }
    }
}

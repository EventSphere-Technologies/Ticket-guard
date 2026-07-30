package com.ticketguard.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_number", unique = true, nullable = false)
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "qr_code")
    private String qrCode;

    // Constructors
    public Booking() {}

    @SuppressWarnings("java:S107")
    public Booking(Long id, String bookingNumber, User user, Event event, LocalDateTime bookingDate, BigDecimal totalAmount, BookingStatus bookingStatus, PaymentStatus paymentStatus, String qrCode) {
        this.id = id;
        this.bookingNumber = bookingNumber;
        this.user = user;
        this.event = event;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
        this.paymentStatus = paymentStatus;
        this.qrCode = qrCode;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (bookingStatus == null) {
            bookingStatus = BookingStatus.PENDING;
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED
    }

    public enum PaymentStatus {
        PENDING, PAID, REFUNDED
    }

    // Builder
    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    public static class BookingBuilder {
        private Long id;
        private String bookingNumber;
        private User user;
        private Event event;
        private LocalDateTime bookingDate;
        private BigDecimal totalAmount;
        private BookingStatus bookingStatus;
        private PaymentStatus paymentStatus;
        private String qrCode;

        public BookingBuilder id(Long id) { this.id = id; return this; }
        public BookingBuilder bookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; return this; }
        public BookingBuilder user(User user) { this.user = user; return this; }
        public BookingBuilder event(Event event) { this.event = event; return this; }
        public BookingBuilder bookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; return this; }
        public BookingBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public BookingBuilder bookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; return this; }
        public BookingBuilder paymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public BookingBuilder qrCode(String qrCode) { this.qrCode = qrCode; return this; }

        public Booking build() {
            return new Booking(id, bookingNumber, user, event, bookingDate, totalAmount, bookingStatus, paymentStatus, qrCode);
        }
    }
}

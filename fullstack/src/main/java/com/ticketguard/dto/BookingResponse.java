package com.ticketguard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {
    private Long bookingId;
    private String bookingNumber;
    private Long eventId;
    private String eventTitle;
    private String venueName;
    private String eventDate;
    private String eventTime;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private String paymentStatus;
    private List<String> seatNames;
    private LocalDateTime reservationExpiry;
    private String qrCode;

    // Constructors
    public BookingResponse() {}

    @SuppressWarnings("java:S107")
    public BookingResponse(Long bookingId, String bookingNumber, Long eventId, String eventTitle, String venueName, String eventDate, String eventTime, BigDecimal totalAmount, String bookingStatus, String paymentStatus, List<String> seatNames, LocalDateTime reservationExpiry, String qrCode) {
        this.bookingId = bookingId;
        this.bookingNumber = bookingNumber;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.venueName = venueName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
        this.paymentStatus = paymentStatus;
        this.seatNames = seatNames;
        this.reservationExpiry = reservationExpiry;
        this.qrCode = qrCode;
    }

    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public List<String> getSeatNames() { return seatNames; }
    public void setSeatNames(List<String> seatNames) { this.seatNames = seatNames; }

    public LocalDateTime getReservationExpiry() { return reservationExpiry; }
    public void setReservationExpiry(LocalDateTime reservationExpiry) { this.reservationExpiry = reservationExpiry; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    // Builder
    public static BookingResponseBuilder builder() {
        return new BookingResponseBuilder();
    }

    public static class BookingResponseBuilder {
        private Long bookingId;
        private String bookingNumber;
        private Long eventId;
        private String eventTitle;
        private String venueName;
        private String eventDate;
        private String eventTime;
        private BigDecimal totalAmount;
        private String bookingStatus;
        private String paymentStatus;
        private List<String> seatNames;
        private LocalDateTime reservationExpiry;
        private String qrCode;

        public BookingResponseBuilder bookingId(Long bookingId) { this.bookingId = bookingId; return this; }
        public BookingResponseBuilder bookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; return this; }
        public BookingResponseBuilder eventId(Long eventId) { this.eventId = eventId; return this; }
        public BookingResponseBuilder eventTitle(String eventTitle) { this.eventTitle = eventTitle; return this; }
        public BookingResponseBuilder venueName(String venueName) { this.venueName = venueName; return this; }
        public BookingResponseBuilder eventDate(String eventDate) { this.eventDate = eventDate; return this; }
        public BookingResponseBuilder eventTime(String eventTime) { this.eventTime = eventTime; return this; }
        public BookingResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public BookingResponseBuilder bookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; return this; }
        public BookingResponseBuilder paymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public BookingResponseBuilder seatNames(List<String> seatNames) { this.seatNames = seatNames; return this; }
        public BookingResponseBuilder reservationExpiry(LocalDateTime reservationExpiry) { this.reservationExpiry = reservationExpiry; return this; }
        public BookingResponseBuilder qrCode(String qrCode) { this.qrCode = qrCode; return this; }

        public BookingResponse build() {
            return new BookingResponse(bookingId, bookingNumber, eventId, eventTitle, venueName, eventDate, eventTime, totalAmount, bookingStatus, paymentStatus, seatNames, reservationExpiry, qrCode);
        }
    }
}

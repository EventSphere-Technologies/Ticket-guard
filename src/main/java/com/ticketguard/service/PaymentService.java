package com.ticketguard.service;

import com.ticketguard.dto.PaymentRequest;
import com.ticketguard.dto.PaymentResponse;
import com.ticketguard.entity.Booking;
import com.ticketguard.entity.Payment;
import com.ticketguard.entity.Ticket;
import com.ticketguard.exception.BadRequestException;
import com.ticketguard.repository.BookingRepository;
import com.ticketguard.repository.PaymentRepository;
import com.ticketguard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingService bookingService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BadRequestException("Booking not found"));

        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new BadRequestException("Booking is not in PENDING state. Current status: " + booking.getBookingStatus());
        }

        // Simulate Gateway Call
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(txnId)
                .amount(booking.getTotalAmount())
                .paymentStatus(Payment.PaymentStatus.SUCCESS)
                .gatewayResponse("{\"status\":\"captured\",\"id\":\"" + txnId + "\",\"currency\":\"INR\"}")
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        // Confirm the booking and mark seats booked
        bookingService.confirmBookingPayment(booking.getId());

        // Generate Ticket
        String ticketNo = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String mockQr = "https://ticketguard.com/verify/" + ticketNo;

        Ticket ticket = Ticket.builder()
                .booking(booking)
                .ticketNumber(ticketNo)
                .qrCode(mockQr)
                .ticketStatus(Ticket.TicketStatus.ACTIVE)
                .build();
        ticketRepository.save(ticket);

        // Save QR code directly into booking
        booking.setQrCode(mockQr);
        bookingRepository.save(booking);

        return PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .bookingId(booking.getId())
                .transactionId(txnId)
                .amount(savedPayment.getAmount())
                .paymentStatus(savedPayment.getPaymentStatus().name())
                .paymentTime(savedPayment.getPaymentTime())
                .qrCode(mockQr)
                .message("Payment captured successfully")
                .build();
    }
}

package com.ticketguard.service;

import com.ticketguard.entity.*;
import com.ticketguard.exception.BadRequestException;
import com.ticketguard.exception.ResourceNotFoundException;
import com.ticketguard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RefundService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SeatLayoutRepository seatLayoutRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Refund issueRefund(Long bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getBookingStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking cannot be refunded because its status is: " + booking.getBookingStatus());
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for booking"));

        // Save Refund log
        Refund refund = Refund.builder()
                .payment(payment)
                .refundAmount(booking.getTotalAmount())
                .refundStatus(Refund.RefundStatus.COMPLETED)
                .reason(reason)
                .build();
        Refund savedRefund = refundRepository.save(refund);

        // Cancel booking
        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
        bookingRepository.save(booking);

        // Cancel Ticket
        ticketRepository.findByBookingId(bookingId).ifPresent(ticket -> {
            ticket.setTicketStatus(Ticket.TicketStatus.CANCELLED);
            ticketRepository.save(ticket);
        });

        // Release seats
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        for (BookingSeat bs : bookingSeats) {
            SeatLayout seat = bs.getSeat();
            seat.setStatus(SeatLayout.SeatStatus.AVAILABLE);
            seatLayoutRepository.save(seat);
        }

        // Send alert
        Notification cancellationAlert = Notification.builder()
                .user(booking.getUser())
                .title("Ticket Cancellation & Refund Issued")
                .message("Your booking " + booking.getBookingNumber() + " has been cancelled. A refund of INR " 
                        + booking.getTotalAmount() + " has been processed to your payment method.")
                .notificationType("REFUND")
                .isRead(false)
                .build();
        notificationRepository.save(cancellationAlert);

        return savedRefund;
    }
}

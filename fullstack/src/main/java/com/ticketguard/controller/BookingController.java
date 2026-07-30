package com.ticketguard.controller;

import com.ticketguard.dto.BookingResponse;
import com.ticketguard.dto.SeatReserveRequest;
import com.ticketguard.entity.Booking;
import com.ticketguard.exception.BadRequestException;
import com.ticketguard.service.AiFraudService;
import com.ticketguard.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AiFraudService aiFraudService;

    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> reserve(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SeatReserveRequest request) {
        
        // 1. Reserve seats (5-min lock)
        BookingResponse response = bookingService.reserveSeats(request, userDetails.getUsername());

        // 2. Fetch booking and run AI Fraud scan
        Booking booking = bookingService.getBookingById(response.getBookingId());
        double riskScore = aiFraudService.scanBookingForFraud(booking, request.getBehaviour());

        if (riskScore >= 85.0) {
            throw new BadRequestException("Booking rejected by security engine. Bot/Scalping activity pattern detected.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userDetails.getUsername());
        return ResponseEntity.ok(bookings);
    }
}

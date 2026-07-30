package com.ticketguard.controller;

import com.ticketguard.dto.PaymentRequest;
import com.ticketguard.dto.PaymentResponse;
import com.ticketguard.entity.Refund;
import com.ticketguard.service.PaymentService;
import com.ticketguard.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefundService refundService;

    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> charge(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<Refund> refund(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "Customer cancellation request") String reason) {
        Refund refund = refundService.issueRefund(bookingId, reason);
        return ResponseEntity.ok(refund);
    }
}

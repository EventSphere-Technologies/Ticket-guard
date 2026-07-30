package com.ticketguard.controller;

import com.ticketguard.dto.UserResponse;
import com.ticketguard.entity.Notification;
import com.ticketguard.entity.PaymentMethod;
import com.ticketguard.entity.User;
import com.ticketguard.exception.ResourceNotFoundException;
import com.ticketguard.repository.UserRepository;
import com.ticketguard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String USER_NOT_FOUND = "User not found";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse profile = userService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        List<Notification> notifications = userService.getUserNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationRead(@PathVariable Long id) {
        userService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        List<PaymentMethod> methods = userService.getSavedPaymentMethods(user.getId());
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/payment-methods")
    public ResponseEntity<PaymentMethod> savePaymentMethod(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentMethod method) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        PaymentMethod saved = userService.savePaymentMethod(user.getId(), method);
        return ResponseEntity.ok(saved);
    }
}

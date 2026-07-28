package com.ticketguard.controller;

import com.ticketguard.dto.DashboardStats;
import com.ticketguard.entity.AiFraudLog;
import com.ticketguard.entity.AuditLog;
import com.ticketguard.entity.Report;
import com.ticketguard.entity.User;
import com.ticketguard.entity.Payment;
import com.ticketguard.entity.Refund;
import com.ticketguard.entity.SeatLayout;
import com.ticketguard.exception.ResourceNotFoundException;
import com.ticketguard.repository.UserRepository;
import com.ticketguard.service.AdminService;
import com.ticketguard.service.AiFraudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.ticketguard.dto.UserResponse;
import com.ticketguard.dto.BookingResponse;
import com.ticketguard.service.UserService;
import com.ticketguard.service.BookingService;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AiFraudService aiFraudService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private com.ticketguard.config.DataSeeder dataSeeder;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fraud-alerts")
    public ResponseEntity<List<AiFraudLog>> getFraudAlerts() {
        List<AiFraudLog> alerts = aiFraudService.getRecentFraudAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        List<AuditLog> logs = adminService.getRecentAuditLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getReports() {
        List<Report> reports = adminService.getReportsList();
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/reports")
    public ResponseEntity<Report> generateReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String reportName,
            @RequestParam String reportType) {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        Report report = adminService.generateReport(reportName, reportType, admin.getId());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(userService::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setStatus(user.getStatus() == User.UserStatus.ACTIVE ? User.UserStatus.BLOCKED : User.UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        
        adminService.logAdminAction(admin.getId(), "TOGGLED USER STATUS: " + saved.getStatus(), "users", saved.getId());
        return ResponseEntity.ok(userService.mapToUserResponse(saved));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @GetMapping("/refunds")
    public ResponseEntity<List<Refund>> getAllRefunds() {
        return ResponseEntity.ok(adminService.getAllRefunds());
    }

    @GetMapping("/seats")
    public ResponseEntity<List<SeatLayout>> getAllSeats() {
        return ResponseEntity.ok(adminService.getAllSeats());
    }

    @PutMapping("/seats/{id}/status")
    public ResponseEntity<SeatLayout> toggleSeatStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        SeatLayout seat = adminService.toggleSeatStatus(id);
        adminService.logAdminAction(admin.getId(), "TOGGLED SEAT STATUS: Row " + seat.getRowName() + seat.getSeatNumber() + " -> " + seat.getStatus(), "seat_layouts", seat.getId());
        return ResponseEntity.ok(seat);
    }

    @PostMapping("/reseed")
    public ResponseEntity<String> reseedDatabase() {
        try {
            dataSeeder.run();
            return ResponseEntity.ok("Database seeded/updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error seeding database: " + e.getMessage());
        }
    }
}

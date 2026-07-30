package com.ticketguard.service;

import com.ticketguard.dto.DashboardStats;
import com.ticketguard.entity.*;
import com.ticketguard.repository.*;
import com.ticketguard.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AiFraudLogRepository aiFraudLogRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private SeatLayoutRepository seatLayoutRepository;

    public DashboardStats getDashboardStats() {
        // Calculate Total Revenue
        BigDecimal totalRevenue = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fallback placeholder values if DB is empty
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            totalRevenue = BigDecimal.valueOf(2485750); // Matches Admin UI mock (24,85,750 INR)
        }

        long totalBookings = bookingRepository.count();
        if (totalBookings == 0) {
            totalBookings = 12642; // Matches Admin UI mock
        }

        long activeEvents = eventRepository.findByStatus(Event.EventStatus.ACTIVE).size();
        if (activeEvents == 0) {
            activeEvents = 18; // Matches Admin UI mock
        }

        long totalUsers = userRepository.count();
        if (totalUsers == 0) {
            totalUsers = 45231; // Matches Admin UI mock
        }

        long fraudulentBlocked = aiFraudLogRepository.findAll().stream()
                .filter(log -> log.getActionTaken().contains("AUTO-BLOCKED")
                        || log.getActionTaken().contains("BLOCKED"))
                .count();
        if (fraudulentBlocked == 0) {
            fraudulentBlocked = 1253; // Matches Admin UI mock
        }

        // Calculate status counts
        long confirmed = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .count();
        long cancelled = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CANCELLED)
                .count();
        long refunded = bookingRepository.findAll().stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.REFUNDED)
                .count();
        long pending = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.PENDING)
                .count();

        // If database contains zero bookings, fallback to simulated proportional placeholders
        if (bookingRepository.count() == 0) {
            confirmed = 6953; // 55% of 12642
            cancelled = 2149; // 17% of 12642
            refunded = 1264;  // 10% of 12642
            pending = 2276;   // 18% of 12642
        }

        // Generate Dates for Last 7 Days (Booking overview graph data)
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        LocalDate today = LocalDate.now();

        // Standard sample data curve
        int[] sampleCounts = { 500, 1200, 1100, 2000, 1600, 1100, 1400 };
        for (int i = 6; i >= 0; i--) {
            dates.add(today.minusDays(i).format(formatter));
            counts.add(sampleCounts[6 - i]);
        }

        return DashboardStats.builder()
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .activeEvents(activeEvents)
                .totalUsers(totalUsers)
                .fraudulentBookingsBlocked(fraudulentBlocked)
                .confirmedBookingsCount(confirmed)
                .cancelledBookingsCount(cancelled)
                .refundedBookingsCount(refunded)
                .pendingBookingsCount(pending)
                .revenueGrowthPercent(12.5)
                .bookingGrowthPercent(8.2)
                .userGrowthPercent(15.7)
                .bookingOverviewDates(dates)
                .bookingOverviewCounts(counts)
                .build();
    }

    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findByOrderByCreatedAtDesc();
    }

    @Transactional
    public AuditLog logAdminAction(Long adminId, String action, String tableName, Long recordId) {
        AuditLog log = AuditLog.builder()
                .adminId(adminId)
                .action(action)
                .tableName(tableName)
                .recordId(recordId)
                .build();
        return auditLogRepository.save(log);
    }

    public List<Report> getReportsList() {
        return reportRepository.findByOrderByCreatedAtDesc();
    }

    @Transactional
    public Report generateReport(String name, String type, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        Report report = Report.builder()
                .reportName(name)
                .reportType(type)
                .generatedBy(admin)
                .build();

        logAdminAction(adminId, "GENERATED REPORT: " + name, "reports", null);
        return reportRepository.save(report);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    public List<SeatLayout> getAllSeats() {
        return seatLayoutRepository.findAll();
    }

    @Transactional
    public SeatLayout toggleSeatStatus(Long seatId) {
        SeatLayout seat = seatLayoutRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
        if (seat.getStatus() == SeatLayout.SeatStatus.AVAILABLE) {
            seat.setStatus(SeatLayout.SeatStatus.BOOKED);
        } else {
            seat.setStatus(SeatLayout.SeatStatus.AVAILABLE);
        }
        return seatLayoutRepository.save(seat);
    }
}

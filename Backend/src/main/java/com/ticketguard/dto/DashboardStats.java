package com.ticketguard.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStats {
    private BigDecimal totalRevenue;
    private long totalBookings;
    private long activeEvents;
    private long totalUsers;
    private long fraudulentBookingsBlocked;
    
    private long confirmedBookingsCount;
    private long cancelledBookingsCount;
    private long refundedBookingsCount;
    private long pendingBookingsCount;
    
    private double revenueGrowthPercent;
    private double bookingGrowthPercent;
    private double userGrowthPercent;
    
    private List<String> bookingOverviewDates;
    private List<Integer> bookingOverviewCounts;

    // Constructors
    public DashboardStats() {}

    public DashboardStats(BigDecimal totalRevenue, long totalBookings, long activeEvents, long totalUsers, long fraudulentBookingsBlocked, long confirmedBookingsCount, long cancelledBookingsCount, long refundedBookingsCount, long pendingBookingsCount, double revenueGrowthPercent, double bookingGrowthPercent, double userGrowthPercent, List<String> bookingOverviewDates, List<Integer> bookingOverviewCounts) {
        this.totalRevenue = totalRevenue;
        this.totalBookings = totalBookings;
        this.activeEvents = activeEvents;
        this.totalUsers = totalUsers;
        this.fraudulentBookingsBlocked = fraudulentBookingsBlocked;
        this.confirmedBookingsCount = confirmedBookingsCount;
        this.cancelledBookingsCount = cancelledBookingsCount;
        this.refundedBookingsCount = refundedBookingsCount;
        this.pendingBookingsCount = pendingBookingsCount;
        this.revenueGrowthPercent = revenueGrowthPercent;
        this.bookingGrowthPercent = bookingGrowthPercent;
        this.userGrowthPercent = userGrowthPercent;
        this.bookingOverviewDates = bookingOverviewDates;
        this.bookingOverviewCounts = bookingOverviewCounts;
    }

    // Getters and Setters
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public long getActiveEvents() { return activeEvents; }
    public void setActiveEvents(long activeEvents) { this.activeEvents = activeEvents; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getFraudulentBookingsBlocked() { return fraudulentBookingsBlocked; }
    public void setFraudulentBookingsBlocked(long fraudulentBookingsBlocked) { this.fraudulentBookingsBlocked = fraudulentBookingsBlocked; }

    public long getConfirmedBookingsCount() { return confirmedBookingsCount; }
    public void setConfirmedBookingsCount(long confirmedBookingsCount) { this.confirmedBookingsCount = confirmedBookingsCount; }

    public long getCancelledBookingsCount() { return cancelledBookingsCount; }
    public void setCancelledBookingsCount(long cancelledBookingsCount) { this.cancelledBookingsCount = cancelledBookingsCount; }

    public long getRefundedBookingsCount() { return refundedBookingsCount; }
    public void setRefundedBookingsCount(long refundedBookingsCount) { this.refundedBookingsCount = refundedBookingsCount; }

    public long getPendingBookingsCount() { return pendingBookingsCount; }
    public void setPendingBookingsCount(long pendingBookingsCount) { this.pendingBookingsCount = pendingBookingsCount; }

    public double getRevenueGrowthPercent() { return revenueGrowthPercent; }
    public void setRevenueGrowthPercent(double revenueGrowthPercent) { this.revenueGrowthPercent = revenueGrowthPercent; }

    public double getBookingGrowthPercent() { return bookingGrowthPercent; }
    public void setBookingGrowthPercent(double bookingGrowthPercent) { this.bookingGrowthPercent = bookingGrowthPercent; }

    public double getUserGrowthPercent() { return userGrowthPercent; }
    public void setUserGrowthPercent(double userGrowthPercent) { this.userGrowthPercent = userGrowthPercent; }

    public List<String> getBookingOverviewDates() { return bookingOverviewDates; }
    public void setBookingOverviewDates(List<String> bookingOverviewDates) { this.bookingOverviewDates = bookingOverviewDates; }

    public List<Integer> getBookingOverviewCounts() { return bookingOverviewCounts; }
    public void setBookingOverviewCounts(List<Integer> bookingOverviewCounts) { this.bookingOverviewCounts = bookingOverviewCounts; }

    // Builder
    public static DashboardStatsBuilder builder() {
        return new DashboardStatsBuilder();
    }

    public static class DashboardStatsBuilder {
        private BigDecimal totalRevenue;
        private long totalBookings;
        private long activeEvents;
        private long totalUsers;
        private long fraudulentBookingsBlocked;
        private long confirmedBookingsCount;
        private long cancelledBookingsCount;
        private long refundedBookingsCount;
        private long pendingBookingsCount;
        private double revenueGrowthPercent;
        private double bookingGrowthPercent;
        private double userGrowthPercent;
        private List<String> bookingOverviewDates;
        private List<Integer> bookingOverviewCounts;

        public DashboardStatsBuilder totalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; return this; }
        public DashboardStatsBuilder totalBookings(long totalBookings) { this.totalBookings = totalBookings; return this; }
        public DashboardStatsBuilder activeEvents(long activeEvents) { this.activeEvents = activeEvents; return this; }
        public DashboardStatsBuilder totalUsers(long totalUsers) { this.totalUsers = totalUsers; return this; }
        public DashboardStatsBuilder fraudulentBookingsBlocked(long fraudulentBookingsBlocked) { this.fraudulentBookingsBlocked = fraudulentBookingsBlocked; return this; }
        public DashboardStatsBuilder confirmedBookingsCount(long confirmedBookingsCount) { this.confirmedBookingsCount = confirmedBookingsCount; return this; }
        public DashboardStatsBuilder cancelledBookingsCount(long cancelledBookingsCount) { this.cancelledBookingsCount = cancelledBookingsCount; return this; }
        public DashboardStatsBuilder refundedBookingsCount(long refundedBookingsCount) { this.refundedBookingsCount = refundedBookingsCount; return this; }
        public DashboardStatsBuilder pendingBookingsCount(long pendingBookingsCount) { this.pendingBookingsCount = pendingBookingsCount; return this; }
        public DashboardStatsBuilder revenueGrowthPercent(double revenueGrowthPercent) { this.revenueGrowthPercent = revenueGrowthPercent; return this; }
        public DashboardStatsBuilder bookingGrowthPercent(double bookingGrowthPercent) { this.bookingGrowthPercent = bookingGrowthPercent; return this; }
        public DashboardStatsBuilder userGrowthPercent(double userGrowthPercent) { this.userGrowthPercent = userGrowthPercent; return this; }
        public DashboardStatsBuilder bookingOverviewDates(List<String> bookingOverviewDates) { this.bookingOverviewDates = bookingOverviewDates; return this; }
        public DashboardStatsBuilder bookingOverviewCounts(List<Integer> bookingOverviewCounts) { this.bookingOverviewCounts = bookingOverviewCounts; return this; }

        public DashboardStats build() {
            return new DashboardStats(totalRevenue, totalBookings, activeEvents, totalUsers, fraudulentBookingsBlocked, confirmedBookingsCount, cancelledBookingsCount, refundedBookingsCount, pendingBookingsCount, revenueGrowthPercent, bookingGrowthPercent, userGrowthPercent, bookingOverviewDates, bookingOverviewCounts);
        }
    }
}

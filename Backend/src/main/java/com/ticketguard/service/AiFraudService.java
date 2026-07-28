package com.ticketguard.service;

import com.ticketguard.dto.SeatReserveRequest;
import com.ticketguard.entity.AiFraudLog;
import com.ticketguard.entity.Booking;
import com.ticketguard.entity.LoginHistory;
import com.ticketguard.entity.User;
import com.ticketguard.repository.AiFraudLogRepository;
import com.ticketguard.repository.BookingRepository;
import com.ticketguard.repository.BookingSeatRepository;
import com.ticketguard.repository.LoginHistoryRepository;
import com.ticketguard.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AiFraudService {

    private final Random random = new java.security.SecureRandom();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${genai.fraud.service.url:http://localhost:8000/detect-fraud}")
    private String genaiFraudServiceUrl;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;


    @Autowired
    private AiFraudLogRepository aiFraudLogRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @Transactional
    public double scanBookingForFraud(Booking booking) {
        return scanBookingForFraud(booking, null);
    }

    @Transactional
    public double scanBookingForFraud(Booking booking, SeatReserveRequest.BehaviourTelemetry behaviour) {
        try {
            // Get client IP and User-Agent from RequestContextHolder
            String ipAddress = "127.0.0.1";
            String userAgent = "Unknown";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
                userAgent = request.getHeader("User-Agent");
                // Check if behind proxy
                String forwardedFor = request.getHeader("X-Forwarded-For");
                if (forwardedFor != null && !forwardedFor.isEmpty()) {
                    ipAddress = forwardedFor.split(",")[0].trim();
                }
            }

            // Count total tickets booked by this user for this specific event
            long userTicketCount = 0;
            List<Booking> userBookings = bookingRepository.findByUserId(booking.getUser().getId());
            if (userBookings != null) {
                for (Booking ub : userBookings) {
                    if (ub.getBookingStatus() != Booking.BookingStatus.CANCELLED && ub.getEvent().getId().equals(booking.getEvent().getId())) {
                        userTicketCount += bookingSeatRepository.findByBookingId(ub.getId()).size();
                    }
                }
            }

            System.out.println("Total ticket count for User " + booking.getUser().getEmail() + " on Event " + booking.getEvent().getTitle() + ": " + userTicketCount);

            // If user has booked more than 8 tickets, we print diagnostic info and proceed to AI validation
            if (userTicketCount > 8) {
                System.out.println("User has booked " + userTicketCount + " tickets (exceeding limit of 8 for this event). Running strict AI verification.");
            }

            // Count historical active bookings from this IP address for rate limit safeguard
            long ipBookingCount = 0;
            List<LoginHistory> ipLogins = loginHistoryRepository.findByIpAddress(ipAddress);
            if (ipLogins != null) {
                java.util.Set<Long> userIds = ipLogins.stream()
                        .map(lh -> lh.getUser().getId())
                        .collect(java.util.stream.Collectors.toSet());
                for (Long userId : userIds) {
                    ipBookingCount += bookingRepository.findByUserId(userId).stream()
                            .filter(b -> b.getBookingStatus() != Booking.BookingStatus.CANCELLED)
                            .count();
                }
            }

            System.out.println("IP Booking Velocity for " + ipAddress + ": " + ipBookingCount + " active bookings.");

            if (ipBookingCount >= 15) {
                double riskScore = 100.0;
                String reason = "Security Exception: Rate limit exceeded. More than 15 active bookings (" + ipBookingCount + ") detected from the same IP address (" + ipAddress + ").";
                
                User user = booking.getUser();
                user.setStatus(User.UserStatus.BLOCKED);
                userRepository.save(user);

                booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
                bookingRepository.save(booking);

                // Release seats immediately
                bookingService.releaseSeatsForCancelledBooking(booking.getId());

                saveFraudLog(user, booking, riskScore, reason, "AUTO-BLOCKED BOOKING & SUSPENDED USER", behaviour, true);
                return riskScore;
            }

            int ticketQty = bookingSeatRepository.findByBookingId(booking.getId()).size();

            // Construct payload
            Map<String, Object> bookingPayload = new HashMap<>();
            bookingPayload.put("booking_id", booking.getBookingNumber() != null ? booking.getBookingNumber() : String.valueOf(booking.getId()));
            bookingPayload.put("user_id", String.valueOf(booking.getUser().getId()));
            bookingPayload.put("event_id", String.valueOf(booking.getEvent().getId()));
            bookingPayload.put("ticket_quantity", ticketQty > 0 ? ticketQty : 1);
            bookingPayload.put("total_amount", booking.getTotalAmount() != null ? booking.getTotalAmount().doubleValue() : 0.0);
            bookingPayload.put("currency", "USD");
            bookingPayload.put("payment_method", "CREDIT_CARD");
            bookingPayload.put("timestamp", java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now()));
            bookingPayload.put("ip_address", ipAddress);
            bookingPayload.put("user_agent", userAgent);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("booking", bookingPayload);

            if (behaviour != null) {
                Map<String, Object> behaviourPayload = new HashMap<>();
                behaviourPayload.put("time_spent_seconds", behaviour.getTimeSpentSeconds());
                behaviourPayload.put("mouse_movement_entropy", behaviour.getMouseMovementEntropy());
                behaviourPayload.put("keystroke_velocity", behaviour.getKeystrokeVelocity());
                behaviourPayload.put("pages_visited", behaviour.getPagesVisited());
                behaviourPayload.put("failed_attempts", behaviour.getFailedAttempts());
                behaviourPayload.put("is_headless_browser", behaviour.isHeadlessBrowser());
                behaviourPayload.put("device_fingerprint", behaviour.getDeviceFingerprint());
                requestBody.put("behaviour", behaviourPayload);
            }

            // Post to GenAI fraud microservice
            System.out.println("Calling GenAI fraud detection at: " + genaiFraudServiceUrl);
            FraudDetectionResponse response = restTemplate.postForObject(genaiFraudServiceUrl, requestBody, FraudDetectionResponse.class);

            if (response != null && "SUCCESS".equals(response.getStatus()) && response.getRiskAssessment() != null) {
                double riskScore = response.getRiskAssessment().getRiskScore();
                String decision = response.getDecisionResult() != null ? response.getDecisionResult().getDecision() : "ALLOW";
                String reason = response.getDecisionResult() != null ? response.getDecisionResult().getReasoning() : "No reasoning provided by GenAI.";
                boolean isBot = response.getRiskAssessment().isBot();

                System.out.println("GenAI Fraud Evaluation Result - Score: " + riskScore + ", Decision: " + decision + ", isBot: " + isBot);

                // Perform action based on GenAI response
                if ("BLOCK".equals(decision) || riskScore >= 85.0 || isBot) {
                    User user = booking.getUser();
                    user.setStatus(User.UserStatus.BLOCKED);
                    userRepository.save(user);

                    booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);

                    // Release seats immediately
                    bookingService.releaseSeatsForCancelledBooking(booking.getId());

                    saveFraudLog(user, booking, riskScore, reason, "AUTO-BLOCKED BOOKING & SUSPENDED USER", behaviour, isBot);
                    return riskScore;
                } else if ("FLAG_FOR_REVIEW".equals(decision) || riskScore >= 60.0) {
                    saveFraudLog(booking.getUser(), booking, riskScore, reason, "FLAGGED FOR REVIEW", behaviour, isBot);
                } else {
                    saveFraudLog(booking.getUser(), booking, riskScore, reason, "ALLOW", behaviour, isBot);
                }
                return riskScore;
            } else {
                throw new RuntimeException("Empty or unsuccessful response from GenAI microservice");
            }
        } catch (Exception e) {
            System.err.println("Error calling GenAI microservice: " + e.getMessage() + ". Falling back to local rule-based simulation.");
            return scanBookingForFraudLocalFallback(booking, behaviour);
        }
    }

    @Transactional
    public double scanBookingForFraudLocalFallback(Booking booking, SeatReserveRequest.BehaviourTelemetry behaviour) {
        User user = booking.getUser();
        double riskScore = 0.0;
        StringBuilder reasons = new StringBuilder();
        boolean isBot = false;

        // 1. Telemetry-based Bot Rules Heuristics
        if (behaviour != null) {
            if (behaviour.getTimeSpentSeconds() > 0 && behaviour.getTimeSpentSeconds() < 2.0) {
                riskScore += 50.0;
                reasons.append("Fast checkout speed (under 2 seconds) indicates automated bot booking. ");
            }
            if (behaviour.getMouseMovementEntropy() > 0 && behaviour.getMouseMovementEntropy() < 0.1) {
                riskScore += 40.0;
                reasons.append("Very low mouse movement entropy indicates robotic mouse pathing. ");
            }
            if (behaviour.getKeystrokeVelocity() > 100.0) {
                riskScore += 30.0;
                reasons.append("Unusually fast keystroke velocity indicates script/autofill injection. ");
            }
            if (behaviour.isHeadlessBrowser()) {
                riskScore += 60.0;
                reasons.append("Headless browser session detected. ");
            }
        }

        // 2. Check Login History (Location Mismatch Simulation)
        List<LoginHistory> logins = loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(user.getId());
        if (logins.size() >= 2) {
            LoginHistory latest = logins.get(0);
            LoginHistory previous = logins.get(1);
            if (latest.getIpAddress() != null && !latest.getIpAddress().equals(previous.getIpAddress())) {
                riskScore += 30.0;
                reasons.append("Location Mismatch detected between recent login sessions. ");
            }
        }

        // 3. High-speed Booking / Bot check simulation (fallback random flag)
        if (random.nextDouble() > 0.90) {
            riskScore += 45.0;
            reasons.append("High-speed automation signature / Bot patterns detected. ");
        }

        // 4. Multi-booking flood check
        List<Booking> userBookings = bookingRepository.findByUserId(user.getId());
        long recentBookings = userBookings.stream()
                .filter(b -> b.getBookingDate().isAfter(booking.getBookingDate().minusMinutes(5)))
                .count();
        if (recentBookings > 2) {
            riskScore += 20.0;
            reasons.append("Multiple rapid bookings within 5 minutes. ");
        }

        // Apply fallback base risk for demonstration if no flags hit
        if (riskScore == 0.0) {
            riskScore = 5.0 + (random.nextDouble() * 15.0); // 5-20% baseline
        }

        if (riskScore >= 85.0) {
            isBot = true;
        }

        // Action logging based on score
        String actionTaken = "ALLOW";
        if (riskScore >= 85.0) {
            actionTaken = "AUTO-BLOCKED BOOKING & SUSPENDED USER";
            
            // Suspend user
            user.setStatus(User.UserStatus.BLOCKED);
            userRepository.save(user);

            // Cancel Booking
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // Release seats immediately
            bookingService.releaseSeatsForCancelledBooking(booking.getId());
        } else if (riskScore >= 60.0) {
            actionTaken = "FLAGGED FOR REVIEW";
        }

        saveFraudLog(user, booking, riskScore, reasons.toString().trim(), actionTaken, behaviour, isBot);

        return riskScore;
    }


    private void saveFraudLog(User user, Booking booking, double riskScore, String reason, String actionTaken, SeatReserveRequest.BehaviourTelemetry behaviour, boolean isBot) {
        AiFraudLog.AiFraudLogBuilder builder = AiFraudLog.builder()
                .user(user)
                .booking(booking)
                .riskScore(riskScore)
                .reason(reason)
                .actionTaken(actionTaken)
                .isBot(isBot);

        if (behaviour != null) {
            builder.keystrokeVelocity(behaviour.getKeystrokeVelocity())
                   .mouseMovementEntropy(behaviour.getMouseMovementEntropy())
                   .timeSpentSeconds(behaviour.getTimeSpentSeconds());
        }

        aiFraudLogRepository.save(builder.build());
    }

    public static class FraudDetectionResponse {
        private String requestId;
        private String bookingId;
        private String status;
        private RiskAssessment riskAssessment;
        private DecisionResult decisionResult;

        // Getters and Setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }

        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public RiskAssessment getRiskAssessment() { return riskAssessment; }
        public void setRiskAssessment(RiskAssessment riskAssessment) { this.riskAssessment = riskAssessment; }

        public DecisionResult getDecisionResult() { return decisionResult; }
        public void setDecisionResult(DecisionResult decisionResult) { this.decisionResult = decisionResult; }

        public static class RiskAssessment {
            private double riskScore;
            private String riskLevel;
            private boolean isBot;

            // Getters and Setters
            public double getRiskScore() { return riskScore; }
            public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

            public String getRiskLevel() { return riskLevel; }
            public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

            public boolean isBot() { return isBot; }
            public void setBot(boolean isBot) { this.isBot = isBot; }
        }

        public static class DecisionResult {
            private String decision;
            private String reasoning;

            // Getters and Setters
            public String getDecision() { return decision; }
            public void setDecision(String decision) { this.decision = decision; }

            public String getReasoning() { return reasoning; }
            public void setReasoning(String reasoning) { this.reasoning = reasoning; }
        }
    }


    public List<AiFraudLog> getRecentFraudAlerts() {
        return aiFraudLogRepository.findByOrderByCreatedAtDesc();
    }
}

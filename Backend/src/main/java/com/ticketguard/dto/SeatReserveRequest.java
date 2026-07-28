package com.ticketguard.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SeatReserveRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<Long> seatIds;

    private BehaviourTelemetry behaviour;

    // Default Constructor
    public SeatReserveRequest() {}

    // Getters and Setters
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }

    public BehaviourTelemetry getBehaviour() { return behaviour; }
    public void setBehaviour(BehaviourTelemetry behaviour) { this.behaviour = behaviour; }

    public static class BehaviourTelemetry {
        private double timeSpentSeconds;
        private double mouseMovementEntropy;
        private double keystrokeVelocity;
        private int pagesVisited;
        private int failedAttempts;
        private boolean isHeadlessBrowser;
        private String deviceFingerprint;

        // Default Constructor
        public BehaviourTelemetry() {}

        // Getters and Setters
        public double getTimeSpentSeconds() { return timeSpentSeconds; }
        public void setTimeSpentSeconds(double timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

        public double getMouseMovementEntropy() { return mouseMovementEntropy; }
        public void setMouseMovementEntropy(double mouseMovementEntropy) { this.mouseMovementEntropy = mouseMovementEntropy; }

        public double getKeystrokeVelocity() { return keystrokeVelocity; }
        public void setKeystrokeVelocity(double keystrokeVelocity) { this.keystrokeVelocity = keystrokeVelocity; }

        public int getPagesVisited() { return pagesVisited; }
        public void setPagesVisited(int pagesVisited) { this.pagesVisited = pagesVisited; }

        public int getFailedAttempts() { return failedAttempts; }
        public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

        public boolean isHeadlessBrowser() { return isHeadlessBrowser; }
        public void setHeadlessBrowser(boolean headlessBrowser) { isHeadlessBrowser = headlessBrowser; }

        public String getDeviceFingerprint() { return deviceFingerprint; }
        public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    }
}

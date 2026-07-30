package com.ticketguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    private String device;

    private String browser;

    @Column(name = "login_time", updatable = false)
    private LocalDateTime loginTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginStatus status;

    // Constructors
    public LoginHistory() {}

    public LoginHistory(Long id, User user, String ipAddress, String device, String browser, LocalDateTime loginTime, LoginStatus status) {
        this.id = id;
        this.user = user;
        this.ipAddress = ipAddress;
        this.device = device;
        this.browser = browser;
        this.loginTime = loginTime;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LoginStatus getStatus() { return status; }
    public void setStatus(LoginStatus status) { this.status = status; }

    @PrePersist
    protected void onCreate() {
        loginTime = LocalDateTime.now();
    }

    public enum LoginStatus {
        SUCCESS, FAILED
    }

    // Builder
    public static LoginHistoryBuilder builder() {
        return new LoginHistoryBuilder();
    }

    public static class LoginHistoryBuilder {
        private Long id;
        private User user;
        private String ipAddress;
        private String device;
        private String browser;
        private LocalDateTime loginTime;
        private LoginStatus status;

        public LoginHistoryBuilder id(Long id) { this.id = id; return this; }
        public LoginHistoryBuilder user(User user) { this.user = user; return this; }
        public LoginHistoryBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public LoginHistoryBuilder device(String device) { this.device = device; return this; }
        public LoginHistoryBuilder browser(String browser) { this.browser = browser; return this; }
        public LoginHistoryBuilder loginTime(LocalDateTime loginTime) { this.loginTime = loginTime; return this; }
        public LoginHistoryBuilder status(LoginStatus status) { this.status = status; return this; }

        public LoginHistory build() {
            return new LoginHistory(id, user, ipAddress, device, browser, loginTime, status);
        }
    }
}

package com.ticketguard.controller;

import com.ticketguard.dto.AuthResponse;
import com.ticketguard.dto.LoginRequest;
import com.ticketguard.dto.RegisterRequest;
import com.ticketguard.dto.UserResponse;
import com.ticketguard.service.AuthService;
import com.ticketguard.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        
        // Simple parsing of User-Agent for device/browser
        String device = "Unknown Device";
        String browser = "Unknown Browser";
        if (userAgent != null) {
            if (userAgent.contains("Mobi")) {
                device = "Mobile Device";
            } else {
                device = "Desktop PC";
            }
            if (userAgent.contains("Chrome")) {
                browser = "Chrome";
            } else if (userAgent.contains("Firefox")) {
                browser = "Firefox";
            } else if (userAgent.contains("Safari")) {
                browser = "Safari";
            }
        }

        AuthResponse response = authService.login(request, ipAddress, device, browser);
        return ResponseEntity.ok(response);
    }
}

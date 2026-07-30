package com.ticketguard.service;

import com.ticketguard.dto.AuthResponse;
import com.ticketguard.dto.LoginRequest;
import com.ticketguard.entity.LoginHistory;
import com.ticketguard.entity.User;
import com.ticketguard.exception.BadRequestException;
import com.ticketguard.repository.LoginHistoryRepository;
import com.ticketguard.repository.UserRepository;
import com.ticketguard.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String device, String browser) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getStatus() == User.UserStatus.BLOCKED) {
            throw new BadRequestException("Your account is blocked. Please contact support.");
        }

        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        // Track Login History
        LoginHistory history = LoginHistory.builder()
                .user(user)
                .ipAddress(ipAddress)
                .device(device)
                .browser(browser)
                .status(passwordMatch ? LoginHistory.LoginStatus.SUCCESS : LoginHistory.LoginStatus.FAILED)
                .build();
        loginHistoryRepository.save(history);

        if (!passwordMatch) {
            throw new BadRequestException("Invalid email or password");
        }

        // Generate Token
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}

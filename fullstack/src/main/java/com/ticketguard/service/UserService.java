package com.ticketguard.service;

import com.ticketguard.dto.RegisterRequest;
import com.ticketguard.dto.UserResponse;
import com.ticketguard.entity.Notification;
import com.ticketguard.entity.PaymentMethod;
import com.ticketguard.entity.User;
import com.ticketguard.exception.BadRequestException;
import com.ticketguard.exception.ResourceNotFoundException;
import com.ticketguard.repository.NotificationRepository;
import com.ticketguard.repository.PaymentMethodRepository;
import com.ticketguard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User.UserRole userRole = User.UserRole.USER;
        if (request.getRole() != null) {
            try {
                userRole = User.UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                // fallback to default USER
            }
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .status(User.UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        // Send a welcome notification
        Notification welcomeNotification = Notification.builder()
                .user(savedUser)
                .title("Welcome to TicketGuard!")
                .message("Hello " + savedUser.getFirstName()
                        + ", your account has been successfully registered. Explore upcoming events now!")
                .notificationType("WELCOME")
                .isRead(false)
                .build();
        notificationRepository.save(welcomeNotification);

        return mapToUserResponse(savedUser);
    }

    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public List<PaymentMethod> getSavedPaymentMethods(Long userId) {
        return paymentMethodRepository.findByUserId(userId);
    }

    @Transactional
    public PaymentMethod savePaymentMethod(Long userId, PaymentMethod paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        paymentMethod.setUser(user);
        return paymentMethodRepository.save(paymentMethod);
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .profileImage(user.getProfileImage())
                .build();
    }
}

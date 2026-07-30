package com.ticketguard.repository;

import com.ticketguard.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    Optional<Booking> findByBookingNumber(String bookingNumber);
    long countByBookingStatus(Booking.BookingStatus bookingStatus);
}

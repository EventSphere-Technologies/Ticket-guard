package com.ticketguard.repository;

import com.ticketguard.entity.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
    List<SeatReservation> findByStatusAndReservationExpiryBefore(SeatReservation.ReservationStatus status, LocalDateTime expiry);
    boolean existsByEventIdAndSeatIdAndStatus(Long eventId, Long seatId, SeatReservation.ReservationStatus status);
    Optional<SeatReservation> findByUserIdAndEventIdAndSeatIdAndStatus(Long userId, Long eventId, Long seatId, SeatReservation.ReservationStatus status);
    List<SeatReservation> findByEventIdAndStatus(Long eventId, SeatReservation.ReservationStatus status);
}

package com.ticketguard.repository;

import com.ticketguard.entity.SeatLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatLayoutRepository extends JpaRepository<SeatLayout, Long> {
    List<SeatLayout> findByVenueId(Long venueId);
}

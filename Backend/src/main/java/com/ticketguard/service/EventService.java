package com.ticketguard.service;

import com.ticketguard.entity.Event;
import com.ticketguard.entity.SeatLayout;
import com.ticketguard.entity.SeatReservation;
import com.ticketguard.entity.Venue;
import com.ticketguard.exception.ResourceNotFoundException;
import com.ticketguard.repository.EventRepository;
import com.ticketguard.repository.SeatLayoutRepository;
import com.ticketguard.repository.SeatReservationRepository;
import com.ticketguard.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private SeatLayoutRepository seatLayoutRepository;

    @Autowired
    private SeatReservationRepository seatReservationRepository;

    public List<Event> getActiveEvents() {
        return eventRepository.findByStatus(Event.EventStatus.ACTIVE);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));
    }

    @Transactional
    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    @Transactional
    public Event createEvent(Event event) {
        if (event.getVenue() != null && event.getVenue().getId() != null) {
            Venue venue = venueRepository.findById(event.getVenue().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
            event.setVenue(venue);
        }
        Event savedEvent = eventRepository.save(event);
        
        // Generate Default Seat Layout if none exists for the venue
        generateDefaultSeatsIfNone(savedEvent.getVenue());
        
        return savedEvent;
    }

    @Transactional
    public void generateDefaultSeatsIfNone(Venue venue) {
        List<SeatLayout> existing = seatLayoutRepository.findByVenueId(venue.getId());
        if (!existing.isEmpty()) {
            return;
        }

        List<SeatLayout> seats = new ArrayList<>();
        // Generate Rows A to E (Regular, VIP, Balcony)
        String[] rows = {"A", "B", "C", "D", "E"};
        for (String row : rows) {
            SeatLayout.SeatType type = SeatLayout.SeatType.REGULAR;
            BigDecimal priceModifier = BigDecimal.valueOf(1.0);
            
            if (row.equals("A")) {
                type = SeatLayout.SeatType.VIP;
                priceModifier = BigDecimal.valueOf(2.5); // VIP seats cost more
            } else if (row.equals("B")) {
                type = SeatLayout.SeatType.VIP;
                priceModifier = BigDecimal.valueOf(1.8);
            } else if (row.equals("E")) {
                type = SeatLayout.SeatType.BALCONY;
                priceModifier = BigDecimal.valueOf(0.8);
            }

            for (int seatNum = 1; seatNum <= 15; seatNum++) {
                seats.add(SeatLayout.builder()
                        .venue(venue)
                        .rowName(row)
                        .seatNumber(seatNum)
                        .seatType(type)
                        .price(BigDecimal.valueOf(1000).multiply(priceModifier))
                        .status(SeatLayout.SeatStatus.AVAILABLE)
                        .build());
            }
        }
        seatLayoutRepository.saveAll(seats);
    }

    public List<SeatLayout> getSeatsWithStatus(Long eventId) {
        Event event = getEventById(eventId);
        List<SeatLayout> venueSeats = seatLayoutRepository.findByVenueId(event.getVenue().getId());
        
        // Fetch active seat reservations for this event
        List<SeatReservation> activeReservations = seatReservationRepository.findByEventIdAndStatus(eventId, SeatReservation.ReservationStatus.LOCKED);
        
        // Match reservations and mark seat statuses dynamically
        LocalDateTime now = LocalDateTime.now();
        for (SeatLayout seat : venueSeats) {
            // Check if locked
            boolean isLocked = activeReservations.stream()
                    .anyMatch(res -> res.getSeat().getId().equals(seat.getId()) && res.getReservationExpiry().isAfter(now));
            
            if (isLocked) {
                seat.setStatus(SeatLayout.SeatStatus.LOCKED);
            }
            // Real booked statuses would override this based on completed bookings
        }
        
        return venueSeats;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional
    public Event toggleEventStatus(Long eventId) {
        Event event = getEventById(eventId);
        if (event.getStatus() == Event.EventStatus.ACTIVE) {
            event.setStatus(Event.EventStatus.INACTIVE);
        } else {
            event.setStatus(Event.EventStatus.ACTIVE);
        }
        return eventRepository.save(event);
    }
}

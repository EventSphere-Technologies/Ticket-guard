package com.ticketguard.controller;

import com.ticketguard.entity.Event;
import com.ticketguard.entity.SeatLayout;
import com.ticketguard.entity.Venue;
import com.ticketguard.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ticketguard.entity.User;
import com.ticketguard.repository.UserRepository;
import com.ticketguard.exception.ResourceNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Event>> listEvents() {
        List<Event> events = eventService.getActiveEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatLayout>> getEventSeats(@PathVariable Long id) {
        List<SeatLayout> seats = eventService.getSeatsWithStatus(id);
        return ResponseEntity.ok(seats);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> createEvent(
            @RequestBody Event event,
            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        event.setCreatedBy(admin);
        Event created = eventService.createEvent(event);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/venues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Venue> createVenue(@RequestBody Venue venue) {
        Venue created = eventService.createVenue(venue);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Event>> getAllEventsAdmin() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> toggleEventStatus(@PathVariable Long id) {
        Event updated = eventService.toggleEventStatus(id);
        return ResponseEntity.ok(updated);
    }
}

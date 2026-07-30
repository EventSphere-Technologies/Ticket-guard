package com.ticketguard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import com.ticketguard.entity.Event;
import com.ticketguard.entity.Venue;
import java.time.LocalTime;
import java.time.LocalDate;
import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;

@SpringBootTest
class TicketGuardApplicationTests {

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private com.ticketguard.repository.EventRepository eventRepository;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(eventRepository);
    }

    @Test
    void updateEventBanners() {
        System.out.println("RUNNING MANUAL BANNER UPDATE TEST...");
        
        updateBanner("Lollapalooza India 2026", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Sunburn Festival Goa", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Comic Con India", "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Zakir Khan Live - Tathastu", "https://images.unsplash.com/photo-1516280440614-37939bbacd6a?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Wimbledon Screenings", "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Grand Theatre - Hamlet", "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Taylor Swift The Eras Tour", "https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Ed Sheeran Live in Bengaluru", "https://images.unsplash.com/photo-1484755560695-a4c7302c3f29?q=80&w=1170&auto=format&fit=crop");
        updateBanner("IPL Final 2026", "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=1170&auto=format&fit=crop");
        updateBanner("Broadway - The Lion King", "https://images.unsplash.com/photo-1503095391755-111c18379075?q=80&w=1170&auto=format&fit=crop");
        
        // delete any duplicates with anirudh in title if found
        eventRepository.findAll().stream()
            .filter(e -> e.getTitle().toLowerCase().contains("anirudh"))
            .forEach(e -> {
                System.out.println("DELETING DUPLICATE ANIRUDH EVENT: " + e.getTitle());
                eventRepository.delete(e);
            });
            
        System.out.println("MANUAL BANNER UPDATE COMPLETE.");
        Assertions.assertNotNull(eventRepository.findAll());
    }

    private void updateBanner(String title, String url) {
        eventRepository.findAll().stream()
            .filter(e -> e.getTitle().equalsIgnoreCase(title))
            .findFirst()
            .ifPresent(e -> {
                System.out.println("UPDATING BANNER FOR EVENT: " + title + " -> " + url);
                e.setBannerImage(url);
                eventRepository.save(e);
            });
    }

    @Test
    void testSerialization() throws Exception {
        Venue venue = Venue.builder().id(1L).venueName("Test Venue").build();
        Event event = Event.builder()
            .title("Test Event")
            .eventDate(LocalDate.now())
            .eventTime(LocalTime.of(19, 0))
            .ticketPrice(BigDecimal.TEN)
            .status(Event.EventStatus.ACTIVE)
            .venue(venue)
            .build();
        String json = objectMapper.writeValueAsString(event);
        System.out.println("SERIALIZED JSON: " + json);
        
        Event deserialized = objectMapper.readValue(json, Event.class);
        System.out.println("DESERIALIZED TIME: " + deserialized.getEventTime());
        Assertions.assertEquals(event.getTitle(), deserialized.getTitle());
    }

}

package com.ticketguard.config;

import com.ticketguard.entity.*;
import com.ticketguard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatLayoutRepository seatLayoutRepository;

    @Autowired
    private AiFraudLogRepository aiFraudLogRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.default.password:123456}")
    private String defaultPassword;

    private static final String CATEGORY_CONCERTS = "Concerts";
    private static final String CATEGORY_SPORTS = "Sports";
    private static final String COUNTRY_INDIA = "India";

    @Override
    public void run(String... args) throws Exception {
        User customer = null;
        User admin = null;

        if (userRepository.count() == 0) {
            // 1. Seed Users
            customer = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@email.com")
                    .phone("9876543210")
                    .password(passwordEncoder.encode(defaultPassword))
                    .role(User.UserRole.USER)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            userRepository.save(customer);

            admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@ticketguard.com")
                    .phone("9999999999")
                    .password(passwordEncoder.encode(defaultPassword))
                    .role(User.UserRole.ADMIN)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
        } else {
            customer = userRepository.findByEmail("john.doe@email.com").orElse(null);
            if (customer == null) {
                customer = userRepository.findAll().stream()
                        .filter(u -> u.getRole() == User.UserRole.USER)
                        .findFirst()
                        .orElse(null);
            }
            admin = userRepository.findByEmail("admin@ticketguard.com").orElse(null);
            if (admin == null) {
                admin = userRepository.findAll().stream()
                        .filter(u -> u.getRole() == User.UserRole.ADMIN)
                        .findFirst()
                        .orElse(null);
            }
        }

        // Clean up Anirudh concerts
        List<Event> toDelete = eventRepository.findAll().stream()
                .filter(e -> e.getTitle().toLowerCase().contains("anirudh"))
                .toList();
        if (!toDelete.isEmpty()) {
            eventRepository.deleteAll(toDelete);
        }

        // 2. Seed Venues
        if (venueRepository.count() == 0) {
            Venue venue1 = Venue.builder().venueName("DY Patil Stadium").city("Mumbai").state("Maharashtra")
                    .country(COUNTRY_INDIA).address("Sector 7, Nerul").capacity(55000).build();
            Venue venue2 = Venue.builder().venueName("M. Chinnaswamy Stadium").city("Bengaluru").state("Karnataka")
                    .country(COUNTRY_INDIA).address("MG Road").capacity(40000).build();
            Venue venue3 = Venue.builder().venueName("MA Chidambaram Stadium").city("Chennai").state("Tamil Nadu")
                    .country(COUNTRY_INDIA).address("Chepauk").capacity(38000).build();
            Venue venue4 = Venue.builder().venueName("Indira Gandhi Arena").city("Delhi").state("Delhi")
                    .country(COUNTRY_INDIA).address("IP Estate").capacity(14300).build();

            venueRepository.save(venue1);
            venueRepository.save(venue2);
            venueRepository.save(venue3);
            venueRepository.save(venue4);

            generateSeatsForVenue(venue1);
            generateSeatsForVenue(venue2);
            generateSeatsForVenue(venue3);
            generateSeatsForVenue(venue4);
        }

        Venue venue1 = venueRepository.findAll().stream().filter(v -> v.getVenueName().equals("DY Patil Stadium"))
                .findFirst().orElse(null);
        Venue venue2 = venueRepository.findAll().stream().filter(v -> v.getVenueName().equals("M. Chinnaswamy Stadium"))
                .findFirst().orElse(null);
        Venue venue3 = venueRepository.findAll().stream().filter(v -> v.getVenueName().equals("MA Chidambaram Stadium"))
                .findFirst().orElse(null);
        Venue venue4 = venueRepository.findAll().stream().filter(v -> v.getVenueName().equals("Indira Gandhi Arena"))
                .findFirst().orElse(null);

        // 3. Seed Default Events
        if (eventRepository.count() == 0) {
            Event event1 = Event.builder()
                    .title("Arijit Singh Live Concert")
                    .description(
                            "Experience the soulful voice of Arijit Singh live in Mumbai. An evening filled with romantic blockbusters and high-energy hits.")
                    .category(CATEGORY_CONCERTS)
                    .artistName("Arijit Singh")
                    .venue(venue1)
                    .eventDate(LocalDate.of(2026, 5, 25))
                    .eventTime(LocalTime.of(19, 0))
                    .bannerImage(
                            "https://images.unsplash.com/photo-1506157786151-b8491531f063?q=80&w=1170&auto=format&fit=crop")
                    .ticketPrice(BigDecimal.valueOf(1499))
                    .status(Event.EventStatus.ACTIVE)
                    .createdBy(admin)
                    .build();

            Event event2 = Event.builder()
                    .title("Coldplay Music Of The Spheres")
                    .description(
                            "The legendary band Coldplay is bringing their global tour to Bengaluru. Prepare for a lifetime visual show and singalong hits.")
                    .category(CATEGORY_CONCERTS)
                    .artistName("Coldplay")
                    .venue(venue2)
                    .eventDate(LocalDate.of(2026, 6, 2))
                    .eventTime(LocalTime.of(19, 0))
                    .bannerImage(
                            "https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?q=80&w=1074&auto=format&fit=crop")
                    .ticketPrice(BigDecimal.valueOf(2499))
                    .status(Event.EventStatus.ACTIVE)
                    .createdBy(admin)
                    .build();

            Event event3 = Event.builder()
                    .title("IPL 2025 - CSK vs MI")
                    .description(
                            "The ultimate rivalry of Indian cricket! Chennai Super Kings face off against Mumbai Indians at Chepauk in a high-octane battle.")
                    .category(CATEGORY_SPORTS)
                    .artistName("IPL Teams")
                    .venue(venue3)
                    .eventDate(LocalDate.of(2026, 6, 15))
                    .eventTime(LocalTime.of(20, 0))
                    .bannerImage(
                            "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=1170&auto=format&fit=crop")
                    .ticketPrice(BigDecimal.valueOf(999))
                    .status(Event.EventStatus.ACTIVE)
                    .createdBy(admin)
                    .build();

            Event event4 = Event.builder()
                    .title("Diljit Dosanjh Live")
                    .description(
                            "Punjabi rockstar Diljit Dosanjh is set to light up Delhi with his famous Dil-Luminati global music tour.")
                    .category(CATEGORY_CONCERTS)
                    .artistName("Diljit Dosanjh")
                    .venue(venue4)
                    .eventDate(LocalDate.of(2026, 6, 21))
                    .eventTime(LocalTime.of(19, 0))
                    .bannerImage(
                            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=1074&auto=format&fit=crop")
                    .ticketPrice(BigDecimal.valueOf(1799))
                    .status(Event.EventStatus.ACTIVE)
                    .createdBy(admin)
                    .build();

            eventRepository.save(event1);
            eventRepository.save(event2);
            eventRepository.save(event3);
            eventRepository.save(event4);
        }

        // 4. Seed 10 NEW premium events
        seedNewEventIfAbsent(Event.builder()
                .title("Lollapalooza India 2026")
                .description("The biggest multi-genre music festival in Asia comes to Mumbai.")
                .category(CATEGORY_CONCERTS)
                .artistName("Various Artists")
                .venue(venue1)
                .eventDate(LocalDate.of(2026, 8, 12))
                .eventTime(LocalTime.of(15, 0))
                .bannerImage("https://images.unsplash.com/photo-1459749411175-04bf5292ceea?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(5999))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Sunburn Festival Goa")
                .description("Asia's premier electronic dance music festival features global top DJs.")
                .category(CATEGORY_CONCERTS)
                .artistName("EDM Artists")
                .venue(venue2)
                .eventDate(LocalDate.of(2026, 9, 20))
                .eventTime(LocalTime.of(16, 0))
                .bannerImage("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(3999))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Comic Con India")
                .description("Celebrate pop culture, movies, gaming, comics and cosplay in Delhi.")
                .category("Comedy")
                .artistName("Cosplayers & Speakers")
                .venue(venue4)
                .eventDate(LocalDate.of(2026, 10, 5))
                .eventTime(LocalTime.of(10, 0))
                .bannerImage("https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(799))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Zakir Khan Live - Tathastu")
                .description("Catch the Sakht Launda himself live in Delhi for a hilarious storytelling show.")
                .category("Comedy")
                .artistName("Zakir Khan")
                .venue(venue4)
                .eventDate(LocalDate.of(2026, 7, 28))
                .eventTime(LocalTime.of(20, 0))
                .bannerImage("https://images.unsplash.com/photo-1516280440614-37939bbacd6a?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(1200))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Wimbledon Screenings")
                .description("Live giant-screen fan park screening of the Wimbledon Men's Singles Finals.")
                .category(CATEGORY_SPORTS)
                .artistName("Tennis Fans")
                .venue(venue3)
                .eventDate(LocalDate.of(2026, 7, 12))
                .eventTime(LocalTime.of(18, 0))
                .bannerImage("https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(499))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Grand Theatre - Hamlet")
                .description("A classic performance of Shakespeare's masterpiece Hamlet live on stage.")
                .category("Theatre")
                .artistName("Royal Theatre Troupe")
                .venue(venue4)
                .eventDate(LocalDate.of(2026, 8, 30))
                .eventTime(LocalTime.of(18, 30))
                .bannerImage("https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(1500))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Taylor Swift The Eras Tour")
                .description("The global sensation Taylor Swift brings her record-breaking Eras Tour to Mumbai.")
                .category(CATEGORY_CONCERTS)
                .artistName("Taylor Swift")
                .venue(venue1)
                .eventDate(LocalDate.of(2026, 11, 15))
                .eventTime(LocalTime.of(18, 0))
                .bannerImage("https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(9999))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Ed Sheeran Live in Bengaluru")
                .description("Global singer-songwriter Ed Sheeran brings his Mathematics Tour to Bengaluru.")
                .category(CATEGORY_CONCERTS)
                .artistName("Ed Sheeran")
                .venue(venue2)
                .eventDate(LocalDate.of(2026, 11, 22))
                .eventTime(LocalTime.of(19, 0))
                .bannerImage("https://images.unsplash.com/photo-1484755560695-a4c7302c3f29?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(4999))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("IPL Final 2026")
                .description("Watch the grand finale of the Indian Premier League live at Chepauk.")
                .category(CATEGORY_SPORTS)
                .artistName("Finalist Teams")
                .venue(venue3)
                .eventDate(LocalDate.of(2026, 5, 30))
                .eventTime(LocalTime.of(20, 0))
                .bannerImage("https://images.unsplash.com/photo-1540747737956-37872404f86f?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(1999))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        seedNewEventIfAbsent(Event.builder()
                .title("Broadway - The Lion King")
                .description("Experience the award-winning musical theatrical production of The Lion King.")
                .category("Theatre")
                .artistName("Broadway Cast")
                .venue(venue2)
                .eventDate(LocalDate.of(2026, 9, 10))
                .eventTime(LocalTime.of(19, 30))
                .bannerImage("https://images.unsplash.com/photo-1503095391755-111c18379075?q=80&w=1170&auto=format&fit=crop")
                .ticketPrice(BigDecimal.valueOf(2500))
                .status(Event.EventStatus.ACTIVE)
                .createdBy(admin)
                .build());

        // 5. Seed login histories
        if (loginHistoryRepository.count() == 0) {
            LoginHistory login1 = LoginHistory.builder()
                    .user(customer)
                    .ipAddress(String.join(".", "192", "168", "1", "100"))
                    .device("Desktop PC")
                    .browser("Chrome")
                    .status(LoginHistory.LoginStatus.SUCCESS)
                    .build();
            loginHistoryRepository.save(login1);

            LoginHistory login2 = LoginHistory.builder()
                    .user(customer)
                    .ipAddress(String.join(".", "10", "200", "5", "42"))
                    .device("Mobile Bot")
                    .browser("Safari")
                    .status(LoginHistory.LoginStatus.SUCCESS)
                    .build();
            loginHistoryRepository.save(login2);
        }

        // 6. Seed mock AI Fraud alerts
        if (aiFraudLogRepository.count() == 0) {
            AiFraudLog alert1 = AiFraudLog.builder()
                    .user(customer)
                    .riskScore(92.0)
                    .reason("High speed booking detected")
                    .actionTaken("AUTO-BLOCKED BOOKING")
                    .keystrokeVelocity(120.0)
                    .mouseMovementEntropy(0.05)
                    .timeSpentSeconds(0.8)
                    .isBot(true)
                    .build();
            aiFraudLogRepository.save(alert1);

            AiFraudLog alert2 = AiFraudLog.builder()
                    .user(customer)
                    .riskScore(88.0)
                    .reason("Multiple accounts detected")
                    .actionTaken("AUTO-BLOCKED")
                    .keystrokeVelocity(45.0)
                    .mouseMovementEntropy(0.4)
                    .timeSpentSeconds(3.2)
                    .isBot(true)
                    .build();
            aiFraudLogRepository.save(alert2);

            AiFraudLog alert3 = AiFraudLog.builder()
                    .user(customer)
                    .riskScore(65.0)
                    .reason("Unusual location mismatch")
                    .actionTaken("FLAGGED FOR REVIEW")
                    .keystrokeVelocity(12.5)
                    .mouseMovementEntropy(2.8)
                    .timeSpentSeconds(45.5)
                    .isBot(false)
                    .build();
            aiFraudLogRepository.save(alert3);
        }
    }

    private void seedNewEventIfAbsent(Event event) {
        Event existing = eventRepository.findAll().stream()
                .filter(e -> e.getTitle().equalsIgnoreCase(event.getTitle()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            if (!event.getBannerImage().equals(existing.getBannerImage())) {
                existing.setBannerImage(event.getBannerImage());
                eventRepository.save(existing);
            }
            return;
        }
        eventRepository.save(event);
    }

    private void generateSeatsForVenue(Venue venue) {
        List<SeatLayout> seats = new ArrayList<>();
        String[] rows = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };
        for (String row : rows) {
            SeatLayout.SeatType type = SeatLayout.SeatType.REGULAR;
            BigDecimal basePrice = BigDecimal.valueOf(1000);

            if (row.equals("A") || row.equals("B")) {
                type = SeatLayout.SeatType.VIP;
                basePrice = BigDecimal.valueOf(2499);
            } else if (row.equals("C") || row.equals("D")) {
                type = SeatLayout.SeatType.VIP;
                basePrice = BigDecimal.valueOf(1800);
            } else if (row.equals("I") || row.equals("J")) {
                type = SeatLayout.SeatType.BALCONY;
                basePrice = BigDecimal.valueOf(800);
            }

            for (int seatNum = 1; seatNum <= 15; seatNum++) {
                seats.add(SeatLayout.builder()
                        .venue(venue)
                        .rowName(row)
                        .seatNumber(seatNum)
                        .seatType(type)
                        .price(basePrice)
                        .status(SeatLayout.SeatStatus.AVAILABLE)
                        .build());
            }
        }
        seatLayoutRepository.saveAll(seats);
    }
}

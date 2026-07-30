package com.ticketguard.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ticketguard.config.FlexibleLocalTimeDeserializer;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    @Column(name = "artist_name")
    private String artistName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm a")
    @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
    private LocalTime eventTime;

    @Column(name = "banner_image")
    private String bannerImage;

    @Column(name = "ticket_price", nullable = false)
    private BigDecimal ticketPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Event() {}

    @SuppressWarnings("java:S107")
    public Event(Long id, String title, String description, String category, String artistName, Venue venue, LocalDate eventDate, LocalTime eventTime, String bannerImage, BigDecimal ticketPrice, EventStatus status, User createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.artistName = artistName;
        this.venue = venue;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.bannerImage = bannerImage;
        this.ticketPrice = ticketPrice;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }

    public String getBannerImage() { return bannerImage; }
    public void setBannerImage(String bannerImage) { this.bannerImage = bannerImage; }

    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = EventStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EventStatus {
        ACTIVE, INACTIVE, DRAFT
    }

    // Builder
    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static class EventBuilder {
        private Long id;
        private String title;
        private String description;
        private String category;
        private String artistName;
        private Venue venue;
        private LocalDate eventDate;
        private LocalTime eventTime;
        private String bannerImage;
        private BigDecimal ticketPrice;
        private EventStatus status;
        private User createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public EventBuilder id(Long id) { this.id = id; return this; }
        public EventBuilder title(String title) { this.title = title; return this; }
        public EventBuilder description(String description) { this.description = description; return this; }
        public EventBuilder category(String category) { this.category = category; return this; }
        public EventBuilder artistName(String artistName) { this.artistName = artistName; return this; }
        public EventBuilder venue(Venue venue) { this.venue = venue; return this; }
        public EventBuilder eventDate(LocalDate eventDate) { this.eventDate = eventDate; return this; }
        public EventBuilder eventTime(LocalTime eventTime) { this.eventTime = eventTime; return this; }
        public EventBuilder bannerImage(String bannerImage) { this.bannerImage = bannerImage; return this; }
        public EventBuilder ticketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; return this; }
        public EventBuilder status(EventStatus status) { this.status = status; return this; }
        public EventBuilder createdBy(User createdBy) { this.createdBy = createdBy; return this; }
        public EventBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public EventBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Event build() {
            return new Event(id, title, description, category, artistName, venue, eventDate, eventTime, bannerImage, ticketPrice, status, createdBy, createdAt, updatedAt);
        }
    }
}

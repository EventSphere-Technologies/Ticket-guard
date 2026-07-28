package com.ticketguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "venues")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venue_name", nullable = false)
    private String venueName;

    @Column(nullable = false)
    private String city;

    private String state;

    private String country;

    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Venue() {}

    public Venue(Long id, String venueName, String city, String state, String country, String address, Integer capacity, LocalDateTime createdAt) {
        this.id = id;
        this.venueName = venueName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.address = address;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Builder
    public static VenueBuilder builder() {
        return new VenueBuilder();
    }

    public static class VenueBuilder {
        private Long id;
        private String venueName;
        private String city;
        private String state;
        private String country;
        private String address;
        private Integer capacity;
        private LocalDateTime createdAt;

        public VenueBuilder id(Long id) { this.id = id; return this; }
        public VenueBuilder venueName(String venueName) { this.venueName = venueName; return this; }
        public VenueBuilder city(String city) { this.city = city; return this; }
        public VenueBuilder state(String state) { this.state = state; return this; }
        public VenueBuilder country(String country) { this.country = country; return this; }
        public VenueBuilder address(String address) { this.address = address; return this; }
        public VenueBuilder capacity(Integer capacity) { this.capacity = capacity; return this; }
        public VenueBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Venue build() {
            return new Venue(id, venueName, city, state, country, address, capacity, createdAt);
        }
    }
}

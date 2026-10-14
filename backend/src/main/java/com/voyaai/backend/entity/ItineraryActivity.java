package com.voyaai.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "itinerary_activities")
public class ItineraryActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itinerary_day_id", nullable = false)
    private ItineraryDay itineraryDay;

    @Column(nullable = false)
    private String time; // e.g. "09:00"

    @Column(nullable = false)
    private String activity; // Real attraction or place name

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String location;

    @Column
    private String address;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String category; // e.g. "Sightseeing", "Food", "Culture", "Nature", "Transport", "Relaxation"

    @Column(name = "estimated_cost")
    private Double estimatedCost = 0.0;

    @Column
    private String duration; // e.g. "2 hours"

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private String websiteUrl;

    @Column(name = "travel_from_previous")
    private String travelFromPrevious;

    public ItineraryActivity() {
    }

    public ItineraryActivity(String time, String activity, String description, String location,
                             String category, Double estimatedCost, String duration, String notes) {
        this.time = time;
        this.activity = activity;
        this.description = description;
        this.location = location;
        this.category = category;
        this.estimatedCost = estimatedCost != null ? estimatedCost : 0.0;
        this.duration = duration;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItineraryDay getItineraryDay() {
        return itineraryDay;
    }

    public void setItineraryDay(ItineraryDay itineraryDay) {
        this.itineraryDay = itineraryDay;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost != null ? estimatedCost : 0.0;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getTravelFromPrevious() {
        return travelFromPrevious;
    }

    public void setTravelFromPrevious(String travelFromPrevious) {
        this.travelFromPrevious = travelFromPrevious;
    }
}

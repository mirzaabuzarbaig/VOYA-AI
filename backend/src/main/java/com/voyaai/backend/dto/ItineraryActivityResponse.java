package com.voyaai.backend.dto;

import com.voyaai.backend.entity.ItineraryActivity;

public class ItineraryActivityResponse {

    private Long id;
    private String time;
    private String activity;
    private String description;
    private String location;
    private String address;
    private Double latitude;
    private Double longitude;
    private String category;
    private Double estimatedCost;
    private String duration;
    private String notes;
    private String websiteUrl;
    private String travelFromPrevious;

    public ItineraryActivityResponse() {
    }

    public static ItineraryActivityResponse fromEntity(ItineraryActivity entity) {
        if (entity == null) return null;
        ItineraryActivityResponse dto = new ItineraryActivityResponse();
        dto.setId(entity.getId());
        dto.setTime(entity.getTime());
        dto.setActivity(entity.getActivity());
        dto.setDescription(entity.getDescription());
        dto.setLocation(entity.getLocation());
        dto.setAddress(entity.getAddress());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setCategory(entity.getCategory());
        dto.setEstimatedCost(entity.getEstimatedCost());
        dto.setDuration(entity.getDuration());
        dto.setNotes(entity.getNotes());
        dto.setWebsiteUrl(entity.getWebsiteUrl());
        dto.setTravelFromPrevious(entity.getTravelFromPrevious());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.estimatedCost = estimatedCost;
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

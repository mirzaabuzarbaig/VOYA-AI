package com.voyaai.backend.dto;

import com.voyaai.backend.entity.Itinerary;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItineraryResponse {

    private Long id;
    private Long tripId;
    private String title;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ItineraryDayResponse> days = new ArrayList<>();

    public ItineraryResponse() {
    }

    public static ItineraryResponse fromEntity(Itinerary entity) {
        if (entity == null) return null;
        ItineraryResponse dto = new ItineraryResponse();
        dto.setId(entity.getId());
        dto.setTripId(entity.getTrip() != null ? entity.getTrip().getId() : null);
        dto.setTitle(entity.getTitle());
        dto.setSummary(entity.getSummary());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getDays() != null) {
            dto.setDays(entity.getDays().stream()
                    .map(ItineraryDayResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ItineraryDayResponse> getDays() {
        return days;
    }

    public void setDays(List<ItineraryDayResponse> days) {
        this.days = days != null ? days : new ArrayList<>();
    }
}

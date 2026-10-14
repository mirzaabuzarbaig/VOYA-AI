package com.voyaai.backend.dto;

import com.voyaai.backend.entity.ItineraryDay;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItineraryDayResponse {

    private Long id;
    private Integer dayNumber;
    private LocalDate date;
    private String title;
    private String summary;
    private List<ItineraryActivityResponse> activities = new ArrayList<>();

    public ItineraryDayResponse() {
    }

    public static ItineraryDayResponse fromEntity(ItineraryDay entity) {
        if (entity == null) return null;
        ItineraryDayResponse dto = new ItineraryDayResponse();
        dto.setId(entity.getId());
        dto.setDayNumber(entity.getDayNumber());
        dto.setDate(entity.getDate());
        dto.setTitle(entity.getTitle());
        dto.setSummary(entity.getSummary());
        if (entity.getActivities() != null) {
            dto.setActivities(entity.getActivities().stream()
                    .map(ItineraryActivityResponse::fromEntity)
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

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public List<ItineraryActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<ItineraryActivityResponse> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
    }
}

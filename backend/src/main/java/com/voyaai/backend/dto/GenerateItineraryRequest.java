package com.voyaai.backend.dto;

public class GenerateItineraryRequest {

    private String notes;
    private String focus; // e.g. "relaxed", "cultural", "foodie", "adventure", "budget"

    public GenerateItineraryRequest() {
    }

    public GenerateItineraryRequest(String notes, String focus) {
        this.notes = notes;
        this.focus = focus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }
}

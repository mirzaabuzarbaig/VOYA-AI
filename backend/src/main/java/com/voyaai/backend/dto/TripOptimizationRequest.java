package com.voyaai.backend.dto;

public class TripOptimizationRequest {

    private String mode = "BALANCED"; // "BUDGET", "BALANCED", "EXPERIENCE"
    private Double maxBudget;
    private Boolean strictBudget = false;
    private Boolean preserveTransport = false;
    private Boolean preserveAccommodation = false;

    public TripOptimizationRequest() {
    }

    public TripOptimizationRequest(String mode, Double maxBudget, Boolean strictBudget,
                                  Boolean preserveTransport, Boolean preserveAccommodation) {
        this.mode = mode != null ? mode : "BALANCED";
        this.maxBudget = maxBudget;
        this.strictBudget = strictBudget != null ? strictBudget : false;
        this.preserveTransport = preserveTransport != null ? preserveTransport : false;
        this.preserveAccommodation = preserveAccommodation != null ? preserveAccommodation : false;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(Double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public Boolean getStrictBudget() {
        return strictBudget;
    }

    public void setStrictBudget(Boolean strictBudget) {
        this.strictBudget = strictBudget;
    }

    public Boolean getPreserveTransport() {
        return preserveTransport;
    }

    public void setPreserveTransport(Boolean preserveTransport) {
        this.preserveTransport = preserveTransport;
    }

    public Boolean getPreserveAccommodation() {
        return preserveAccommodation;
    }

    public void setPreserveAccommodation(Boolean preserveAccommodation) {
        this.preserveAccommodation = preserveAccommodation;
    }
}

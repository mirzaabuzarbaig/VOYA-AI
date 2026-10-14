package com.voyaai.backend.dto;

import java.time.LocalDateTime;

public class TripCostPredictionResponse {

    private Long tripId;
    private Double budget;
    private Double predictedCost;
    private Double difference;
    private String currency;
    private String model;
    private String modelVersion;
    private String unit;
    private String message;
    private LocalDateTime timestamp;

    public TripCostPredictionResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public TripCostPredictionResponse(Long tripId, Double budget, Double predictedCost,
                                      Double difference, String currency, String model,
                                      String modelVersion, String unit, String message) {
        this.tripId = tripId;
        this.budget = budget;
        this.predictedCost = predictedCost;
        this.difference = difference;
        this.currency = currency;
        this.model = model;
        this.modelVersion = modelVersion;
        this.unit = unit;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Double getPredictedCost() {
        return predictedCost;
    }

    public void setPredictedCost(Double predictedCost) {
        this.predictedCost = predictedCost;
    }

    public Double getDifference() {
        return difference;
    }

    public void setDifference(Double difference) {
        this.difference = difference;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

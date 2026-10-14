package com.voyaai.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class TripOptimizationResponse {

    private Long tripId;
    private String destination;
    private String tripType;
    private String mode;
    private Double baselineCost;
    private String currency = "INR";
    private String weatherSummary;
    private List<OptimizedCandidateDto> results = new ArrayList<>();
    private String message;
    private String disclaimer = "Optimization results are recommendations based on available trip data and ML-estimated costs. They are not guaranteed live booking prices.";

    public TripOptimizationResponse() {
    }

    public TripOptimizationResponse(Long tripId, String destination, String tripType,
                                  String mode, Double baselineCost, String currency,
                                  String weatherSummary, List<OptimizedCandidateDto> results,
                                  String message) {
        this.tripId = tripId;
        this.destination = destination;
        this.tripType = tripType;
        this.mode = mode;
        this.baselineCost = baselineCost;
        this.currency = currency != null ? currency : "INR";
        this.weatherSummary = weatherSummary;
        this.results = results != null ? results : new ArrayList<>();
        this.message = message;
        this.disclaimer = "Optimization results are recommendations based on available trip data and ML-estimated costs. They are not guaranteed live booking prices.";
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Double getBaselineCost() {
        return baselineCost;
    }

    public void setBaselineCost(Double baselineCost) {
        this.baselineCost = baselineCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getWeatherSummary() {
        return weatherSummary;
    }

    public void setWeatherSummary(String weatherSummary) {
        this.weatherSummary = weatherSummary;
    }

    public List<OptimizedCandidateDto> getResults() {
        return results;
    }

    public void setResults(List<OptimizedCandidateDto> results) {
        this.results = results != null ? results : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}

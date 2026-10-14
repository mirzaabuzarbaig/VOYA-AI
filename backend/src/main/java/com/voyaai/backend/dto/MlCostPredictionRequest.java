package com.voyaai.backend.dto;

public class MlCostPredictionRequest {

    private String destination;
    private Integer durationDays;
    private Integer travelers;
    private Double budget;
    private String currency;
    private String accommodation;
    private String transport;
    private String tripType;
    private Integer month;

    public MlCostPredictionRequest() {
    }

    public MlCostPredictionRequest(String destination, Integer durationDays, Integer travelers,
                                   Double budget, String currency, String accommodation,
                                   String transport, String tripType, Integer month) {
        this.destination = destination;
        this.durationDays = durationDays;
        this.travelers = travelers;
        this.budget = budget;
        this.currency = currency;
        this.accommodation = accommodation;
        this.transport = transport;
        this.tripType = tripType;
        this.month = month;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getTravelers() {
        return travelers;
    }

    public void setTravelers(Integer travelers) {
        this.travelers = travelers;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(String accommodation) {
        this.accommodation = accommodation;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }
}

package com.voyaai.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class OptimizedCandidateDto {

    private int rank;
    private String title;
    private String mode; // BUDGET, BALANCED, EXPERIENCE
    private String transport;
    private String accommodation;
    private int activityCount;
    private String activityFocus;
    private Double estimatedCost;
    private String currency = "INR";
    private Double estimatedSavings;
    private Double interestMatchScore;
    private List<String> matchedInterests = new ArrayList<>();
    private Double comfortScore;
    private Double optimizationScore;
    private List<String> highlights = new ArrayList<>();
    private String weatherAdvisory;
    private String costDifferenceMessage;

    public OptimizedCandidateDto() {
    }

    public OptimizedCandidateDto(int rank, String title, String mode, String transport,
                                 String accommodation, int activityCount, String activityFocus,
                                 Double estimatedCost, String currency, Double estimatedSavings,
                                 Double interestMatchScore, List<String> matchedInterests,
                                 Double comfortScore, Double optimizationScore,
                                 List<String> highlights, String weatherAdvisory,
                                 String costDifferenceMessage) {
        this.rank = rank;
        this.title = title;
        this.mode = mode;
        this.transport = transport;
        this.accommodation = accommodation;
        this.activityCount = activityCount;
        this.activityFocus = activityFocus;
        this.estimatedCost = estimatedCost;
        this.currency = currency != null ? currency : "INR";
        this.estimatedSavings = estimatedSavings;
        this.interestMatchScore = interestMatchScore;
        this.matchedInterests = matchedInterests != null ? matchedInterests : new ArrayList<>();
        this.comfortScore = comfortScore;
        this.optimizationScore = optimizationScore;
        this.highlights = highlights != null ? highlights : new ArrayList<>();
        this.weatherAdvisory = weatherAdvisory;
        this.costDifferenceMessage = costDifferenceMessage;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(String accommodation) {
        this.accommodation = accommodation;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(int activityCount) {
        this.activityCount = activityCount;
    }

    public String getActivityFocus() {
        return activityFocus;
    }

    public void setActivityFocus(String activityFocus) {
        this.activityFocus = activityFocus;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getEstimatedSavings() {
        return estimatedSavings;
    }

    public void setEstimatedSavings(Double estimatedSavings) {
        this.estimatedSavings = estimatedSavings;
    }

    public Double getInterestMatchScore() {
        return interestMatchScore;
    }

    public void setInterestMatchScore(Double interestMatchScore) {
        this.interestMatchScore = interestMatchScore;
    }

    public List<String> getMatchedInterests() {
        return matchedInterests;
    }

    public void setMatchedInterests(List<String> matchedInterests) {
        this.matchedInterests = matchedInterests;
    }

    public Double getComfortScore() {
        return comfortScore;
    }

    public void setComfortScore(Double comfortScore) {
        this.comfortScore = comfortScore;
    }

    public Double getOptimizationScore() {
        return optimizationScore;
    }

    public void setOptimizationScore(Double optimizationScore) {
        this.optimizationScore = optimizationScore;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public String getWeatherAdvisory() {
        return weatherAdvisory;
    }

    public void setWeatherAdvisory(String weatherAdvisory) {
        this.weatherAdvisory = weatherAdvisory;
    }

    public String getCostDifferenceMessage() {
        return costDifferenceMessage;
    }

    public void setCostDifferenceMessage(String costDifferenceMessage) {
        this.costDifferenceMessage = costDifferenceMessage;
    }
}

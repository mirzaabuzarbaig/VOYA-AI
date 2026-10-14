package com.voyaai.backend.dto;

public class MlCostPredictionResponse {

    private Double predictedCost;
    private String currency;
    private String model;
    private String modelVersion;
    private String unit;
    private String message;

    public MlCostPredictionResponse() {
    }

    public MlCostPredictionResponse(Double predictedCost, String currency, String model,
                                    String modelVersion, String unit, String message) {
        this.predictedCost = predictedCost;
        this.currency = currency;
        this.model = model;
        this.modelVersion = modelVersion;
        this.unit = unit;
        this.message = message;
    }

    public Double getPredictedCost() {
        return predictedCost;
    }

    public void setPredictedCost(Double predictedCost) {
        this.predictedCost = predictedCost;
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
}

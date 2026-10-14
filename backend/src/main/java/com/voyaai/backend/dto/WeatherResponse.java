package com.voyaai.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class WeatherResponse {
    private String destination;
    private Double temperature;
    private String condition;
    private Integer humidity;
    private Double windSpeed;
    private Double high;
    private Double low;
    private List<ForecastDayDto> forecast = new ArrayList<>();

    public WeatherResponse() {
    }

    public WeatherResponse(String destination, Double temperature, String condition, Integer humidity, Double windSpeed) {
        this.destination = destination;
        this.temperature = temperature;
        this.condition = condition;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public List<ForecastDayDto> getForecast() {
        return forecast;
    }

    public void setForecast(List<ForecastDayDto> forecast) {
        this.forecast = forecast != null ? forecast : new ArrayList<>();
    }
}

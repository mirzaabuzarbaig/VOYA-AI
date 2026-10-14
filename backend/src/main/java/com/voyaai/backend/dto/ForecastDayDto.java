package com.voyaai.backend.dto;

public class ForecastDayDto {
    private String date;
    private String day;
    private Double high;
    private Double low;
    private String condition;
    private Integer humidity;
    private Double wind;

    public ForecastDayDto() {
    }

    public ForecastDayDto(String date, String day, Double high, Double low, String condition, Integer humidity, Double wind) {
        this.date = date;
        this.day = day;
        this.high = high;
        this.low = low;
        this.condition = condition;
        this.humidity = humidity;
        this.wind = wind;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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

    public Double getWind() {
        return wind;
    }

    public void setWind(Double wind) {
        this.wind = wind;
    }
}

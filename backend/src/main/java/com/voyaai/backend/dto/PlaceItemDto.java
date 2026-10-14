package com.voyaai.backend.dto;

public class PlaceItemDto {
    private String id;
    private String name;
    private String category;
    private String location;
    private String rating;
    private String image;
    private String description;
    private String url;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer durationMinutes;
    private Double estimatedCost;
    private String clusterArea;

    public PlaceItemDto() {
    }

    public PlaceItemDto(String id, String name, String category, String location, String rating, String image, String description, String url) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.rating = rating;
        this.image = image;
        this.description = description;
        this.url = url;
    }

    public PlaceItemDto(String id, String name, String category, String location, String rating, String image,
                        String description, String url, String address, Double latitude, Double longitude,
                        Integer durationMinutes, Double estimatedCost, String clusterArea) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.location = location;
        this.rating = rating;
        this.image = image;
        this.description = description;
        this.url = url;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.durationMinutes = durationMinutes;
        this.estimatedCost = estimatedCost;
        this.clusterArea = clusterArea;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAddress() {
        return address != null ? address : location;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getClusterArea() {
        return clusterArea;
    }

    public void setClusterArea(String clusterArea) {
        this.clusterArea = clusterArea;
    }
}

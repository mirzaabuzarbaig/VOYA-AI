package com.voyaai.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class PlacesResponse {
    private String destination;
    private List<PlaceItemDto> places = new ArrayList<>();

    public PlacesResponse() {
    }

    public PlacesResponse(String destination, List<PlaceItemDto> places) {
        this.destination = destination;
        this.places = places != null ? places : new ArrayList<>();
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<PlaceItemDto> getPlaces() {
        return places;
    }

    public void setPlaces(List<PlaceItemDto> places) {
        this.places = places != null ? places : new ArrayList<>();
    }
}

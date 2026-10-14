package com.voyaai.backend.controller;

import com.voyaai.backend.dto.LocationSuggestionDto;
import com.voyaai.backend.dto.PlacesResponse;
import com.voyaai.backend.dto.WeatherResponse;
import com.voyaai.backend.service.GeocodingService;
import com.voyaai.backend.service.PlacesService;
import com.voyaai.backend.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/travel")
public class TravelController {

    private final WeatherService weatherService;
    private final PlacesService placesService;
    private final GeocodingService geocodingService;

    public TravelController(WeatherService weatherService, PlacesService placesService, GeocodingService geocodingService) {
        this.weatherService = weatherService;
        this.placesService = placesService;
        this.geocodingService = geocodingService;
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationSuggestionDto>> searchLocations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "8") Integer limit) {

        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<LocationSuggestionDto> suggestions = geocodingService.searchLocations(query.trim(), limit, type);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/weather")
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam(required = false) String destination) {

        if (destination == null || destination.trim().isBlank()) {
            throw new IllegalArgumentException("Destination query parameter is required.");
        }

        WeatherResponse weather = weatherService.getWeather(destination.trim());
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/places")
    public ResponseEntity<PlacesResponse> getPlaces(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) String query) {

        if (destination == null || destination.trim().isBlank()) {
            throw new IllegalArgumentException("Destination query parameter is required.");
        }

        PlacesResponse places = placesService.getPlaces(destination.trim(), category, limit, query);
        return ResponseEntity.ok(places);
    }
}

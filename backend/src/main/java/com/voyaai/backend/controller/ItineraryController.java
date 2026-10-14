package com.voyaai.backend.controller;

import com.voyaai.backend.dto.GenerateItineraryRequest;
import com.voyaai.backend.dto.ItineraryResponse;
import com.voyaai.backend.service.ItineraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{tripId}/itinerary")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ItineraryResponse> generateItinerary(
            @PathVariable Long tripId,
            @RequestBody(required = false) GenerateItineraryRequest request,
            Authentication authentication) {
        ItineraryResponse response = itineraryService.generateItinerary(
                tripId,
                request != null ? request : new GenerateItineraryRequest(),
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ItineraryResponse> getItinerary(
            @PathVariable Long tripId,
            Authentication authentication) {
        ItineraryResponse response = itineraryService.getItinerary(tripId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/regenerate")
    public ResponseEntity<ItineraryResponse> regenerateItinerary(
            @PathVariable Long tripId,
            @RequestBody(required = false) GenerateItineraryRequest request,
            Authentication authentication) {
        ItineraryResponse response = itineraryService.regenerateItinerary(
                tripId,
                request != null ? request : new GenerateItineraryRequest(),
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }
}

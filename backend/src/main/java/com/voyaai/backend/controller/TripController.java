package com.voyaai.backend.controller;

import com.voyaai.backend.dto.CreateTripRequest;
import com.voyaai.backend.dto.TripResponse;
import com.voyaai.backend.dto.UpdateTripRequest;
import com.voyaai.backend.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody CreateTripRequest request,
                                                  Authentication authentication) {
        TripResponse response = tripService.createTrip(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getUserTrips(Authentication authentication) {
        List<TripResponse> trips = tripService.getUserTrips(authentication.getName());
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long id,
                                                   Authentication authentication) {
        TripResponse trip = tripService.getTripById(id, authentication.getName());
        return ResponseEntity.ok(trip);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateTripRequest request,
                                                  Authentication authentication) {
        TripResponse trip = tripService.updateTrip(id, request, authentication.getName());
        return ResponseEntity.ok(trip);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTrip(@PathVariable Long id,
                                                          Authentication authentication) {
        tripService.deleteTrip(id, authentication.getName());
        return ResponseEntity.ok(Map.of("success", true, "message", "Trip deleted successfully"));
    }
}

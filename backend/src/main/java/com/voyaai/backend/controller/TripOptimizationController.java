package com.voyaai.backend.controller;

import com.voyaai.backend.dto.TripOptimizationRequest;
import com.voyaai.backend.dto.TripOptimizationResponse;
import com.voyaai.backend.service.TripOptimizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/trips/{tripId}/optimize")
public class TripOptimizationController {

    private final TripOptimizationService tripOptimizationService;

    public TripOptimizationController(TripOptimizationService tripOptimizationService) {
        this.tripOptimizationService = tripOptimizationService;
    }

    @PostMapping
    public ResponseEntity<TripOptimizationResponse> optimizeTrip(
            @PathVariable Long tripId,
            @RequestBody(required = false) TripOptimizationRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to optimize trips.");
        }

        String userEmail = authentication.getName();
        TripOptimizationResponse response = tripOptimizationService.optimizeTrip(tripId, request, userEmail);
        return ResponseEntity.ok(response);
    }
}

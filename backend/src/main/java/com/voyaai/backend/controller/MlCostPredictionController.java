package com.voyaai.backend.controller;

import com.voyaai.backend.dto.TripCostPredictionResponse;
import com.voyaai.backend.service.MlCostPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{tripId}/cost-prediction")
public class MlCostPredictionController {

    private final MlCostPredictionService mlCostPredictionService;

    public MlCostPredictionController(MlCostPredictionService mlCostPredictionService) {
        this.mlCostPredictionService = mlCostPredictionService;
    }

    @PostMapping
    public ResponseEntity<TripCostPredictionResponse> predictTripCost(
            @PathVariable Long tripId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        TripCostPredictionResponse response = mlCostPredictionService.predictCostForTrip(tripId, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<TripCostPredictionResponse> getLatestTripCostPrediction(
            @PathVariable Long tripId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return mlCostPredictionService.getLatestPredictionForTrip(tripId, userEmail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

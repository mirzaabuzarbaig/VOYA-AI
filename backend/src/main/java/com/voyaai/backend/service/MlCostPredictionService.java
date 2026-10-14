package com.voyaai.backend.service;

import com.voyaai.backend.dto.MlCostPredictionRequest;
import com.voyaai.backend.dto.MlCostPredictionResponse;
import com.voyaai.backend.dto.TripCostPredictionResponse;
import com.voyaai.backend.entity.Trip;
import com.voyaai.backend.entity.TripCostPrediction;
import com.voyaai.backend.exception.MlServiceException;
import com.voyaai.backend.exception.ResourceNotFoundException;
import com.voyaai.backend.repository.TripCostPredictionRepository;
import com.voyaai.backend.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class MlCostPredictionService {

    private static final Logger log = LoggerFactory.getLogger(MlCostPredictionService.class);

    private final TripRepository tripRepository;
    private final TripCostPredictionRepository predictionRepository;
    private final RestClient restClient;

    @Value("${ml.service.base-url:http://localhost:8000}")
    private String mlBaseUrl;

    public MlCostPredictionService(TripRepository tripRepository,
                                   TripCostPredictionRepository predictionRepository,
                                   RestClient restClient) {
        this.tripRepository = tripRepository;
        this.predictionRepository = predictionRepository;
        this.restClient = restClient;
    }

    @Transactional
    public TripCostPredictionResponse predictCostForTrip(Long tripId, String userEmail) {
        log.info("Initiating ML cost prediction for trip ID={} by user='{}'", tripId, userEmail);

        // 1. Retrieve trip and verify existence
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        // 2. Strict trip ownership authorization
        if (trip.getUser() == null || !trip.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            log.warn("Forbidden ML prediction attempt on trip ID={} by user='{}'", tripId, userEmail);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this trip.");
        }

        // 3. Extract and normalize ML input features
        int durationDays = 1;
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
            durationDays = Math.max(1, (int) days);
        }

        int travelers = (trip.getTravelers() != null && trip.getTravelers() > 0) ? trip.getTravelers() : 1;
        double budget = trip.getBudget() != null ? trip.getBudget() : 0.0;
        int month = trip.getStartDate() != null ? trip.getStartDate().getMonthValue() : 6;
        String tripType = (trip.getTripType() != null && !trip.getTripType().isBlank()) ? trip.getTripType() : "NATIONAL";

        MlCostPredictionRequest requestPayload = new MlCostPredictionRequest(
                trip.getDestination(),
                durationDays,
                travelers,
                budget,
                "INR",
                "Hotel",
                "Flight",
                tripType,
                month
        );

        // 4. Call Python FastAPI ML Service
        MlCostPredictionResponse mlResponse = predictCost(requestPayload);

        double predictedCost = mlResponse.getPredictedCost();
        double difference = predictedCost - budget;

        // 5. Persist latest prediction record
        TripCostPrediction predictionEntity = new TripCostPrediction(
                trip,
                predictedCost,
                mlResponse.getCurrency() != null ? mlResponse.getCurrency() : "INR",
                mlResponse.getModel() != null ? mlResponse.getModel() : "LinearRegression",
                mlResponse.getModelVersion() != null ? mlResponse.getModelVersion() : "cost-model-v1"
        );
        predictionRepository.save(predictionEntity);

        log.info("ML cost prediction successful for trip ID={}: Predicted=INR {}, Budget=INR {}",
                tripId, predictedCost, budget);

        return new TripCostPredictionResponse(
                tripId,
                budget,
                predictedCost,
                difference,
                mlResponse.getCurrency(),
                mlResponse.getModel(),
                mlResponse.getModelVersion(),
                mlResponse.getUnit(),
                mlResponse.getMessage()
        );
    }

    @Transactional(readOnly = true)
    public Optional<TripCostPredictionResponse> getLatestPredictionForTrip(Long tripId, String userEmail) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (trip.getUser() == null || !trip.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this trip.");
        }

        return predictionRepository.findTopByTripIdOrderByCreatedAtDesc(tripId)
                .map(p -> new TripCostPredictionResponse(
                        tripId,
                        trip.getBudget(),
                        p.getPredictedCost(),
                        p.getPredictedCost() - (trip.getBudget() != null ? trip.getBudget() : 0.0),
                        p.getCurrency(),
                        p.getModelName(),
                        p.getModelVersion(),
                        "total_trip",
                        "Latest saved ML cost estimate."
                ));
    }

    public MlCostPredictionResponse predictCost(MlCostPredictionRequest requestPayload) {
        try {
            String url = mlBaseUrl + "/predict";
            log.debug("Dispatching request to Python FastAPI ML service at: {}", url);

            MlCostPredictionResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(MlCostPredictionResponse.class);

            if (response == null || response.getPredictedCost() == null) {
                throw new MlServiceException("Received empty or invalid prediction from ML service.", HttpStatus.BAD_GATEWAY);
            }
            return response;

        } catch (ResourceAccessException ex) {
            log.error("Failed to connect to Python FastAPI ML service at {}: {}", mlBaseUrl, ex.getMessage());
            throw new MlServiceException(
                    "ML cost prediction service is currently unavailable. Please ensure the Python FastAPI service is running.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientResponseException ex) {
            log.error("ML service returned HTTP status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.BAD_GATEWAY;
            }
            throw new MlServiceException(
                    "ML service error: " + ex.getResponseBodyAsString(),
                    status
            );
        } catch (MlServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error contacting ML service: ", ex);
            throw new MlServiceException(
                    "Unexpected error in ML cost prediction: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

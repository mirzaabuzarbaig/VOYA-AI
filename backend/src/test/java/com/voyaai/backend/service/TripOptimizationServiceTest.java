package com.voyaai.backend.service;

import com.voyaai.backend.dto.*;
import com.voyaai.backend.entity.Trip;
import com.voyaai.backend.entity.User;
import com.voyaai.backend.exception.ResourceNotFoundException;
import com.voyaai.backend.repository.ItineraryRepository;
import com.voyaai.backend.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripOptimizationServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private MlCostPredictionService mlCostPredictionService;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private TripOptimizationService optimizationService;

    private User testUser;
    private Trip nationalTrip;
    private Trip internationalTrip;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Traveler Alice");
        testUser.setEmail("alice@voyaai.com");

        nationalTrip = new Trip();
        nationalTrip.setId(10L);
        nationalTrip.setUser(testUser);
        nationalTrip.setDestination("Goa");
        nationalTrip.setStartDate(LocalDate.of(2026, 12, 10));
        nationalTrip.setEndDate(LocalDate.of(2026, 12, 15));
        nationalTrip.setTravelers(2);
        nationalTrip.setBudget(60000.0);
        nationalTrip.setTripType("NATIONAL");
        nationalTrip.setInterests(List.of("Beach", "Food", "Culture"));

        internationalTrip = new Trip();
        internationalTrip.setId(20L);
        internationalTrip.setUser(testUser);
        internationalTrip.setDestination("Paris");
        internationalTrip.setStartDate(LocalDate.of(2026, 6, 1));
        internationalTrip.setEndDate(LocalDate.of(2026, 6, 7));
        internationalTrip.setTravelers(2);
        internationalTrip.setBudget(180000.0);
        internationalTrip.setTripType("INTERNATIONAL");
        internationalTrip.setInterests(List.of("Museums", "Culture", "Dining"));
    }

    @Test
    @DisplayName("Should throw 404 when trip is not found")
    void testTripNotFound() {
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                optimizationService.optimizeTrip(999L, new TripOptimizationRequest(), "alice@voyaai.com")
        );
    }

    @Test
    @DisplayName("Should throw 403 Forbidden when user is not the trip owner")
    void testTripForbiddenForOtherUser() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(nationalTrip));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                optimizationService.optimizeTrip(10L, new TripOptimizationRequest(), "bob@other.com")
        );
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Should optimize national trip in BUDGET mode prioritizing low-cost candidates")
    void testBudgetModeOptimization() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(nationalTrip));
        when(itineraryRepository.findByTrip(nationalTrip)).thenReturn(Optional.empty());
        when(weatherService.getWeather("Goa")).thenReturn(new WeatherResponse("Goa", 28.5, "Sunny", 65, 12.0));

        // Mock baseline and candidate predictions with varying costs
        when(mlCostPredictionService.predictCost(any(MlCostPredictionRequest.class))).thenAnswer(invocation -> {
            MlCostPredictionRequest req = invocation.getArgument(0);
            double cost = 60000.0;
            if ("Hostel".equalsIgnoreCase(req.getAccommodation())) {
                cost = 32000.0;
            } else if ("Guesthouse".equalsIgnoreCase(req.getAccommodation())) {
                cost = 42000.0;
            } else if ("Hotel".equalsIgnoreCase(req.getAccommodation())) {
                cost = 55000.0;
            } else if ("Resort".equalsIgnoreCase(req.getAccommodation())) {
                cost = 75000.0;
            } else if ("Luxury Hotel".equalsIgnoreCase(req.getAccommodation())) {
                cost = 98000.0;
            }
            return new MlCostPredictionResponse(cost, "INR", "LinearRegression", "cost-model-v1", "total_trip", "OK");
        });

        TripOptimizationRequest req = new TripOptimizationRequest("BUDGET", null, false, false, false);
        TripOptimizationResponse response = optimizationService.optimizeTrip(10L, req, "alice@voyaai.com");

        assertNotNull(response);
        assertEquals(10L, response.getTripId());
        assertEquals("Goa", response.getDestination());
        assertEquals("BUDGET", response.getMode());
        assertFalse(response.getResults().isEmpty());

        OptimizedCandidateDto topCandidate = response.getResults().get(0);
        assertEquals(1, topCandidate.getRank());
        assertTrue(topCandidate.getEstimatedCost() < response.getBaselineCost(), "Top candidate in BUDGET mode should reduce cost");
        assertTrue(topCandidate.getEstimatedSavings() > 0, "Top candidate should show positive estimated savings");
    }

    @Test
    @DisplayName("Should optimize trip in BALANCED mode and return diverse options")
    void testBalancedModeOptimization() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(nationalTrip));
        when(itineraryRepository.findByTrip(nationalTrip)).thenReturn(Optional.empty());
        when(weatherService.getWeather("Goa")).thenReturn(null);

        when(mlCostPredictionService.predictCost(any(MlCostPredictionRequest.class))).thenAnswer(invocation -> {
            MlCostPredictionRequest r = invocation.getArgument(0);
            double cost = 50000.0;
            if ("Hostel".equalsIgnoreCase(r.getAccommodation())) cost = 35000.0;
            if ("Hotel".equalsIgnoreCase(r.getAccommodation())) cost = 52000.0;
            if ("Resort".equalsIgnoreCase(r.getAccommodation())) cost = 68000.0;
            if ("Luxury Hotel".equalsIgnoreCase(r.getAccommodation())) cost = 95000.0;
            return new MlCostPredictionResponse(cost, "INR", "LinearRegression", "cost-model-v1", "total_trip", "OK");
        });

        TripOptimizationRequest req = new TripOptimizationRequest("BALANCED", null, false, false, false);
        TripOptimizationResponse response = optimizationService.optimizeTrip(10L, req, "alice@voyaai.com");

        assertNotNull(response);
        assertEquals("BALANCED", response.getMode());
        assertTrue(response.getResults().size() <= 3);
        assertTrue(response.getResults().size() > 0);
    }

    @Test
    @DisplayName("Should optimize trip in EXPERIENCE mode prioritizing comfort")
    void testExperienceModeOptimization() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(nationalTrip));
        when(itineraryRepository.findByTrip(nationalTrip)).thenReturn(Optional.empty());

        when(mlCostPredictionService.predictCost(any(MlCostPredictionRequest.class))).thenAnswer(invocation -> {
            MlCostPredictionRequest r = invocation.getArgument(0);
            double cost = 50000.0;
            if ("Hostel".equalsIgnoreCase(r.getAccommodation())) cost = 30000.0;
            if ("Hotel".equalsIgnoreCase(r.getAccommodation())) cost = 52000.0;
            if ("Resort".equalsIgnoreCase(r.getAccommodation())) cost = 70000.0;
            if ("Luxury Hotel".equalsIgnoreCase(r.getAccommodation())) cost = 92000.0;
            return new MlCostPredictionResponse(cost, "INR", "LinearRegression", "cost-model-v1", "total_trip", "OK");
        });

        TripOptimizationRequest req = new TripOptimizationRequest("EXPERIENCE", null, false, false, false);
        TripOptimizationResponse response = optimizationService.optimizeTrip(10L, req, "alice@voyaai.com");

        assertNotNull(response);
        assertEquals("EXPERIENCE", response.getMode());
        OptimizedCandidateDto topCandidate = response.getResults().get(0);
        assertTrue(topCandidate.getComfortScore() >= 0.7, "Top candidate in EXPERIENCE mode should have high comfort score");
    }

    @Test
    @DisplayName("Should strictly reject candidates exceeding maximum budget when strictBudget is enabled")
    void testStrictBudgetRejection() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(nationalTrip));
        when(itineraryRepository.findByTrip(nationalTrip)).thenReturn(Optional.empty());

        when(mlCostPredictionService.predictCost(any(MlCostPredictionRequest.class))).thenAnswer(invocation -> {
            MlCostPredictionRequest r = invocation.getArgument(0);
            double cost = 60000.0;
            if ("Hostel".equalsIgnoreCase(r.getAccommodation())) cost = 38000.0;
            if ("Guesthouse".equalsIgnoreCase(r.getAccommodation())) cost = 45000.0;
            if ("Hotel".equalsIgnoreCase(r.getAccommodation())) cost = 58000.0;
            if ("Resort".equalsIgnoreCase(r.getAccommodation())) cost = 75000.0; // exceeds 50000
            if ("Luxury Hotel".equalsIgnoreCase(r.getAccommodation())) cost = 95000.0; // exceeds 50000
            return new MlCostPredictionResponse(cost, "INR", "LinearRegression", "cost-model-v1", "total_trip", "OK");
        });

        // Set maxBudget = 50,000 with strictBudget = true
        TripOptimizationRequest req = new TripOptimizationRequest("BUDGET", 50000.0, true, false, false);
        TripOptimizationResponse response = optimizationService.optimizeTrip(10L, req, "alice@voyaai.com");

        for (OptimizedCandidateDto candidate : response.getResults()) {
            assertTrue(candidate.getEstimatedCost() <= 50000.0,
                    "Candidate cost " + candidate.getEstimatedCost() + " exceeded strict maximum budget of 50000");
        }
    }

    @Test
    @DisplayName("Should return clean explanation when no feasible solution fits strict budget")
    void testNoFeasibleSolution() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(nationalTrip));
        when(itineraryRepository.findByTrip(nationalTrip)).thenReturn(Optional.empty());

        // All candidates predict cost >= 25,000
        when(mlCostPredictionService.predictCost(any(MlCostPredictionRequest.class)))
                .thenReturn(new MlCostPredictionResponse(35000.0, "INR", "LinearRegression", "cost-model-v1", "total_trip", "OK"));

        // Impossible budget = 5,000
        TripOptimizationRequest req = new TripOptimizationRequest("BUDGET", 5000.0, true, false, false);
        TripOptimizationResponse response = optimizationService.optimizeTrip(10L, req, "alice@voyaai.com");

        assertNotNull(response);
        assertTrue(response.getResults().isEmpty(), "No candidates should be returned when all exceed strict budget");
        assertTrue(response.getMessage().contains("No feasible option was found"));
    }

    @Test
    @DisplayName("Should optimize international trip while preserving Flight transport constraint")
    void testInternationalTripOptimization() {
        when(tripRepository.findById(20L)).thenReturn(Optional.of(internationalTrip));
        when(itineraryRepository.findByTrip(internationalTrip)).thenReturn(Optional.empty());

        when(mlCostPredictionService.predictCost(any(MlCostPredictionRequest.class))).thenAnswer(invocation -> {
            MlCostPredictionRequest r = invocation.getArgument(0);
            assertEquals("INTERNATIONAL", r.getTripType());
            assertEquals("Flight", r.getTransport(), "International transport should remain Flight");
            return new MlCostPredictionResponse(150000.0, "INR", "LinearRegression", "cost-model-v1", "total_trip", "OK");
        });

        TripOptimizationRequest req = new TripOptimizationRequest("BALANCED", null, false, false, false);
        TripOptimizationResponse response = optimizationService.optimizeTrip(20L, req, "alice@voyaai.com");

        assertNotNull(response);
        assertEquals("INTERNATIONAL", response.getTripType());
        for (OptimizedCandidateDto c : response.getResults()) {
            assertEquals("Flight", c.getTransport());
        }
    }
}

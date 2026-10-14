package com.voyaai.backend.controller;

import tools.jackson.databind.ObjectMapper;
import com.voyaai.backend.dto.OptimizedCandidateDto;
import com.voyaai.backend.dto.TripOptimizationRequest;
import com.voyaai.backend.dto.TripOptimizationResponse;
import com.voyaai.backend.exception.GlobalExceptionHandler;
import com.voyaai.backend.exception.ResourceNotFoundException;
import com.voyaai.backend.service.TripOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TripOptimizationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TripOptimizationService tripOptimizationService;

    @InjectMocks
    private TripOptimizationController tripOptimizationController;

    private Authentication testAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripOptimizationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        testAuth = new UsernamePasswordAuthenticationToken("alice@voyaai.com", "password", Collections.emptyList());
    }

    @Test
    @DisplayName("Unauthenticated request without authentication returns 401 Unauthorized")
    void testUnauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(post("/api/trips/10/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated request with valid parameters returns 200 OK and ranked candidate results")
    void testAuthenticatedValidOptimizationReturns200() throws Exception {
        OptimizedCandidateDto candidate = new OptimizedCandidateDto(
                1,
                "Budget Saver (Scenic Rail & Hostel)",
                "BUDGET",
                "Train",
                "Hostel",
                6,
                "Historic sights and local food",
                38000.0,
                "INR",
                12000.0,
                0.85,
                List.of("Food", "Culture"),
                0.45,
                88.5,
                List.of("Rail savings", "Hostel accommodation"),
                "Clear sunny weather",
                "Saves ₹12000 compared to base plan"
        );

        TripOptimizationResponse mockResponse = new TripOptimizationResponse(
                10L,
                "Goa",
                "NATIONAL",
                "BUDGET",
                50000.0,
                "INR",
                "Clear, 28°C",
                List.of(candidate),
                "Found 1 optimized trip configuration in BUDGET mode."
        );

        when(tripOptimizationService.optimizeTrip(eq(10L), any(TripOptimizationRequest.class), eq("alice@voyaai.com")))
                .thenReturn(mockResponse);

        TripOptimizationRequest request = new TripOptimizationRequest("BUDGET", 45000.0, true, false, false);

        mockMvc.perform(post("/api/trips/10/optimize")
                        .principal(testAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tripId").value(10))
                .andExpect(jsonPath("$.destination").value("Goa"))
                .andExpect(jsonPath("$.mode").value("BUDGET"))
                .andExpect(jsonPath("$.baselineCost").value(50000.0))
                .andExpect(jsonPath("$.results[0].rank").value(1))
                .andExpect(jsonPath("$.results[0].transport").value("Train"))
                .andExpect(jsonPath("$.results[0].accommodation").value("Hostel"))
                .andExpect(jsonPath("$.results[0].estimatedCost").value(38000.0))
                .andExpect(jsonPath("$.results[0].estimatedSavings").value(12000.0))
                .andExpect(jsonPath("$.disclaimer").exists());
    }

    @Test
    @DisplayName("Request by non-owner returns 403 Forbidden")
    void testNonOwnerForbiddenReturns403() throws Exception {
        Authentication attackerAuth = new UsernamePasswordAuthenticationToken("attacker@other.com", "pwd", Collections.emptyList());

        when(tripOptimizationService.optimizeTrip(eq(10L), any(), eq("attacker@other.com")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access or optimize this trip."));

        mockMvc.perform(post("/api/trips/10/optimize")
                        .principal(attackerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Request for non-existent trip returns 404 Not Found")
    void testNonExistentTripReturns404() throws Exception {
        when(tripOptimizationService.optimizeTrip(eq(999L), any(), eq("alice@voyaai.com")))
                .thenThrow(new ResourceNotFoundException("Trip not found with id: 999"));

        mockMvc.perform(post("/api/trips/999/optimize")
                        .principal(testAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}

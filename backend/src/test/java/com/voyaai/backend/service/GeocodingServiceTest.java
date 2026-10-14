package com.voyaai.backend.service;

import tools.jackson.databind.ObjectMapper;
import com.voyaai.backend.dto.LocationSuggestionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private RestClient restClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingService(restClient, objectMapper);
    }

    @Test
    @DisplayName("searchLocations returns empty list when query is null, empty, or single character")
    void testSearchLocationsReturnsEmptyForShortQueries() {
        List<LocationSuggestionDto> nullRes = geocodingService.searchLocations(null, 5, "national");
        assertTrue(nullRes.isEmpty());

        List<LocationSuggestionDto> emptyRes = geocodingService.searchLocations("   ", 5, "national");
        assertTrue(emptyRes.isEmpty());

        List<LocationSuggestionDto> singleCharRes = geocodingService.searchLocations("a", 5, "national");
        assertTrue(singleCharRes.isEmpty());

        List<LocationSuggestionDto> singleCharG = geocodingService.searchLocations("G", 5, "international");
        assertTrue(singleCharG.isEmpty());
    }
}

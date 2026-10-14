package com.voyaai.backend.service;

import tools.jackson.databind.ObjectMapper;
import com.voyaai.backend.dto.PlaceItemDto;
import com.voyaai.backend.dto.PlacesResponse;
import com.voyaai.backend.entity.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
public class RealPlacesItineraryTest {

    private PlacesService placesService;
    private AiService aiService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        RestClient restClient = RestClient.builder().build();
        GeocodingService geocodingService = new GeocodingService(restClient, objectMapper);
        placesService = new PlacesService(restClient, objectMapper, geocodingService);
        aiService = new AiService(restClient, objectMapper);
    }

    @Test
    void testDubaiPlacesContainRealAttractions() {
        PlacesResponse response = placesService.getPlaces("Dubai", "sightseeing,culture", 25, null);
        assertNotNull(response);
        assertNotNull(response.getPlaces());
        assertFalse(response.getPlaces().isEmpty());

        List<String> names = response.getPlaces().stream().map(PlaceItemDto::getName).toList();

        // Must contain iconic Dubai sights
        assertTrue(names.contains("Burj Khalifa"), "Should contain Burj Khalifa");
        assertTrue(names.contains("The Dubai Mall & Dubai Aquarium"), "Should contain Dubai Mall");
        assertTrue(names.contains("Museum of the Future"), "Should contain Museum of the Future");
        assertTrue(names.contains("The Dubai Fountain"), "Should contain Dubai Fountain");
    }

    @Test
    void testHyderabadPlacesContainRealAttractions() {
        PlacesResponse response = placesService.getPlaces("Hyderabad", "history,food", 20, null);
        assertNotNull(response);
        assertNotNull(response.getPlaces());

        List<String> names = response.getPlaces().stream().map(PlaceItemDto::getName).toList();

        // Must contain iconic Hyderabad sights
        assertTrue(names.contains("Charminar"), "Should contain Charminar");
        assertTrue(names.contains("Golconda Fort"), "Should contain Golconda Fort");
        assertTrue(names.contains("Salar Jung Museum"), "Should contain Salar Jung Museum");
        assertTrue(names.contains("Chowmahalla Palace"), "Should contain Chowmahalla Palace");
        assertTrue(names.contains("Hussain Sagar Lake & Buddha Statue"), "Should contain Hussain Sagar");
    }

    @Test
    void testParisPlacesContainRealAttractions() {
        PlacesResponse response = placesService.getPlaces("Paris", "sightseeing,museum", 20, null);
        assertNotNull(response);
        assertNotNull(response.getPlaces());

        List<String> names = response.getPlaces().stream().map(PlaceItemDto::getName).toList();

        assertTrue(names.contains("Eiffel Tower"), "Should contain Eiffel Tower");
        assertTrue(names.contains("Louvre Museum"), "Should contain Louvre Museum");
        assertTrue(names.contains("Musée d'Orsay"), "Should contain Musée d'Orsay");
        assertTrue(names.contains("Cathédrale Notre-Dame de Paris"), "Should contain Notre-Dame");
    }

    @Test
    void testInterestRankingBoostsRelevantCategories() {
        PlacesResponse foodResponse = placesService.getPlaces("Dubai", "food,culinary", 20, null);
        List<PlaceItemDto> foodPlaces = foodResponse.getPlaces();
        assertFalse(foodPlaces.isEmpty());

        // Top ranked place should have Food category or culinary description
        PlaceItemDto topPlace = foodPlaces.get(0);
        String topCat = topPlace.getCategory().toLowerCase();
        assertTrue(topCat.contains("food") || topPlace.getName().toLowerCase().contains("market") || topPlace.getName().toLowerCase().contains("restaurant") || topPlace.getName().toLowerCase().contains("tea"),
                "Top place for food interest should be culinary venue: " + topPlace.getName());

        PlacesResponse beachResponse = placesService.getPlaces("Dubai", "beaches", 20, null);
        List<PlaceItemDto> beachPlaces = beachResponse.getPlaces();
        PlaceItemDto topBeach = beachPlaces.get(0);
        assertTrue(topBeach.getCategory().toLowerCase().contains("beach") || topBeach.getName().toLowerCase().contains("beach"),
                "Top place for beach interest should be beach venue: " + topBeach.getName());
    }

    @Test
    void testNegativeKeywordExclusionFilter() {
        assertTrue(placesService.isExcludedTitle("Dubai Highway E 311 Interchange"));
        assertTrue(placesService.isExcludedTitle("Hyderabad University College of Engineering"));
        assertTrue(placesService.isExcludedTitle("Air Crash Disaster 1985"));
        assertTrue(placesService.isExcludedTitle("North Ward Electoral Constituency"));

        assertFalse(placesService.isExcludedTitle("Burj Khalifa"));
        assertFalse(placesService.isExcludedTitle("Charminar"));
        assertFalse(placesService.isExcludedTitle("Louvre Museum"));
    }

    @Test
    void testSevenDayItineraryHasZeroDuplicateAttractionsAcrossDays() {
        Trip trip = new Trip();
        trip.setId(101L);
        trip.setDestination("Dubai");
        trip.setStartDate(LocalDate.of(2026, 11, 1));
        trip.setEndDate(LocalDate.of(2026, 11, 7)); // 7 full days
        trip.setTravelers(2);
        trip.setBudget(120000.0);
        trip.setTripType("leisure");
        trip.setInterests(List.of("sightseeing", "culture", "food", "beaches"));

        PlacesResponse places = placesService.getPlaces("Dubai", "sightseeing,culture,food", 35, null);
        AiService.RawItinerary itinerary = aiService.generateIntelligentFallbackItinerary(trip, null, places, null);

        assertNotNull(itinerary);
        assertEquals(7, itinerary.days.size(), "Should have exactly 7 days");

        Set<String> seenAttractions = new HashSet<>();
        List<String> genericBlacklist = List.of(
                "morning exploration in dubai", "local culinary experience",
                "afternoon cultural walk", "sunset & evening dinner", "morning tour",
                "afternoon walk", "sunset views"
        );

        for (AiService.RawItineraryDay day : itinerary.days) {
            assertNotNull(day.title);
            assertFalse(day.activities.isEmpty(), "Day " + day.dayNumber + " must have activities");

            for (AiService.RawItineraryActivity act : day.activities) {
                assertNotNull(act.activity, "Activity name must not be null");
                String actLower = act.activity.toLowerCase().trim();

                // Check that no generic activity titles are present
                for (String generic : genericBlacklist) {
                    assertNotEquals(generic, actLower, "Activity should not be generic placeholder: " + act.activity);
                }

                // If it's a specific attraction, ensure no duplicates across the 7 days
                if (!actLower.contains("waterfront promenade & relaxation")) {
                    assertFalse(seenAttractions.contains(actLower),
                            "Duplicate attraction found across trip days: " + act.activity);
                    seenAttractions.add(actLower);
                }

                // Verify transit time is populated on subsequent stops
                if (day.activities.indexOf(act) > 0) {
                    assertNotNull(act.travelFromPrevious, "Transit details should be calculated from previous stop");
                }
            }
        }

        // At least 18 unique attractions across the 7 days
        assertTrue(seenAttractions.size() >= 18, "Should feature at least 18 distinct verified attractions for 7 days, found: " + seenAttractions.size());
    }

    @Test
    void testDifferentDestinationsProduceDifferentPlaces() {
        PlacesResponse dubaiPlaces = placesService.getPlaces("Dubai", null, 15, null);
        PlacesResponse hydPlaces = placesService.getPlaces("Hyderabad", null, 15, null);

        Set<String> dubaiNames = new HashSet<>(dubaiPlaces.getPlaces().stream().map(PlaceItemDto::getName).toList());
        Set<String> hydNames = new HashSet<>(hydPlaces.getPlaces().stream().map(PlaceItemDto::getName).toList());

        // Zero overlap between Dubai and Hyderabad
        Set<String> intersection = new HashSet<>(dubaiNames);
        intersection.retainAll(hydNames);
        assertTrue(intersection.isEmpty(), "Dubai and Hyderabad must have distinct attractions");
    }

    @Test
    void testTransitDistanceAndDurationCalculations() {
        // Distance between Burj Khalifa (25.1972, 55.2744) and Dubai Marina (25.0784, 55.1415) is ~19 km
        String transit = AiService.calculateTravelDetails(25.1972, 55.2744, 25.0784, 55.1415);
        assertNotNull(transit);
        assertTrue(transit.contains("km"), "Transit string should contain distance in km: " + transit);
        assertTrue(transit.contains("mins"), "Transit string should contain duration in mins: " + transit);
    }
}

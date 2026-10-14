package com.voyaai.backend.service;

import tools.jackson.databind.ObjectMapper;
import com.voyaai.backend.dto.*;
import com.voyaai.backend.entity.*;
import com.voyaai.backend.exception.ResourceNotFoundException;
import com.voyaai.backend.repository.ItineraryRepository;
import com.voyaai.backend.repository.TripRepository;
import com.voyaai.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class ItineraryService {

    private static final Logger log = LoggerFactory.getLogger(ItineraryService.class);

    private final ItineraryRepository itineraryRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final WeatherService weatherService;
    private final PlacesService placesService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public ItineraryService(ItineraryRepository itineraryRepository,
                            TripRepository tripRepository,
                            UserRepository userRepository,
                            WeatherService weatherService,
                            PlacesService placesService,
                            AiService aiService,
                            ObjectMapper objectMapper) {
        this.itineraryRepository = itineraryRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.weatherService = weatherService;
        this.placesService = placesService;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    private User getAuthenticatedUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private Trip getTripAndValidateOwnership(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to access or generate an itinerary for this trip.");
        }
        return trip;
    }

    public ItineraryResponse generateItinerary(Long tripId, GenerateItineraryRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Trip trip = getTripAndValidateOwnership(tripId, user);

        log.info("Starting AI itinerary generation for trip ID={} by user='{}'", tripId, userEmail);

        // 1. Fetch real Weather
        WeatherResponse weather = null;
        try {
            weather = weatherService.getWeather(trip.getDestination());
        } catch (Exception ex) {
            log.warn("Weather service unavailable for destination '{}': {}", trip.getDestination(), ex.getMessage());
        }

        // 2. Fetch real Places with dynamic interest ranking and quantity for multi-day plans
        PlacesResponse places = null;
        try {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
            int neededPlaces = (int) Math.min(45, Math.max(20, totalDays * 5));
            String primaryCategory = (trip.getInterests() != null && !trip.getInterests().isEmpty())
                    ? String.join(",", trip.getInterests())
                    : null;
            places = placesService.getPlaces(trip.getDestination(), primaryCategory, neededPlaces, null);
        } catch (Exception ex) {
            log.warn("Places service unavailable for destination '{}': {}", trip.getDestination(), ex.getMessage());
        }

        // 3. Call AI Service to generate structured itinerary with real places
        AiService.RawItinerary rawItinerary = aiService.generateItinerary(trip, weather, places, request);

        // 4. Save/Update in PostgreSQL
        Itinerary itinerary = itineraryRepository.findByTrip(trip).orElse(null);
        if (itinerary == null) {
            itinerary = new Itinerary();
            itinerary.setTrip(trip);
            itinerary.setCreatedAt(LocalDateTime.now());
        } else {
            itinerary.getDays().clear();
        }

        itinerary.setTitle(rawItinerary.title);
        itinerary.setSummary(rawItinerary.summary);
        itinerary.setUpdatedAt(LocalDateTime.now());

        for (AiService.RawItineraryDay rawDay : rawItinerary.days) {
            ItineraryDay day = new ItineraryDay();
            day.setDayNumber(rawDay.dayNumber);
            day.setDate(rawDay.date);
            day.setTitle(rawDay.title);
            day.setSummary(rawDay.summary);

            for (AiService.RawItineraryActivity rawAct : rawDay.activities) {
                ItineraryActivity act = new ItineraryActivity();
                act.setTime(rawAct.time);
                act.setActivity(rawAct.activity);
                act.setDescription(rawAct.description);
                act.setLocation(rawAct.location);
                act.setAddress(rawAct.address);
                act.setLatitude(rawAct.latitude);
                act.setLongitude(rawAct.longitude);
                act.setCategory(rawAct.category);
                act.setEstimatedCost(rawAct.estimatedCost);
                act.setDuration(rawAct.duration);
                act.setNotes(rawAct.notes);
                act.setWebsiteUrl(rawAct.websiteUrl);
                act.setTravelFromPrevious(rawAct.travelFromPrevious);
                day.addActivity(act);
            }

            itinerary.addDay(day);
        }

        Itinerary saved = itineraryRepository.save(itinerary);

        // Also sync trip's itineraryJson field for seamless backward compatibility
        try {
            ItineraryResponse res = ItineraryResponse.fromEntity(saved);
            trip.setItineraryJson(objectMapper.writeValueAsString(res.getDays()));
            tripRepository.save(trip);
        } catch (Exception e) {
            log.warn("Could not serialize itinerary days to trip itineraryJson: {}", e.getMessage());
        }

        log.info("Successfully saved AI itinerary ID={} for trip ID={}", saved.getId(), trip.getId());
        return ItineraryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ItineraryResponse getItinerary(Long tripId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Trip trip = getTripAndValidateOwnership(tripId, user);

        Itinerary itinerary = itineraryRepository.findByTrip(trip)
                .orElseThrow(() -> new ResourceNotFoundException("No itinerary found for trip ID: " + tripId));

        return ItineraryResponse.fromEntity(itinerary);
    }

    public ItineraryResponse regenerateItinerary(Long tripId, GenerateItineraryRequest request, String userEmail) {
        return generateItinerary(tripId, request, userEmail);
    }
}

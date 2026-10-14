package com.voyaai.backend.service;

import com.voyaai.backend.dto.CreateTripRequest;
import com.voyaai.backend.dto.TripResponse;
import com.voyaai.backend.dto.UpdateTripRequest;
import com.voyaai.backend.entity.Trip;
import com.voyaai.backend.entity.User;
import com.voyaai.backend.exception.ResourceNotFoundException;
import com.voyaai.backend.repository.TripRepository;
import com.voyaai.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    private static final Map<String, String> DESTINATION_IMAGES = Map.ofEntries(
            Map.entry("goa", "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop&q=80"),
            Map.entry("kerala", "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?w=800&auto=format&fit=crop&q=80"),
            Map.entry("rajasthan", "https://images.unsplash.com/photo-1477587458883-47145ed94245?w=800&auto=format&fit=crop&q=80"),
            Map.entry("jaipur", "https://images.unsplash.com/photo-1477587458883-47145ed94245?w=800&auto=format&fit=crop&q=80"),
            Map.entry("manali", "https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?w=800&auto=format&fit=crop&q=80"),
            Map.entry("ladakh", "https://images.unsplash.com/photo-1581793745862-99fde7fa73d2?w=800&auto=format&fit=crop&q=80"),
            Map.entry("dubai", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80"),
            Map.entry("bali", "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=800&auto=format&fit=crop&q=80"),
            Map.entry("bangkok", "https://images.unsplash.com/photo-1508009603885-50cf7c579365?w=800&auto=format&fit=crop&q=80"),
            Map.entry("singapore", "https://images.unsplash.com/photo-1525625293386-3f8f99389edd?w=800&auto=format&fit=crop&q=80"),
            Map.entry("switzerland", "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?w=800&auto=format&fit=crop&q=80"),
            Map.entry("tokyo", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800&auto=format&fit=crop&q=80"),
            Map.entry("japan", "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800&auto=format&fit=crop&q=80"),
            Map.entry("andaman", "https://images.unsplash.com/photo-1589308078059-be1415eab4c3?w=800&auto=format&fit=crop&q=80")
    );

    private static final String DEFAULT_IMAGE = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800&auto=format&fit=crop&q=80";

    public TripService(TripRepository tripRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
    }

    private String resolveImage(String providedImage, String destination) {
        if (providedImage != null && !providedImage.isBlank()) {
            return providedImage;
        }
        if (destination != null) {
            String destLower = destination.toLowerCase();
            for (Map.Entry<String, String> entry : DESTINATION_IMAGES.entrySet()) {
                if (destLower.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return DEFAULT_IMAGE;
    }

    private String normalizeTripType(String tripType) {
        if (tripType == null || tripType.isBlank()) {
            return "NATIONAL";
        }
        String lower = tripType.trim().toLowerCase();
        if (lower.equals("domestic") || lower.equals("national")) {
            return "NATIONAL";
        }
        if (lower.equals("international")) {
            return "INTERNATIONAL";
        }
        return tripType.trim().toUpperCase();
    }

    public TripResponse createTrip(CreateTripRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);

        if (request.getStartDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("Trip start date must be tomorrow or later for new trips.");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setDestination(request.getDestination().trim());
        trip.setSource(request.getSource() != null ? request.getSource().trim() : null);
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setTravelers(request.getTravelers() != null ? request.getTravelers() : 1);
        trip.setBudget(request.getBudget());
        trip.setTotalCost(request.getTotalCost() != null ? request.getTotalCost() : request.getBudget() * 0.9);
        trip.setTripType(normalizeTripType(request.getTripType()));
        trip.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim().toLowerCase() : "upcoming");
        trip.setInterests(request.getInterests());
        trip.setTitle(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle().trim() : request.getDestination().trim());
        trip.setImage(resolveImage(request.getImage(), request.getDestination()));
        trip.setItineraryJson(request.getItineraryJson());

        Trip saved = tripRepository.save(trip);
        return TripResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getUserTrips(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        return tripRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this trip.");
        }

        return TripResponse.fromEntity(trip);
    }

    public TripResponse updateTrip(Long id, UpdateTripRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this trip.");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : trip.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : trip.getEndDate();

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        if (request.getDestination() != null && !request.getDestination().isBlank()) {
            trip.setDestination(request.getDestination().trim());
        }
        if (request.getSource() != null) {
            trip.setSource(request.getSource().trim());
        }
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        if (request.getTravelers() != null) {
            trip.setTravelers(request.getTravelers());
        }
        if (request.getBudget() != null) {
            trip.setBudget(request.getBudget());
        }
        if (request.getTotalCost() != null) {
            trip.setTotalCost(request.getTotalCost());
        }
        if (request.getTripType() != null && !request.getTripType().isBlank()) {
            trip.setTripType(normalizeTripType(request.getTripType()));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            trip.setStatus(request.getStatus().trim().toLowerCase());
        }
        if (request.getInterests() != null) {
            trip.setInterests(request.getInterests());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            trip.setTitle(request.getTitle().trim());
        }
        if (request.getImage() != null && !request.getImage().isBlank()) {
            trip.setImage(request.getImage());
        }
        if (request.getItineraryJson() != null) {
            trip.setItineraryJson(request.getItineraryJson());
        }

        Trip updated = tripRepository.save(trip);
        return TripResponse.fromEntity(updated);
    }

    public void deleteTrip(Long id, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));

        if (!trip.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this trip.");
        }

        tripRepository.delete(trip);
    }
}

package com.voyaai.backend.service;

import com.voyaai.backend.dto.*;
import com.voyaai.backend.entity.Itinerary;
import com.voyaai.backend.entity.ItineraryDay;
import com.voyaai.backend.entity.Trip;
import com.voyaai.backend.exception.ResourceNotFoundException;
import com.voyaai.backend.repository.ItineraryRepository;
import com.voyaai.backend.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TripOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(TripOptimizationService.class);

    private final TripRepository tripRepository;
    private final ItineraryRepository itineraryRepository;
    private final MlCostPredictionService mlCostPredictionService;
    private final WeatherService weatherService;

    public TripOptimizationService(TripRepository tripRepository,
                                   ItineraryRepository itineraryRepository,
                                   MlCostPredictionService mlCostPredictionService,
                                   WeatherService weatherService) {
        this.tripRepository = tripRepository;
        this.itineraryRepository = itineraryRepository;
        this.mlCostPredictionService = mlCostPredictionService;
        this.weatherService = weatherService;
    }

    private static class CandidateTemplate {
        final String title;
        final String transport;
        final String accommodation;
        final double activityFactor;
        final double baseComfort;
        final String activityFocus;
        final List<String> highlights;

        CandidateTemplate(String title, String transport, String accommodation,
                          double activityFactor, double baseComfort,
                          String activityFocus, List<String> highlights) {
            this.title = title;
            this.transport = transport;
            this.accommodation = accommodation;
            this.activityFactor = activityFactor;
            this.baseComfort = baseComfort;
            this.activityFocus = activityFocus;
            this.highlights = highlights;
        }
    }

    public TripOptimizationResponse optimizeTrip(Long tripId, TripOptimizationRequest request, String userEmail) {
        log.info("Starting trip optimization for trip ID={} by user='{}'", tripId, userEmail);

        if (request == null) {
            request = new TripOptimizationRequest();
        }

        // 1. Retrieve trip and strictly authorize ownership
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (trip.getUser() == null || !trip.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            log.warn("Forbidden optimization attempt on trip ID={} by user='{}'", tripId, userEmail);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access or optimize this trip.");
        }

        // 2. Extract and preserve hard constraints from original trip
        String destination = trip.getDestination();
        int travelers = (trip.getTravelers() != null && trip.getTravelers() > 0) ? trip.getTravelers() : 1;
        String tripType = (trip.getTripType() != null && !trip.getTripType().isBlank())
                ? trip.getTripType().trim().toUpperCase()
                : "NATIONAL";
        if (tripType.equals("DOMESTIC")) {
            tripType = "NATIONAL";
        }

        int durationDays = 1;
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
            durationDays = Math.max(1, (int) days);
        }

        double userBudget = trip.getBudget() != null ? trip.getBudget() : 0.0;
        int month = trip.getStartDate() != null ? trip.getStartDate().getMonthValue() : 6;
        List<String> userInterests = trip.getInterests() != null ? trip.getInterests() : Collections.emptyList();

        // 3. Inspect existing itinerary activities and categories
        int baseActivityCount = Math.max(2, durationDays * 2);
        Set<String> activityCategories = new HashSet<>();
        Optional<Itinerary> itineraryOpt = itineraryRepository.findByTrip(trip);
        if (itineraryOpt.isPresent()) {
            Itinerary itinerary = itineraryOpt.get();
            int totalActivities = 0;
            for (ItineraryDay day : itinerary.getDays()) {
                if (day.getActivities() != null) {
                    totalActivities += day.getActivities().size();
                    day.getActivities().forEach(act -> {
                        if (act.getCategory() != null && !act.getCategory().isBlank()) {
                            activityCategories.add(act.getCategory().trim().toLowerCase());
                        }
                    });
                }
            }
            if (totalActivities > 0) {
                baseActivityCount = totalActivities;
            }
        }

        // 4. Retrieve real weather if available for destination
        String weatherSummary = null;
        String weatherAdvisory = null;
        try {
            WeatherResponse weather = weatherService.getWeather(destination);
            if (weather != null && weather.getCondition() != null) {
                weatherSummary = weather.getCondition() + ", " + Math.round(weather.getTemperature()) + "°C";
                String condLower = weather.getCondition().toLowerCase();
                if (condLower.contains("rain") || condLower.contains("drizzle") || condLower.contains("thunderstorm") || condLower.contains("snow")) {
                    weatherAdvisory = "Destination weather indicates precipitation. Optimizer prioritizes sheltered cultural, culinary, and museum visits.";
                } else {
                    weatherAdvisory = "Favorable clear weather conditions. Optimizer favors outdoor sightseeing, walking exploration, and scenic views.";
                }
            }
        } catch (Exception ex) {
            log.info("Weather information unavailable for '{}': {}", destination, ex.getMessage());
        }

        // 5. Establish Baseline Cost via existing ML cost prediction service
        MlCostPredictionRequest baselinePayload = new MlCostPredictionRequest(
                destination,
                durationDays,
                travelers,
                userBudget,
                "INR",
                "Hotel",
                "Flight",
                tripType,
                month
        );
        MlCostPredictionResponse baselineResponse = mlCostPredictionService.predictCost(baselinePayload);
        double baselineCost = baselineResponse.getPredictedCost();

        // 6. Generate candidate configurations
        String mode = (request.getMode() != null && !request.getMode().isBlank())
                ? request.getMode().trim().toUpperCase()
                : "BALANCED";
        if (!mode.equals("BUDGET") && !mode.equals("EXPERIENCE")) {
            mode = "BALANCED";
        }

        List<CandidateTemplate> templates = getCandidateTemplates(tripType);

        // Apply soft constraints (preserve transport / accommodation if requested)
        if (Boolean.TRUE.equals(request.getPreserveTransport())) {
            templates = templates.stream()
                    .filter(t -> t.transport.equalsIgnoreCase("Flight"))
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(request.getPreserveAccommodation())) {
            templates = templates.stream()
                    .filter(t -> t.accommodation.equalsIgnoreCase("Hotel"))
                    .collect(Collectors.toList());
        }

        Double maxBudgetCap = request.getMaxBudget();
        boolean isStrictBudget = Boolean.TRUE.equals(request.getStrictBudget()) || maxBudgetCap != null;
        double effectiveBudgetLimit = maxBudgetCap != null ? maxBudgetCap : userBudget;

        List<OptimizedCandidateDto> evaluatedCandidates = new ArrayList<>();

        // 7. Evaluate each candidate through real ML service & compute transparent optimization scores
        for (CandidateTemplate tpl : templates) {
            MlCostPredictionRequest candidatePayload = new MlCostPredictionRequest(
                    destination,
                    durationDays,
                    travelers,
                    userBudget,
                    "INR",
                    tpl.accommodation,
                    tpl.transport,
                    tripType,
                    month
            );

            MlCostPredictionResponse mlPrediction = mlCostPredictionService.predictCost(candidatePayload);
            double estimatedCost = mlPrediction.getPredictedCost();

            // Filter out candidate if it violates strict budget hard constraint
            if (isStrictBudget && effectiveBudgetLimit > 0 && estimatedCost > effectiveBudgetLimit) {
                log.debug("Discarding candidate '{}' exceeding budget limit: {} > {}", tpl.title, estimatedCost, effectiveBudgetLimit);
                continue;
            }

            double estimatedSavings = Math.round((baselineCost - estimatedCost) * 100.0) / 100.0;
            int candidateActivityCount = Math.max(1, (int) Math.round(baseActivityCount * tpl.activityFactor));

            // Genuine interest matching
            List<String> matchedInterests = new ArrayList<>();
            String combinedText = (tpl.activityFocus + " " + String.join(" ", tpl.highlights)).toLowerCase();
            for (String interest : userInterests) {
                String intLower = interest.trim().toLowerCase();
                if (matchesInterest(intLower, combinedText) || activityCategories.contains(intLower)) {
                    matchedInterests.add(interest);
                }
            }

            double interestMatchScore;
            if (userInterests.isEmpty()) {
                interestMatchScore = 0.85; // neutral baseline when user entered no specific interests
            } else {
                interestMatchScore = Math.max(0.25, (double) matchedInterests.size() / userInterests.size());
            }
            interestMatchScore = Math.round(interestMatchScore * 100.0) / 100.0;

            // Transparent scoring formula
            double costEff;
            if (estimatedCost <= baselineCost) {
                double savingsRatio = (baselineCost - estimatedCost) / Math.max(1.0, 0.40 * baselineCost);
                costEff = 0.50 + 0.50 * Math.min(1.0, savingsRatio);
            } else {
                double excessRatio = (estimatedCost - baselineCost) / Math.max(1.0, 0.50 * baselineCost);
                costEff = Math.max(0.0, 0.50 - 0.50 * excessRatio);
            }

            double comfortEff = tpl.baseComfort;
            double interestEff = interestMatchScore;

            double rawScore;
            if (mode.equals("BUDGET")) {
                rawScore = 0.60 * costEff + 0.25 * interestEff + 0.15 * comfortEff;
            } else if (mode.equals("EXPERIENCE")) {
                rawScore = 0.15 * costEff + 0.40 * interestEff + 0.45 * comfortEff;
            } else { // BALANCED
                rawScore = 0.35 * costEff + 0.35 * interestEff + 0.30 * comfortEff;
            }

            double optimizationScore = Math.round(rawScore * 1000.0) / 10.0;

            String costDiffMessage;
            if (estimatedSavings > 50) {
                costDiffMessage = "Estimated savings of ₹" + Math.round(estimatedSavings) + " compared to base plan";
            } else if (estimatedSavings < -50) {
                costDiffMessage = "Estimated comfort addition of ₹" + Math.round(Math.abs(estimatedSavings)) + " over base plan";
            } else {
                costDiffMessage = "Matches base plan ML cost estimation";
            }

            OptimizedCandidateDto candidateDto = new OptimizedCandidateDto(
                    0, // will be ranked subsequently
                    tpl.title,
                    mode,
                    tpl.transport,
                    tpl.accommodation,
                    candidateActivityCount,
                    tpl.activityFocus,
                    estimatedCost,
                    "INR",
                    estimatedSavings,
                    interestMatchScore,
                    matchedInterests,
                    tpl.baseComfort,
                    optimizationScore,
                    tpl.highlights,
                    weatherAdvisory,
                    costDiffMessage
            );

            evaluatedCandidates.add(candidateDto);
        }

        // 8. Rank candidates descending by optimization score
        evaluatedCandidates.sort((a, b) -> Double.compare(b.getOptimizationScore(), a.getOptimizationScore()));

        // Select top distinct candidates (maximum 3 options)
        List<OptimizedCandidateDto> finalResults = new ArrayList<>();
        int rank = 1;
        for (OptimizedCandidateDto c : evaluatedCandidates) {
            c.setRank(rank++);
            finalResults.add(c);
            if (finalResults.size() >= 3) {
                break;
            }
        }

        String returnMessage;
        if (finalResults.isEmpty()) {
            returnMessage = "No feasible option was found within your current constraints. Try increasing your budget or relaxing a preference.";
        } else {
            returnMessage = String.format("Found %d optimized trip configuration%s in %s mode.",
                    finalResults.size(), finalResults.size() > 1 ? "s" : "", mode);
        }

        log.info("Optimization completed for trip ID={}. Generated {} ranked candidates.", tripId, finalResults.size());

        return new TripOptimizationResponse(
                tripId,
                destination,
                tripType,
                mode,
                baselineCost,
                "INR",
                weatherSummary,
                finalResults,
                returnMessage
        );
    }

    private List<CandidateTemplate> getCandidateTemplates(String tripType) {
        List<CandidateTemplate> templates = new ArrayList<>();

        if ("INTERNATIONAL".equalsIgnoreCase(tripType)) {
            templates.add(new CandidateTemplate(
                    "Smart International Explorer",
                    "Flight",
                    "Hostel",
                    0.75,
                    0.55,
                    "Essential city landmarks, public transit, and vibrant street markets",
                    List.of(
                            "International flight connection paired with central hostel stay",
                            "Focuses on free architecture, public parks, and neighborhood discoveries",
                            "Significantly reduces nightly accommodation overhead while preserving key sights"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Value City Voyager",
                    "Flight",
                    "Guesthouse",
                    0.90,
                    0.68,
                    "Cultural walking tours, local dining gems, and neighborhood heritage",
                    List.of(
                            "Authentic boutique guesthouse stay for greater privacy and charm",
                            "Well-paced itinerary balancing premier landmarks with local cuisine",
                            "Moderate spending efficiency without high-tier hotel premiums"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Balanced International Journey",
                    "Flight",
                    "Hotel",
                    1.0,
                    0.85,
                    "Premier attractions, verified cultural museums, and curated dining",
                    List.of(
                            "Direct flight transit and dependable mid-scale hotel comfort",
                            "Comprehensive itinerary covering all verified destination highlights",
                            "Optimal equilibrium between convenience, safety, and sightseeing depth"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Resort & Scenic Retreat",
                    "Flight",
                    "Resort",
                    1.0,
                    0.93,
                    "Coastal excursions, scenic landscape tours, and wellness relaxation",
                    List.of(
                            "Scenic resort stay with expansive grounds and relaxation amenities",
                            "Combines structured daytime touring with dedicated leisure afternoons",
                            "Elevated comfort tailored to scenic and relaxation travel interests"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Signature Luxury Experience",
                    "Flight",
                    "Luxury Hotel",
                    1.25,
                    1.0,
                    "Private tours, premier heritage monuments, and fine dining",
                    List.of(
                            "Five-star hospitality with dedicated concierge and luxury amenities",
                            "Expanded itinerary with exclusive access and premium guided tours",
                            "Top-tier comfort and memorable dining experiences throughout"
                    )
            ));
        } else {
            // NATIONAL TRIPS
            templates.add(new CandidateTemplate(
                    "Budget Saver (Scenic Rail & Hostel)",
                    "Train",
                    "Hostel",
                    0.70,
                    0.45,
                    "Self-guided walks, scenic vistas, and authentic street food",
                    List.of(
                            "Economical rail transport significantly lowers intercity travel spend",
                            "Hostel accommodation keeps nightly lodging costs minimal",
                            "Curated focus on public beaches, historic streets, and authentic local food"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Value Route (Train & Cozy Guesthouse)",
                    "Train",
                    "Guesthouse",
                    0.85,
                    0.58,
                    "Neighborhood exploration, regional dining, and local heritage sights",
                    List.of(
                            "Scenic train journey paired with hospitable family-run guesthouse",
                            "Steady itinerary pace without surge pricing",
                            "Balanced mix of cultural sights, markets, and regional delicacies"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Road Explorer (Car & Quality Hotel)",
                    "Car",
                    "Hotel",
                    1.0,
                    0.75,
                    "Flexible regional sightseeing, scenic detours, and handpicked restaurants",
                    List.of(
                            "Private road travel provides complete route flexibility and door-to-door convenience",
                            "Standard hotel lodging with reliable hospitality and amenities",
                            "Comprehensive itinerary covering city landmarks and nearby scenic spots"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Balanced Express (Flight & 3-Star Hotel)",
                    "Flight",
                    "Hotel",
                    1.0,
                    0.85,
                    "City exploration, premier heritage monuments, and popular dining",
                    List.of(
                            "Fast flight travel saves hours of transit time",
                            "Centrally located hotel for convenient access to top attractions",
                            "Well-rounded itinerary covering all core trip interests"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Leisure Comfort (Flight & Premium Resort)",
                    "Flight",
                    "Resort",
                    1.0,
                    0.92,
                    "Scenic relaxation, beach/nature leisure spots, cultural sights, and signature regional dining",
                    List.of(
                            "Flight convenience coupled with rejuvenating resort stay near scenic beaches and nature",
                            "Spacious property amenities with pool, wellness leisure facilities, and authentic food",
                            "Unrushed itinerary harmonizing cultural excursions, dining, and downtime"
                    )
            ));

            templates.add(new CandidateTemplate(
                    "Luxury Experience (Flight & 5-Star Hotel)",
                    "Flight",
                    "Luxury Hotel",
                    1.25,
                    1.0,
                    "Private tours, premier heritage monuments, culture, and gourmet fine dining food experiences",
                    List.of(
                            "Full flight comfort with five-star luxury hotel accommodation",
                            "Elevated comfort standard with top hospitality and culinary dining",
                            "Expanded activity schedule covering premier cultural experiences, beaches, and fine food"
                    )
            ));
        }

        return templates;
    }

    private boolean matchesInterest(String interest, String text) {
        if (text == null || interest == null) return false;
        String intLower = interest.trim().toLowerCase();
        if (text.contains(intLower)) return true;
        if (intLower.equals("food") && (text.contains("dining") || text.contains("cuisine") || text.contains("culinary") || text.contains("restaurant") || text.contains("delicacies"))) return true;
        if (intLower.equals("culture") && (text.contains("heritage") || text.contains("monument") || text.contains("historic") || text.contains("museum"))) return true;
        if (intLower.equals("nature") && (text.contains("scenic") || text.contains("landscape") || text.contains("greenery") || text.contains("vista"))) return true;
        if (intLower.equals("beach") && (text.contains("coastal") || text.contains("sea") || text.contains("ocean") || text.contains("shore"))) return true;
        if (intLower.equals("relaxation") && (text.contains("leisure") || text.contains("wellness") || text.contains("retreat") || text.contains("resort"))) return true;
        return false;
    }
}

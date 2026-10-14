package com.voyaai.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.voyaai.backend.dto.*;
import com.voyaai.backend.entity.Trip;
import com.voyaai.backend.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.provider:gemini}")
    private String provider;

    @Value("${ai.gemini.api-key:${ai.api-key:${GEMINI_API_KEY:${AI_API_KEY:}}}}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com}")
    private String geminiBaseUrl;

    @Value("${ai.openai.api-key:${ai.api-key:${OPENAI_API_KEY:${AI_API_KEY:}}}}")
    private String openaiApiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openaiModel;

    @Value("${ai.openai.base-url:https://api.openai.com}")
    private String openaiBaseUrl;

    private static final Set<String> GENERIC_ACTIVITY_TITLES = Set.of(
            "morning exploration", "exploration in", "local culinary experience", "culinary experience",
            "afternoon cultural walk", "cultural walk", "sunset & evening dinner", "sunset views",
            "evening dining", "afternoon walk", "morning tour", "heritage walk", "explore ",
            "dinner", "lunch", "sightseeing", "arrival & welcome", "sunset dinner"
    );

    public AiService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public static class RawItineraryActivity {
        public String time;
        public String activity;
        public String description;
        public String location;
        public String address;
        public Double latitude;
        public Double longitude;
        public String category;
        public Double estimatedCost;
        public String duration;
        public String notes;
        public String websiteUrl;
        public String travelFromPrevious;
    }

    public static class RawItineraryDay {
        public Integer dayNumber;
        public LocalDate date;
        public String title;
        public String summary;
        public List<RawItineraryActivity> activities = new ArrayList<>();
    }

    public static class RawItinerary {
        public String title;
        public String summary;
        public List<RawItineraryDay> days = new ArrayList<>();
    }

    public RawItinerary generateItinerary(Trip trip, WeatherResponse weather, PlacesResponse places,
                                         GenerateItineraryRequest request) {
        log.info("Generating AI itinerary for trip ID={} (destination='{}', provider='{}')",
                trip.getId(), trip.getDestination(), provider);

        boolean isConfigured = "openai".equalsIgnoreCase(provider)
                ? (openaiApiKey != null && !openaiApiKey.isBlank() && !openaiApiKey.startsWith("test_"))
                : (geminiApiKey != null && !geminiApiKey.isBlank() && !geminiApiKey.startsWith("test_"));

        if (isConfigured) {
            try {
                String systemPrompt = buildSystemPrompt();
                String userPrompt = buildUserPrompt(trip, weather, places, request);

                String rawResponseText;
                if ("openai".equalsIgnoreCase(provider)) {
                    rawResponseText = callOpenAi(systemPrompt, userPrompt);
                } else {
                    rawResponseText = callGemini(systemPrompt, userPrompt);
                }

                return parseAndValidateItinerary(rawResponseText, trip, places);
            } catch (Exception ex) {
                log.warn("External AI call failed ({}). Generating real-places structured itinerary for trip ID={}",
                        ex.getMessage(), trip.getId());
                return generateIntelligentFallbackItinerary(trip, weather, places, request);
            }
        }

        log.info("External AI key not configured or using test placeholder. Using real-places itinerary engine for trip ID={}",
                trip.getId());
        return generateIntelligentFallbackItinerary(trip, weather, places, request);
    }

    private String buildSystemPrompt() {
        return "You are the expert itinerary planning engine for VoyaAI travel platform.\n"
                + "Generate a practical, realistic, and personalized travel itinerary based STRICTLY on the supplied trip information and travel context.\n"
                + "MANDATORY RULES FOR REAL-PLACE SELECTION:\n"
                + "1. EVERY activity title ('activity' field) MUST be the exact name of a real attraction, landmark, museum, restaurant, market, or waterfront venue in the selected destination.\n"
                + "2. NEVER output generic placeholders. Under NO circumstances should activity titles like 'Morning Exploration in Dubai', 'Local Culinary Experience', 'Afternoon Cultural Walk', 'Sunset & Evening Dinner', 'Morning Tour', 'Sightseeing Walk', or 'Dinner' be generated.\n"
                + "3. ZERO DUPLICATE ATTRACTIONS ACROSS DAYS: Each day must feature distinct places. An attraction or venue must not be repeated across multiple days.\n"
                + "4. GEOGRAPHIC REALISM: Group nearby attractions together in the same day (e.g. Downtown one day, Old Town another day, Coast/Marina another day).\n"
                + "5. Output MUST be a single, valid JSON object with NO surrounding markdown backticks or commentary.\n"
                + "6. Schema:\n"
                + "   {\n"
                + "     \"title\": string,\n"
                + "     \"summary\": string,\n"
                + "     \"days\": [\n"
                + "       {\n"
                + "         \"dayNumber\": integer (1..N),\n"
                + "         \"date\": string (YYYY-MM-DD),\n"
                + "         \"title\": string (day theme e.g. 'Downtown Dubai & Modern Marvels'),\n"
                + "         \"summary\": string,\n"
                + "         \"activities\": [\n"
                + "           {\n"
                + "             \"time\": string (e.g. '09:30', '13:00', '15:30', '18:30'),\n"
                + "             \"activity\": string (EXACT REAL PLACE NAME e.g. 'Burj Khalifa', 'Louvre Museum', 'Charminar'),\n"
                + "             \"description\": string (place-specific historical or visitor details),\n"
                + "             \"location\": string,\n"
                + "             \"address\": string,\n"
                + "             \"latitude\": number,\n"
                + "             \"longitude\": number,\n"
                + "             \"category\": string (Sightseeing, Food, Culture, Nature, Transport, Relaxation, Shopping, or Adventure),\n"
                + "             \"estimatedCost\": number,\n"
                + "             \"duration\": string (e.g. '2 hours', '1.5 hours'),\n"
                + "             \"notes\": string,\n"
                + "             \"websiteUrl\": string,\n"
                + "             \"travelFromPrevious\": string (e.g. '🚗 ~15 mins from previous stop')\n"
                + "           }\n"
                + "         ]\n"
                + "       }\n"
                + "     ]\n"
                + "   }\n"
                + "7. Cover EVERY calendar day from start date to end date consecutively.\n"
                + "8. Use verified places from the provided list. Do NOT invent fictional places.";
    }

    private String buildUserPrompt(Trip trip, WeatherResponse weather, PlacesResponse places,
                                   GenerateItineraryRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("TRIP SPECIFICATIONS:\n");
        sb.append("- Destination: ").append(trip.getDestination()).append("\n");
        if (trip.getSource() != null && !trip.getSource().isBlank()) {
            sb.append("- Origin/Departure: ").append(trip.getSource()).append("\n");
        }
        sb.append("- Start Date: ").append(trip.getStartDate()).append("\n");
        sb.append("- End Date: ").append(trip.getEndDate()).append("\n");

        long numDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        sb.append("- Trip Duration: ").append(numDays).append(" day(s)\n");
        sb.append("- Number of Travelers: ").append(trip.getTravelers()).append("\n");
        sb.append("- Total Budget: ₹").append(trip.getBudget()).append("\n");
        sb.append("- Trip Type: ").append(trip.getTripType()).append("\n");

        if (trip.getInterests() != null && !trip.getInterests().isEmpty()) {
            sb.append("- User Interests: ").append(String.join(", ", trip.getInterests())).append("\n");
        }
        if (request != null && request.getFocus() != null && !request.getFocus().isBlank()) {
            sb.append("- Special Focus/Pace: ").append(request.getFocus()).append("\n");
        }
        if (request != null && request.getNotes() != null && !request.getNotes().isBlank()) {
            sb.append("- User Notes: ").append(request.getNotes()).append("\n");
        }

        // Weather Context
        sb.append("\nVERIFIED WEATHER CONTEXT:\n");
        if (weather != null) {
            sb.append("- Current Condition: ").append(weather.getCondition())
                    .append(" (").append(weather.getTemperature()).append("°C)\n");
            if (weather.getForecast() != null && !weather.getForecast().isEmpty()) {
                sb.append("- Upcoming Daily Forecast: ");
                for (ForecastDayDto f : weather.getForecast()) {
                    sb.append(f.getDay()).append(" (").append(f.getDate()).append("): ")
                            .append(f.getCondition()).append(" ").append(f.getLow())
                            .append("°C-").append(f.getHigh()).append("°C; ");
                }
                sb.append("\n");
            }
        }

        // Real Verified Places
        sb.append("\nVERIFIED REAL DESTINATION ATTRACTIONS & VENUES (Select activities ONLY from this pool):\n");
        if (places != null && places.getPlaces() != null && !places.getPlaces().isEmpty()) {
            for (PlaceItemDto p : places.getPlaces()) {
                sb.append("- Name: \"").append(p.getName()).append("\" | Category: ").append(p.getCategory());
                if (p.getAddress() != null) sb.append(" | Address: ").append(p.getAddress());
                if (p.getLatitude() != null && p.getLongitude() != null) {
                    sb.append(String.format(Locale.US, " | Coordinates: (%.4f, %.4f)", p.getLatitude(), p.getLongitude()));
                }
                if (p.getDescription() != null && !p.getDescription().isBlank()) {
                    sb.append(" | Details: ").append(p.getDescription());
                }
                if (p.getUrl() != null) sb.append(" | URL: ").append(p.getUrl());
                sb.append("\n");
            }
        }

        sb.append("\nGenerate the complete structured JSON itinerary now for exactly ")
                .append(numDays).append(" days. Use ONLY real places from the list above for activity titles.");
        return sb.toString();
    }

    private String callGemini(String systemPrompt, String userPrompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new AiServiceException("AI API key is not configured.", 400);
        }

        String url = geminiBaseUrl + "/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;

        try {
            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode contents = root.putArray("contents");
            ObjectNode contentObj = contents.addObject();
            ArrayNode parts = contentObj.putArray("parts");
            parts.addObject().put("text", systemPrompt + "\n\n" + userPrompt);

            ObjectNode genConfig = root.putObject("generationConfig");
            genConfig.put("responseMimeType", "application/json");
            genConfig.put("temperature", 0.4);

            String requestBody = objectMapper.writeValueAsString(root);

            String responseBody = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new AiServiceException("Gemini returned empty response.", 502);
            }

            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode candidates = responseJson.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new AiServiceException("Gemini returned no candidates.", 502);
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode textPart = firstCandidate.path("content").path("parts");
            if (textPart.isArray() && !textPart.isEmpty()) {
                return textPart.get(0).path("text").asString("");
            }

            throw new AiServiceException("Gemini response missing text parts.", 502);
        } catch (RestClientResponseException ex) {
            String errDetail = extractErrorMessage(ex.getResponseBodyAsString());
            log.error("Gemini API call failed with status {}: {}", ex.getStatusCode().value(), errDetail);
            throw new AiServiceException("Gemini API failed: " + errDetail, ex.getStatusCode().value());
        } catch (RestClientException ex) {
            log.error("Network or timeout error calling Gemini: {}", ex.getMessage());
            throw new AiServiceException("Failed to connect to Gemini API: " + ex.getMessage(), 504);
        } catch (Exception ex) {
            log.error("Unexpected error in callGemini: ", ex);
            throw new AiServiceException("Gemini invocation failed: " + ex.getMessage(), 500);
        }
    }

    private String callOpenAi(String systemPrompt, String userPrompt) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new AiServiceException("OpenAI API key is not configured.", 400);
        }

        String url = openaiBaseUrl + "/v1/chat/completions";

        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", openaiModel);
            root.put("temperature", 0.4);

            ArrayNode messages = root.putArray("messages");
            ObjectNode sysMsg = messages.addObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);

            ObjectNode usrMsg = messages.addObject();
            usrMsg.put("role", "user");
            usrMsg.put("content", userPrompt);

            ObjectNode respFormat = root.putObject("response_format");
            respFormat.put("type", "json_object");

            String requestBody = objectMapper.writeValueAsString(root);

            String responseBody = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + openaiApiKey.trim())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new AiServiceException("OpenAI returned empty response.", 502);
            }

            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode choices = responseJson.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiServiceException("OpenAI returned no choices.", 502);
            }

            return choices.get(0).path("message").path("content").asString("");
        } catch (RestClientResponseException ex) {
            String errDetail = extractErrorMessage(ex.getResponseBodyAsString());
            log.error("OpenAI API call failed with status {}: {}", ex.getStatusCode().value(), errDetail);
            throw new AiServiceException("OpenAI API failed: " + errDetail, ex.getStatusCode().value());
        } catch (RestClientException ex) {
            log.error("Network or timeout error calling OpenAI: {}", ex.getMessage());
            throw new AiServiceException("Failed to connect to OpenAI API: " + ex.getMessage(), 504);
        } catch (Exception ex) {
            log.error("Unexpected error in callOpenAi: ", ex);
            throw new AiServiceException("OpenAI invocation failed: " + ex.getMessage(), 500);
        }
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null) return "Unknown error";
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.has("error")) {
                JsonNode err = node.get("error");
                if (err.isString()) return err.asString();
                if (err.has("message")) return err.get("message").asString();
            }
        } catch (Exception ignored) {
        }
        return responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
    }

    public RawItinerary parseAndValidateItinerary(String jsonText, Trip trip, PlacesResponse places) {
        if (jsonText == null || jsonText.isBlank()) {
            throw new AiServiceException("AI response text is null or blank.", 502);
        }

        String cleaned = jsonText.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        JsonNode root;
        try {
            root = objectMapper.readTree(cleaned);
        } catch (Exception ex) {
            log.error("Failed to parse JSON response from AI: {}", ex.getMessage());
            throw new AiServiceException("Failed to parse AI output as JSON: " + ex.getMessage(), 502);
        }

        String title = root.path("title").asString("").trim();
        if (title.isBlank()) {
            title = trip.getDestination() + " Verified Experience";
        }

        String summary = root.path("summary").asString("").trim();
        if (summary.isBlank()) {
            summary = "Tailored itinerary for " + trip.getDestination() + " featuring verified destination attractions.";
        }

        JsonNode daysNode = root.path("days");
        if (!daysNode.isArray() || daysNode.isEmpty()) {
            throw new AiServiceException("AI output did not contain valid 'days' array.", 502);
        }

        // Build place lookup map
        Map<String, PlaceItemDto> placeMap = new HashMap<>();
        List<PlaceItemDto> availablePlaces = (places != null && places.getPlaces() != null)
                ? new ArrayList<>(places.getPlaces())
                : new ArrayList<>();

        for (PlaceItemDto p : availablePlaces) {
            placeMap.put(p.getName().toLowerCase().trim(), p);
        }

        RawItinerary result = new RawItinerary();
        result.title = title;
        result.summary = summary;

        LocalDate curDate = trip.getStartDate();
        Set<String> usedPlaceNames = new HashSet<>();

        for (int i = 0; i < daysNode.size(); i++) {
            JsonNode dayNode = daysNode.get(i);
            RawItineraryDay day = new RawItineraryDay();
            day.dayNumber = dayNode.path("dayNumber").asInt(i + 1);

            String dateStr = dayNode.path("date").asString("").trim();
            if (!dateStr.isBlank()) {
                try {
                    day.date = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    day.date = curDate.plusDays(i);
                }
            } else {
                day.date = curDate.plusDays(i);
            }

            day.title = dayNode.path("title").asString("").trim();
            if (day.title.isBlank()) {
                day.title = "Day " + day.dayNumber + " in " + trip.getDestination();
            }

            day.summary = dayNode.path("summary").asString("").trim();

            Double prevLat = null;
            Double prevLon = null;

            JsonNode actsNode = dayNode.path("activities");
            if (actsNode.isArray()) {
                for (JsonNode actNode : actsNode) {
                    RawItineraryActivity act = new RawItineraryActivity();
                    act.time = actNode.path("time").asString("09:30").trim();
                    String actTitle = actNode.path("activity").asString("").trim();
                    if (actTitle.isBlank()) {
                        actTitle = actNode.path("name").asString("").trim();
                    }

                    // Check if AI generated a generic placeholder or repeated a place
                    boolean isGeneric = isGenericActivity(actTitle);
                    String normalizedTitle = actTitle.toLowerCase();
                    boolean isDuplicate = usedPlaceNames.contains(normalizedTitle);

                    if (isGeneric || isDuplicate || !isRealPlace(actTitle, placeMap)) {
                        // Replace with an unused verified real place from destination pool
                        PlaceItemDto replacement = findNextUnusedPlace(availablePlaces, usedPlaceNames);
                        if (replacement != null) {
                            act.activity = replacement.getName();
                            act.description = (replacement.getDescription() != null && !replacement.getDescription().isBlank())
                                    ? replacement.getDescription()
                                    : "Explore " + replacement.getName() + " in " + trip.getDestination() + ".";
                            act.location = (replacement.getAddress() != null && !replacement.getAddress().isBlank())
                                    ? replacement.getAddress() : trip.getDestination();
                            act.address = replacement.getAddress();
                            act.category = replacement.getCategory();
                            act.latitude = replacement.getLatitude();
                            act.longitude = replacement.getLongitude();
                            act.websiteUrl = replacement.getUrl();
                            act.duration = replacement.getDurationMinutes() != null
                                    ? formatDuration(replacement.getDurationMinutes()) : "2 hours";
                            act.estimatedCost = replacement.getEstimatedCost() != null ? replacement.getEstimatedCost() : 0.0;
                            usedPlaceNames.add(replacement.getName().toLowerCase().trim());
                        } else {
                            act.activity = actTitle;
                            act.description = actNode.path("description").asString("").trim();
                            act.location = actNode.path("location").asString(trip.getDestination()).trim();
                            act.category = actNode.path("category").asString("Sightseeing").trim();
                            act.duration = actNode.path("duration").asString("2 hours").trim();
                            act.estimatedCost = actNode.path("estimatedCost").asDouble(0.0);
                        }
                    } else {
                        act.activity = actTitle;
                        act.description = actNode.path("description").asString("").trim();
                        act.location = actNode.path("location").asString(trip.getDestination()).trim();
                        act.category = actNode.path("category").asString("Sightseeing").trim();
                        act.duration = actNode.path("duration").asString("2 hours").trim();
                        act.estimatedCost = actNode.path("estimatedCost").asDouble(0.0);
                        act.notes = actNode.path("notes").asString("").trim();

                        // Enrich with verified coordinates and links if known
                        PlaceItemDto matched = placeMap.get(normalizedTitle);
                        if (matched != null) {
                            act.address = matched.getAddress();
                            act.latitude = matched.getLatitude();
                            act.longitude = matched.getLongitude();
                            act.websiteUrl = matched.getUrl();
                        }
                        usedPlaceNames.add(normalizedTitle);
                    }

                    // Calculate travel time and distance from previous activity
                    if (prevLat != null && prevLon != null && act.latitude != null && act.longitude != null) {
                        act.travelFromPrevious = calculateTravelDetails(prevLat, prevLon, act.latitude, act.longitude);
                    } else if (prevLat != null) {
                        act.travelFromPrevious = "🚗 ~15 mins transit from previous stop";
                    } else {
                        act.travelFromPrevious = null; // First activity of the day
                    }

                    if (act.latitude != null && act.longitude != null) {
                        prevLat = act.latitude;
                        prevLon = act.longitude;
                    }

                    day.activities.add(act);
                }
            }

            result.days.add(day);
        }

        return result;
    }

    private boolean isGenericActivity(String title) {
        if (title == null || title.isBlank()) return true;
        String lower = title.toLowerCase().trim();
        for (String pattern : GENERIC_ACTIVITY_TITLES) {
            if (lower.contains(pattern) || pattern.contains(lower)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRealPlace(String title, Map<String, PlaceItemDto> placeMap) {
        if (title == null || title.isBlank()) return false;
        String lower = title.toLowerCase().trim();
        if (placeMap.containsKey(lower)) return true;
        for (String key : placeMap.keySet()) {
            if (lower.contains(key) || key.contains(lower)) return true;
        }
        return false;
    }

    private PlaceItemDto findNextUnusedPlace(List<PlaceItemDto> places, Set<String> usedPlaceNames) {
        if (places == null || places.isEmpty()) return null;
        for (PlaceItemDto p : places) {
            String lower = p.getName().toLowerCase().trim();
            if (!usedPlaceNames.contains(lower)) {
                return p;
            }
        }
        return null;
    }

    public RawItinerary generateIntelligentFallbackItinerary(Trip trip, WeatherResponse weather,
                                                            PlacesResponse places,
                                                            GenerateItineraryRequest request) {
        String dest = trip.getDestination();
        int travelers = trip.getTravelers() != null && trip.getTravelers() > 0 ? trip.getTravelers() : 1;
        double budget = trip.getBudget() != null && trip.getBudget() > 0 ? trip.getBudget() : 50000.0;
        long totalDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        if (totalDays < 1) totalDays = 1;
        if (totalDays > 30) totalDays = 30;

        RawItinerary result = new RawItinerary();
        result.title = dest + " Verified Experience & Exploration";
        result.summary = String.format("A bespoke %d-day itinerary for %d traveler(s) in %s featuring real landmarks, cultural sights, and dining venues.",
                totalDays, travelers, dest);

        List<PlaceItemDto> placeList = (places != null && places.getPlaces() != null)
                ? new ArrayList<>(places.getPlaces())
                : new ArrayList<>();

        // If places list is empty, fetch from CuratedDestinations
        if (placeList.isEmpty() && CuratedDestinations.hasCuratedPlaces(dest)) {
            placeList = CuratedDestinations.getCuratedPlaces(dest);
        }

        Set<String> usedPlaceNames = new HashSet<>();
        double dailyBudgetPerPerson = (budget / travelers) / totalDays;

        // Group places by cluster/theme if available
        Map<String, List<PlaceItemDto>> clusteredPlaces = new LinkedHashMap<>();
        for (PlaceItemDto p : placeList) {
            String cluster = (p.getClusterArea() != null && !p.getClusterArea().isBlank())
                    ? p.getClusterArea() : "City Highlights";
            clusteredPlaces.computeIfAbsent(cluster, k -> new ArrayList<>()).add(p);
        }

        List<String> clusters = new ArrayList<>(clusteredPlaces.keySet());

        for (int dayNum = 1; dayNum <= totalDays; dayNum++) {
            RawItineraryDay day = new RawItineraryDay();
            day.dayNumber = dayNum;
            day.date = trip.getStartDate().plusDays(dayNum - 1);

            String clusterTheme = clusters.isEmpty() ? dest + " Highlights" : clusters.get((dayNum - 1) % clusters.size());

            String weatherSnippet = "";
            if (weather != null && weather.getForecast() != null && dayNum - 1 < weather.getForecast().size()) {
                ForecastDayDto f = weather.getForecast().get(dayNum - 1);
                weatherSnippet = " Expected weather: " + f.getCondition() + " (High " + f.getHigh() + "°C).";
            } else if (weather != null && weather.getCondition() != null) {
                weatherSnippet = " Weather forecast: " + weather.getCondition() + " (" + weather.getTemperature() + "°C).";
            }

            day.title = clusterTheme + " (Day " + dayNum + ")";
            day.summary = "Immerse in " + clusterTheme + " visiting iconic monuments, authentic food, and scenic highlights." + weatherSnippet;

            // Pick 3 to 4 distinct, real places for today
            int activitiesCount = (dayNum == 1 || dayNum == totalDays) ? 3 : 4;
            String[] times = activitiesCount == 3
                    ? new String[]{"10:00", "13:30", "16:30"}
                    : new String[]{"09:30", "12:30", "15:30", "18:30"};

            Double prevLat = null;
            Double prevLon = null;

            for (int actIdx = 0; actIdx < activitiesCount; actIdx++) {
                PlaceItemDto selectedPlace = null;

                // First try to find unused place in today's cluster
                if (clusteredPlaces.containsKey(clusterTheme)) {
                    for (PlaceItemDto cp : clusteredPlaces.get(clusterTheme)) {
                        if (!usedPlaceNames.contains(cp.getName().toLowerCase().trim())) {
                            selectedPlace = cp;
                            break;
                        }
                    }
                }

                // If none left in cluster, find from overall placeList
                if (selectedPlace == null) {
                    for (PlaceItemDto p : placeList) {
                        if (!usedPlaceNames.contains(p.getName().toLowerCase().trim())) {
                            selectedPlace = p;
                            break;
                        }
                    }
                }

                RawItineraryActivity act = new RawItineraryActivity();
                act.time = times[actIdx];

                if (selectedPlace != null) {
                    usedPlaceNames.add(selectedPlace.getName().toLowerCase().trim());
                    act.activity = selectedPlace.getName();
                    act.description = (selectedPlace.getDescription() != null && !selectedPlace.getDescription().isBlank())
                            ? selectedPlace.getDescription()
                            : "Visit " + selectedPlace.getName() + ", a celebrated point of interest in " + dest + ".";
                    act.location = (selectedPlace.getAddress() != null && !selectedPlace.getAddress().isBlank())
                            ? selectedPlace.getAddress() : dest;
                    act.address = selectedPlace.getAddress();
                    act.category = (selectedPlace.getCategory() != null && !selectedPlace.getCategory().isBlank())
                            ? selectedPlace.getCategory() : "Sightseeing";
                    act.latitude = selectedPlace.getLatitude();
                    act.longitude = selectedPlace.getLongitude();
                    act.websiteUrl = selectedPlace.getUrl();
                    act.duration = selectedPlace.getDurationMinutes() != null
                            ? formatDuration(selectedPlace.getDurationMinutes()) : "2 hours";
                    act.estimatedCost = selectedPlace.getEstimatedCost() != null
                            ? selectedPlace.getEstimatedCost()
                            : Math.round(dailyBudgetPerPerson * 0.15 * 10.0) / 10.0;
                    act.notes = "Best experienced with advance planning and comfortable footwear.";
                } else {
                    // All places in placeList have been uniquely visited
                    act.activity = dest + " Waterfront & Leisure Promenade";
                    act.description = "Relaxed stroll along local shopping arcades, garden walkways, and tea houses in " + dest + ".";
                    act.location = dest;
                    act.category = "Relaxation";
                    act.duration = "2 hours";
                    act.estimatedCost = Math.round(dailyBudgetPerPerson * 0.10 * 10.0) / 10.0;
                    act.notes = "Free time allocated as all top verified attractions have been scheduled.";
                }

                // Calculate travel time and transit details
                if (prevLat != null && prevLon != null && act.latitude != null && act.longitude != null) {
                    act.travelFromPrevious = calculateTravelDetails(prevLat, prevLon, act.latitude, act.longitude);
                } else if (actIdx > 0) {
                    act.travelFromPrevious = "🚗 ~15 mins transit from previous stop";
                } else {
                    act.travelFromPrevious = null;
                }

                if (act.latitude != null && act.longitude != null) {
                    prevLat = act.latitude;
                    prevLon = act.longitude;
                }

                day.activities.add(act);
            }

            result.days.add(day);
        }

        return result;
    }

    public static String calculateTravelDetails(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return "🚗 ~15 mins transit from previous stop";
        }
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = 6371 * c;

        int minutes = Math.max(8, (int) Math.round(distanceKm * 3.0 + 5));
        return String.format(Locale.US, "🚗 ~%d mins (%.1f km) from previous stop", minutes, distanceKm);
    }

    private static String formatDuration(int minutes) {
        if (minutes < 60) return minutes + " mins";
        double hours = minutes / 60.0;
        if (hours == Math.floor(hours)) {
            return (int) hours + " hours";
        }
        return String.format(Locale.US, "%.1f hours", hours);
    }
}

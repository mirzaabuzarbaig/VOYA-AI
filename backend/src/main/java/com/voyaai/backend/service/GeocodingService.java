package com.voyaai.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.voyaai.backend.dto.GeocodingResult;
import com.voyaai.backend.dto.LocationSuggestionDto;
import com.voyaai.backend.exception.DestinationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // In-memory cache for resolved geocoding results
    private final Map<String, GeocodingResult> geocodingCache = new ConcurrentHashMap<>();

    // In-memory cache for location suggestions (1-hour TTL)
    private static class SuggestionCacheEntry {
        final List<LocationSuggestionDto> data;
        final long expiresAt;

        SuggestionCacheEntry(List<LocationSuggestionDto> data, long ttlMillis) {
            this.data = data;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, SuggestionCacheEntry> suggestionCache = new ConcurrentHashMap<>();
    private static final long SUGGESTION_TTL_MS = 60 * 60 * 1000L;

    public GeocodingService(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Searches for location suggestions matching a user's autocomplete query.
     * Uses Open-Meteo Geocoding API with fallback to OpenStreetMap Nominatim.
     */
    public List<LocationSuggestionDto> searchLocations(String rawQuery, Integer limit, String tripType) {
        if (rawQuery == null || rawQuery.trim().length() < 2) {
            return Collections.emptyList();
        }

        String query = rawQuery.trim();
        int maxCount = (limit != null && limit > 0 && limit <= 20) ? limit : 8;
        String cacheKey = (query + "|" + (tripType != null ? tripType.trim() : "") + "|" + maxCount).toLowerCase();

        SuggestionCacheEntry cached = suggestionCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        List<LocationSuggestionDto> results = searchOpenMeteoLocations(query, maxCount, tripType);
        if (results.isEmpty()) {
            results = searchNominatimLocations(query, maxCount, tripType);
        }

        // Ensure key destination queries resolve immediately with canonical Indian locations
        if (query.equalsIgnoreCase("goa")) {
            results.add(0, new LocationSuggestionDto("in-goa-state", "Goa", "Goa", "Goa", "India", "IN", 15.2993, 74.1240, "Asia/Kolkata", "Goa, India", "domestic"));
        } else if (query.equalsIgnoreCase("ooty") || query.equalsIgnoreCase("udhagamandalam")) {
            results.add(0, new LocationSuggestionDto("in-ooty-city", "Ooty", "Ooty", "Tamil Nadu", "India", "IN", 11.4134, 76.6952, "Asia/Kolkata", "Ooty, Tamil Nadu, India", "domestic"));
        } else if (query.equalsIgnoreCase("kerala")) {
            results.add(0, new LocationSuggestionDto("in-kerala-state", "Kerala", "Munnar & Alleppey", "Kerala", "India", "IN", 9.9312, 76.2673, "Asia/Kolkata", "Kerala (Munnar & Alleppey), India", "domestic"));
        }

        // Prioritize by tripType if requested
        if ("domestic".equalsIgnoreCase(tripType) || "national".equalsIgnoreCase(tripType)) {
            results.sort((a, b) -> {
                boolean aDom = "domestic".equalsIgnoreCase(a.getTripType());
                boolean bDom = "domestic".equalsIgnoreCase(b.getTripType());
                return Boolean.compare(!aDom, !bDom);
            });
        } else if ("international".equalsIgnoreCase(tripType)) {
            results.sort((a, b) -> {
                boolean aInt = "international".equalsIgnoreCase(a.getTripType());
                boolean bInt = "international".equalsIgnoreCase(b.getTripType());
                return Boolean.compare(!aInt, !bInt);
            });
        }

        suggestionCache.put(cacheKey, new SuggestionCacheEntry(results, SUGGESTION_TTL_MS));
        return results;
    }

    private List<LocationSuggestionDto> searchOpenMeteoLocations(String query, int count, String tripType) {
        List<LocationSuggestionDto> list = new ArrayList<>();
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://geocoding-api.open-meteo.com/v1/search")
                    .queryParam("name", query)
                    .queryParam("count", count)
                    .queryParam("language", "en")
                    .queryParam("format", "json")
                    .build()
                    .toUri();

            String responseBody = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return list;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("results");
            if (!results.isArray() || results.isEmpty()) {
                return list;
            }

            for (JsonNode node : results) {
                String id = String.valueOf(node.path("id").asLong(node.hashCode()));
                String name = node.path("name").asString("").trim();
                String admin1 = node.path("admin1").asString("").trim();
                String country = node.path("country").asString("").trim();
                String countryCode = node.path("country_code").asString("").trim().toUpperCase();
                double lat = node.path("latitude").asDouble();
                double lon = node.path("longitude").asDouble();
                String tz = node.path("timezone").asString("UTC");

                if (name.isBlank()) continue;

                // Build context-rich display name to distinguish same-name locations (e.g. Paris, France vs Paris, Texas)
                StringBuilder display = new StringBuilder(name);
                if (!admin1.isBlank() && !admin1.equalsIgnoreCase(name)) {
                    display.append(", ").append(admin1);
                }
                if (!country.isBlank()) {
                    display.append(", ").append(country);
                }

                String type = (countryCode.equals("IN") || country.equalsIgnoreCase("India"))
                        ? "domestic" : "international";

                list.add(new LocationSuggestionDto(
                        id,
                        name,
                        name,
                        admin1,
                        country,
                        countryCode,
                        lat,
                        lon,
                        tz,
                        display.toString(),
                        type
                ));
            }
        } catch (Exception e) {
            log.warn("Open-Meteo search failed for '{}': {}", query, e.getMessage());
        }
        return list;
    }

    private List<LocationSuggestionDto> searchNominatimLocations(String query, int count, String tripType) {
        List<LocationSuggestionDto> list = new ArrayList<>();
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", count)
                    .queryParam("addressdetails", 1)
                    .build()
                    .toUri();

            String responseBody = restClient.get()
                    .uri(uri)
                    .header("User-Agent", "VoyaAITravelApp/1.0 (travel@voyaai.com)")
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return list;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            if (!root.isArray() || root.isEmpty()) {
                return list;
            }

            for (JsonNode item : root) {
                String id = item.path("place_id").asString(UUID.randomUUID().toString());
                double lat = Double.parseDouble(item.path("lat").asString("0"));
                double lon = Double.parseDouble(item.path("lon").asString("0"));
                String displayName = item.path("display_name").asString(query);

                JsonNode address = item.path("address");
                String country = address.path("country").asString("");
                String countryCode = address.path("country_code").asString("").toUpperCase();
                String state = address.path("state").asString("");
                String name = address.has("city") ? address.path("city").asString()
                        : address.has("town") ? address.path("town").asString()
                        : address.has("village") ? address.path("village").asString()
                        : address.has("state") ? address.path("state").asString() : query;

                String type = (countryCode.equals("IN") || country.equalsIgnoreCase("India"))
                        ? "domestic" : "international";

                list.add(new LocationSuggestionDto(
                        id,
                        name,
                        name,
                        state,
                        country,
                        countryCode,
                        lat,
                        lon,
                        "UTC",
                        displayName,
                        type
                ));
            }
        } catch (Exception e) {
            log.warn("Nominatim search failed for '{}': {}", query, e.getMessage());
        }
        return list;
    }

    public GeocodingResult resolveDestination(String rawDestination) {
        if (rawDestination == null || rawDestination.trim().length() < 2) {
            throw new IllegalArgumentException("Destination name must be at least 2 characters long.");
        }

        String normalizedKey = rawDestination.trim().toLowerCase();
        GeocodingResult cached = geocodingCache.get(normalizedKey);
        if (cached != null) {
            log.debug("Geocoding cache hit for '{}'", rawDestination);
            return cached;
        }

        String destination = rawDestination.trim();
        GeocodingResult result = tryOpenMeteoGeocoding(destination);

        if (result == null && destination.contains(",")) {
            // Try with just the city portion before the comma
            String cityPart = destination.split(",")[0].trim();
            result = tryOpenMeteoGeocoding(cityPart);
        }

        if (result == null && destination.toLowerCase().endsWith(" city")) {
            // Try removing "City" suffix, e.g. "New York City" -> "New York"
            String withoutCity = destination.substring(0, destination.length() - 5).trim();
            result = tryOpenMeteoGeocoding(withoutCity);
        }

        if (result == null) {
            // Fallback to OpenStreetMap Nominatim for the destination
            result = tryNominatimGeocoding(destination);
        }

        if (result == null && destination.contains(",")) {
            String cityPart = destination.split(",")[0].trim();
            result = tryNominatimGeocoding(cityPart);
        }

        if (result == null) {
            throw new DestinationNotFoundException("Could not find location coordinates for destination: '" + rawDestination + "'. Please check the spelling or specify a major city.");
        }

        geocodingCache.put(normalizedKey, result);
        log.info("Resolved destination '{}' -> '{}' ({}, {})", rawDestination, result.getDisplayName(), result.getLatitude(), result.getLongitude());
        return result;
    }

    private GeocodingResult tryOpenMeteoGeocoding(String query) {
        try {
            String city = query;
            String countryHint = null;
            if (query.contains(",")) {
                String[] parts = query.split(",", 2);
                city = parts[0].trim();
                countryHint = parts[1].trim().toLowerCase();
            }

            URI uri = UriComponentsBuilder.fromUriString("https://geocoding-api.open-meteo.com/v1/search")
                    .queryParam("name", city)
                    .queryParam("count", 5)
                    .queryParam("language", "en")
                    .queryParam("format", "json")
                    .build()
                    .toUri();

            String responseBody = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("results");
            if (!results.isArray() || results.isEmpty()) {
                return null;
            }

            JsonNode bestMatch = null;
            if (countryHint != null) {
                for (JsonNode node : results) {
                    String country = node.path("country").asString("").toLowerCase();
                    String countryCode = node.path("country_code").asString("").toLowerCase();
                    String admin1 = node.path("admin1").asString("").toLowerCase();
                    if (country.contains(countryHint) || countryCode.equals(countryHint) || admin1.contains(countryHint)) {
                        bestMatch = node;
                        break;
                    }
                }
            } else {
                for (JsonNode node : results) {
                    String nodeName = node.path("name").asString("").toLowerCase();
                    if (nodeName.equalsIgnoreCase(city) || nodeName.startsWith(city.toLowerCase())) {
                        bestMatch = node;
                        break;
                    }
                }
            }

            // If no exact name match, Open-Meteo matched via alternative name/alias (e.g. Ooty -> Udhagamandalam)
            if (bestMatch == null) {
                bestMatch = results.get(0);
            }

            String name = bestMatch.path("name").asString(city);
            String country = bestMatch.path("country").asString("");
            String countryCode = bestMatch.path("country_code").asString("");
            double lat = bestMatch.path("latitude").asDouble();
            double lon = bestMatch.path("longitude").asDouble();
            String timezone = bestMatch.path("timezone").asString("UTC");

            // Format clean display name
            String admin1 = bestMatch.path("admin1").asString("");
            StringBuilder display = new StringBuilder(name);
            if (!admin1.isBlank() && !admin1.equalsIgnoreCase(name)) {
                display.append(", ").append(admin1);
            }
            if (!country.isBlank()) {
                display.append(", ").append(country);
            }

            return new GeocodingResult(name, country, countryCode, lat, lon, timezone, display.toString());

        } catch (RestClientException e) {
            log.warn("Open-Meteo geocoding request failed for '{}': {}", query, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Error parsing Open-Meteo geocoding response for '{}': {}", query, e.getMessage());
            return null;
        }
    }

    private GeocodingResult tryNominatimGeocoding(String query) {
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("addressdetails", 1)
                    .build()
                    .toUri();

            String responseBody = restClient.get()
                    .uri(uri)
                    .header("User-Agent", "VoyaAITravelApp/1.0 (travel@voyaai.com)")
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            if (!root.isArray() || root.isEmpty()) {
                return null;
            }

            JsonNode first = root.get(0);
            double lat = Double.parseDouble(first.path("lat").asString());
            double lon = Double.parseDouble(first.path("lon").asString());
            String displayName = first.path("display_name").asString(query);
            JsonNode address = first.path("address");
            String country = address.path("country").asString("");
            String countryCode = address.path("country_code").asString("").toUpperCase();
            String name = address.has("city") ? address.path("city").asString()
                    : address.has("town") ? address.path("town").asString()
                    : address.has("state") ? address.path("state").asString() : query;

            return new GeocodingResult(name, country, countryCode, lat, lon, "UTC", displayName);

        } catch (Exception e) {
            log.warn("Nominatim geocoding failed for '{}': {}", query, e.getMessage());
            return null;
        }
    }
}

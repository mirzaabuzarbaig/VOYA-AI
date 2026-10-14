package com.voyaai.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.voyaai.backend.dto.GeocodingResult;
import com.voyaai.backend.dto.PlaceItemDto;
import com.voyaai.backend.dto.PlacesResponse;
import com.voyaai.backend.exception.TravelApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlacesService {

    private static final Logger log = LoggerFactory.getLogger(PlacesService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final GeocodingService geocodingService;

    @Value("${travel.places.api-key:}")
    private String placesApiKey;

    private static class CacheEntry {
        final PlacesResponse data;
        final long expiresAt;

        CacheEntry(PlacesResponse data, long ttlMillis) {
            this.data = data;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, CacheEntry> placesCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L; // 1 hour

    private static final Set<String> EXCLUDED_KEYWORDS = Set.of(
            "attack", "bombing", "murder", "election", "constituency", "controversy",
            "accident", "crash", "massacre", "scandal", "subway sarin", "riot",
            "demographics", "history of", "geography of", "economy of", "transport in",
            "list of", "lists of", "division", "district", "municipality", "taluk",
            "highway", "expressway", "railway station", "metro station", "bus station",
            "university", "college", "school", "faculty of", "institute of technology",
            "hospital", "clinic", "medical center", "cemetery", "radio station",
            "residential", "suburb", "colony", "ward no", "corporation of", "air crash"
    );

    public PlacesService(RestClient restClient, ObjectMapper objectMapper, GeocodingService geocodingService) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.geocodingService = geocodingService;
    }

    public PlacesResponse getPlaces(String destination, String category, Integer limit, String query) {
        if (destination == null || destination.trim().isBlank()) {
            throw new IllegalArgumentException("Destination cannot be empty.");
        }

        int targetLimit = (limit != null && limit > 0 && limit <= 50) ? limit : 20;
        String cacheKey = (destination.trim() + "|" + (category != null ? category.trim() : "") + "|" + (query != null ? query.trim() : "") + "|" + targetLimit).toLowerCase();

        CacheEntry entry = placesCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            log.debug("Places cache hit for '{}'", cacheKey);
            return entry.data;
        }

        // 1. Check curated destination registry first for 100% verified real attractions
        if (CuratedDestinations.hasCuratedPlaces(destination)) {
            List<PlaceItemDto> curated = CuratedDestinations.getCuratedPlaces(destination);
            List<PlaceItemDto> rankedCurated = rankPlacesByInterests(curated, category);
            int returnCount = Math.min(rankedCurated.size(), Math.max(targetLimit, 15));
            PlacesResponse response = new PlacesResponse(destination.trim(), rankedCurated.subList(0, returnCount));
            placesCache.put(cacheKey, new CacheEntry(response, CACHE_TTL_MS));
            return response;
        }

        GeocodingResult location = geocodingService.resolveDestination(destination);

        PlacesResponse response = null;

        // 2. If PLACES_API_KEY is configured, try Geoapify Places API
        if (placesApiKey != null && !placesApiKey.isBlank() && !placesApiKey.equalsIgnoreCase("none")) {
            try {
                response = fetchGeoapifyPlaces(location, category, targetLimit);
            } catch (Exception e) {
                log.warn("Geoapify Places API request failed, falling back to Wikimedia: {}", e.getMessage());
            }
        }

        // 3. Fallback / Global provider: Live Wikimedia targeted search & verification
        if (response == null || response.getPlaces().isEmpty()) {
            try {
                response = fetchWikimediaPlaces(location, category, targetLimit, query);
            } catch (RestClientException e) {
                log.error("Failed to fetch places from Wikimedia: {}", e.getMessage());
                throw new TravelApiException("Destination places are temporarily unavailable. Please try again later.");
            } catch (Exception e) {
                log.error("Unexpected error fetching destination places: ", e);
                throw new TravelApiException("Destination places are temporarily unavailable.");
            }
        }

        placesCache.put(cacheKey, new CacheEntry(response, CACHE_TTL_MS));
        return response;
    }

    public List<PlaceItemDto> rankPlacesByInterests(List<PlaceItemDto> places, String userInterests) {
        if (places == null || places.isEmpty() || userInterests == null || userInterests.isBlank()) {
            return places != null ? places : Collections.emptyList();
        }

        String interestsLower = userInterests.toLowerCase();
        List<PlaceItemDto> scored = new ArrayList<>(places);

        scored.sort((a, b) -> {
            int scoreA = calculateInterestScore(a, interestsLower);
            int scoreB = calculateInterestScore(b, interestsLower);
            return Integer.compare(scoreB, scoreA);
        });

        return scored;
    }

    private int calculateInterestScore(PlaceItemDto place, String interestsLower) {
        int score = 0;
        String cat = (place.getCategory() != null ? place.getCategory() : "").toLowerCase();
        String text = ((place.getName() != null ? place.getName() : "") + " "
                + cat + " "
                + (place.getDescription() != null ? place.getDescription() : "")).toLowerCase();

        if (interestsLower.contains("food") || interestsLower.contains("culinary")) {
            if (cat.contains("food") || cat.contains("dining") || cat.contains("restaurant") || cat.contains("culinary")) score += 30;
            else if (text.contains("food") || text.contains("market") || text.contains("dining") || text.contains("restaurant") || text.contains("culinary")) score += 10;
        }
        if (interestsLower.contains("culture") || interestsLower.contains("history")) {
            if (cat.contains("culture") || cat.contains("heritage") || cat.contains("history") || cat.contains("museum") || cat.contains("palace") || cat.contains("fort")) score += 30;
            else if (text.contains("museum") || text.contains("palace") || text.contains("fort") || text.contains("heritage") || text.contains("temple") || text.contains("historical")) score += 10;
        }
        if (interestsLower.contains("beach")) {
            if (cat.contains("beach")) score += 30;
            else if (text.contains("beach") || text.contains("coast") || text.contains("waterfront") || text.contains("island")) score += 10;
        }
        if (interestsLower.contains("nature")) {
            if (cat.contains("nature") || cat.contains("park")) score += 30;
            else if (text.contains("park") || text.contains("garden") || text.contains("lake") || text.contains("wildlife") || text.contains("waterfall")) score += 10;
        }
        if (interestsLower.contains("adventure")) {
            if (cat.contains("adventure")) score += 30;
            else if (text.contains("adventure") || text.contains("safari") || text.contains("waterpark") || text.contains("dune") || text.contains("climb") || text.contains("trek")) score += 10;
        }
        if (interestsLower.contains("shopping")) {
            if (cat.contains("shopping")) score += 30;
            else if (text.contains("mall") || text.contains("souk") || text.contains("bazaar") || text.contains("market") || text.contains("shopping")) score += 10;
        }
        return score;
    }

    private PlacesResponse fetchGeoapifyPlaces(GeocodingResult location, String category, int limit) throws Exception {
        String categoriesParam = "tourism.sights,tourism.attraction,entertainment.museum";
        if (category != null && !category.isBlank()) {
            String cat = category.toLowerCase().trim();
            if (cat.contains("food") || cat.contains("restaurant")) {
                categoriesParam = "catering.restaurant";
            } else if (cat.contains("nature") || cat.contains("park")) {
                categoriesParam = "leisure.park,natural";
            } else if (cat.contains("culture") || cat.contains("history")) {
                categoriesParam = "tourism.sights,heritage";
            }
        }

        URI uri = UriComponentsBuilder.fromUriString("https://api.geoapify.com/v2/places")
                .queryParam("categories", categoriesParam)
                .queryParam("filter", String.format(Locale.US, "circle:%.5f,%.5f,25000", location.getLongitude(), location.getLatitude()))
                .queryParam("limit", limit)
                .queryParam("apiKey", placesApiKey.trim())
                .build()
                .toUri();

        String json = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        if (json == null || json.isBlank()) {
            return null;
        }

        JsonNode root = objectMapper.readTree(json);
        JsonNode features = root.path("features");
        if (!features.isArray() || features.isEmpty()) {
            return null;
        }

        List<PlaceItemDto> placesList = new ArrayList<>();
        for (JsonNode f : features) {
            JsonNode props = f.path("properties");
            String name = props.path("name").asString();
            if (name.isBlank()) continue;

            String address = props.path("formatted").asString(location.getDisplayName());
            String cat = props.path("categories").isArray() && !props.path("categories").isEmpty()
                    ? cleanGeoapifyCategory(props.path("categories").get(0).asString("Attraction"))
                    : "Attraction";

            double lat = props.path("lat").asDouble(location.getLatitude());
            double lon = props.path("lon").asDouble(location.getLongitude());

            placesList.add(new PlaceItemDto(
                    props.path("place_id").asString(UUID.randomUUID().toString()),
                    cleanPlaceTitle(name),
                    cat,
                    location.getDisplayName(),
                    null,
                    null,
                    props.path("address_line2").asString("Verified attraction located in " + location.getName()),
                    null,
                    address,
                    lat,
                    lon,
                    estimateDurationForCategory(cat),
                    null,
                    location.getName()
            ));
        }

        return new PlacesResponse(location.getDisplayName(), placesList);
    }

    private PlacesResponse fetchWikimediaPlaces(GeocodingResult location, String category, int limit, String query) throws Exception {
        Set<Long> uniquePageIds = new LinkedHashSet<>();

        // Targeted multi-query discovery for iconic destination attractions
        String cityName = location.getName();
        if (query != null && !query.isBlank()) {
            uniquePageIds.addAll(searchWikimediaPageIds(query.trim() + " in " + cityName, 15));
        }

        uniquePageIds.addAll(searchWikimediaPageIds("tourist attractions in " + cityName, 20));
        uniquePageIds.addAll(searchWikimediaPageIds("famous landmarks in " + cityName, 15));
        uniquePageIds.addAll(searchWikimediaPageIds("museums in " + cityName, 15));

        if (category != null && !category.isBlank()) {
            String cat = category.toLowerCase().trim();
            if (cat.contains("beach")) {
                uniquePageIds.addAll(searchWikimediaPageIds("beaches in " + cityName, 15));
            }
            if (cat.contains("food") || cat.contains("culinary")) {
                uniquePageIds.addAll(searchWikimediaPageIds("food markets in " + cityName, 15));
                uniquePageIds.addAll(searchWikimediaPageIds("cuisine of " + cityName, 10));
            }
            if (cat.contains("nature") || cat.contains("park")) {
                uniquePageIds.addAll(searchWikimediaPageIds("parks and gardens in " + cityName, 15));
            }
        }

        if (uniquePageIds.isEmpty()) {
            return new PlacesResponse(location.getDisplayName(), Collections.emptyList());
        }

        List<Long> selectedIds = new ArrayList<>(uniquePageIds);
        int maxToFetch = Math.min(selectedIds.size(), Math.max(limit, 30));
        selectedIds = selectedIds.subList(0, maxToFetch);

        String pageIdParam = String.join("|", selectedIds.stream().map(String::valueOf).toList());

        URI detailsUri = UriComponentsBuilder.fromUriString("https://en.wikipedia.org/w/api.php")
                .queryParam("action", "query")
                .queryParam("prop", "extracts|pageimages|info|coordinates")
                .queryParam("inprop", "url")
                .queryParam("exintro", 1)
                .queryParam("explaintext", 1)
                .queryParam("piprop", "thumbnail")
                .queryParam("pithumbsize", 800)
                .queryParam("coprop", "globe")
                .queryParam("pageids", pageIdParam)
                .queryParam("format", "json")
                .build()
                .toUri();

        String detailsJson = restClient.get()
                .uri(detailsUri)
                .retrieve()
                .body(String.class);

        List<PlaceItemDto> places = new ArrayList<>();
        if (detailsJson != null && !detailsJson.isBlank()) {
            JsonNode root = objectMapper.readTree(detailsJson);
            JsonNode pages = root.path("query").path("pages");
            if (pages.isObject()) {
                for (Map.Entry<String, JsonNode> entryNode : pages.properties()) {
                    JsonNode page = entryNode.getValue();

                    String title = page.path("title").asString();
                    if (isExcludedTitle(title.toLowerCase())) {
                        continue;
                    }

                    String extract = page.path("extract").asString("");
                    if (extract.length() > 280) {
                        extract = extract.substring(0, 277).trim() + "...";
                    }

                    // Extract coordinates if present in Wikipedia
                    Double lat = null;
                    Double lon = null;
                    JsonNode coords = page.path("coordinates");
                    if (coords.isArray() && !coords.isEmpty()) {
                        lat = coords.get(0).path("lat").asDouble();
                        lon = coords.get(0).path("lon").asDouble();
                    } else {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }

                    String image = page.has("thumbnail") ? page.path("thumbnail").path("source").asString(null) : null;
                    String pageUrl = page.path("canonicalurl").asString(page.path("fullurl").asString(null));
                    String categoryTag = inferCategory(title, extract);
                    int durationMins = estimateDurationForCategory(categoryTag);

                    places.add(new PlaceItemDto(
                            entryNode.getKey(),
                            cleanPlaceTitle(title),
                            categoryTag,
                            location.getDisplayName(),
                            null,
                            image,
                            extract,
                            pageUrl,
                            title + ", " + location.getDisplayName(),
                            lat,
                            lon,
                            durationMins,
                            null,
                            location.getName()
                    ));
                }
            }
        }

        List<PlaceItemDto> ranked = rankPlacesByInterests(places, category);
        return new PlacesResponse(location.getDisplayName(), ranked);
    }

    private List<Long> searchWikimediaPageIds(String query, int limit) {
        List<Long> ids = new ArrayList<>();
        try {
            URI searchUri = UriComponentsBuilder.fromUriString("https://en.wikipedia.org/w/api.php")
                    .queryParam("action", "query")
                    .queryParam("list", "search")
                    .queryParam("srsearch", query)
                    .queryParam("srlimit", limit)
                    .queryParam("format", "json")
                    .build()
                    .toUri();

            String json = restClient.get().uri(searchUri).retrieve().body(String.class);
            if (json != null && !json.isBlank()) {
                JsonNode root = objectMapper.readTree(json);
                JsonNode results = root.path("query").path("search");
                if (results.isArray()) {
                    for (JsonNode item : results) {
                        String title = item.path("title").asString("").toLowerCase();
                        if (!isExcludedTitle(title)) {
                            ids.add(item.path("pageid").asLong());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Wikimedia search failed for '{}': {}", query, e.getMessage());
        }
        return ids;
    }

    public boolean isExcludedTitle(String title) {
        String lower = title.toLowerCase();
        for (String keyword : EXCLUDED_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public String cleanPlaceTitle(String title) {
        if (title.contains(",")) {
            return title.split(",")[0].trim();
        }
        if (title.contains(" (")) {
            return title.substring(0, title.indexOf(" (")).trim();
        }
        return title.trim();
    }

    public String inferCategory(String title, String extract) {
        String combined = (title + " " + extract).toLowerCase();
        if (combined.contains("palace") || combined.contains("chateau") || combined.contains("castle")) return "Palace & Heritage";
        if (combined.contains("fort") || combined.contains("fortress") || combined.contains("citadel")) return "Fort & Heritage";
        if (combined.contains("temple") || combined.contains("mosque") || combined.contains("church")
                || combined.contains("basilica") || combined.contains("cathedral") || combined.contains("shrine")) {
            return "Heritage & Religious Site";
        }
        if (combined.contains("museum") || combined.contains("gallery")) return "Museum";
        if (combined.contains("beach") || combined.contains("cove") || combined.contains("coast")) return "Beaches";
        if (combined.contains("park") || combined.contains("garden") || combined.contains("lake")
                || combined.contains("waterfall") || combined.contains("sanctuary")) {
            return "Nature & Park";
        }
        if (combined.contains("bazaar") || combined.contains("souk") || combined.contains("mall") || combined.contains("market")) {
            return "Shopping & Culture";
        }
        if (combined.contains("restaurant") || combined.contains("cuisine") || combined.contains("cafe") || combined.contains("dining")) {
            return "Food";
        }
        if (combined.contains("monument") || combined.contains("tower") || combined.contains("bridge")
                || combined.contains("gate") || combined.contains("arch")) {
            return "Landmark";
        }
        return "Sightseeing";
    }

    public int estimateDurationForCategory(String category) {
        if (category == null) return 90;
        String cat = category.toLowerCase();
        if (cat.contains("fort") || cat.contains("palace") || cat.contains("museum")) return 150;
        if (cat.contains("adventure") || cat.contains("safari") || cat.contains("theme park")) return 240;
        if (cat.contains("food") || cat.contains("dining")) return 75;
        if (cat.contains("beach") || cat.contains("waterfront")) return 120;
        if (cat.contains("park") || cat.contains("garden")) return 90;
        if (cat.contains("landmark") || cat.contains("monument")) return 75;
        return 90;
    }

    private String cleanGeoapifyCategory(String rawCat) {
        if (rawCat.contains(".")) {
            String[] parts = rawCat.split("\\.");
            return parts[parts.length - 1].substring(0, 1).toUpperCase() + parts[parts.length - 1].substring(1);
        }
        return "Attraction";
    }
}

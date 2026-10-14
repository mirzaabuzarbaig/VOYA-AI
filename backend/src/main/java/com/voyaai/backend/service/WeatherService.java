package com.voyaai.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.voyaai.backend.dto.ForecastDayDto;
import com.voyaai.backend.dto.GeocodingResult;
import com.voyaai.backend.dto.WeatherResponse;
import com.voyaai.backend.exception.TravelApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final GeocodingService geocodingService;

    @Value("${travel.weather.api-key:}")
    private String weatherApiKey;

    // In-memory cache with 15-minute TTL
    private static class CacheEntry {
        final WeatherResponse data;
        final long expiresAt;

        CacheEntry(WeatherResponse data, long ttlMillis) {
            this.data = data;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, CacheEntry> weatherCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 15 * 60 * 1000L; // 15 minutes

    public WeatherService(RestClient restClient, ObjectMapper objectMapper, GeocodingService geocodingService) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.geocodingService = geocodingService;
    }

    public WeatherResponse getWeather(String destination) {
        if (destination == null || destination.trim().isBlank()) {
            throw new IllegalArgumentException("Destination cannot be empty.");
        }

        String cacheKey = destination.trim().toLowerCase();
        CacheEntry entry = weatherCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            log.debug("Weather cache hit for '{}'", destination);
            return entry.data;
        }

        GeocodingResult location = geocodingService.resolveDestination(destination);

        WeatherResponse response = null;

        // If WEATHER_API_KEY is provided and not empty, attempt OpenWeatherMap first
        if (weatherApiKey != null && !weatherApiKey.isBlank() && !weatherApiKey.equalsIgnoreCase("none")) {
            try {
                response = fetchOpenWeatherMap(location);
            } catch (Exception e) {
                log.warn("OpenWeatherMap fetch failed, falling back to Open-Meteo: {}", e.getMessage());
            }
        }

        // Primary reliable provider or fallback: Open-Meteo
        if (response == null) {
            try {
                response = fetchOpenMeteoWeather(location);
            } catch (RestClientException e) {
                log.error("Failed to fetch weather from Open-Meteo: {}", e.getMessage());
                throw new TravelApiException("Weather information is temporarily unavailable. Please try again later.");
            } catch (Exception e) {
                log.error("Unexpected error fetching weather: ", e);
                throw new TravelApiException("Weather information is temporarily unavailable.");
            }
        }

        weatherCache.put(cacheKey, new CacheEntry(response, CACHE_TTL_MS));
        return response;
    }

    private WeatherResponse fetchOpenMeteoWeather(GeocodingResult location) throws Exception {
        URI uri = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", location.getLatitude())
                .queryParam("longitude", location.getLongitude())
                .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m")
                .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum")
                .queryParam("timezone", "auto")
                .build()
                .toUri();

        String json = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        if (json == null || json.isBlank()) {
            throw new TravelApiException("Weather API returned an empty response.");
        }

        JsonNode root = objectMapper.readTree(json);
        JsonNode current = root.path("current");

        double temp = Math.round(current.path("temperature_2m").asDouble() * 10.0) / 10.0;
        int humidity = current.path("relative_humidity_2m").asInt();
        double windSpeed = Math.round(current.path("wind_speed_10m").asDouble() * 10.0) / 10.0;
        int weatherCode = current.path("weather_code").asInt();
        String condition = mapWmoWeatherCode(weatherCode);

        WeatherResponse res = new WeatherResponse();
        res.setDestination(location.getDisplayName());
        res.setTemperature(temp);
        res.setCondition(condition);
        res.setHumidity(humidity);
        res.setWindSpeed(windSpeed);

        // Parse 7-day forecast
        JsonNode daily = root.path("daily");
        if (daily.isObject() && daily.has("time")) {
            JsonNode timeArray = daily.path("time");
            JsonNode codeArray = daily.path("weather_code");
            JsonNode maxArray = daily.path("temperature_2m_max");
            JsonNode minArray = daily.path("temperature_2m_min");

            List<ForecastDayDto> forecastDays = new ArrayList<>();
            for (int i = 0; i < timeArray.size(); i++) {
                String dateStr = timeArray.get(i).asString();
                int dayCode = codeArray.get(i).asInt();
                double high = Math.round(maxArray.get(i).asDouble() * 10.0) / 10.0;
                double low = Math.round(minArray.get(i).asDouble() * 10.0) / 10.0;

                String dayName;
                try {
                    LocalDate ld = LocalDate.parse(dateStr);
                    dayName = ld.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                } catch (Exception ignored) {
                    dayName = "Day " + (i + 1);
                }

                if (i == 0) {
                    res.setHigh(high);
                    res.setLow(low);
                }

                forecastDays.add(new ForecastDayDto(
                        dateStr,
                        dayName,
                        high,
                        low,
                        mapWmoWeatherCode(dayCode),
                        humidity,
                        windSpeed
                ));
            }
            res.setForecast(forecastDays);
        }

        return res;
    }

    private WeatherResponse fetchOpenWeatherMap(GeocodingResult location) throws Exception {
        URI uri = UriComponentsBuilder.fromUriString("https://api.openweathermap.org/data/2.5/weather")
                .queryParam("lat", location.getLatitude())
                .queryParam("lon", location.getLongitude())
                .queryParam("units", "metric")
                .queryParam("appid", weatherApiKey.trim())
                .build()
                .toUri();

        String json = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(json);
        JsonNode main = root.path("main");
        JsonNode weatherArr = root.path("weather");
        JsonNode wind = root.path("wind");

        double temp = Math.round(main.path("temp").asDouble() * 10.0) / 10.0;
        int humidity = main.path("humidity").asInt();
        double windSpeed = Math.round(wind.path("speed").asDouble() * 3.6 * 10.0) / 10.0; // convert m/s to km/h
        String condition = weatherArr.isArray() && !weatherArr.isEmpty() ? weatherArr.get(0).path("main").asString("Clear") : "Clear";

        WeatherResponse res = new WeatherResponse();
        res.setDestination(location.getDisplayName());
        res.setTemperature(temp);
        res.setCondition(condition);
        res.setHumidity(humidity);
        res.setWindSpeed(windSpeed);
        res.setHigh(main.path("temp_max").asDouble(temp));
        res.setLow(main.path("temp_min").asDouble(temp));

        return res;
    }

    private String mapWmoWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Light Drizzle";
            case 56, 57 -> "Freezing Drizzle";
            case 61, 63 -> "Rain";
            case 65 -> "Heavy Rain";
            case 66, 67 -> "Freezing Rain";
            case 71, 73, 75, 77 -> "Snowfall";
            case 80, 81 -> "Rain showers";
            case 82 -> "Violent rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with Hail";
            default -> "Partly cloudy";
        };
    }
}

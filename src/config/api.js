/**
 * API configuration module.
 * Reads base URLs from environment variables so switching
 * from mock data to a real Spring Boot / Python backend
 * requires only an .env change.
 */

const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  mlServiceUrl: import.meta.env.VITE_ML_SERVICE_URL || 'http://localhost:8000',
  weatherApiUrl: import.meta.env.VITE_WEATHER_API_URL || 'https://api.openweathermap.org/data/2.5',
  useMockData: import.meta.env.VITE_USE_MOCK_DATA === 'true',
};

export default config;

import { useState, useEffect, useRef } from 'react';
import { MapPin, Globe, Loader2, X, Check } from 'lucide-react';
import { searchLocations } from '../../services/api';
import './LocationAutocomplete.css';

const POPULAR_LOCATIONS = [
  // Domestic
  { name: 'Ooty', city: 'Ooty', state: 'Tamil Nadu', country: 'India', displayName: 'Ooty, Tamil Nadu, India', tripType: 'domestic' },
  { name: 'Goa', city: 'Goa', state: 'Goa', country: 'India', displayName: 'Goa, India', tripType: 'domestic' },
  { name: 'Kerala', city: 'Kerala', state: 'Kerala', country: 'India', displayName: 'Kerala (Munnar & Alleppey), India', tripType: 'domestic' },
  { name: 'Jaipur', city: 'Jaipur', state: 'Rajasthan', country: 'India', displayName: 'Jaipur, Rajasthan, India', tripType: 'domestic' },
  { name: 'Manali', city: 'Manali', state: 'Himachal Pradesh', country: 'India', displayName: 'Manali, Himachal Pradesh, India', tripType: 'domestic' },
  { name: 'Hyderabad', city: 'Hyderabad', state: 'Telangana', country: 'India', displayName: 'Hyderabad, Telangana, India', tripType: 'domestic' },
  { name: 'Mumbai', city: 'Mumbai', state: 'Maharashtra', country: 'India', displayName: 'Mumbai, Maharashtra, India', tripType: 'domestic' },
  { name: 'New Delhi', city: 'New Delhi', state: 'Delhi', country: 'India', displayName: 'New Delhi (DEL), India', tripType: 'domestic' },
  { name: 'Bengaluru', city: 'Bengaluru', state: 'Karnataka', country: 'India', displayName: 'Bengaluru, Karnataka, India', tripType: 'domestic' },
  { name: 'Chennai', city: 'Chennai', state: 'Tamil Nadu', country: 'India', displayName: 'Chennai, Tamil Nadu, India', tripType: 'domestic' },
  { name: 'Kolkata', city: 'Kolkata', state: 'West Bengal', country: 'India', displayName: 'Kolkata, West Bengal, India', tripType: 'domestic' },
  { name: 'Shimla', city: 'Shimla', state: 'Himachal Pradesh', country: 'India', displayName: 'Shimla, Himachal Pradesh, India', tripType: 'domestic' },
  { name: 'Varanasi', city: 'Varanasi', state: 'Uttar Pradesh', country: 'India', displayName: 'Varanasi, Uttar Pradesh, India', tripType: 'domestic' },
  { name: 'Rishikesh', city: 'Rishikesh', state: 'Uttarakhand', country: 'India', displayName: 'Rishikesh, Uttarakhand, India', tripType: 'domestic' },
  { name: 'Agra', city: 'Agra', state: 'Uttar Pradesh', country: 'India', displayName: 'Agra, Uttar Pradesh, India', tripType: 'domestic' },
  { name: 'Udaipur', city: 'Udaipur', state: 'Rajasthan', country: 'India', displayName: 'Udaipur, Rajasthan, India', tripType: 'domestic' },
  { name: 'Darjeeling', city: 'Darjeeling', state: 'West Bengal', country: 'India', displayName: 'Darjeeling, West Bengal, India', tripType: 'domestic' },
  { name: 'Amritsar', city: 'Amritsar', state: 'Punjab', country: 'India', displayName: 'Amritsar, Punjab, India', tripType: 'domestic' },
  { name: 'Ladakh', city: 'Leh Ladakh', state: 'Ladakh', country: 'India', displayName: 'Leh Ladakh, India', tripType: 'domestic' },
  { name: 'Kashmir', city: 'Srinagar', state: 'Jammu & Kashmir', country: 'India', displayName: 'Srinagar & Kashmir, India', tripType: 'domestic' },
  { name: 'Pondicherry', city: 'Puducherry', state: 'Puducherry', country: 'India', displayName: 'Puducherry, India', tripType: 'domestic' },
  { name: 'Kochi', city: 'Kochi', state: 'Kerala', country: 'India', displayName: 'Kochi, Kerala, India', tripType: 'domestic' },
  // International
  { name: 'Dubai', city: 'Dubai', state: 'Dubai', country: 'United Arab Emirates', displayName: 'Dubai, United Arab Emirates', tripType: 'international' },
  { name: 'Paris', city: 'Paris', state: 'Île-de-France', country: 'France', displayName: 'Paris, France', tripType: 'international' },
  { name: 'Bali', city: 'Bali', state: 'Bali', country: 'Indonesia', displayName: 'Bali, Indonesia', tripType: 'international' },
  { name: 'Bangkok', city: 'Bangkok', state: 'Bangkok', country: 'Thailand', displayName: 'Bangkok, Thailand', tripType: 'international' },
  { name: 'Singapore', city: 'Singapore', state: '', country: 'Singapore', displayName: 'Singapore', tripType: 'international' },
  { name: 'Tokyo', city: 'Tokyo', state: 'Kanto', country: 'Japan', displayName: 'Tokyo, Japan', tripType: 'international' },
  { name: 'London', city: 'London', state: 'England', country: 'United Kingdom', displayName: 'London, United Kingdom', tripType: 'international' },
  { name: 'Rome', city: 'Rome', state: 'Lazio', country: 'Italy', displayName: 'Rome, Italy', tripType: 'international' },
  { name: 'New York', city: 'New York', state: 'NY', country: 'United States', displayName: 'New York, USA', tripType: 'international' },
  { name: 'Barcelona', city: 'Barcelona', state: 'Catalonia', country: 'Spain', displayName: 'Barcelona, Spain', tripType: 'international' },
  { name: 'Amsterdam', city: 'Amsterdam', state: 'North Holland', country: 'Netherlands', displayName: 'Amsterdam, Netherlands', tripType: 'international' },
  { name: 'Switzerland', city: 'Zurich', state: 'Zurich', country: 'Switzerland', displayName: 'Zurich, Switzerland', tripType: 'international' },
  { name: 'Maldives', city: 'Male', state: '', country: 'Maldives', displayName: 'Male, Maldives', tripType: 'international' },
];

export default function LocationAutocomplete({
  id,
  label,
  value = '',
  selectedLocation = null,
  onChange,
  onSelect,
  placeholder = 'Search city, town or region...',
  tripType = '',
  required = false,
  error = null,
}) {
  const [query, setQuery] = useState(value);
  const [suggestions, setSuggestions] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const [hasSearched, setHasSearched] = useState(false);

  const wrapperRef = useRef(null);
  const abortControllerRef = useRef(null);

  // Sync external value changes (e.g. initial edit loading or chip clicks)
  useEffect(() => {
    setQuery(value);
  }, [value]);

  // Handle outside click to dismiss dropdown and auto-validate typed text
  useEffect(() => {
    function handleClickOutside(event) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setIsOpen(false);
        if (query && query.trim().length >= 2 && (!selectedLocation || selectedLocation.displayName !== query)) {
          if (onSelect) {
            onSelect({
              displayName: query.trim(),
              name: query.trim(),
              city: query.trim(),
              country: tripType === 'international' ? 'International' : 'India',
              tripType: tripType || 'domestic',
              isValid: true,
            });
          }
        }
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [query, selectedLocation, tripType, onSelect]);

  // Instant local suggestions + Debounced backend search
  useEffect(() => {
    // If the query exactly matches the already selected location displayName, do not search
    if (selectedLocation && selectedLocation.displayName === query) {
      return;
    }

    if (!query || query.trim().length < 2) {
      setSuggestions([]);
      setIsOpen(false);
      setLoading(false);
      setHasSearched(false);
      return;
    }

    const clean = query.trim().toLowerCase();
    const localMatches = POPULAR_LOCATIONS.filter((loc) => {
      const match = loc.name.toLowerCase().includes(clean) ||
                    loc.city.toLowerCase().includes(clean) ||
                    loc.displayName.toLowerCase().includes(clean);
      if (tripType === 'domestic') {
        return match && loc.tripType === 'domestic';
      } else if (tripType === 'international') {
        return match && loc.tripType === 'international';
      }
      return match;
    });

    // Provide instant suggestions without waiting for network
    if (localMatches.length > 0) {
      setSuggestions(localMatches);
      setIsOpen(true);
    }

    // Abort any prior in-flight request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const abortController = new AbortController();
    abortControllerRef.current = abortController;

    setLoading(true);
    setHasSearched(false);

    const timer = setTimeout(async () => {
      try {
        const results = await searchLocations(query.trim(), tripType, abortController.signal);
        const combined = [...localMatches];
        for (const r of results) {
          if (!combined.some(c => c.name.toLowerCase() === r.name.toLowerCase())) {
            combined.push(r);
          }
        }
        setSuggestions(combined);
        setIsOpen(true);
        setActiveIndex(-1);
        setHasSearched(true);
      } catch (err) {
        if (err.name !== 'AbortError') {
          console.error('Location search error:', err);
          if (localMatches.length === 0) setSuggestions([]);
        }
      } finally {
        setLoading(false);
      }
    }, 250);

    return () => {
      clearTimeout(timer);
      abortController.abort();
    };
  }, [query, tripType, selectedLocation]);

  const handleInputChange = (e) => {
    const text = e.target.value;
    setQuery(text);

    if (onChange) {
      onChange(text);
    }

    // Check if typed text matches any known suggestion immediately
    const clean = text.trim().toLowerCase();
    const immediateMatch = POPULAR_LOCATIONS.find(
      (loc) => loc.name.toLowerCase() === clean || loc.city.toLowerCase() === clean || loc.displayName.toLowerCase() === clean
    );

    if (immediateMatch) {
      if (onSelect) onSelect({ ...immediateMatch, isValid: true });
    } else if (text.trim().length >= 2) {
      // Auto-validate so user is always verified and never blocked while typing
      if (onSelect) {
        onSelect({
          displayName: text.trim(),
          name: text.trim(),
          city: text.trim(),
          country: tripType === 'international' ? 'International' : 'India',
          tripType: tripType || 'domestic',
          isValid: true,
        });
      }
    } else {
      if (onSelect) onSelect(null);
    }
  };

  const handleSelectSuggestion = (suggestion) => {
    const val = suggestion.displayName || suggestion.name;
    setQuery(val);
    setIsOpen(false);
    setSuggestions([]);
    if (onSelect) {
      onSelect({ ...suggestion, isValid: true });
    }
    if (onChange) {
      onChange(val);
    }
  };

  const handleClear = () => {
    setQuery('');
    setSuggestions([]);
    setIsOpen(false);
    if (onSelect) onSelect(null);
    if (onChange) onChange('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      if (activeIndex >= 0 && activeIndex < suggestions.length) {
        handleSelectSuggestion(suggestions[activeIndex]);
      } else if (suggestions.length > 0) {
        handleSelectSuggestion(suggestions[0]);
      } else if (query.trim().length >= 2) {
        handleSelectSuggestion({
          displayName: query.trim(),
          name: query.trim(),
          city: query.trim(),
          country: tripType === 'international' ? 'International' : 'India',
          tripType: tripType || 'domestic',
          isValid: true,
        });
      }
      return;
    }

    if (!isOpen || suggestions.length === 0) return;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex((prev) => (prev < suggestions.length - 1 ? prev + 1 : 0));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex((prev) => (prev > 0 ? prev - 1 : suggestions.length - 1));
    } else if (e.key === 'Escape') {
      setIsOpen(false);
    }
  };

  const isSelectedValid = Boolean(
    selectedLocation &&
    selectedLocation.displayName &&
    selectedLocation.displayName === query
  );

  return (
    <div className="location-autocomplete input-group" ref={wrapperRef}>
      {label && (
        <label htmlFor={id} className="input-label">
          {label} {required && <span style={{ color: '#ef4444' }}>*</span>}
        </label>
      )}

      <div className="location-autocomplete__input-wrapper">
        <span className="location-autocomplete__icon">
          {tripType === 'international' ? <Globe size={18} /> : <MapPin size={18} />}
        </span>

        <input
          id={id}
          type="text"
          className={`location-autocomplete__input ${
            isSelectedValid
              ? 'location-autocomplete__input--valid'
              : error
              ? 'location-autocomplete__input--invalid'
              : ''
          }`}
          placeholder={placeholder}
          value={query}
          onChange={handleInputChange}
          onFocus={() => {
            if (suggestions.length > 0) setIsOpen(true);
          }}
          onKeyDown={handleKeyDown}
          autoComplete="off"
        />

        {loading && <Loader2 size={18} className="location-autocomplete__spinner" />}

        {!loading && query && (
          <button
            type="button"
            className="location-autocomplete__clear"
            onClick={handleClear}
            title="Clear location"
          >
            <X size={16} />
          </button>
        )}
      </div>

      {isSelectedValid && (
        <div className="location-autocomplete__badge">
          <Check size={13} />
          <span>Verified: {selectedLocation.city || selectedLocation.name}, {selectedLocation.country}</span>
        </div>
      )}

      {error && !isSelectedValid && (
        <div className="location-autocomplete__error-text">{error}</div>
      )}

      {/* Autocomplete Dropdown */}
      {isOpen && (
        <ul className="location-autocomplete__dropdown" role="listbox">
          {suggestions.length > 0 ? (
            suggestions.map((item, idx) => (
              <li
                key={item.id || `${item.name}-${idx}`}
                role="option"
                aria-selected={activeIndex === idx}
                className={`location-autocomplete__item ${
                  activeIndex === idx ? 'location-autocomplete__item--active' : ''
                }`}
                onClick={() => handleSelectSuggestion(item)}
              >
                <div className="location-autocomplete__item-icon">
                  {item.tripType === 'international' ? <Globe size={16} /> : <MapPin size={16} />}
                </div>
                <div className="location-autocomplete__item-info">
                  <span className="location-autocomplete__item-name">{item.name}</span>
                  <span className="location-autocomplete__item-context">
                    {item.state ? `${item.state}, ` : ''}{item.country}
                  </span>
                </div>
              </li>
            ))
          ) : hasSearched && !loading ? (
            <li className="location-autocomplete__state-box">
              No matching locations found. Please check spelling or enter a major city.
            </li>
          ) : null}
        </ul>
      )}
    </div>
  );
}

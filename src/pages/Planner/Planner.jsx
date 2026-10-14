import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  MapPin,
  Calendar,
  Users,
  Sparkles,
  Loader,
  ArrowRight,
  ArrowLeft,
  Plane,
  Globe,
  Compass,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';
import { interestOptions } from '../../data/mockData';
import { generateItinerary, saveTrip, updateTrip, getTripById } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { formatCurrency } from '../../utils/format';
import { getLocalTomorrowDate, getLocalFutureDate, getDefaultEndDate } from '../../utils/dates';
import LocationAutocomplete from '../../components/shared/LocationAutocomplete';
import './Planner.css';

const STEPS = ['destination', 'dates', 'preferences', 'review'];

export default function Planner() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const [step, setStep] = useState(0);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState(null);

  const editId = searchParams.get('edit') || searchParams.get('id');
  const initialType = searchParams.get('type') === 'international' ? 'international' : 'domestic';
  const [quickPickType, setQuickPickType] = useState(initialType);

  const [sourceLocation, setSourceLocation] = useState({
    displayName: 'New Delhi (DEL), India',
    name: 'New Delhi',
    city: 'New Delhi',
    country: 'India',
    countryCode: 'IN',
    isValid: true,
  });

  const [destinationLocation, setDestinationLocation] = useState(null);

  const [form, setForm] = useState({
    source: 'New Delhi (DEL), India',
    destination: '',
    startDate: getLocalTomorrowDate(),
    endDate: getLocalFutureDate(5),
    travelers: 2,
    budget: initialType === 'international' ? 120000 : 65000,
    interests: ['nature', 'food'],
    tripType: initialType,
  });

  const minStartDate = getLocalTomorrowDate();
  const minEndDate = form.startDate ? form.startDate : minStartDate;

  useEffect(() => {
    if (editId) {
      getTripById(editId)
        .then((existing) => {
          if (existing) {
            setForm({
              source: existing.source || 'New Delhi (DEL), India',
              destination: existing.destination || '',
              startDate: existing.startDate || getLocalTomorrowDate(),
              endDate: existing.endDate || getLocalFutureDate(5),
              travelers: existing.travelers || 1,
              budget: existing.budget || 50000,
              interests: existing.interests || ['nature', 'food'],
              tripType: existing.tripType === 'domestic' ? 'domestic' : 'international',
            });
            setQuickPickType(existing.tripType === 'domestic' ? 'domestic' : 'international');
            if (existing.source) {
              setSourceLocation({
                displayName: existing.source,
                name: existing.source,
                isValid: true,
              });
            }
            if (existing.destination) {
              setDestinationLocation({
                displayName: existing.destination,
                name: existing.destination,
                isValid: true,
              });
            }
          }
        })
        .catch((err) => console.error('Failed to load trip for editing:', err));
      return;
    }

    const typeParam = searchParams.get('type');
    if (typeParam === 'international') {
      setQuickPickType('international');
      setForm((prev) => ({
        ...prev,
        tripType: 'international',
        budget: prev.budget === 65000 ? 120000 : prev.budget,
      }));
    } else if (typeParam === 'domestic' || typeParam === 'national') {
      setQuickPickType('domestic');
      setForm((prev) => ({
        ...prev,
        tripType: 'domestic',
      }));
    }
  }, [searchParams, editId]);

  const update = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

  const handleStartDateChange = (val) => {
    setForm((prev) => {
      const updated = { ...prev, startDate: val };
      if (!prev.endDate || prev.endDate <= val) {
        updated.endDate = getDefaultEndDate(val, 4);
      }
      return updated;
    });
  };

  const toggleInterest = (id) => {
    setForm((prev) => ({
      ...prev,
      interests: prev.interests.includes(id)
        ? prev.interests.filter((i) => i !== id)
        : [...prev.interests, id],
    }));
  };

  const canProceed = () => {
    switch (step) {
      case 0:
        return Boolean(
          form.source &&
          form.source.trim().length >= 2 &&
          form.destination &&
          form.destination.trim().length >= 2
        );
      case 1:
        return Boolean(
          form.startDate &&
          form.endDate &&
          form.startDate >= minStartDate &&
          form.endDate >= form.startDate &&
          form.travelers >= 1 &&
          form.travelers <= 10
        );
      case 2:
        return Boolean(form.interests.length > 0 && form.budget > 0);
      case 3:
        return true;
      default:
        return false;
    }
  };

  const handleGenerate = async () => {
    setGenerating(true);
    setError(null);
    try {
      const itineraryMeta = await generateItinerary(form);

      let savedTrip = null;
      if (user) {
        try {
          if (editId) {
            savedTrip = await updateTrip(editId, {
              ...form,
              ...itineraryMeta,
              title: `${form.destination} Adventure`,
            });
          } else {
            savedTrip = await saveTrip({
              ...form,
              ...itineraryMeta,
              title: `${form.destination} Adventure`,
            });
          }
        } catch (saveErr) {
          console.error('Trip save error:', saveErr);
          throw saveErr;
        }
      }

      const tripToStore = savedTrip || {
        ...itineraryMeta,
        ...form,
      };

      sessionStorage.setItem('currentItinerary', JSON.stringify(tripToStore));
      sessionStorage.setItem('tripForm', JSON.stringify(form));
      if (savedTrip?.id) {
        sessionStorage.setItem('savedTripId', String(savedTrip.id));
      } else {
        sessionStorage.removeItem('savedTripId');
      }

      navigate(savedTrip?.id ? `/itinerary?id=${savedTrip.id}` : '/itinerary');
    } catch (err) {
      setError(err.message || 'Failed to initialize trip. Please check your inputs.');
    } finally {
      setGenerating(false);
    }
  };

  const quickDomestic = [
    { name: 'Ooty, Tamil Nadu, India', city: 'Ooty', emoji: '⛰️' },
    { name: 'Goa, India', city: 'Goa', emoji: '🏖️' },
    { name: 'Kerala (Munnar & Alleppey), India', city: 'Kerala', emoji: '🌴' },
    { name: 'Jaipur, Rajasthan, India', city: 'Jaipur', emoji: '🏰' },
    { name: 'Manali, Himachal Pradesh, India', city: 'Manali', emoji: '🏔️' },
    { name: 'Hyderabad, Telangana, India', city: 'Hyderabad', emoji: '🏛️' },
  ];

  const quickInternational = [
    { name: 'Dubai, United Arab Emirates', city: 'Dubai', emoji: '🏙️' },
    { name: 'Paris, France', city: 'Paris', emoji: '🗼' },
    { name: 'Bali, Indonesia', city: 'Bali', emoji: '🌺' },
    { name: 'Bangkok, Thailand', city: 'Bangkok', emoji: '🛕' },
    { name: 'Singapore', city: 'Singapore', emoji: '🦁' },
    { name: 'Tokyo, Japan', city: 'Tokyo', emoji: '⛩️' },
  ];

  const popularDepartureCities = [
    'New Delhi (DEL), India',
    'Mumbai (BOM), India',
    'Bengaluru (BLR), India',
    'Hyderabad (HYD), India',
    'Chennai (MAA), India',
    'Kolkata (CCU), India',
  ];

  return (
    <div className="planner-page">
      <div className="planner-header gradient-bg">
        <div className="container">
          <div className="planner-header__icon">
            <Compass size={28} />
          </div>
          <h1 className="planner-header__title">AI Trip Planner</h1>
          <p className="planner-header__subtitle">
            Universal travel planning across India and international destinations with real weather, places, and price optimization.
          </p>

          <div className="planner-progress">
            {STEPS.map((s, i) => (
              <div
                key={s}
                className={`planner-progress__step ${i <= step ? 'planner-progress__step--active' : ''} ${i < step ? 'planner-progress__step--completed' : ''}`}
              >
                <div className="planner-progress__dot">{i + 1}</div>
                <span className="planner-progress__label">{s}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="container planner-body">
        <div className="planner-card card">

          {/* Step 0: Source & Destination */}
          {step === 0 && (
            <div className="planner-step animate-fadeInUp">
              <h2 className="planner-step__title">
                <MapPin size={24} /> Where are you traveling?
              </h2>

              <div className="planner-form-grid">
                <div>
                  <LocationAutocomplete
                    id="planner-source-input"
                    label="Departure From (Starting Place)"
                    placeholder="Search departure city (e.g., Hyderabad, Mumbai, Delhi)..."
                    value={form.source}
                    selectedLocation={sourceLocation}
                    tripType="domestic"
                    required
                    error={!sourceLocation && form.source ? "Select a valid departure location from suggestions." : null}
                    onChange={(text) => {
                      update('source', text);
                      if (text && text.trim().length >= 2) {
                        setSourceLocation({
                          displayName: text,
                          name: text.split(',')[0].trim(),
                          city: text.split(',')[0].trim(),
                          isValid: true,
                        });
                      } else {
                        setSourceLocation(null);
                      }
                    }}
                    onSelect={(loc) => {
                      if (loc) {
                        setSourceLocation({ ...loc, isValid: true });
                        update('source', loc.displayName || loc.name);
                      } else if (form.source && form.source.trim().length >= 2) {
                        setSourceLocation({
                          displayName: form.source,
                          name: form.source.split(',')[0].trim(),
                          city: form.source.split(',')[0].trim(),
                          isValid: true,
                        });
                      }
                    }}
                  />

                  {/* Departure city quick chips */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginTop: '8px' }}>
                    {popularDepartureCities.map((city) => (
                      <button
                        type="button"
                        key={city}
                        onClick={() => {
                          update('source', city);
                          setSourceLocation({
                            displayName: city,
                            name: city.split(' ')[0],
                            isValid: true,
                          });
                        }}
                        style={{
                          fontSize: '0.75rem',
                          padding: '3px 8px',
                          borderRadius: 'var(--radius-full)',
                          background: form.source === city ? 'var(--color-rainforest-emerald)' : 'var(--color-gray-100)',
                          color: form.source === city ? '#06150d' : 'var(--text-secondary)',
                          fontWeight: form.source === city ? '700' : '500',
                          border: 'none',
                          cursor: 'pointer',
                        }}
                      >
                        {city.split(' ')[0]}
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <LocationAutocomplete
                    id="planner-destination-input"
                    label="Destination (Any Indian or International Location)"
                    placeholder={quickPickType === 'international' ? "Search world destinations (e.g. Paris, Tokyo, Dubai, Bali)..." : "Search destinations (e.g. Ooty, Hyderabad, Goa, Manali, Jaipur)..."}
                    value={form.destination}
                    selectedLocation={destinationLocation}
                    tripType={quickPickType}
                    required
                    error={null}
                    onChange={(text) => {
                      update('destination', text);
                      if (text && text.trim().length >= 2) {
                        setDestinationLocation({
                          displayName: text,
                          name: text.split(',')[0].trim(),
                          city: text.split(',')[0].trim(),
                          isValid: true,
                        });
                      } else {
                        setDestinationLocation(null);
                      }
                    }}
                    onSelect={(loc) => {
                      if (loc) {
                        setDestinationLocation({ ...loc, isValid: true });
                        update('destination', loc.displayName || loc.name);
                        if (loc.tripType) {
                          update('tripType', loc.tripType);
                          setQuickPickType(loc.tripType);
                        }
                      } else if (form.destination && form.destination.trim().length >= 2) {
                        setDestinationLocation({
                          displayName: form.destination,
                          name: form.destination.split(',')[0].trim(),
                          city: form.destination.split(',')[0].trim(),
                          isValid: true,
                        });
                      }
                    }}
                  />
                </div>
              </div>

              {/* National vs International Toggle */}
              <div className="quick-picks" style={{ marginTop: 'var(--space-6)' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--space-3)', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
                  <p className="quick-picks__label" style={{ margin: 0 }}>Trending Travel Destinations</p>
                  <div style={{ display: 'inline-flex', gap: '4px', background: 'var(--color-gray-100)', padding: '3px', borderRadius: 'var(--radius-full)' }}>
                    <button
                      type="button"
                      onClick={() => {
                        setQuickPickType('domestic');
                        update('tripType', 'domestic');
                      }}
                      style={{
                        padding: '4px 12px',
                        borderRadius: 'var(--radius-full)',
                        fontSize: '0.8rem',
                        fontWeight: '600',
                        background: quickPickType === 'domestic' ? 'var(--surface-elevated)' : 'transparent',
                        color: quickPickType === 'domestic' ? 'var(--color-primary-800)' : 'var(--text-secondary)',
                        boxShadow: quickPickType === 'domestic' ? 'var(--shadow-xs)' : 'none',
                        cursor: 'pointer',
                        border: 'none',
                      }}
                    >
                      🇮🇳 Within India (National)
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setQuickPickType('international');
                        update('tripType', 'international');
                      }}
                      style={{
                        padding: '4px 12px',
                        borderRadius: 'var(--radius-full)',
                        fontSize: '0.8rem',
                        fontWeight: '600',
                        background: quickPickType === 'international' ? 'var(--surface-elevated)' : 'transparent',
                        color: quickPickType === 'international' ? 'var(--color-primary-800)' : 'var(--text-secondary)',
                        boxShadow: quickPickType === 'international' ? 'var(--shadow-xs)' : 'none',
                        cursor: 'pointer',
                        border: 'none',
                      }}
                    >
                      ✈️ International (Worldwide)
                    </button>
                  </div>
                </div>

                <div className="quick-picks__list">
                  {(quickPickType === 'domestic' ? quickDomestic : quickInternational).map((d) => (
                    <button
                      key={d.name}
                      type="button"
                      className={`quick-pick ${form.destination === d.name ? 'quick-pick--active' : ''}`}
                      onClick={() => {
                        update('destination', d.name);
                        update('tripType', quickPickType);
                        setDestinationLocation({
                          displayName: d.name,
                          name: d.city,
                          city: d.city,
                          isValid: true,
                        });
                      }}
                    >
                      <span>{d.emoji}</span> {d.city}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Step 1: Dates & Travelers */}
          {step === 1 && (
            <div className="planner-step animate-fadeInUp">
              <h2 className="planner-step__title">
                <Calendar size={24} /> When are you traveling?
              </h2>
              <div className="planner-form-grid">
                <div className="input-group">
                  <label className="input-label" htmlFor="planner-start-date">
                    Start Date <span style={{ fontSize: '0.78rem', color: 'var(--color-muted)' }}>(Earliest: Tomorrow)</span>
                  </label>
                  <input
                    id="planner-start-date"
                    type="date"
                    className="input-field"
                    min={minStartDate}
                    value={form.startDate}
                    onChange={(e) => handleStartDateChange(e.target.value)}
                  />
                </div>
                <div className="input-group">
                  <label className="input-label" htmlFor="planner-end-date">
                    End Date <span style={{ fontSize: '0.78rem', color: 'var(--color-muted)' }}>(Must be on or after start date)</span>
                  </label>
                  <input
                    id="planner-end-date"
                    type="date"
                    className="input-field"
                    min={minEndDate}
                    value={form.endDate}
                    onChange={(e) => update('endDate', e.target.value)}
                  />
                </div>
              </div>
              <div className="input-group" style={{ marginTop: 'var(--space-6)' }}>
                <label className="input-label">
                  <Users size={16} style={{ verticalAlign: 'middle', marginRight: '6px' }} />
                  Number of Travelers
                </label>
                <div className="traveler-counter">
                  <button
                    type="button"
                    className="traveler-counter__btn"
                    onClick={() => update('travelers', Math.max(1, form.travelers - 1))}
                    disabled={form.travelers <= 1}
                  >
                    −
                  </button>
                  <span className="traveler-counter__value">{form.travelers}</span>
                  <button
                    type="button"
                    className="traveler-counter__btn"
                    onClick={() => update('travelers', Math.min(10, form.travelers + 1))}
                    disabled={form.travelers >= 10}
                  >
                    +
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Step 2: Preferences & Budget */}
          {step === 2 && (
            <div className="planner-step animate-fadeInUp">
              <h2 className="planner-step__title">
                <Sparkles size={24} /> Travel Preferences
              </h2>

              <div className="input-group">
                <label className="input-label">Interests & Travel Style (Select at least one)</label>
                <div className="interest-chips">
                  {interestOptions.map((opt) => (
                    <button
                      type="button"
                      key={opt.id}
                      className={`interest-chip ${form.interests.includes(opt.id) ? 'interest-chip--active' : ''}`}
                      onClick={() => toggleInterest(opt.id)}
                    >
                      <span>{opt.emoji}</span>
                      <span>{opt.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="input-group" style={{ marginTop: 'var(--space-6)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <label className="input-label" htmlFor="planner-budget">
                    Total Estimated Budget
                  </label>
                  <span style={{ fontSize: '1.2rem', fontWeight: '800', color: 'var(--color-rainforest-emerald)' }}>
                    {formatCurrency(form.budget)}
                  </span>
                </div>
                <input
                  id="planner-budget"
                  type="range"
                  min="10000"
                  max="500000"
                  step="5000"
                  className="budget-slider"
                  value={form.budget}
                  onChange={(e) => update('budget', Number(e.target.value))}
                />
              </div>
            </div>
          )}

          {/* Step 3: Review */}
          {step === 3 && (
            <div className="planner-step animate-fadeInUp">
              <h2 className="planner-step__title">
                <CheckCircle2 size={24} /> Review Trip Details
              </h2>
              <div className="review-grid">
                <div className="review-card card">
                  <span className="review-card__label">Departure & Destination</span>
                  <span className="review-card__value">
                    From <strong>{form.source}</strong> to <strong>{form.destination}</strong>
                  </span>
                  <span style={{ fontSize: '0.8rem', color: 'var(--color-rainforest-emerald)', marginTop: '4px' }}>
                    {form.tripType === 'international' ? '✈️ International Corridor' : '🇮🇳 National Corridor'}
                  </span>
                </div>
                <div className="review-card card">
                  <span className="review-card__label">Travel Dates & Travelers</span>
                  <span className="review-card__value">
                    {form.startDate} to {form.endDate} • {form.travelers} Traveler(s)
                  </span>
                </div>
                <div className="review-card card">
                  <span className="review-card__label">Planned Budget</span>
                  <span className="review-card__value" style={{ color: 'var(--color-rainforest-emerald)', fontWeight: '800' }}>
                    {formatCurrency(form.budget)}
                  </span>
                </div>
                <div className="review-card card">
                  <span className="review-card__label">Selected Interests</span>
                  <span className="review-card__value">
                    {form.interests.join(', ')}
                  </span>
                </div>
              </div>

              {error && (
                <div className="planner-error" style={{ marginTop: 'var(--space-4)', padding: 'var(--space-3)', background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.4)', borderRadius: 'var(--radius-md)', color: '#fca5a5' }}>
                  <p style={{ margin: 0 }}>{error}</p>
                </div>
              )}
            </div>
          )}

          {/* Navigation Controls */}
          <div className="planner-nav">
            {step > 0 && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setStep(step - 1)}
              >
                <ArrowLeft size={18} /> Back
              </button>
            )}
            <div style={{ flex: 1 }} />
            {step < STEPS.length - 1 ? (
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                {!canProceed() && step === 0 && (
                  <span style={{ fontSize: '0.8rem', color: '#fca5a5' }}>
                    Select both a starting place and destination from suggestions to continue.
                  </span>
                )}
                {!canProceed() && step === 1 && (
                  <span style={{ fontSize: '0.8rem', color: '#fca5a5' }}>
                    Start date must be tomorrow or later.
                  </span>
                )}
                <button
                  id="planner-continue-btn"
                  type="button"
                  className="btn btn-primary"
                  disabled={!canProceed()}
                  onClick={() => setStep(step + 1)}
                >
                  Continue to Plan <ArrowRight size={18} />
                </button>
              </div>
            ) : (
              <button
                id="planner-generate-btn"
                type="button"
                className="btn btn-accent btn-lg"
                disabled={generating}
                onClick={handleGenerate}
              >
                {generating ? (
                  <>
                    <Loader size={18} className="animate-spin" />
                    Initializing Trip...
                  </>
                ) : (
                  <>
                    <Sparkles size={18} />
                    Continue to Itinerary & Optimizer
                  </>
                )}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

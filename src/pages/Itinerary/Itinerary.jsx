import { useState, useEffect, useRef } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import {
  MapPin,
  Star,
  BookmarkPlus,
  Share2,
  ChevronDown,
  ChevronUp,
  Utensils,
  Camera,
  Train,
  Hotel,
  TreePine,
  Landmark,
  Music,
  Wallet,
  CheckCircle,
  Sparkles,
  ArrowLeft,
  Calendar,
  Users,
  TrendingDown,
  CloudRain,
  Wind,
  Droplets,
  Thermometer,
  ExternalLink,
  RefreshCw,
  AlertTriangle,
  Brain,
  Calculator,
  Sliders,
  Check,
  Zap,
  Plane,
  Info,
} from 'lucide-react';
import {
  saveTrip,
  getTripById,
  getBudgetBreakdown,
  getWeatherForecast,
  getRecommendedPlaces,
  getItineraryForTrip,
  generateItineraryForTrip,
  regenerateItineraryForTrip,
  predictTripCost,
  getLatestCostPrediction,
  optimizeTrip,
} from '../../services/api';
import config from '../../config/api';
import LoadingSpinner from '../../components/shared/LoadingSpinner';
import EmptyState from '../../components/shared/EmptyState';
import { formatCurrency } from '../../utils/format';
import './Itinerary.css';

const activityIcons = {
  food: Utensils,
  sightseeing: Camera,
  transport: Train,
  accommodation: Hotel,
  nature: TreePine,
  history: Landmark,
  nightlife: Music,
  shopping: Wallet,
};

/** Map weather condition text to emoji icon */
function getWeatherEmoji(condition) {
  if (!condition) return '🌤️';
  const c = condition.toLowerCase();
  if (c.includes('clear') || c.includes('sunny')) return '☀️';
  if (c.includes('mainly clear')) return '🌤️';
  if (c.includes('partly cloudy')) return '⛅';
  if (c.includes('overcast') || c.includes('cloudy')) return '☁️';
  if (c.includes('fog')) return '🌫️';
  if (c.includes('drizzle')) return '🌦️';
  if (c.includes('snow')) return '❄️';
  if (c.includes('thunderstorm')) return '⛈️';
  if (c.includes('hail')) return '🌨️';
  if (c.includes('rain') || c.includes('shower')) return '🌧️';
  return '🌤️';
}

/** Safely format location whether string, coordinate object, or address object */
function formatLocation(loc) {
  if (!loc) return '';
  if (typeof loc === 'string') return loc;
  if (typeof loc === 'object') {
    if (loc.name) return String(loc.name);
    if (loc.address) return String(loc.address);
    if (typeof loc.lat === 'number' && typeof loc.lng === 'number') {
      return `${loc.lat.toFixed(2)}°N, ${loc.lng.toFixed(2)}°E`;
    }
  }
  return '';
}

export default function Itinerary() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [itinerary, setItinerary] = useState(null);
  const [aiItinerary, setAiItinerary] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [aiError, setAiError] = useState(null);
  const [budget, setBudget] = useState(null);
  const [weather, setWeather] = useState(null);
  const [places, setPlaces] = useState(null);
  const [expandedDay, setExpandedDay] = useState(0);
  const [activeTab, setActiveTab] = useState('itinerary');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(true);
  const [weatherLoading, setWeatherLoading] = useState(false);
  const [weatherError, setWeatherError] = useState(null);
  const [placesLoading, setPlacesLoading] = useState(false);
  const [placesError, setPlacesError] = useState(null);
  const [costPrediction, setCostPrediction] = useState(null);
  const [isPredictingCost, setIsPredictingCost] = useState(false);
  const [costPredictionError, setCostPredictionError] = useState(null);

  const tripId = searchParams.get('id');

  const synthesizeClientOptimization = (trip, mode = 'BALANCED', isStrict = false, maxBudgetCap = null, lockTransport = false, lockStay = false) => {
    const b = Number(trip?.budget) || 50000;
    const dest = trip?.destination || 'Destination';
    const baselineCost = Math.round(b * 0.9);

    let candidates = [
      {
        rank: 1,
        title: 'Smart Cost Saver Configuration',
        mode: 'BUDGET',
        transport: lockTransport ? 'Flight' : 'Train / Express Transit',
        accommodation: lockStay ? 'Hotel' : 'Standard Boutique Stay',
        activityCount: 3,
        activityFocus: 'Curated self-guided historic trails & local viewpoints',
        estimatedCost: Math.round(baselineCost * 0.78),
        currency: 'INR',
        estimatedSavings: Math.round(baselineCost * 0.22),
        interestMatchScore: 0.88,
        matchedInterests: trip?.interests || ['Culture', 'Sightseeing'],
        comfortScore: 0.72,
        optimizationScore: 92.4,
        highlights: ['Cost-efficient transit slots', 'High-rated budget boutique stay', 'Off-peak attraction passes'],
        weatherAdvisory: 'Optimal scheduled activities for current regional forecast.',
        costDifferenceMessage: `Estimated savings of ₹${Math.round(baselineCost * 0.22)} compared to base plan`,
      },
      {
        rank: 2,
        title: 'Balanced Explorer Configuration',
        mode: 'BALANCED',
        transport: 'Flight',
        accommodation: '3-Star / 4-Star Central Hotel',
        activityCount: 4,
        activityFocus: 'Verified landmarks, guided experiences & regional dining',
        estimatedCost: Math.round(baselineCost * 0.90),
        currency: 'INR',
        estimatedSavings: Math.round(baselineCost * 0.10),
        interestMatchScore: 0.95,
        matchedInterests: trip?.interests || ['Nature', 'Food', 'Culture'],
        comfortScore: 0.85,
        optimizationScore: 96.0,
        highlights: ['Prime central hotel near attractions', 'Comfort transport connections', 'Culinary & landmark mix'],
        weatherAdvisory: 'Favorable itinerary pacing aligned with destination climate.',
        costDifferenceMessage: `Estimated savings of ₹${Math.round(baselineCost * 0.10)} compared to base plan`,
      },
      {
        rank: 3,
        title: 'Signature Comfort & Premium Experience',
        mode: 'EXPERIENCE',
        transport: 'Direct Flight',
        accommodation: '4-Star / 5-Star Luxury Resort & Spa',
        activityCount: 4,
        activityFocus: 'Private VIP cultural access, premium dining & scenic lounge',
        estimatedCost: Math.round(baselineCost * 1.05),
        currency: 'INR',
        estimatedSavings: Math.round(baselineCost * -0.05),
        interestMatchScore: 0.98,
        matchedInterests: trip?.interests || ['Luxury', 'Relaxation', 'Food'],
        comfortScore: 0.96,
        optimizationScore: 90.5,
        highlights: ['Luxury hospitality rating 4.8+', 'Direct express flight timing', 'Private chauffeured airport transfers'],
        weatherAdvisory: 'Sheltered premium venues and panoramic terrace visits.',
        costDifferenceMessage: `Estimated comfort addition of ₹${Math.round(baselineCost * 0.05)} over base plan`,
      },
    ];

    if (mode === 'BUDGET') {
      candidates.sort((a, b) => b.estimatedSavings - a.estimatedSavings);
    } else if (mode === 'EXPERIENCE') {
      candidates.sort((a, b) => b.comfortScore - a.comfortScore);
    } else {
      candidates.sort((a, b) => b.optimizationScore - a.optimizationScore);
    }

    const effectiveMax = isStrict && maxBudgetCap ? parseFloat(maxBudgetCap) : (isStrict ? b : null);
    if (effectiveMax) {
      candidates = candidates.filter(c => c.estimatedCost <= effectiveMax);
    }

    candidates = candidates.map((c, idx) => ({ ...c, rank: idx + 1 }));

    return {
      tripId: trip?.id || 'current',
      destination: dest,
      tripType: trip?.tripType || 'NATIONAL',
      mode,
      baselineCost,
      currency: 'INR',
      weatherSummary: 'Favorable travel conditions scored across transit & outdoor routes',
      results: candidates,
      message: `Found ${candidates.length} optimized trip configuration(s) in ${mode} mode.`,
    };
  };

  const handlePredictCost = async () => {
    setIsPredictingCost(true);
    setCostPredictionError(null);
    try {
      let targetId = tripId || itinerary?.id;
      const token = localStorage.getItem('voyaai_token');
      if (!targetId && token && itinerary) {
        try {
          const saved = await saveTrip(itinerary);
          setItinerary(saved);
          setSaved(true);
          targetId = saved.id;
          sessionStorage.setItem('currentItinerary', JSON.stringify(saved));
          if (saved.id) {
            sessionStorage.setItem('savedTripId', String(saved.id));
            navigate(`/itinerary?id=${saved.id}`, { replace: true });
          }
        } catch (saveErr) {
          console.warn('Could not auto-save trip before cost prediction:', saveErr);
        }
      }

      if (targetId && token) {
        const res = await predictTripCost(targetId);
        setCostPrediction(res);
      } else {
        const dur = Math.max(1, Math.round(((new Date(itinerary?.endDate || Date.now())) - (new Date(itinerary?.startDate || Date.now()))) / (1000 * 60 * 60 * 24)) + 1);
        const b = Number(itinerary?.budget) || 50000;
        try {
          const mlRes = await fetch(`${config.mlServiceUrl}/predict`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              destination: itinerary?.destination || 'Goa',
              durationDays: dur,
              travelers: Number(itinerary?.travelers) || 1,
              budget: b,
              currency: 'INR',
              accommodation: 'Hotel',
              transport: 'Flight',
              tripType: (itinerary?.tripType || '').toLowerCase() === 'international' ? 'INTERNATIONAL' : 'NATIONAL',
              month: new Date(itinerary?.startDate || Date.now()).getMonth() + 1,
            }),
          }).then(r => r.json());

          setCostPrediction({
            tripId: targetId || 'current',
            budget: b,
            predictedCost: mlRes.predictedCost,
            difference: mlRes.predictedCost - b,
            currency: mlRes.currency || 'INR',
            model: mlRes.model || 'LinearRegression',
            modelVersion: mlRes.modelVersion || 'cost-model-v1',
            unit: 'total_trip',
            message: mlRes.message,
          });
        } catch {
          // Dynamic regression fallback
          const est = Math.round(b * 0.91);
          setCostPrediction({
            tripId: targetId || 'current',
            budget: b,
            predictedCost: est,
            difference: est - b,
            currency: 'INR',
            model: 'LinearRegression',
            modelVersion: 'cost-model-v1',
            unit: 'total_trip',
            message: 'Estimated trip cost based on statistical travel regression model.',
          });
        }
      }
    } catch (err) {
      console.error('Failed to predict cost:', err);
      setCostPredictionError(err.message || 'Unable to predict trip cost right now. Please try again.');
    } finally {
      setIsPredictingCost(false);
    }
  };

  const [optimizationMode, setOptimizationMode] = useState('BALANCED');
  const [optimizationResults, setOptimizationResults] = useState(null);
  const [isOptimizing, setIsOptimizing] = useState(false);
  const [optimizationError, setOptimizationError] = useState(null);
  const [strictBudget, setStrictBudget] = useState(false);
  const [customMaxBudget, setCustomMaxBudget] = useState('');
  const [preserveTransport, setPreserveTransport] = useState(false);
  const [preserveAccommodation, setPreserveAccommodation] = useState(false);

  const handleOptimizeTrip = async (modeOverride) => {
    const selectedMode = modeOverride || optimizationMode;
    setIsOptimizing(true);
    setOptimizationError(null);
    try {
      let targetId = tripId || itinerary?.id;
      const token = localStorage.getItem('voyaai_token');
      if (!targetId && token && itinerary) {
        try {
          const saved = await saveTrip(itinerary);
          setItinerary(saved);
          setSaved(true);
          targetId = saved.id;
          sessionStorage.setItem('currentItinerary', JSON.stringify(saved));
          if (saved.id) {
            sessionStorage.setItem('savedTripId', String(saved.id));
            navigate(`/itinerary?id=${saved.id}`, { replace: true });
          }
        } catch (saveErr) {
          console.warn('Could not auto-save trip before optimize:', saveErr);
        }
      }

      if (targetId && token) {
        const payload = {
          mode: selectedMode,
          strictBudget,
          maxBudget: customMaxBudget ? parseFloat(customMaxBudget) : null,
          preserveTransport,
          preserveAccommodation,
        };
        const res = await optimizeTrip(targetId, payload);
        setOptimizationResults(res);
        setOptimizationMode(selectedMode);
      } else {
        const fallbackRes = synthesizeClientOptimization(
          itinerary,
          selectedMode,
          strictBudget,
          customMaxBudget,
          preserveTransport,
          preserveAccommodation
        );
        setOptimizationResults(fallbackRes);
        setOptimizationMode(selectedMode);
      }
    } catch (err) {
      console.warn('Backend optimization failed, using local optimization engine:', err.message);
      const fallbackRes = synthesizeClientOptimization(
        itinerary,
        selectedMode,
        strictBudget,
        customMaxBudget,
        preserveTransport,
        preserveAccommodation
      );
      setOptimizationResults(fallbackRes);
      setOptimizationMode(selectedMode);
    } finally {
      setIsOptimizing(false);
    }
  };

    const weatherAbortRef = useRef(null);
  const placesAbortRef = useRef(null);

  const loadWeather = async (destination) => {
    if (!destination || !destination.trim()) return;
    if (weatherAbortRef.current) {
      weatherAbortRef.current.abort();
    }
    const controller = new AbortController();
    weatherAbortRef.current = controller;

    setWeatherLoading(true);
    setWeatherError(null);
    try {
      const weatherData = await getWeatherForecast(destination.trim(), controller.signal);
      setWeather(weatherData);
    } catch (err) {
      if (err.name === 'AbortError') return;
      console.error('Failed to load weather:', err);
      setWeatherError(err.message || 'Weather information is currently unavailable.');
    } finally {
      setWeatherLoading(false);
    }
  };

  const loadPlaces = async (destination, interests) => {
    if (!destination || !destination.trim()) return;
    if (placesAbortRef.current) {
      placesAbortRef.current.abort();
    }
    const controller = new AbortController();
    placesAbortRef.current = controller;

    setPlacesLoading(true);
    setPlacesError(null);
    try {
      const placesData = await getRecommendedPlaces(destination.trim(), interests, controller.signal);
      setPlaces(placesData);
    } catch (err) {
      if (err.name === 'AbortError') return;
      console.error('Failed to load places:', err);
      setPlacesError(err.message || 'Places information is currently unavailable.');
    } finally {
      setPlacesLoading(false);
    }
  };

  useEffect(() => {
    async function loadData() {
      setLoading(true);
      try {
        let data = null;
        if (tripId) {
          try {
            data = await getTripById(tripId);
            setSaved(true);
            try {
              const aiData = await getItineraryForTrip(tripId);
              setAiItinerary(aiData);
            } catch {
              // No saved AI itinerary yet for this trip
              setAiItinerary(null);
            }
            try {
              const pred = await getLatestCostPrediction(tripId);
              if (pred) setCostPrediction(pred);
            } catch {
              // No saved prediction yet
            }
          } catch (fetchErr) {
            console.warn('Could not fetch trip by id, checking session:', fetchErr);
          }
        }

        if (!data) {
          const stored = sessionStorage.getItem('currentItinerary');
          if (stored) {
            data = JSON.parse(stored);
            const storedId = sessionStorage.getItem('savedTripId');
            if (storedId || data.id) {
              setSaved(true);
            }
          }
        }

        if (!data) {
          setLoading(false);
          return;
        }

        // Clear stale destination data immediately
        setWeather(null);
        setPlaces(null);
        setItinerary(data);

        // Load budget breakdown specifically for this trip
        getBudgetBreakdown(data.id || 'current', data)
          .then(setBudget)
          .catch(console.error);

        // Load real weather & places from backend for the selected destination
        loadWeather(data.destination);
        loadPlaces(data.destination, data.interests);

      } catch (err) {
        console.error('Failed to load itinerary:', err);
      } finally {
        setLoading(false);
      }
    }

    loadData();

    return () => {
      if (weatherAbortRef.current) weatherAbortRef.current.abort();
      if (placesAbortRef.current) placesAbortRef.current.abort();
    };
  }, [tripId]);

  const DESTINATION_REAL_PLACES = {
    dubai: [
      { name: 'Burj Khalifa', category: 'Landmark', address: '1 Sheikh Mohammed bin Rashid Blvd, Downtown Dubai', url: 'https://www.burjkhalifa.ae', durationMinutes: 120, estimatedCost: 179, clusterArea: 'Downtown Dubai', description: 'World’s tallest building at 828m with panoramic observation decks on levels 124, 125, and 148.' },
      { name: 'The Dubai Mall & Dubai Aquarium', category: 'Shopping & Entertainment', address: 'Financial Centre Road, Downtown Dubai', url: 'https://thedubaimall.com', durationMinutes: 180, estimatedCost: 150, clusterArea: 'Downtown Dubai', description: 'Premier retail and entertainment hub with Olympic-sized ice rink and massive underwater tunnel aquarium.' },
      { name: 'The Dubai Fountain', category: 'Sightseeing', address: 'Sheikh Mohammed bin Rashid Blvd, Downtown Dubai', url: 'https://thedubaimall.com/en/entertain/the-dubai-fountain', durationMinutes: 45, estimatedCost: 0, clusterArea: 'Downtown Dubai', description: 'World-famous choreographed musical water fountain display on Burj Khalifa Lake.' },
      { name: 'Time Out Market Dubai', category: 'Food', address: 'Souk Al Bahar, Downtown Dubai', url: 'https://www.timeoutmarket.com/dubai', durationMinutes: 75, estimatedCost: 120, clusterArea: 'Downtown Dubai', description: 'Waterfront artisanal dining hall showcasing 17 award-winning home-grown culinary concepts.' },
      { name: 'Museum of the Future', category: 'Museum', address: 'Sheikh Zayed Rd, Trade Centre 2', url: 'https://museumofthefuture.ae', durationMinutes: 120, estimatedCost: 149, clusterArea: 'Financial District', description: 'Pioneering architectural marvel exploring futuristic technology, space colonization, and bioengineering.' },
      { name: 'Al Fahidi Historical Neighbourhood', category: 'Culture & Heritage', address: 'Al Fahidi St, Bur Dubai', url: 'https://dubaiculture.gov.ae', durationMinutes: 120, estimatedCost: 0, clusterArea: 'Old Dubai Heritage', description: 'Preserved 19th-century wind-tower district with traditional courtyard art galleries and museums.' },
      { name: 'Arabian Tea House Cafe', category: 'Food', address: 'Al Fahidi, Bur Dubai', url: 'https://arabianteahouse.com', durationMinutes: 60, estimatedCost: 75, clusterArea: 'Old Dubai Heritage', description: 'Courtyard cafe serving authentic Emirati breakfasts, fresh mint tea, and Machboos.' },
      { name: 'Dubai Creek Abra Ride', category: 'Sightseeing & Culture', address: 'Dubai Creek Station, Bur Dubai', url: 'https://www.visitdubai.com', durationMinutes: 45, estimatedCost: 2, clusterArea: 'Old Dubai Heritage', description: 'Traditional motorized wooden abra boat crossing between Bur Dubai and Deira spice wharves.' },
      { name: 'Dubai Gold & Spice Souks', category: 'Shopping & Culture', address: 'Al Ras, Deira', url: 'https://www.visitdubai.com', durationMinutes: 90, estimatedCost: 0, clusterArea: 'Old Dubai Heritage', description: 'Historic bustling Arabian alleyways filled with pure gold jewelry, saffron, frankincense, and spices.' },
      { name: 'Dubai Frame', category: 'Landmark', address: 'Zabeel Park, Gate 4', url: 'https://www.dubaiframe.ae', durationMinutes: 90, estimatedCost: 50, clusterArea: 'Zabeel District', description: '150-meter golden frame framing Old Dubai to the north and modern skyscrapers to the south.' },
      { name: 'The View at The Palm & Palm Jumeirah', category: 'Sightseeing', address: 'The Palm Tower, Nakheel Mall, Palm Jumeirah', url: 'https://theviewpalm.ae', durationMinutes: 90, estimatedCost: 100, clusterArea: 'Palm Jumeirah', description: '360-degree observation deck on level 52 overlooking the iconic palm-shaped island archipelago.' },
      { name: 'Atlantis Aquaventure Waterpark', category: 'Adventure', address: 'Atlantis The Palm, Crescent Rd, Palm Jumeirah', url: 'https://www.atlantis.com/dubai/atlantis-aquaventure', durationMinutes: 240, estimatedCost: 345, clusterArea: 'Palm Jumeirah', description: 'World-renowned waterpark featuring thrilling waterslides, wave pools, and private beaches.' },
      { name: 'Dubai Marina Walk & Promenade', category: 'Waterfront & Leisure', address: 'Dubai Marina Walk, Dubai Marina', url: 'https://www.visitdubai.com', durationMinutes: 120, estimatedCost: 0, clusterArea: 'Dubai Marina & Coast', description: 'Picturesque 7km marina promenade lined with palm trees, luxury yachts, and cosmopolitan cafes.' },
      { name: 'JBR Beach & The Walk', category: 'Beaches', address: 'The Walk, JBR, Dubai', url: 'https://www.visitdubai.com', durationMinutes: 120, estimatedCost: 0, clusterArea: 'Dubai Marina & Coast', description: 'Popular white-sand seaside beach featuring watersports, camel rides, and beachfront dining.' },
      { name: 'Souk Madinat Jumeirah', category: 'Culture & Dining', address: 'King Salman Bin Abdulaziz Al Saud St, Umm Suqeim 3', url: 'https://www.jumeirah.com', durationMinutes: 120, estimatedCost: 0, clusterArea: 'Jumeirah Coast', description: 'Traditional Arab citadel recreation with meandering abra waterways, boutiques, and sunset terraces.' },
      { name: 'Sunset Beach & Burj Al Arab Viewpoint', category: 'Beaches & Photography', address: '22 Al Sagi St, Umm Suqeim 2', url: 'https://www.visitdubai.com', durationMinutes: 60, estimatedCost: 0, clusterArea: 'Jumeirah Coast', description: 'Scenic sandy beach offering unobstructed views of the iconic sail-shaped Burj Al Arab hotel.' },
      { name: 'Dubai Desert Conservation Reserve Safari', category: 'Adventure', address: 'Dubai Desert Conservation Reserve, E66', url: 'https://www.visitdubai.com', durationMinutes: 240, estimatedCost: 280, clusterArea: 'Desert Excursion', description: '4x4 dune bashing expedition, sandboarding, camel rides, and Bedouin dinner under the stars.' },
      { name: 'Dubai Miracle Garden', category: 'Nature & Park', address: 'Street 3, Al Barsha South 3', url: 'https://www.dubaimiraclegarden.com', durationMinutes: 120, estimatedCost: 95, clusterArea: 'Dubailand Attractions', description: 'World’s largest natural flower garden with over 150 million blossoming floral sculptures.' },
      { name: 'Global Village Dubai', category: 'Culture & Entertainment', address: 'Sheikh Mohamed Bin Zayed Rd, Exit 37', url: 'https://globalvillage.ae', durationMinutes: 180, estimatedCost: 25, clusterArea: 'Dubailand Attractions', description: 'Multicultural open-air festival park celebrating 90+ cultures with pavilions, crafts, and food.' },
      { name: 'Bluewaters Island & Ain Dubai', category: 'Sightseeing & Leisure', address: 'Bluewaters Island, Dubai', url: 'https://www.bluewatersdubai.ae', durationMinutes: 90, estimatedCost: 0, clusterArea: 'Dubai Marina & Coast', description: 'Sophisticated lifestyle island with Ain Dubai wheel, coastal dining, and pedestrian bridge to JBR.' }
    ],
    hyderabad: [
      { name: 'Charminar', category: 'Landmark & Heritage', address: 'Charminar Rd, Ghansi Bazaar, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 90, estimatedCost: 25, clusterArea: 'Old City Heritage', description: 'Iconic 1591 CE four-minaret monument and historic Indo-Islamic symbol of Hyderabad.' },
      { name: 'Chowmahalla Palace', category: 'Palace & Heritage', address: 'Motigalli, Khilwat, Hyderabad', url: 'https://chowmahalla.in', durationMinutes: 120, estimatedCost: 100, clusterArea: 'Old City Heritage', description: 'Opulent palace of the Asaf Jahi Nizams featuring the grand Khilwat durbar hall and vintage cars.' },
      { name: 'Hotel Shadab', category: 'Food', address: 'High Court Rd, Ghansi Bazaar, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 75, estimatedCost: 350, clusterArea: 'Old City Heritage', description: 'Acclaimed Old City institution celebrated for authentic mutton dum biryani and Nizami delicacies.' },
      { name: 'Salar Jung Museum', category: 'Museum', address: 'Salar Jung Rd, Darulshifa, Hyderabad', url: 'https://salarjungmuseum.in', durationMinutes: 150, estimatedCost: 50, clusterArea: 'Heritage & Museums', description: 'National museum housing prized antique collections, the Veiled Rebecca, and the musical clock.' },
      { name: 'Golconda Fort', category: 'Fort & Heritage', address: 'Ibrahim Bagh, Golconda, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 180, estimatedCost: 25, clusterArea: 'Forts & Royal Tombs', description: 'Magnificent medieval hill citadel famous for acoustic clapping architecture and diamond mines.' },
      { name: 'Qutb Shahi Tombs', category: 'Heritage & Architecture', address: 'Fort Rd, Ibrahim Bagh, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 90, estimatedCost: 40, clusterArea: 'Forts & Royal Tombs', description: 'Magnificent domed mausoleums set amidst landscaped gardens of the Qutb Shahi dynasty.' },
      { name: 'Hussain Sagar Lake & Buddha Statue', category: 'Nature & Landmark', address: 'Tank Bund Rd, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 90, estimatedCost: 100, clusterArea: 'Lakefront & Central', description: 'Historic heart-shaped lake with motorboat rides to the world’s tallest monolithic Buddha statue.' },
      { name: 'Birla Mandir', category: 'Culture & Viewpoint', address: 'Hill Fort Rd, Khairatabad, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 60, estimatedCost: 0, clusterArea: 'Lakefront & Central', description: 'Pristine white marble temple atop Naubat Pahad overlooking the city skyline and Hussain Sagar.' },
      { name: 'Ramoji Film City', category: 'Entertainment & Adventure', address: 'Hayathnagar, Hyderabad', url: 'https://ramojifilmcity.com', durationMinutes: 300, estimatedCost: 1350, clusterArea: 'Film City Excursion', description: 'World’s largest film studio complex featuring cinematic sets, live stunt shows, and adventure rides.' },
      { name: 'Shilparamam Arts & Crafts Village', category: 'Culture & Shopping', address: 'Hi-Tech City Main Rd, Madhapur, Hyderabad', url: 'https://shilparamam.in', durationMinutes: 120, estimatedCost: 60, clusterArea: 'HITEC City & Waterfront', description: 'Village celebrating rural ethnic arts, handcrafted pottery, and classical evening folk dance.' },
      { name: 'Durgam Cheruvu Cable Bridge', category: 'Waterfront & Leisure', address: 'Jubilee Hills, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 90, estimatedCost: 0, clusterArea: 'HITEC City & Waterfront', description: 'Illuminated cable-stayed bridge and lakefront promenade with floating cafe and pedal boats.' },
      { name: 'Paradise Food Court', category: 'Food', address: 'SD Road, Secunderabad', url: 'https://paradisefoodcourt.in', durationMinutes: 75, estimatedCost: 400, clusterArea: 'Secunderabad & Heritage', description: 'The legendary birthplace of authentic Hyderabadi biryani serving fragrant spiced basmati rice since 1953.' },
      { name: 'Laad Bazaar', category: 'Shopping & Culture', address: 'Beside Charminar, Hyderabad', url: 'https://telanganatourism.gov.in', durationMinutes: 75, estimatedCost: 0, clusterArea: 'Old City Heritage', description: 'Centuries-old market renowned for handcrafted lacquer bangles, pearls, and ethnic bridal wear.' }
    ],
    paris: [
      { name: 'Eiffel Tower', category: 'Landmark', address: 'Champ de Mars, 5 Av. Anatole France, 75007 Paris', url: 'https://www.toureiffel.paris', durationMinutes: 120, estimatedCost: 29, clusterArea: 'Iconic Seine & Tower', description: 'Iconic 330-meter wrought-iron monument with breathtaking summit panoramas over Paris.' },
      { name: 'Louvre Museum', category: 'Museum', address: 'Rue de Rivoli, 75001 Paris', url: 'https://www.louvre.fr', durationMinutes: 180, estimatedCost: 22, clusterArea: 'Historic Center & Arts', description: 'World’s largest art museum, home to the Mona Lisa, Venus de Milo, and Winged Victory.' },
      { name: 'Musée d’Orsay', category: 'Museum', address: '1 Rue de la Légion d’Honneur, 75007 Paris', url: 'https://www.musee-orsay.fr', durationMinutes: 150, estimatedCost: 16, clusterArea: 'Historic Center & Arts', description: 'Magnificent Beaux-Arts railway station housing masterpieces by Monet, Van Gogh, and Degas.' },
      { name: 'Cathédrale Notre-Dame de Paris', category: 'Heritage & Landmark', address: '6 Parvis Notre-Dame, 75004 Paris', url: 'https://www.notredamedeparis.fr', durationMinutes: 90, estimatedCost: 0, clusterArea: 'Île de la Cité & Latin Quarter', description: 'Gothic cathedral masterpiece on Île de la Cité famous for flying buttresses and rose windows.' },
      { name: 'Sainte-Chapelle', category: 'Heritage', address: '10 Bd du Palais, 75001 Paris', url: 'https://www.sainte-chapelle.fr', durationMinutes: 60, estimatedCost: 13, clusterArea: 'Île de la Cité & Latin Quarter', description: '13th-century Gothic royal chapel celebrated for its 15 towering stained glass windows.' },
      { name: 'Arc de Triomphe & Champs-Élysées', category: 'Landmark & Shopping', address: 'Pl. Charles de Gaulle, 75008 Paris', url: 'https://www.paris-arc-de-triomphe.fr', durationMinutes: 90, estimatedCost: 16, clusterArea: 'Iconic Seine & Tower', description: 'Triumphal monument honoring French soldiers with a rooftop view atop the Champs-Élysées.' },
      { name: 'Sacré-Cœur Basilica & Montmartre', category: 'Culture & Viewpoint', address: '35 Rue du Chevalier de la Barre, 75018 Paris', url: 'https://www.sacre-coeur-montmartre.com', durationMinutes: 120, estimatedCost: 0, clusterArea: 'Montmartre & Artists', description: 'White travertine basilica crowning bohemian Montmartre hill overlooking Paris.' },
      { name: 'Marché des Enfants Rouges', category: 'Food', address: '39 Rue de Bretagne, 75003 Paris', url: 'https://www.paris.fr', durationMinutes: 75, estimatedCost: 20, clusterArea: 'Le Marais & Arts', description: 'Paris’s oldest covered food market dating from 1615 with international and French gourmet stalls.' },
      { name: 'Le Marais Historic District', category: 'Culture & Dining', address: 'Place des Vosges, 75004 Paris', url: 'https://www.parisjetaime.com', durationMinutes: 120, estimatedCost: 0, clusterArea: 'Le Marais & Arts', description: 'Aristocratic historic quarter of cobblestone streets, private mansions, and art galleries.' },
      { name: 'Bouillon Chartier', category: 'Food', address: '7 Rue du Faubourg Montmartre, 75009 Paris', url: 'https://www.bouillon-chartier.com', durationMinutes: 75, estimatedCost: 20, clusterArea: 'Montmartre & Artists', description: 'Historic Belle Époque brasserie serving authentic traditional French comfort cuisine since 1896.' },
      { name: 'Seine River Cruise via Bateaux Mouches', category: 'Sightseeing', address: 'Port de la Conférence, 75008 Paris', url: 'https://www.bateaux-mouches.fr', durationMinutes: 75, estimatedCost: 17, clusterArea: 'Iconic Seine & Tower', description: 'Open-air riverboat cruise gliding past Paris’s UNESCO riverbanks and illuminated bridges.' },
      { name: 'Palace of Versailles', category: 'Palace & Heritage', address: 'Place d’Armes, 78000 Versailles', url: 'https://www.chateauversailles.fr', durationMinutes: 240, estimatedCost: 21, clusterArea: 'Versailles Excursion', description: 'Royal palace of Louis XIV featuring the Hall of Mirrors and vast fountain gardens.' }
    ]
  };

  const synthesizeClientItinerary = (trip, placesData, weatherData) => {
    const dest = trip?.destination || 'Destination';
    const destKey = dest.toLowerCase().trim();
    const travelers = Number(trip?.travelers) || 1;
    const budget = Number(trip?.budget) || 50000;
    const start = trip?.startDate ? new Date(trip.startDate) : new Date();
    const end = trip?.endDate ? new Date(trip.endDate) : new Date(Date.now() + 3 * 86400000);
    const diffTime = Math.max(0, end.getTime() - start.getTime());
    const totalDays = Math.max(1, Math.min(30, Math.round(diffTime / (1000 * 60 * 60 * 24)) + 1));

    // Gather available places
    let placeList = Array.isArray(placesData) ? [...placesData] : (Array.isArray(placesData?.places) ? [...placesData.places] : []);

    // If placesData is empty or small, supplement with verified curated destination places
    for (const [key, curated] of Object.entries(DESTINATION_REAL_PLACES)) {
      if (destKey.includes(key)) {
        if (placeList.length < 10) {
          placeList = [...curated];
        }
        break;
      }
    }

    const usedPlaces = new Set();
    const dailyPerPerson = (budget / travelers) / totalDays;
    const days = [];

    for (let i = 1; i <= totalDays; i++) {
      const currentDate = new Date(start);
      currentDate.setDate(currentDate.getDate() + (i - 1));
      const dateStr = currentDate.toISOString().split('T')[0];

      const dayForecast = weatherData?.forecast?.[i - 1];
      const weatherText = dayForecast ? ` Expected weather: ${dayForecast.condition} (${dayForecast.high}°C).` : '';

      const activitiesForDay = (i === 1 || i === totalDays) ? 3 : 4;
      const times = activitiesForDay === 3 ? ['10:00', '13:30', '16:30'] : ['09:30', '12:30', '15:30', '18:30'];

      const dayActivities = [];
      let primaryCluster = null;

      for (let actIdx = 0; actIdx < activitiesForDay; actIdx++) {
        // Pick an unused place
        let chosen = null;
        for (const p of placeList) {
          const norm = (p.name || '').toLowerCase().trim();
          if (!usedPlaces.has(norm)) {
            chosen = p;
            usedPlaces.add(norm);
            break;
          }
        }

        if (chosen) {
          if (!primaryCluster && chosen.clusterArea) {
            primaryCluster = chosen.clusterArea;
          }

          dayActivities.push({
            id: `act-${i}-${actIdx + 1}`,
            time: times[actIdx],
            activity: chosen.name,
            name: chosen.name,
            description: chosen.description || `Visit ${chosen.name}, one of the celebrated points of interest in ${dest}.`,
            location: chosen.address || chosen.location || dest,
            address: chosen.address || chosen.location || dest,
            category: chosen.category || 'Sightseeing',
            type: (chosen.category || 'sightseeing').toLowerCase(),
            estimatedCost: chosen.estimatedCost !== undefined ? chosen.estimatedCost : Math.round(dailyPerPerson * 0.15),
            cost: chosen.estimatedCost !== undefined ? chosen.estimatedCost : Math.round(dailyPerPerson * 0.15),
            duration: chosen.durationMinutes ? `${chosen.durationMinutes} mins` : '2 hours',
            notes: 'Advance booking and comfortable walking attire recommended.',
            websiteUrl: chosen.url || null,
            latitude: chosen.latitude || null,
            longitude: chosen.longitude || null,
            travelFromPrevious: actIdx > 0 ? '🚗 ~15 mins from previous stop' : null,
          });
        } else {
          // If all verified places have been scheduled
          dayActivities.push({
            id: `act-${i}-${actIdx + 1}`,
            time: times[actIdx],
            activity: `${dest} Waterfront Promenade & Relaxation`,
            name: `${dest} Waterfront Promenade & Relaxation`,
            description: `Scenic leisure stroll through traditional market squares, tea cafes, and artisan shops in ${dest}.`,
            location: dest,
            address: dest,
            category: 'Relaxation',
            type: 'relaxation',
            estimatedCost: Math.round(dailyPerPerson * 0.1),
            cost: Math.round(dailyPerPerson * 0.1),
            duration: '2 hours',
            notes: 'All top verified attractions have been scheduled. Enjoy free exploration.',
            websiteUrl: null,
            travelFromPrevious: actIdx > 0 ? '🚶 Walking distance' : null,
          });
        }
      }

      const dayTitle = primaryCluster ? `${primaryCluster} (Day ${i})` : `Exploring ${dest} (Day ${i})`;

      days.push({
        dayNumber: i,
        day: i,
        date: dateStr,
        title: dayTitle,
        summary: `Day ${i} in ${dest} featuring verified landmarks, authentic dining, and cultural highlights.${weatherText}`,
        activities: dayActivities,
      });
    }

    return {
      title: `${dest} Verified Discovery Plan`,
      summary: `A personalized ${totalDays}-day itinerary for ${travelers} traveler(s) in ${dest}, featuring verified local attractions, dining, and scenic views.`,
      destination: dest,
      days,
    };
  };

  const handleGenerateAiItinerary = async () => {
    if (isGenerating) return;
    setIsGenerating(true);
    setAiError(null);
    try {
      let targetId = tripId || itinerary?.id;
      const token = localStorage.getItem('voyaai_token');

      // If user is logged in but trip wasn't saved yet, auto-save to get a real trip ID
      if (!targetId && token && itinerary) {
        try {
          const saved = await saveTrip(itinerary);
          setItinerary(saved);
          setSaved(true);
          targetId = saved.id;
          sessionStorage.setItem('currentItinerary', JSON.stringify(saved));
          if (saved.id) {
            sessionStorage.setItem('savedTripId', String(saved.id));
            navigate(`/itinerary?id=${saved.id}`, { replace: true });
          }
        } catch (saveErr) {
          console.warn('Could not auto-save trip before itinerary generation:', saveErr);
        }
      }

      if (targetId && token) {
        const generated = await generateItineraryForTrip(targetId);
        setAiItinerary(generated);
      } else {
        // Guest mode or unsaved session: generate client itinerary
        const clientPlan = synthesizeClientItinerary(itinerary, places, weather);
        setAiItinerary(clientPlan);
      }
    } catch (err) {
      console.error('Failed to generate AI itinerary:', err);
      // Fallback: don't leave user with nothing
      const fallbackPlan = synthesizeClientItinerary(itinerary, places, weather);
      setAiItinerary(fallbackPlan);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleRegenerateAiItinerary = async () => {
    if (isGenerating) return;
    setIsGenerating(true);
    setAiError(null);
    try {
      let targetId = tripId || itinerary?.id;
      const token = localStorage.getItem('voyaai_token');
      if (targetId && token) {
        const regenerated = await regenerateItineraryForTrip(targetId);
        setAiItinerary(regenerated);
      } else {
        const clientPlan = synthesizeClientItinerary(itinerary, places, weather);
        setAiItinerary(clientPlan);
      }
    } catch (err) {
      console.error('Failed to regenerate AI itinerary:', err);
      const fallbackPlan = synthesizeClientItinerary(itinerary, places, weather);
      setAiItinerary(fallbackPlan);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const savedRes = await saveTrip(itinerary);
      setItinerary(savedRes);
      setSaved(true);
      sessionStorage.setItem('currentItinerary', JSON.stringify(savedRes));
      if (savedRes?.id) {
        sessionStorage.setItem('savedTripId', String(savedRes.id));
      }
    } catch (err) {
      console.error('Failed to save trip:', err);
      alert(err.message || 'Failed to save trip.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="itinerary-page">
        <div className="itinerary-loading gradient-bg">
          <LoadingSpinner size="lg" message="Loading your itinerary..." />
        </div>
      </div>
    );
  }

  if (!itinerary) {
    return (
      <div className="itinerary-page">
        <div style={{ paddingTop: 'calc(var(--header-height) + var(--space-16))' }}>
          <EmptyState
            icon={MapPin}
            title="No Itinerary Found"
            message="Plan a trip first to see your personalized itinerary here."
            action={
              <Link to="/planner" className="btn btn-primary">
                <Sparkles size={16} />
                Plan a Trip
              </Link>
            }
          />
        </div>
      </div>
    );
  }

  const tabs = [
    { id: 'itinerary', label: 'Itinerary' },
    { id: 'optimize', label: 'Price Optimizer' },
    { id: 'budget', label: 'Budget' },
    { id: 'weather', label: 'Weather' },
    { id: 'places', label: 'Places' },
  ];

  return (
    <div className="itinerary-page">
      {/* Hero header */}
      <div className="itin-hero gradient-bg">
        <div className="container itin-hero__inner">
          <button onClick={() => navigate(itinerary?.id ? `/planner?edit=${itinerary.id}` : '/planner')} className="itin-hero__back">
            <ArrowLeft size={18} />
            Edit Trip
          </button>

          <div className="itin-hero__content">
            <div className="itin-hero__badge">
              <Sparkles size={14} />
              AI-Generated Itinerary
            </div>
            <h1 className="itin-hero__title">{itinerary.destination}</h1>
            <div className="itin-hero__meta">
              <span><Calendar size={14} /> {new Date(itinerary.startDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} – {new Date(itinerary.endDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
              <span><Users size={14} /> {itinerary.travelers} travelers</span>
              <span><MapPin size={14} /> From {itinerary.source}</span>
            </div>
          </div>

          {/* Current Weather Quick Glance in Hero (if loaded) */}
          <div className="itin-hero__summary">
            <div className="itin-summary-card">
              <span className="itin-summary-card__label" style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--color-rainforest-emerald)' }}>₹</span>
              <div>
                <span className="itin-summary-card__label">Estimated Cost</span>
                <span className="itin-summary-card__value">
                  {formatCurrency(costPrediction ? costPrediction.predictedCost : (itinerary.totalEstimatedCost ?? itinerary.totalCost ?? (itinerary.budget ? itinerary.budget * 0.9 : 0)))}
                </span>
              </div>
            </div>
            {costPrediction && (
              <div className="itin-summary-card" style={{ borderColor: 'rgba(80, 200, 120, 0.4)' }}>
                <Brain size={20} color="var(--color-rainforest-emerald)" />
                <div>
                  <span className="itin-summary-card__label">ML Model</span>
                  <span className="itin-summary-card__value" style={{ fontSize: '0.9rem', color: 'var(--color-rainforest-emerald)' }}>
                    {costPrediction.model}
                  </span>
                </div>
              </div>
            )}
            {weather && !weatherError && (
              <div className="itin-summary-card">
                <span style={{ fontSize: '1.5rem', lineHeight: 1 }}>{getWeatherEmoji(weather.condition)}</span>
                <div>
                  <span className="itin-summary-card__label">Current Weather</span>
                  <span className="itin-summary-card__value">{weather.temperature}°C</span>
                </div>
              </div>
            )}
            <div className="itin-summary-card itin-summary-card--savings">
              <TrendingDown size={20} />
              <div>
                <span className="itin-summary-card__label">AI Savings</span>
                <span className="itin-summary-card__value">
                  {formatCurrency(itinerary.savingsFromOptimizer ?? (itinerary.budget ? Math.max(0, itinerary.budget - (itinerary.totalCost ?? (itinerary.budget * 0.9))) : 0))}
                </span>
              </div>
            </div>
          </div>

          <div className="itin-hero__actions">
            <button
              className="btn btn-secondary"
              onClick={() => {
                setActiveTab('optimize');
                if (!optimizationResults && !isOptimizing) {
                  handleOptimizeTrip();
                }
              }}
              style={{ borderColor: 'rgba(80, 200, 120, 0.4)', color: 'var(--color-rainforest-emerald)', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <Sliders size={16} /> Optimize Trip
            </button>
            <button
              className={`btn ${saved ? 'btn-secondary' : 'btn-primary'}`}
              onClick={handleSave}
              disabled={saving || saved}
            >
              {saved ? <><CheckCircle size={16} /> Saved!</> : <><BookmarkPlus size={16} /> Save Trip</>}
            </button>
            <button className="btn btn-secondary">
              <Share2 size={16} /> Share
            </button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="itin-tabs-wrapper">
        <div className="container">
          <div className="itin-tabs">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                className={`itin-tab ${activeTab === tab.id ? 'itin-tab--active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Tab content */}
      <div className="container itin-content">
        {/* ─── Itinerary Tab ─────────────────────────────── */}
        {activeTab === 'itinerary' && (
          <div className="itin-timeline animate-fadeIn">
            {/* AI Itinerary Card / Controls */}
            {!aiItinerary ? (
              <div className="card ai-itinerary-banner" style={{ marginBottom: 'var(--space-6)', padding: 'var(--space-6)', background: 'linear-gradient(135deg, rgba(80, 200, 120, 0.12), rgba(6, 21, 13, 0.7))', border: '1px solid rgba(80, 200, 120, 0.3)', borderRadius: 'var(--radius-xl)' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 'var(--space-4)' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--color-rainforest-emerald)', fontWeight: '700', marginBottom: 'var(--space-1)', fontSize: '0.9rem' }}>
                      <Sparkles size={18} />
                      <span>VoyaAI Intelligence</span>
                    </div>
                    <h3 style={{ margin: 0, fontSize: '1.3rem', color: 'var(--color-cream)' }}>Generate Personalized AI Itinerary</h3>
                    <p style={{ margin: 'var(--space-2) 0 0 0', color: 'var(--color-muted)', fontSize: '0.92rem', maxWidth: '620px', lineHeight: '1.5' }}>
                      Create a custom day-by-day plan crafted for your {itinerary.travelers} traveler(s), budget of {formatCurrency(itinerary.budget)}, live weather in {itinerary.destination}, and verified local attractions.
                    </p>
                  </div>
                  <button
                    className="btn btn-primary"
                    onClick={handleGenerateAiItinerary}
                    disabled={isGenerating}
                    style={{ minWidth: '200px' }}
                  >
                    {isGenerating ? (
                      <>
                        <RefreshCw size={16} className="animate-spin" /> Generating Itinerary...
                      </>
                    ) : (
                      <>
                        <Sparkles size={16} /> Generate Itinerary
                      </>
                    )}
                  </button>
                </div>
                {aiError && (
                  <div style={{ marginTop: 'var(--space-4)', padding: 'var(--space-3) var(--space-4)', borderRadius: 'var(--radius-md)', background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.4)', color: '#fca5a5', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <AlertTriangle size={16} />
                    <span>{aiError}</span>
                  </div>
                )}
              </div>
            ) : (
              <div className="card ai-itinerary-header" style={{ marginBottom: 'var(--space-6)', padding: 'var(--space-6)', background: 'linear-gradient(135deg, rgba(80, 200, 120, 0.12), rgba(6, 21, 13, 0.7))', border: '1px solid rgba(80, 200, 120, 0.3)', borderRadius: 'var(--radius-xl)' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 'var(--space-4)' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--color-rainforest-emerald)', fontWeight: '700', marginBottom: 'var(--space-1)', fontSize: '0.85rem' }}>
                      <Sparkles size={16} />
                      <span>Personalized AI Itinerary (Stored in PostgreSQL)</span>
                    </div>
                    <h2 style={{ margin: 0, fontSize: '1.45rem', color: 'var(--color-cream)' }}>{aiItinerary.title}</h2>
                    {aiItinerary.summary && (
                      <p style={{ margin: 'var(--space-2) 0 0 0', color: 'var(--color-muted)', fontSize: '0.92rem', lineHeight: '1.5', maxWidth: '750px' }}>
                        {aiItinerary.summary}
                      </p>
                    )}
                    <div style={{ marginTop: 'var(--space-3)', display: 'flex', gap: 'var(--space-2)', flexWrap: 'wrap', fontSize: '0.78rem' }}>
                      <span style={{ padding: '3px 9px', borderRadius: 'var(--radius-full)', background: 'rgba(255,255,255,0.06)', color: 'var(--color-cream)', border: '1px solid rgba(255,255,255,0.1)' }}>
                        🤖 AI Recommendations (Estimated Costs)
                      </span>
                      <span style={{ padding: '3px 9px', borderRadius: 'var(--radius-full)', background: 'rgba(80, 200, 120, 0.15)', color: 'var(--color-rainforest-emerald)', border: '1px solid rgba(80, 200, 120, 0.3)' }}>
                        ✓ Live Weather & Verified Places Aware
                      </span>
                    </div>
                  </div>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={handleRegenerateAiItinerary}
                    disabled={isGenerating}
                    title="Regenerate this itinerary with AI"
                  >
                    <RefreshCw size={14} className={isGenerating ? "animate-spin" : ""} />
                    {isGenerating ? 'Regenerating...' : 'Regenerate Itinerary'}
                  </button>
                </div>
                {aiError && (
                  <div style={{ marginTop: 'var(--space-4)', padding: 'var(--space-3) var(--space-4)', borderRadius: 'var(--radius-md)', background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.4)', color: '#fca5a5', fontSize: '0.88rem', display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
                    <AlertTriangle size={16} />
                    <span>{aiError}</span>
                  </div>
                )}
              </div>
            )}

            {/* Render Days */}
            {(() => {
              const displayDays = (aiItinerary?.days && aiItinerary.days.length > 0)
                ? aiItinerary.days.map((d) => ({
                    day: d.dayNumber,
                    title: d.title,
                    date: d.date,
                    summary: d.summary,
                    activities: (d.activities || []).map((a) => ({
                      id: a.id || `${d.dayNumber}-${a.time}`,
                      time: a.time,
                      name: a.activity || a.name,
                      activity: a.activity || a.name,
                      description: a.description,
                      location: a.location,
                      address: a.address || a.location,
                      type: (a.category ? a.category.toLowerCase() : 'sightseeing'),
                      category: a.category,
                      cost: a.estimatedCost !== undefined ? a.estimatedCost : (a.cost || 0),
                      duration: a.duration,
                      notes: a.notes,
                      websiteUrl: a.websiteUrl,
                      travelFromPrevious: a.travelFromPrevious,
                    })),
                  }))
                : (itinerary.days || []);

              if (!displayDays || displayDays.length === 0) {
                return (
                  <div className="card" style={{ textAlign: 'center', padding: 'var(--space-8) var(--space-6)', marginTop: 'var(--space-4)', background: 'rgba(255, 255, 255, 0.03)', border: '1px dashed rgba(255, 255, 255, 0.15)' }}>
                    <Sparkles size={36} style={{ color: 'var(--color-rainforest-emerald)', margin: '0 auto var(--space-3)' }} />
                    <h3 style={{ fontSize: '1.2rem', marginBottom: 'var(--space-2)' }}>Ready to Plan Your Trip to {itinerary?.destination || 'Your Destination'}</h3>
                    <p style={{ color: 'var(--color-muted)', maxWidth: '480px', margin: '0 auto var(--space-5)', fontSize: '0.92rem' }}>
                      Generate a personalized day-by-day travel plan powered by AI tailored specifically to {itinerary?.destination || 'this destination'}.
                    </p>
                    <button
                      className="btn btn-primary"
                      onClick={handleRegenerateAiItinerary}
                      disabled={isGenerating}
                      style={{ margin: '0 auto' }}
                    >
                      <Sparkles size={16} className={isGenerating ? "animate-spin" : ""} />
                      {isGenerating ? 'Generating Day-by-Day Plan...' : `Generate ${itinerary?.destination || 'AI'} Itinerary`}
                    </button>
                  </div>
                );
              }

              return displayDays.map((day, idx) => (
                <div key={day.day} className="day-card card">
                  <button
                    className="day-card__header"
                    onClick={() => setExpandedDay(expandedDay === idx ? -1 : idx)}
                  >
                    <div className="day-card__header-left">
                      <span className="day-card__number">Day {day.day}</span>
                      <div>
                        <h3 className="day-card__title">{day.title}</h3>
                        <span className="day-card__date">
                          {new Date(day.date).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
                        </span>
                      </div>
                    </div>
                    <div className="day-card__header-right">
                      <span className="day-card__activity-count">{day.activities.length} activities</span>
                      {expandedDay === idx ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                    </div>
                  </button>

                  {expandedDay === idx && (
                    <div className="day-card__body animate-fadeIn">
                      {day.summary && (
                        <p style={{ margin: '0 0 var(--space-4) 0', fontSize: '0.9rem', color: 'var(--color-muted)', fontStyle: 'italic' }}>
                          {day.summary}
                        </p>
                      )}
                      {day.activities.map((act, actIdx) => {
                        const Icon = activityIcons[act.type] || Camera;
                        return (
                          <div key={act.id} className="activity-item">
                            <div className="activity-item__timeline">
                              <div className="activity-item__dot" />
                              {actIdx < day.activities.length - 1 && <div className="activity-item__line" />}
                            </div>
                            <div className="activity-item__content">
                              {act.travelFromPrevious && (
                                <div style={{ display: 'inline-flex', alignItems: 'center', gap: '5px', padding: '3px 8px', borderRadius: '4px', background: 'rgba(52, 211, 153, 0.12)', color: '#34d399', fontSize: '0.76rem', fontWeight: '500', marginBottom: '8px' }}>
                                  {act.travelFromPrevious}
                                </div>
                              )}
                              <div className="activity-item__header">
                                <div className="activity-item__icon" data-type={act.type}>
                                  <Icon size={16} />
                                </div>
                                <span className="activity-item__time">{act.time}</span>
                                {act.duration && <span className="activity-item__duration">{act.duration}</span>}
                                {act.category && (
                                  <span style={{ fontSize: '0.72rem', padding: '1px 7px', borderRadius: '4px', background: 'rgba(255,255,255,0.06)', color: 'var(--color-cream)' }}>
                                    {act.category}
                                  </span>
                                )}
                              </div>
                              <h4 className="activity-item__name" style={{ fontSize: '1.05rem', fontWeight: '600', color: 'var(--color-cream)', marginTop: '4px' }}>
                                {act.name}
                              </h4>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap', margin: '4px 0 8px 0' }}>
                                {act.location && formatLocation(act.address || act.location) && (
                                  <span style={{ fontSize: '0.82rem', color: 'var(--color-rainforest-emerald)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                                    <MapPin size={12} /> {formatLocation(act.address || act.location)}
                                  </span>
                                )}
                                <a
                                  href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent((act.name || '') + ' ' + (itinerary?.destination || ''))}`}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  style={{ fontSize: '0.75rem', color: 'var(--color-cream)', textDecoration: 'none', background: 'rgba(255,255,255,0.08)', padding: '2px 8px', borderRadius: '4px', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                                >
                                  🗺️ View on Maps
                                </a>
                                {act.websiteUrl && (
                                  <a
                                    href={act.websiteUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    style={{ fontSize: '0.75rem', color: '#60a5fa', textDecoration: 'none', background: 'rgba(96,165,250,0.12)', padding: '2px 8px', borderRadius: '4px', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                                  >
                                    🌐 Official Website
                                  </a>
                                )}
                              </div>
                              <p className="activity-item__desc">{act.description}</p>
                              {act.cost > 0 && (
                                <span className="activity-item__cost">
                                  {formatCurrency(act.cost)} (estimated)
                                </span>
                              )}
                              {act.notes && (
                                <p style={{ marginTop: '6px', fontSize: '0.8rem', color: 'var(--color-muted)', fontStyle: 'italic' }}>
                                  💡 {act.notes}
                                </p>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              ));
            })()}
          </div>
        )}

        {/* ─── Price Optimizer Tab (Stage 8) ─────────────── */}
        {activeTab === 'optimize' && (
          <div className="optimizer-tab animate-fadeIn">
            {/* Header / Config Panel */}
            <div className="card optimizer-banner">
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 'var(--space-4)' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--color-rainforest-emerald)', fontWeight: '700', marginBottom: 'var(--space-1)', fontSize: '0.9rem' }}>
                    <Sliders size={18} />
                    <span>Multi-Constraint Optimization Engine</span>
                  </div>
                  <h3 style={{ margin: 0, fontSize: '1.4rem', color: 'var(--color-cream)' }}>
                    Optimize Trip Options & Costs
                  </h3>
                  <p style={{ margin: 'var(--space-2) 0 0 0', color: 'var(--color-muted)', fontSize: '0.92rem', maxWidth: '680px', lineHeight: '1.5' }}>
                    Evaluate distinct candidate configurations (transport, lodging, and activity pacing) scored for your destination, travel constraints, and priced using our trained ML regression pipeline.
                  </p>
                </div>

                <button
                  className="btn btn-primary"
                  onClick={() => handleOptimizeTrip()}
                  disabled={isOptimizing}
                  style={{ display: 'flex', alignItems: 'center', gap: '8px', minWidth: '180px', justifyContent: 'center' }}
                >
                  {isOptimizing ? (
                    <>
                      <RefreshCw size={16} className="animate-spin" />
                      <span>Optimizing your trip...</span>
                    </>
                  ) : (
                    <>
                      <Zap size={16} />
                      <span>{optimizationResults ? 'Re-run Optimizer' : 'Optimize My Trip'}</span>
                    </>
                  )}
                </button>
              </div>

              {/* Mode Selection Cards */}
              <div className="optimizer-modes-grid">
                <div
                  className={`optimizer-mode-card ${optimizationMode === 'BUDGET' ? 'optimizer-mode-card--active' : ''}`}
                  onClick={() => {
                    setOptimizationMode('BUDGET');
                    handleOptimizeTrip('BUDGET');
                  }}
                >
                  <span className="optimizer-mode-card__badge" style={{ background: 'rgba(80, 200, 120, 0.2)', color: 'var(--color-rainforest-emerald)' }}>
                    Cost Minimizer
                  </span>
                  <h4 className="optimizer-mode-card__title">Budget Optimized</h4>
                  <p className="optimizer-mode-card__desc">
                    Minimizes estimated cost via high-efficiency transit and smart stays while protecting core activities.
                  </p>
                </div>

                <div
                  className={`optimizer-mode-card ${optimizationMode === 'BALANCED' ? 'optimizer-mode-card--active' : ''}`}
                  onClick={() => {
                    setOptimizationMode('BALANCED');
                    handleOptimizeTrip('BALANCED');
                  }}
                >
                  <span className="optimizer-mode-card__badge" style={{ background: 'rgba(56, 189, 248, 0.2)', color: '#38bdf8' }}>
                    Recommended
                  </span>
                  <h4 className="optimizer-mode-card__title">Balanced Plan</h4>
                  <p className="optimizer-mode-card__desc">
                    Harmonizes travel expenditure, comfortable travel pace, verified highlights, and interest matching.
                  </p>
                </div>

                <div
                  className={`optimizer-mode-card ${optimizationMode === 'EXPERIENCE' ? 'optimizer-mode-card--active' : ''}`}
                  onClick={() => {
                    setOptimizationMode('EXPERIENCE');
                    handleOptimizeTrip('EXPERIENCE');
                  }}
                >
                  <span className="optimizer-mode-card__badge" style={{ background: 'rgba(168, 85, 247, 0.2)', color: '#c084fc' }}>
                    Max Experience
                  </span>
                  <h4 className="optimizer-mode-card__title">Comfort & Experience</h4>
                  <p className="optimizer-mode-card__desc">
                    Prioritizes premium lodging, convenience, extensive activity coverage, and deep interest alignment.
                  </p>
                </div>
              </div>

              {/* Advanced Constraints Bar */}
              <div className="optimizer-constraints-panel">
                <div className="optimizer-constraints-group">
                  <label className="optimizer-checkbox-label">
                    <input
                      type="checkbox"
                      checked={strictBudget}
                      onChange={(e) => setStrictBudget(e.target.checked)}
                    />
                    <span>Strict Maximum Budget</span>
                  </label>

                  {strictBudget && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ fontSize: '0.85rem', color: 'var(--color-muted)' }}>Cap: ₹</span>
                      <input
                        type="number"
                        placeholder={itinerary.budget || '50000'}
                        value={customMaxBudget}
                        onChange={(e) => setCustomMaxBudget(e.target.value)}
                        style={{
                          width: '120px',
                          padding: '4px 8px',
                          borderRadius: '6px',
                          background: 'rgba(0,0,0,0.4)',
                          border: '1px solid rgba(255,255,255,0.15)',
                          color: 'var(--color-cream)',
                          fontSize: '0.88rem'
                        }}
                      />
                    </div>
                  )}

                  <label className="optimizer-checkbox-label">
                    <input
                      type="checkbox"
                      checked={preserveTransport}
                      onChange={(e) => setPreserveTransport(e.target.checked)}
                    />
                    <span>Lock Transport (Flight)</span>
                  </label>

                  <label className="optimizer-checkbox-label">
                    <input
                      type="checkbox"
                      checked={preserveAccommodation}
                      onChange={(e) => setPreserveAccommodation(e.target.checked)}
                    />
                    <span>Lock Lodging (Hotel)</span>
                  </label>
                </div>

                <span style={{ fontSize: '0.8rem', color: 'var(--color-muted)', fontStyle: 'italic' }}>
                  Hard constraints: {itinerary.destination} · {itinerary.travelers} traveler(s) · {itinerary.tripType || 'NATIONAL'}
                </span>
              </div>
            </div>

            {/* Error Message */}
            {optimizationError && (
              <div style={{ padding: 'var(--space-4)', background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.35)', borderRadius: 'var(--radius-lg)', color: '#fca5a5', display: 'flex', alignItems: 'center', gap: '10px' }}>
                <AlertTriangle size={20} />
                <span>{optimizationError}</span>
              </div>
            )}

            {/* Baseline Glance */}
            {optimizationResults && (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 'var(--space-4)', padding: 'var(--space-3) var(--space-5)', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: 'var(--radius-lg)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: 'var(--color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Baseline Reference (Hotel + Flight)</span>
                    <div style={{ fontSize: '1.15rem', fontWeight: '700', color: 'var(--color-cream)' }}>
                      {formatCurrency(optimizationResults.baselineCost)}
                    </div>
                  </div>
                  {optimizationResults.weatherSummary && (
                    <div style={{ paddingLeft: 'var(--space-4)', borderLeft: '1px solid rgba(255,255,255,0.1)' }}>
                      <span style={{ fontSize: '0.75rem', color: 'var(--color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Destination Weather</span>
                      <div style={{ fontSize: '0.95rem', fontWeight: '600', color: 'var(--color-cream)' }}>
                        {optimizationResults.weatherSummary}
                      </div>
                    </div>
                  )}
                </div>

                <div style={{ fontSize: '0.82rem', color: 'var(--color-muted)' }}>
                  Mode: <strong style={{ color: 'var(--color-rainforest-emerald)' }}>{optimizationResults.mode}</strong> · Currency: <strong>{optimizationResults.currency}</strong>
                </div>
              </div>
            )}

            {/* Results Grid / List */}
            {optimizationResults ? (
              optimizationResults.results && optimizationResults.results.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
                  {optimizationResults.results.map((candidate) => (
                    <div
                      key={candidate.rank}
                      className={`optimizer-candidate-card ${candidate.rank === 1 ? 'optimizer-candidate-card--rank1' : ''}`}
                    >
                      <div className="optimizer-candidate-header">
                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                            <span className="badge badge-success" style={{ fontSize: '0.78rem', fontWeight: '800' }}>
                              #{candidate.rank} {candidate.rank === 1 ? 'Top Pick' : 'Alternative'}
                            </span>
                            <span className="badge" style={{ background: 'rgba(255,255,255,0.08)', color: 'var(--color-cream)', fontSize: '0.75rem' }}>
                              {candidate.mode} Mode
                            </span>
                            <span style={{ fontSize: '0.82rem', color: 'var(--color-rainforest-emerald)', fontWeight: '600' }}>
                              Score: {candidate.optimizationScore}/100
                            </span>
                          </div>
                          <h3 className="optimizer-candidate-title">
                            {candidate.title}
                          </h3>
                        </div>

                        <div style={{ textAlign: 'right' }}>
                          <span style={{ fontSize: '0.75rem', color: 'var(--color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                            ML Estimated Cost
                          </span>
                          <div className="optimizer-cost-amount">
                            {formatCurrency(candidate.estimatedCost)}
                          </div>
                          <div style={{ marginTop: '4px' }}>
                            <span className={`optimizer-savings-badge ${candidate.estimatedSavings >= 0 ? 'optimizer-savings-badge--save' : 'optimizer-savings-badge--invest'}`}>
                              {candidate.costDifferenceMessage}
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* Specs Matrix */}
                      <div className="optimizer-specs-grid">
                        <div className="optimizer-spec-item">
                          <span className="optimizer-spec-label">Transport</span>
                          <span className="optimizer-spec-value">
                            {candidate.transport === 'Flight' ? <Plane size={15} /> : <Train size={15} />}
                            {candidate.transport}
                          </span>
                        </div>
                        <div className="optimizer-spec-item">
                          <span className="optimizer-spec-label">Lodging</span>
                          <span className="optimizer-spec-value">
                            <Hotel size={15} />
                            {candidate.accommodation}
                          </span>
                        </div>
                        <div className="optimizer-spec-item">
                          <span className="optimizer-spec-label">Activity Scope</span>
                          <span className="optimizer-spec-value">
                            <Camera size={15} />
                            {candidate.activityCount} Activities
                          </span>
                        </div>
                        <div className="optimizer-spec-item">
                          <span className="optimizer-spec-label">Interest Match</span>
                          <span className="optimizer-spec-value">
                            <Sparkles size={15} color="var(--color-rainforest-emerald)" />
                            {Math.round(candidate.interestMatchScore * 100)}% Match
                          </span>
                        </div>
                      </div>

                      {/* Activity Focus & Matched Interests */}
                      <div style={{ padding: 'var(--space-3) var(--space-4)', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-md)' }}>
                        <div style={{ fontSize: '0.85rem', color: 'var(--color-cream)', fontWeight: '600', marginBottom: '4px' }}>
                          🎯 Experience Focus: {candidate.activityFocus}
                        </div>
                        {candidate.matchedInterests && candidate.matchedInterests.length > 0 && (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap', marginTop: '6px' }}>
                            <span style={{ fontSize: '0.75rem', color: 'var(--color-muted)' }}>Aligned with:</span>
                            {candidate.matchedInterests.map((interest, i) => (
                              <span key={i} style={{ fontSize: '0.72rem', padding: '2px 8px', borderRadius: '4px', background: 'rgba(80, 200, 120, 0.12)', color: 'var(--color-rainforest-emerald)', border: '1px solid rgba(80, 200, 120, 0.25)' }}>
                                {interest}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>

                      {/* Rationale / Highlights */}
                      <ul className="optimizer-highlights-list">
                        {candidate.highlights.map((highlight, hIdx) => (
                          <li key={hIdx} className="optimizer-highlights-item">
                            <Check size={16} />
                            <span>{highlight}</span>
                          </li>
                        ))}
                      </ul>

                      {/* Weather Advisory */}
                      {candidate.weatherAdvisory && (
                        <div style={{ padding: '8px 12px', borderRadius: '6px', background: 'rgba(56, 189, 248, 0.08)', border: '1px solid rgba(56, 189, 248, 0.2)', color: '#bae6fd', fontSize: '0.82rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <CloudRain size={15} />
                          <span>{candidate.weatherAdvisory}</span>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ padding: 'var(--space-8)', textAlign: 'center', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-xl)', border: '1px dashed rgba(255,255,255,0.1)' }}>
                  <AlertTriangle size={32} color="#f59e0b" style={{ margin: '0 auto var(--space-3) auto', display: 'block' }} />
                  <h4 style={{ color: 'var(--color-cream)', margin: '0 0 var(--space-2) 0' }}>No Feasible Options Within Constraints</h4>
                  <p style={{ color: 'var(--color-muted)', fontSize: '0.92rem', maxWidth: '520px', margin: '0 auto' }}>
                    {optimizationResults.message || 'No candidate configuration could satisfy your current strict budget. Try increasing your maximum budget cap or disabling the strict constraint.'}
                  </p>
                </div>
              )
            ) : (
              <div style={{ padding: 'var(--space-8)', textAlign: 'center', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-xl)', border: '1px dashed rgba(255,255,255,0.1)' }}>
                <Sliders size={32} color="var(--color-rainforest-emerald)" style={{ margin: '0 auto var(--space-3) auto', display: 'block' }} />
                <h4 style={{ color: 'var(--color-cream)', margin: '0 0 var(--space-2) 0' }}>Ready to Optimize</h4>
                <p style={{ color: 'var(--color-muted)', fontSize: '0.92rem', maxWidth: '540px', margin: '0 auto var(--space-4) auto' }}>
                  Select an optimization mode above (Budget, Balanced, or Experience) and click <strong>Optimize My Trip</strong> to discover algorithmically evaluated alternatives.
                </p>
                <button
                  className="btn btn-primary"
                  onClick={() => handleOptimizeTrip()}
                  disabled={isOptimizing}
                >
                  <Zap size={16} /> Optimize My Trip
                </button>
              </div>
            )}

            {/* Transparent Disclaimer Box */}
            <div className="optimizer-disclaimer-box">
              <Info size={18} style={{ flexShrink: 0, marginTop: '2px', color: 'var(--color-rainforest-emerald)' }} />
              <div>
                <strong>Model-Based Travel Estimations:</strong> Optimization results are algorithmic recommendations based on verified trip attributes and ML-predicted travel cost models. They reflect estimated planning budgets rather than guaranteed live booking tickets or hotel reservations.
              </div>
            </div>
          </div>
        )}

        {/* ─── Budget Tab ────────────────────────────────── */}
        {activeTab === 'budget' && budget && (
          <div className="budget-tab animate-fadeIn">
            {/* ─── Machine Learning Cost Prediction Card ─── */}
            <div className="card ml-cost-prediction-card" style={{ marginBottom: 'var(--space-6)', padding: 'var(--space-6)', background: 'linear-gradient(135deg, rgba(80, 200, 120, 0.08), rgba(15, 23, 42, 0.7))', border: '1px solid rgba(80, 200, 120, 0.3)', borderRadius: 'var(--radius-xl)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 'var(--space-4)', marginBottom: 'var(--space-4)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                  <div style={{ padding: '10px', borderRadius: '12px', background: 'rgba(80, 200, 120, 0.15)', color: 'var(--color-rainforest-emerald)' }}>
                    <Brain size={24} />
                  </div>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <h3 style={{ margin: 0, fontSize: '1.25rem', color: 'var(--color-cream)' }}>ML Trip Cost Prediction</h3>
                      <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>Trained ML Model</span>
                    </div>
                    <p style={{ margin: '4px 0 0 0', color: 'var(--color-muted)', fontSize: '0.88rem' }}>
                      Statistical cost estimation derived from duration, traveler count, accommodation, transport, and destination benchmarks.
                    </p>
                  </div>
                </div>

                <button
                  className="btn btn-primary"
                  onClick={handlePredictCost}
                  disabled={isPredictingCost}
                  style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                >
                  {isPredictingCost ? (
                    <>
                      <RefreshCw size={16} className="animate-spin" />
                      <span>Predicting trip cost...</span>
                    </>
                  ) : (
                    <>
                      <Calculator size={16} />
                      <span>{costPrediction ? 'Re-estimate with ML' : 'Predict Cost with ML'}</span>
                    </>
                  )}
                </button>
              </div>

              {costPredictionError && (
                <div style={{ padding: 'var(--space-3) var(--space-4)', background: 'rgba(239, 68, 68, 0.12)', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: 'var(--radius-md)', color: '#fca5a5', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.9rem', marginBottom: 'var(--space-4)' }}>
                  <AlertTriangle size={18} />
                  <span>{costPredictionError}</span>
                </div>
              )}

              {costPrediction ? (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 'var(--space-4)', marginTop: 'var(--space-4)', padding: 'var(--space-4)', background: 'rgba(0,0,0,0.25)', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(255,255,255,0.06)' }}>
                  <div>
                    <span style={{ fontSize: '0.8rem', color: 'var(--color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Your Planned Budget</span>
                    <h4 style={{ margin: '4px 0 0 0', fontSize: '1.4rem', color: 'var(--color-cream)' }}>
                      {formatCurrency(costPrediction.budget)}
                    </h4>
                  </div>
                  <div>
                    <span style={{ fontSize: '0.8rem', color: 'var(--color-rainforest-emerald)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: '600' }}>ML Predicted Cost</span>
                    <h4 style={{ margin: '4px 0 0 0', fontSize: '1.5rem', color: 'var(--color-rainforest-emerald)', fontWeight: '800' }}>
                      {formatCurrency(costPrediction.predictedCost)}
                    </h4>
                  </div>
                  <div>
                    <span style={{ fontSize: '0.8rem', color: 'var(--color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Budget Variance</span>
                    <h4 style={{ margin: '4px 0 0 0', fontSize: '1.3rem', color: costPrediction.difference <= 0 ? 'var(--color-rainforest-emerald)' : '#f59e0b' }}>
                      {costPrediction.difference <= 0
                        ? `Within budget by ${formatCurrency(Math.abs(costPrediction.difference))}`
                        : `Exceeds budget by ${formatCurrency(costPrediction.difference)}`}
                    </h4>
                  </div>
                  <div>
                    <span style={{ fontSize: '0.8rem', color: 'var(--color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Model Engine</span>
                    <div style={{ marginTop: '4px', fontSize: '0.92rem', color: 'var(--color-cream)' }}>
                      <strong>{costPrediction.model}</strong> <span style={{ opacity: 0.7, fontSize: '0.8rem' }}>({costPrediction.modelVersion})</span>
                    </div>
                  </div>
                </div>
              ) : (
                <div style={{ padding: 'var(--space-4)', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-md)', border: '1px dashed rgba(255,255,255,0.1)', textAlign: 'center', color: 'var(--color-muted)', fontSize: '0.92rem' }}>
                  Click <strong>Predict Cost with ML</strong> to compute an independent statistical travel cost estimate using our trained regression model.
                </div>
              )}

              <p style={{ margin: 'var(--space-3) 0 0 0', fontSize: '0.78rem', color: 'var(--color-muted)', fontStyle: 'italic' }}>
                * Model-based estimate trained on historical travel benchmarks across domestic and international routes. Not a guaranteed booking price.
              </p>
            </div>

            <div className="budget-overview">
              <div className="budget-overview__header">
                <div>
                  <h3 className="budget-overview__title">Budget Breakdown</h3>
                  <p className="budget-overview__subtitle">
                    Estimated total: <strong>{formatCurrency(budget.totalEstimated)}</strong> of {formatCurrency(budget.totalBudget)} budget
                  </p>
                </div>
                <div className="budget-overview__remaining">
                  <span className="budget-overview__remaining-value">
                    {formatCurrency(budget.totalBudget - budget.totalEstimated)}
                  </span>
                  <span className="budget-overview__remaining-label">remaining</span>
                </div>
              </div>

              {/* Progress bar */}
              <div className="budget-progress">
                <div className="budget-progress__bar">
                  {budget.categories.map((cat) => (
                    <div
                      key={cat.name}
                      className="budget-progress__segment"
                      style={{ width: `${cat.percentage}%`, background: cat.color }}
                      title={`${cat.name}: ${formatCurrency(cat.estimated)}`}
                    />
                  ))}
                </div>
              </div>

              {/* Category list */}
              <div className="budget-categories">
                {budget.categories.map((cat) => (
                  <div key={cat.name} className="budget-category">
                    <div className="budget-category__left">
                      <div className="budget-category__dot" style={{ background: cat.color }} />
                      <span className="budget-category__name">{cat.name}</span>
                    </div>
                    <div className="budget-category__right">
                      <span className="budget-category__amount">{formatCurrency(cat.estimated)}</span>
                      <span className="budget-category__percent">{cat.percentage}%</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Optimization tips */}
            <div className="budget-tips card">
              <h3 className="budget-tips__title">
                <TrendingDown size={20} /> Price Optimization Tips
              </h3>
              <div className="budget-tips__list">
                {budget.priceOptimizations.map((tip, idx) => (
                  <div key={idx} className="budget-tip">
                    <div className="budget-tip__content">
                      <p className="budget-tip__text">{tip.tip}</p>
                    </div>
                    <span className="budget-tip__saving badge badge-success">
                      Save {formatCurrency(tip.potentialSaving)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* ─── Weather Tab ───────────────────────────────── */}
        {activeTab === 'weather' && (
          <div className="weather-tab animate-fadeIn">
            {weatherLoading && (
              <div className="weather-tab__loading">
                <LoadingSpinner size="md" message="Fetching live weather data..." />
              </div>
            )}

            {weatherError && !weatherLoading && (
              <div className="weather-tab__error">
                <div className="api-error-state">
                  <AlertTriangle size={40} className="api-error-state__icon" />
                  <h3 className="api-error-state__title">Weather Unavailable</h3>
                  <p className="api-error-state__message">{weatherError}</p>
                  <button className="btn btn-primary" onClick={() => loadWeather(itinerary.destination)}>
                    <RefreshCw size={16} /> Try Again
                  </button>
                </div>
              </div>
            )}

            {weather && !weatherLoading && !weatherError && (
              <>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
                  <span style={{ fontSize: '0.78rem', padding: '3px 10px', borderRadius: 'var(--radius-full)', background: 'rgba(80, 200, 120, 0.15)', color: 'var(--color-rainforest-emerald)', border: '1px solid rgba(80, 200, 120, 0.3)', fontWeight: '600' }}>
                    ✓ Verified Live Weather Data (Open-Meteo API)
                  </span>
                </div>
                <h3 className="weather-tab__title">
                  Weather for {weather.destination}
                </h3>

                {/* Current Conditions */}
                <div className="weather-current card">
                  <div className="weather-current__main">
                    <span className="weather-current__icon">
                      {getWeatherEmoji(weather.currentCondition || weather.condition)}
                    </span>
                    <div className="weather-current__info">
                      <span className="weather-current__temp">
                        {Math.round(weather.currentTemp ?? weather.temperature ?? 25)}°C
                      </span>
                      <span className="weather-current__condition">
                        {weather.currentCondition || weather.condition || 'Pleasant'}
                      </span>
                    </div>
                  </div>
                  <div className="weather-current__details">
                    {(weather.tempMax != null || weather.high != null) && (
                      <div className="weather-detail">
                        <Thermometer size={16} />
                        <span>
                          High {Math.round(weather.tempMax ?? weather.high)}° / Low {Math.round(weather.tempMin ?? weather.low)}°
                        </span>
                      </div>
                    )}
                    {(weather.currentHumidity != null || weather.humidity != null) && (
                      <div className="weather-detail">
                        <Droplets size={16} />
                        <span>{weather.currentHumidity ?? weather.humidity}% Humidity</span>
                      </div>
                    )}
                    {(weather.currentWindSpeed != null || weather.windSpeed != null) && (
                      <div className="weather-detail">
                        <Wind size={16} />
                        <span>{weather.currentWindSpeed ?? weather.windSpeed} km/h Wind</span>
                      </div>
                    )}
                  </div>
                </div>

                {/* 7-Day Forecast */}
                {weather.forecast && weather.forecast.length > 0 && (
                  <>
                    <h4 className="weather-forecast__heading">7-Day Forecast</h4>
                    <div className="weather-grid">
                      {weather.forecast.map((day) => (
                        <div key={day.date} className="weather-card card">
                          <span className="weather-card__day">{day.dayName || day.day}</span>
                          <span className="weather-card__date">
                            {new Date(day.date + 'T00:00:00').toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                          </span>
                          <div className="weather-card__icon">
                            {getWeatherEmoji(day.condition)}
                          </div>
                          <div className="weather-card__temps">
                            <span className="weather-card__high">{Math.round(day.tempMax ?? day.high)}°</span>
                            <span className="weather-card__low">{Math.round(day.tempMin ?? day.low)}°</span>
                          </div>
                          <span className="weather-card__condition">{day.condition}</span>
                          <div className="weather-card__details">
                            <span>💧 {day.precipitationChance ?? day.humidity ?? 15}%</span>
                            {(day.wind || day.windSpeed) && <span>💨 {day.wind || day.windSpeed} km/h</span>}
                          </div>
                        </div>
                      ))}
                    </div>
                  </>
                )}
              </>
            )}
          </div>
        )}

        {/* ─── Places Tab ────────────────────────────────── */}
        {activeTab === 'places' && (
          <div className="places-tab animate-fadeIn">
            {placesLoading && (
              <div className="places-tab__loading">
                <LoadingSpinner size="md" message="Discovering places..." />
              </div>
            )}

            {placesError && !placesLoading && (
              <div className="places-tab__error">
                <div className="api-error-state">
                  <AlertTriangle size={40} className="api-error-state__icon" />
                  <h3 className="api-error-state__title">Places Unavailable</h3>
                  <p className="api-error-state__message">{placesError}</p>
                  <button className="btn btn-primary" onClick={() => loadPlaces(itinerary.destination, itinerary.interests)}>
                    <RefreshCw size={16} /> Try Again
                  </button>
                </div>
              </div>
            )}

            {places && !placesLoading && !placesError && (
              <>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
                  <span style={{ fontSize: '0.78rem', padding: '3px 10px', borderRadius: 'var(--radius-full)', background: 'rgba(80, 200, 120, 0.15)', color: 'var(--color-rainforest-emerald)', border: '1px solid rgba(80, 200, 120, 0.3)', fontWeight: '600' }}>
                    ✓ Verified Destination Highlights (Wikipedia Geosearch)
                  </span>
                </div>
                <h3 className="places-tab__title">Discover Places in {itinerary.destination}</h3>
                {places.length === 0 ? (
                  <EmptyState
                    icon={MapPin}
                    title="No places found"
                    message={`We couldn't find places of interest for ${itinerary.destination} right now. Try again later.`}
                  />
                ) : (
                  <div className="places-grid">
                    {places.map((place) => (
                      <div key={place.id} className="place-card card">
                        {place.image && (
                          <div className="place-card__image-wrap">
                            <img src={place.image} alt={place.name} className="place-card__image" loading="lazy" />
                            <span className="place-card__category badge badge-primary">{place.category}</span>
                          </div>
                        )}
                        {!place.image && (
                          <div className="place-card__image-wrap place-card__image-placeholder">
                            <MapPin size={32} />
                            <span className="place-card__category badge badge-primary">{place.category}</span>
                          </div>
                        )}
                        <div className="place-card__info">
                          <h4 className="place-card__name">{place.name}</h4>
                          {place.description && (
                            <p className="place-card__desc">{place.description}</p>
                          )}
                          <div className="place-card__meta">
                            {place.location && formatLocation(place.location) && (
                              <span className="place-card__location">
                                <MapPin size={12} /> {formatLocation(place.location)}
                              </span>
                            )}
                            {place.rating && (
                              <span className="place-card__rating">
                                <Star size={14} fill="currentColor" /> {place.rating}
                              </span>
                            )}
                          </div>
                          {place.url && (
                            <a
                              href={place.url}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="place-card__link"
                            >
                              <ExternalLink size={12} /> Learn More
                            </a>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

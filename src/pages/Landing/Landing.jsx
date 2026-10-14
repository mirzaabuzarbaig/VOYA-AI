import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Sparkles,
  ArrowRight,
  TrendingDown,
  MapPin,
  Calendar,
  DollarSign,
  Globe,
  Shield,
  Zap,
  ChevronRight,
  Compass,
  Route,
  CloudSun,
} from 'lucide-react';
import { getPopularDestinations } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import LoadingSpinner from '../../components/shared/LoadingSpinner';
import TypewriterText from '../../components/shared/TypewriterText';
import ScrollReveal from '../../components/shared/ScrollReveal';
import HeroSlideshow from '../../components/HeroSlideshow/HeroSlideshow';
import { formatCurrency } from '../../utils/format';
import './Landing.css';

export default function Landing() {
  const [destinations, setDestinations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('all'); // 'all' | 'domestic' | 'international'
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    getPopularDestinations()
      .then(setDestinations)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const filteredDestinations = destinations.filter((dest) => {
    if (activeTab === 'domestic') return dest.type === 'domestic';
    if (activeTab === 'international') return dest.type === 'international';
    return true;
  });

  const handleStartPlanning = () => {
    if (isAuthenticated) {
      navigate('/planner');
    } else {
      navigate('/login', { state: { from: { pathname: '/planner' } } });
    }
  };

  const features = [
    {
      icon: Sparkles,
      title: 'AI-Powered Itineraries',
      description: 'Personalized day-by-day plans crafted around your specific interests, pace, and travel style.',
    },
    {
      icon: TrendingDown,
      title: 'Price Optimization',
      description: 'Intelligent budget algorithms find the most cost-effective flights, hotels, and activities.',
    },
    {
      icon: Globe,
      title: 'Weather-Smart Planning',
      description: 'Seasonal climate forecasts and real-time packing tips tailored to your destination.',
    },
    {
      icon: Shield,
      title: 'Save & Revisit',
      description: 'Keep all your dream trips organized. Revisit, modify, and export your itineraries anytime.',
    },
  ];

  return (
    <div className="landing">
      {/* ─── Section 1: Hero with Destination Slideshow ───────── */}
      <section className="hero">
        <HeroSlideshow />

        <div className="container hero__content">
          <div className="hero__badge animate-fadeInUp">
            <Zap size={14} />
            <span>AI-Powered Travel Intelligence</span>
          </div>

          <h1 className="hero__title animate-fadeInUp" style={{ animationDelay: '0.1s' }}>
            <TypewriterText
              phrasePairs={[
                [
                  { text: 'Plan Smarter.' },
                  { text: 'Travel Better.', className: 'gradient-text' },
                ],
                [
                  { text: 'Explore India.' },
                  { text: 'Roam The World.', className: 'gradient-text' },
                ],
                [
                  { text: 'Optimized Budgets.' },
                  { text: 'Unforgettable Trips.', className: 'gradient-text' },
                ],
              ]}
              speed={80}
              deleteSpeed={35}
              pauseBetweenLines={350}
              pauseAtEnd={2500}
            />
          </h1>

          <p className="hero__subtitle animate-fadeInUp" style={{ animationDelay: '0.2s' }}>
            VoyaAI creates personalized itineraries, optimizes your budget, and
            discovers hidden gems — all powered by intelligent algorithms.
          </p>

          <div className="hero__actions animate-fadeInUp" style={{ animationDelay: '0.3s' }}>
            <button onClick={handleStartPlanning} className="btn btn-accent btn-lg hero__cta">
              <Sparkles size={20} />
              Start Planning
              <ArrowRight size={18} />
            </button>
          </div>
        </div>
      </section>

      {/* ─── Section 2: How It Works (Deep Forest Green Theme) ── */}
      <ScrollReveal distance={50} duration={700}>
        <section className="section how-it-works">
          <div className="container">
            <div className="section-header text-center">
              <div className="section-badge section-badge--forest">
                <Compass size={13} />
                <span>Seamless Travel Flow</span>
              </div>
              <h2 className="section-title section-title--dark">Plan Your Dream Trip in Minutes</h2>
              <p className="section-subtitle section-subtitle--dark">
                Four simple steps from initial inspiration to a fully optimized travel itinerary.
              </p>
            </div>

            <div className="steps-grid">
              {[
                { step: '01', icon: MapPin, title: 'Choose Destination', desc: 'Enter where you want to go and where you are traveling from.' },
                { step: '02', icon: Calendar, title: 'Set Travel Dates', desc: 'Select your preferred dates and specify how many travelers are joining.' },
                { step: '03', icon: DollarSign, title: 'Define Your Budget', desc: 'Set your total budget and choose interests from culture to nature.' },
                { step: '04', icon: Sparkles, title: 'Receive AI Itinerary', desc: 'Our engine generates a customized, cost-optimized daily schedule.' },
              ].map(({ step, icon: Icon, title, desc }) => (
                <div key={step} className="step-card step-card--dark">
                  <div className="step-card__number">{step}</div>
                  <div className="step-card__icon">
                    <Icon size={24} />
                  </div>
                  <h3 className="step-card__title">{title}</h3>
                  <p className="step-card__desc">{desc}</p>
                </div>
              ))}
            </div>
          </div>
        </section>
      </ScrollReveal>

      {/* ─── Section 3: Features & Scenic Panel (Warm Sand Theme) ─ */}
      <ScrollReveal distance={50} duration={700}>
        <section className="section features-section">
          <div className="container">
            <div className="section-header text-center">
              <div className="section-badge section-badge--sand">
                <Route size={13} />
                <span>Intelligent Exploration</span>
              </div>
              <h2 className="section-title section-title--sand">Why Travelers Love VoyaAI</h2>
              <p className="section-subtitle section-subtitle--sand">
                Every tool designed to make discovery seamless, thoughtful, and cost-effective.
              </p>
            </div>

            <div className="features-showcase-split">
              {/* Feature cards column */}
              <div className="features-grid">
                {features.map(({ icon: Icon, title, description }) => (
                  <div key={title} className="feature-card feature-card--sand">
                    <div className="feature-card__icon feature-card__icon--sand">
                      <Icon size={24} />
                    </div>
                    <h3 className="feature-card__title feature-card__title--sand">{title}</h3>
                    <p className="feature-card__desc feature-card__desc--sand">{description}</p>
                  </div>
                ))}
              </div>

              {/* Large Scenic Visual Panel */}
              <div className="features-scenic-panel">
                <div className="features-scenic-panel__image-wrap">
                  <img
                    src="/scenic-journey.jpg"
                    alt="Scenic train journey traveling through misty green tea estates and mountain valleys at golden hour"
                    className="features-scenic-panel__image"
                    loading="lazy"
                  />
                  <div className="features-scenic-panel__overlay" />
                  <div className="features-scenic-panel__content">
                    <div className="features-scenic-panel__tag">
                      <Compass size={13} />
                      <span>Scenic Journeys & Curated Trails</span>
                    </div>
                    <h3 className="features-scenic-panel__title">
                      Crafted for Real Exploration
                    </h3>
                    <p className="features-scenic-panel__desc">
                      From mountain railways to coastal sunsets, VoyaAI balances popular landmarks with off-the-beaten-path wonders.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </ScrollReveal>

      {/* ─── Section 4: Scenic Travel Interlude Banner ─────────── */}
      <ScrollReveal distance={50} duration={700}>
        <section className="scenic-interlude">
          <div className="scenic-interlude__bg">
            <img
              src="/hero-nature.jpg"
              alt="Lush tropical Kerala backwaters with houseboat"
              className="scenic-interlude__image"
              loading="lazy"
            />
            <div className="scenic-interlude__overlay" />
          </div>
          <div className="container scenic-interlude__content">
            <span className="scenic-interlude__badge">
              <CloudSun size={14} />
              <span>THE ART OF WANDERLUST</span>
            </span>
            <h2 className="scenic-interlude__quote">
              &ldquo;Travel is not just about the destination — it is the stories and perspectives you collect along the way.&rdquo;
            </h2>
            <div className="scenic-interlude__pills">
              <div className="scenic-interlude__pill">
                <span>🌿 Eco & Heritage Trails</span>
              </div>
              <div className="scenic-interlude__pill">
                <span>🧭 Curated Local Gems</span>
              </div>
              <div className="scenic-interlude__pill">
                <span>⚡ Real-Time Price Optimization</span>
              </div>
            </div>
          </div>
        </section>
      </ScrollReveal>

      {/* ─── Section 5: Trending Trips (Deep Oceanic Teal Theme) ─ */}
      <ScrollReveal distance={50} duration={700}>
        <section className="section destinations-section">
          <div className="container">
            <div className="section-header text-center">
              <div className="section-badge section-badge--teal">
                <Globe size={13} />
                <span>Handpicked Getaways</span>
              </div>
              <h2 className="section-title section-title--dark">Trending National & International Trips</h2>
              <p className="section-subtitle section-subtitle--dark">
                Explore handpicked getaways across India or fly abroad with smart AI pricing.
              </p>

              {/* Filter Tabs for Domestic vs International */}
              <div className="destinations-tabs destinations-tabs--teal">
                {[
                  { id: 'all', label: 'All Destinations' },
                  { id: 'domestic', label: '🇮🇳 Within India (Domestic)' },
                  { id: 'international', label: '✈️ International (From India)' },
                ].map((tab) => (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`dest-tab-btn dest-tab-btn--teal ${activeTab === tab.id ? 'dest-tab-btn--teal-active' : ''}`}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>
            </div>

            {loading ? (
              <LoadingSpinner message="Discovering destinations..." />
            ) : (
              <div className="destinations-grid">
                {filteredDestinations.map((dest) => (
                  <Link
                    key={dest.id}
                    to={isAuthenticated ? '/planner' : '/login'}
                    className="dest-card dest-card--dark"
                  >
                    <div className="dest-card__image-wrap">
                      <img src={dest.image} alt={dest.name} className="dest-card__image" loading="lazy" />
                      <div className="dest-card__overlay dest-card__overlay--dark" />
                      <div className="dest-card__type-tag dest-card__type-tag--dark">
                        {dest.type === 'domestic' ? '🇮🇳 India' : '✈️ Global'}
                      </div>
                    </div>
                    <div className="dest-card__info dest-card__info--dark">
                      <h3 className="dest-card__name dest-card__name--dark">{dest.name}</h3>
                      <p className="dest-card__tagline dest-card__tagline--dark">{dest.tagline}</p>
                      <div className="dest-card__meta">
                        <span className="dest-card__price dest-card__price--dark">
                          From {formatCurrency(dest.avgCostPerDay)}/day
                        </span>
                        <span className="dest-card__season dest-card__season--dark">{dest.bestSeason}</span>
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </section>
      </ScrollReveal>

      {/* ─── Section 6: Final CTA (Full-Width Scenic Overlay) ──── */}
      <ScrollReveal distance={50} duration={700}>
        <section className="cta-banner">
          <div className="cta-banner__bg">
            <img
              src="/hero-beach.jpg"
              alt="Tropical turquoise shores and coastal palm trees"
              className="cta-banner__image"
              loading="lazy"
            />
            <div className="cta-banner__overlay" />
          </div>

          <div className="container cta-banner__inner">
            <div className="cta-banner__content">
              <span className="cta-banner__tag">
                <Sparkles size={14} />
                <span>START EXPLORING TODAY</span>
              </span>
              <h2 className="cta-banner__title">
                Ready for Your Next Great Journey?
              </h2>
              <p className="cta-banner__subtitle">
                Let our AI tailor the ideal route, budget, and experiences for you. Free to plan.
              </p>
            </div>
            <button onClick={handleStartPlanning} className="btn btn-accent btn-lg cta-banner__btn">
              <Sparkles size={20} />
              Start Planning
              <ChevronRight size={18} />
            </button>
          </div>
        </section>
      </ScrollReveal>
    </div>
  );
}

import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Sparkles,
  MapPin,
  BookmarkCheck,
  Globe,
  ArrowRight,
  Compass,
  Calendar,
  TrendingUp,
  Plane,
  User,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { getSavedTrips } from '../../services/api';
import { formatCurrency } from '../../utils/format';
import ScrollReveal from '../../components/shared/ScrollReveal';
import './Dashboard.css';

export default function Dashboard() {
  const { user } = useAuth();
  const [trips, setTrips] = useState([]);
  const [_loading, setLoading] = useState(true);

  useEffect(() => {
    getSavedTrips()
      .then(setTrips)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const recentTrips = trips.slice(0, 3);
  const domesticCount = trips.filter((t) => t.tripType === 'domestic').length;
  const internationalCount = trips.filter((t) => t.tripType === 'international').length;

  const quickActions = [
    {
      icon: Sparkles,
      title: 'Plan a New Trip',
      desc: 'AI-powered itinerary with smart pricing',
      to: '/planner',
      accent: true,
    },
    {
      icon: MapPin,
      title: 'National Trips',
      desc: 'Explore destinations within India',
      to: '/planner?type=domestic',
      accent: false,
    },
    {
      icon: Globe,
      title: 'International Trips',
      desc: 'Plan your next international adventure',
      to: '/planner?type=international',
      accent: false,
    },
    {
      icon: BookmarkCheck,
      title: 'Saved Trips',
      desc: `${trips.length} trip${trips.length !== 1 ? 's' : ''} saved`,
      to: '/saved',
      accent: false,
    },
  ];

  return (
    <div className="dashboard-page">
      {/* Header */}
      <div className="dashboard-header gradient-bg">
        <div className="container">
          <div className="dashboard-header__greeting">
            <div className="dashboard-header__avatar">
              <User size={24} />
            </div>
            <div>
              <h1 className="dashboard-header__title">
                Welcome back, {user?.name?.split(' ')[0] || 'Traveler'}!
              </h1>
              <p className="dashboard-header__subtitle">
                Ready to plan your next adventure?
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="container dashboard-body">
        {/* Quick Actions */}
        <ScrollReveal direction="up" delay={0}>
          <section className="dashboard-section">
            <h2 className="dashboard-section__title">
              <Compass size={20} /> Quick Actions
            </h2>
            <div className="dashboard-actions-grid">
              {quickActions.map(({ icon: Icon, title, desc, to, accent }) => (
                <Link key={title} to={to} className={`dashboard-action-card card ${accent ? 'dashboard-action-card--accent' : ''}`}>
                  <div className={`dashboard-action-card__icon ${accent ? 'dashboard-action-card__icon--accent' : ''}`}>
                    <Icon size={24} />
                  </div>
                  <div className="dashboard-action-card__content">
                    <h3 className="dashboard-action-card__title">{title}</h3>
                    <p className="dashboard-action-card__desc">{desc}</p>
                  </div>
                  <ArrowRight size={18} className="dashboard-action-card__arrow" />
                </Link>
              ))}
            </div>
          </section>
        </ScrollReveal>

        {/* Trip Summary */}
        <ScrollReveal direction="up" delay={100}>
          <section className="dashboard-section">
            <h2 className="dashboard-section__title">
              <TrendingUp size={20} /> Trip Summary
            </h2>
            <div className="dashboard-stats-grid">
              <div className="dashboard-stat-card">
                <div className="dashboard-stat-card__icon">
                  <BookmarkCheck size={22} />
                </div>
                <span className="dashboard-stat-card__value">{trips.length}</span>
                <span className="dashboard-stat-card__label">Total Trips</span>
              </div>
              <div className="dashboard-stat-card">
                <div className="dashboard-stat-card__icon dashboard-stat-card__icon--domestic">
                  <Plane size={22} />
                </div>
                <span className="dashboard-stat-card__value">{domesticCount}</span>
                <span className="dashboard-stat-card__label">🇮🇳 Domestic</span>
              </div>
              <div className="dashboard-stat-card">
                <div className="dashboard-stat-card__icon dashboard-stat-card__icon--intl">
                  <Globe size={22} />
                </div>
                <span className="dashboard-stat-card__value">{internationalCount}</span>
                <span className="dashboard-stat-card__label">✈️ International</span>
              </div>
            </div>
          </section>
        </ScrollReveal>

        {/* Recent Trips */}
        {recentTrips.length > 0 && (
          <ScrollReveal direction="up" delay={200}>
            <section className="dashboard-section">
              <div className="dashboard-section__header-row">
                <h2 className="dashboard-section__title">
                  <Calendar size={20} /> Recent Trips
                </h2>
                <Link to="/saved" className="dashboard-section__see-all">
                  See all <ArrowRight size={14} />
                </Link>
              </div>
              <div className="dashboard-recent-grid">
                {recentTrips.map((trip) => (
                  <Link key={trip.id} to={`/itinerary?id=${trip.id}`} className="dashboard-recent-card card">
                    <div className="dashboard-recent-card__image-wrap">
                      <img src={trip.image} alt={trip.destination} className="dashboard-recent-card__image" loading="lazy" />
                      <div className="dashboard-recent-card__overlay" />
                      <span className="dashboard-recent-card__type-tag">
                        {trip.tripType === 'domestic' ? '🇮🇳 India' : '✈️ Global'}
                      </span>
                    </div>
                    <div className="dashboard-recent-card__info">
                      <h4 className="dashboard-recent-card__name">{trip.destination}</h4>
                      <p className="dashboard-recent-card__dates">
                        {new Date(trip.startDate).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })}
                        {' – '}
                        {new Date(trip.endDate).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })}
                      </p>
                      <span className="dashboard-recent-card__budget">
                        {formatCurrency(trip.totalCost)}
                      </span>
                    </div>
                  </Link>
                ))}
              </div>
            </section>
          </ScrollReveal>
        )}
      </div>
    </div>
  );
}

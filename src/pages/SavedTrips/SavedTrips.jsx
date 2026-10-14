import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  MapPin,
  Calendar,
  Users,
  DollarSign,
  Trash2,
  Eye,
  Sparkles,
  BookmarkCheck,
  Clock,
  CheckCircle2,
  FileEdit,
} from 'lucide-react';
import { getSavedTrips, deleteTrip } from '../../services/api';
import LoadingSpinner from '../../components/shared/LoadingSpinner';
import EmptyState from '../../components/shared/EmptyState';
import ErrorState from '../../components/shared/ErrorState';
import { formatCurrency } from '../../utils/format';
import './SavedTrips.css';

const statusConfig = {
  upcoming: { label: 'Upcoming', icon: Clock, className: 'badge-primary' },
  completed: { label: 'Completed', icon: CheckCircle2, className: 'badge-success' },
  draft: { label: 'Draft', icon: FileEdit, className: 'badge-warning' },
};

export default function SavedTrips() {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all');

  const fetchTrips = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getSavedTrips();
      setTrips(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTrips();
  }, []);

  const handleDelete = async (tripId) => {
    if (!window.confirm('Are you sure you want to delete this trip?')) {
      return;
    }
    try {
      await deleteTrip(tripId);
      setTrips((prev) => prev.filter((t) => t.id !== tripId));
    } catch (err) {
      console.error(err);
      alert('Failed to delete trip: ' + (err.message || 'Unknown error'));
    }
  };

  const filteredTrips = filter === 'all' ? trips : trips.filter((t) => t.status === filter);

  return (
    <div className="saved-page">
      <div className="saved-header gradient-bg">
        <div className="container saved-header__inner">
          <div className="saved-header__icon">
            <BookmarkCheck size={28} />
          </div>
          <h1 className="saved-header__title">My Trips</h1>
          <p className="saved-header__subtitle">
            All your planned adventures in one place.
          </p>
        </div>
      </div>

      <div className="container saved-body">
        {/* Filters */}
        <div className="saved-filters">
          {['all', 'upcoming', 'completed', 'draft'].map((f) => (
            <button
              key={f}
              className={`saved-filter ${filter === f ? 'saved-filter--active' : ''}`}
              onClick={() => setFilter(f)}
            >
              {f.charAt(0).toUpperCase() + f.slice(1)}
              {f !== 'all' && (
                <span className="saved-filter__count">
                  {trips.filter((t) => t.status === f).length}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Content */}
        {loading ? (
          <LoadingSpinner message="Loading your trips..." />
        ) : error ? (
          <ErrorState message={error} onRetry={fetchTrips} />
        ) : filteredTrips.length === 0 ? (
          <EmptyState
            icon={MapPin}
            title={filter === 'all' ? 'No Trips Yet' : `No ${filter} Trips`}
            message="Start planning your next adventure and it will appear here."
            action={
              <Link to="/planner" className="btn btn-primary">
                <Sparkles size={16} /> Plan a Trip
              </Link>
            }
          />
        ) : (
          <div className="saved-grid stagger-children">
            {filteredTrips.map((trip) => {
              const status = statusConfig[trip.status];
              const StatusIcon = status.icon;
              const budgetUsed = Math.round((trip.totalCost / trip.budget) * 100);

              return (
                <div key={trip.id} className="trip-card card">
                  <div className="trip-card__image-wrap">
                    <img src={trip.image} alt={trip.destination} className="trip-card__image" loading="lazy" />
                    <div className="trip-card__image-overlay" />
                    <span className={`trip-card__status badge ${status.className}`}>
                      <StatusIcon size={12} /> {status.label}
                    </span>
                    <span
                      style={{
                        position: 'absolute',
                        bottom: 'var(--space-3)',
                        left: 'var(--space-3)',
                        padding: '2px 8px',
                        borderRadius: 'var(--radius-full)',
                        background: 'rgba(6, 21, 13, 0.78)',
                        backdropFilter: 'blur(8px)',
                        color: 'var(--color-cream)',
                        fontSize: '0.72rem',
                        fontWeight: '600',
                        border: '1px solid rgba(80, 200, 120, 0.3)',
                      }}
                    >
                      {trip.tripType === 'domestic' ? '🇮🇳 Domestic' : '✈️ International'}
                    </span>
                  </div>
                  <div className="trip-card__body">
                    <h3 className="trip-card__destination">{trip.destination}</h3>
                    <div className="trip-card__meta-grid">
                      <span className="trip-card__meta-item">
                        <Calendar size={14} />
                        {new Date(trip.startDate).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })}
                        {' – '}
                        {new Date(trip.endDate).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })}
                      </span>
                      <span className="trip-card__meta-item">
                        <Users size={14} /> {trip.travelers} travelers
                      </span>
                    </div>

                    {/* Budget bar */}
                    <div className="trip-card__budget">
                      <div className="trip-card__budget-header">
                        <span className="trip-card__budget-label" style={{ fontWeight: '600' }}>
                          {formatCurrency(trip.totalCost)} / {formatCurrency(trip.budget)}
                        </span>
                        <span className="trip-card__budget-percent">{budgetUsed}%</span>
                      </div>
                      <div className="trip-card__budget-bar">
                        <div
                          className="trip-card__budget-fill"
                          style={{
                            width: `${Math.min(budgetUsed, 100)}%`,
                            background: budgetUsed > 90 ? 'var(--color-warning)' : 'var(--color-rainforest-emerald)',
                          }}
                        />
                      </div>
                    </div>

                    {/* Interests */}
                    <div className="trip-card__interests">
                      {trip.interests.map((i) => (
                        <span key={i} className="trip-card__interest">{i}</span>
                      ))}
                    </div>

                    {/* Actions */}
                    <div className="trip-card__actions">
                      <Link to={`/itinerary?id=${trip.id}`} className="btn btn-primary btn-sm">
                        <Eye size={14} /> View
                      </Link>
                      <Link to={`/planner?edit=${trip.id}`} className="btn btn-secondary btn-sm">
                        <FileEdit size={14} /> Edit
                      </Link>
                      <button
                        className="btn btn-ghost btn-sm trip-card__delete"
                        onClick={() => handleDelete(trip.id)}
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

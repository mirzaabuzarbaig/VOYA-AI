import { useState, useEffect } from 'react';
import {
  User,
  Mail,
  Globe,
  Thermometer,
  MapPin,
  DollarSign,
  TrendingDown,
  BookmarkCheck,
  Save,
  Loader,
  CheckCircle,
} from 'lucide-react';
import { getUserProfile, updateUserProfile } from '../../services/api';
import LoadingSpinner from '../../components/shared/LoadingSpinner';
import ErrorState from '../../components/shared/ErrorState';
import { formatCurrency } from '../../utils/format';
import './Profile.css';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState(null);

  // Editable fields
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [currency, setCurrency] = useState('USD');
  const [tempUnit, setTempUnit] = useState('celsius');
  const [travelStyle, setTravelStyle] = useState('balanced');

  useEffect(() => {
    getUserProfile()
      .then((data) => {
        setProfile(data);
        setName(data.name);
        setEmail(data.email);
        setCurrency(data.preferences.currency);
        setTempUnit(data.preferences.temperatureUnit);
        setTravelStyle(data.preferences.travelStyle);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async () => {
    setSaving(true);
    setSaved(false);
    try {
      await updateUserProfile({
        name,
        email,
        preferences: { currency, temperatureUnit: tempUnit, travelStyle },
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="profile-page"><LoadingSpinner size="lg" message="Loading profile..." /></div>;
  if (error) return <div className="profile-page"><ErrorState message={error} /></div>;
  if (!profile) return null;

  return (
    <div className="profile-page">
      <div className="profile-header gradient-bg">
        <div className="container profile-header__inner">
          <div className="profile-avatar">
            <User size={36} />
          </div>
          <h1 className="profile-header__name">{profile.name}</h1>
          <p className="profile-header__email">{profile.email}</p>

          {/* Stats */}
          <div className="profile-stats">
            <div className="profile-stat">
              <BookmarkCheck size={20} />
              <div>
                <span className="profile-stat__value">{profile.stats.tripsPlanned}</span>
                <span className="profile-stat__label">Trips Planned</span>
              </div>
            </div>
            <div className="profile-stat">
              <Globe size={20} />
              <div>
                <span className="profile-stat__value">{profile.stats.countriesVisited}</span>
                <span className="profile-stat__label">Countries</span>
              </div>
            </div>
            <div className="profile-stat">
              <TrendingDown size={20} />
              <div>
                <span className="profile-stat__value">{formatCurrency(profile.stats.totalSaved)}</span>
                <span className="profile-stat__label">Total Saved</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container profile-body">
        {/* Personal Info */}
        <div className="settings-section card">
          <h2 className="settings-section__title">
            <User size={20} /> Personal Information
          </h2>
          <div className="settings-grid">
            <div className="input-group">
              <label className="input-label">Full Name</label>
              <input
                type="text"
                className="input-field"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="input-group">
              <label className="input-label">Email</label>
              <input
                type="email"
                className="input-field"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>
        </div>

        {/* Preferences */}
        <div className="settings-section card">
          <h2 className="settings-section__title">
            <Globe size={20} /> Travel Preferences
          </h2>
          <div className="settings-grid">
            <div className="input-group">
              <label className="input-label">
                <span style={{ fontWeight: '700', marginRight: '4px' }}>₹</span> Currency
              </label>
              <select className="input-field" value={currency} onChange={(e) => setCurrency(e.target.value)}>
                <option value="INR">INR – Indian Rupee (₹)</option>
                <option value="USD">USD – US Dollar ($)</option>
                <option value="EUR">EUR – Euro (€)</option>
                <option value="GBP">GBP – British Pound (£)</option>
                <option value="AED">AED – UAE Dirham</option>
              </select>
            </div>
            <div className="input-group">
              <label className="input-label">
                <Thermometer size={14} style={{ verticalAlign: 'middle' }} /> Temperature Unit
              </label>
              <select className="input-field" value={tempUnit} onChange={(e) => setTempUnit(e.target.value)}>
                <option value="celsius">Celsius (°C)</option>
                <option value="fahrenheit">Fahrenheit (°F)</option>
              </select>
            </div>
            <div className="input-group settings-grid--full">
              <label className="input-label">
                <MapPin size={14} style={{ verticalAlign: 'middle' }} /> Travel Style
              </label>
              <div className="travel-style-options">
                {[
                  { id: 'budget', label: 'Budget', desc: 'Maximum value, hostels & street food' },
                  { id: 'balanced', label: 'Balanced', desc: 'Mix of comfort and experiences' },
                  { id: 'luxury', label: 'Luxury', desc: 'Premium stays and fine dining' },
                ].map((style) => (
                  <button
                    key={style.id}
                    className={`travel-style-option ${travelStyle === style.id ? 'travel-style-option--active' : ''}`}
                    onClick={() => setTravelStyle(style.id)}
                  >
                    <span className="travel-style-option__label">{style.label}</span>
                    <span className="travel-style-option__desc">{style.desc}</span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Save button */}
        <div className="profile-save">
          <button
            className={`btn ${saved ? 'btn-secondary' : 'btn-primary'} btn-lg`}
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? (
              <><Loader size={18} className="loading-spinner__icon" /> Saving...</>
            ) : saved ? (
              <><CheckCircle size={18} /> Saved Successfully</>
            ) : (
              <><Save size={18} /> Save Changes</>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

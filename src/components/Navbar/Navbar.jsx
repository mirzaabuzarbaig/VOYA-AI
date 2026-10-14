import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Map,
  BookmarkCheck,
  User,
  Menu,
  X,
  Sparkles,
  LogIn,
  UserPlus,
  LayoutDashboard,
  LogOut,
  ChevronDown,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

const publicLinks = [];

const authLinks = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/planner', label: 'Plan Trip', icon: Sparkles },
  { path: '/saved', label: 'My Trips', icon: BookmarkCheck },
];

export default function Navbar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setIsScrolled(window.scrollY > 20);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  // Close menus on route change
  useEffect(() => {
    setMobileOpen(false);
    setProfileOpen(false);
  }, [location.pathname]);

  // Close profile dropdown on outside click
  useEffect(() => {
    if (!profileOpen) return;
    const handleClick = () => setProfileOpen(false);
    document.addEventListener('click', handleClick);
    return () => document.removeEventListener('click', handleClick);
  }, [profileOpen]);

  const isLanding = location.pathname === '/';
  const isAuthPage = ['/login', '/signup'].includes(location.pathname);
  const isTransparent = (isLanding || isAuthPage) && !isScrolled;

  const navLinks = isAuthenticated ? authLinks : publicLinks;

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header
      className={`navbar ${isTransparent ? 'navbar--transparent' : 'navbar--solid'} ${isScrolled ? 'navbar--scrolled' : ''}`}
    >
      <nav className="navbar__inner container">
        <Link to="/" className="navbar__brand">
          <Map className="navbar__logo-icon" size={28} />
          <span className="navbar__logo-text">
            Voya<span className="navbar__logo-accent">AI</span>
          </span>
        </Link>

        {/* Desktop nav */}
        <ul className="navbar__links">
          {navLinks.map(({ path, label, icon: Icon }) => (
            <li key={path}>
              <Link
                to={path}
                className={`navbar__link ${location.pathname === path ? 'navbar__link--active' : ''}`}
              >
                <Icon size={18} />
                <span>{label}</span>
              </Link>
            </li>
          ))}
        </ul>

        <div className="navbar__actions">
          {isAuthenticated ? (
            /* Logged-in: profile dropdown */
            <div className="navbar__profile-wrap" onClick={(e) => e.stopPropagation()}>
              <button
                className="navbar__profile-btn"
                onClick={() => setProfileOpen(!profileOpen)}
                aria-label="User menu"
              >
                <div className="navbar__profile-avatar">
                  <User size={16} />
                </div>
                <span className="navbar__profile-name">
                  {user?.name?.split(' ')[0] || 'User'}
                </span>
                <ChevronDown size={14} className={`navbar__profile-chevron ${profileOpen ? 'navbar__profile-chevron--open' : ''}`} />
              </button>

              {profileOpen && (
                <div className="navbar__profile-dropdown">
                  <div className="navbar__profile-dropdown-header">
                    <p className="navbar__profile-dropdown-name">{user?.name}</p>
                    <p className="navbar__profile-dropdown-email">{user?.email}</p>
                  </div>
                  <div className="navbar__profile-dropdown-divider" />
                  <Link to="/profile" className="navbar__profile-dropdown-item">
                    <User size={16} /> Profile
                  </Link>
                  <Link to="/dashboard" className="navbar__profile-dropdown-item">
                    <LayoutDashboard size={16} /> Dashboard
                  </Link>
                  <Link to="/saved" className="navbar__profile-dropdown-item">
                    <BookmarkCheck size={16} /> My Trips
                  </Link>
                  <div className="navbar__profile-dropdown-divider" />
                  <button className="navbar__profile-dropdown-item navbar__profile-dropdown-item--danger" onClick={handleLogout}>
                    <LogOut size={16} /> Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            /* Not logged-in: Login + Sign Up */
            <>
              <Link to="/login" className="btn btn-ghost btn-sm navbar__auth-link">
                <LogIn size={16} />
                Login
              </Link>
              <Link to="/signup" className="btn btn-accent btn-sm navbar__cta">
                <UserPlus size={16} />
                Sign Up
              </Link>
            </>
          )}
        </div>

        {/* Mobile hamburger */}
        <button
          className="navbar__hamburger"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          {mobileOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </nav>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div className="navbar__mobile-overlay" onClick={() => setMobileOpen(false)}>
          <div className="navbar__mobile-drawer" onClick={(e) => e.stopPropagation()}>
            <ul className="navbar__mobile-links">
              {navLinks.map(({ path, label, icon: Icon }) => (
                <li key={path}>
                  <Link
                    to={path}
                    className={`navbar__mobile-link ${location.pathname === path ? 'navbar__mobile-link--active' : ''}`}
                  >
                    <Icon size={20} />
                    <span>{label}</span>
                  </Link>
                </li>
              ))}
              {isAuthenticated && (
                <li>
                  <Link
                    to="/profile"
                    className={`navbar__mobile-link ${location.pathname === '/profile' ? 'navbar__mobile-link--active' : ''}`}
                  >
                    <User size={20} />
                    <span>Profile</span>
                  </Link>
                </li>
              )}
            </ul>

            {isAuthenticated ? (
              <button className="btn btn-secondary btn-lg" style={{ width: '100%' }} onClick={handleLogout}>
                <LogOut size={18} />
                Sign Out
              </button>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-3)' }}>
                <Link to="/login" className="btn btn-secondary btn-lg" style={{ width: '100%' }}>
                  <LogIn size={18} />
                  Login
                </Link>
                <Link to="/signup" className="btn btn-accent btn-lg" style={{ width: '100%' }}>
                  <UserPlus size={18} />
                  Sign Up
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}

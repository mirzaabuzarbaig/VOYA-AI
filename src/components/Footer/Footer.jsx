import { Link } from 'react-router-dom';
import { Map, Heart } from 'lucide-react';
import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer__inner">
        <div className="footer__brand">
          <Link to="/" className="footer__logo">
            <Map size={24} />
            <span>Voya<span className="footer__logo-accent">AI</span></span>
          </Link>
          <p className="footer__tagline">
            AI-powered travel planning that saves you time and money.
          </p>
        </div>

        <div className="footer__links-grid">
          <div className="footer__column">
            <h4 className="footer__column-title">Product</h4>
            <ul>
              <li><Link to="/planner">Plan a Trip</Link></li>
              <li><Link to="/saved">My Trips</Link></li>
              <li><Link to="/profile">Profile</Link></li>
            </ul>
          </div>
          <div className="footer__column">
            <h4 className="footer__column-title">Resources</h4>
            <ul>
              <li><a href="#">Travel Guides</a></li>
              <li><a href="#">Blog</a></li>
              <li><a href="#">FAQ</a></li>
            </ul>
          </div>
          <div className="footer__column">
            <h4 className="footer__column-title">Company</h4>
            <ul>
              <li><a href="#">About</a></li>
              <li><a href="#">Privacy</a></li>
              <li><a href="#">Terms</a></li>
            </ul>
          </div>
        </div>

        <div className="footer__bottom">
          <p>
            &copy; {new Date().getFullYear()} VoyaAI. Made with <Heart size={14} className="footer__heart" /> for travelers.
          </p>
        </div>
      </div>
    </footer>
  );
}

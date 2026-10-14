import { useState, useEffect } from 'react';
import { MapPin } from 'lucide-react';
import './HeroSlideshow.css';

export const HERO_SLIDES = [
  {
    id: 'mountains',
    src: '/hero-mountains.jpg',
    title: 'Himalayan Heights',
    location: 'Himalayas, India',
    alt: 'Breathtaking snow-capped Himalayan mountains at golden hour',
  },
  {
    id: 'beach',
    src: '/hero-beach.jpg',
    title: 'Tropical Shores',
    location: 'Coastal Escapes',
    alt: 'Crystal-clear turquoise waters and tropical palm trees at sunset',
  },
  {
    id: 'monument',
    src: '/hero-monument.jpg',
    title: 'Historic Architecture',
    location: 'Taj Mahal, Agra',
    alt: 'Majestic Taj Mahal at sunrise with reflection pool',
  },
  {
    id: 'city',
    src: '/hero-city.jpg',
    title: 'Scenic Skylines',
    location: 'Dubai & Global Cities',
    alt: 'Spectacular aerial view of modern illuminated city skyline at twilight',
  },
  {
    id: 'nature',
    src: '/hero-nature.jpg',
    title: 'Kerala Backwaters',
    location: 'Alleppey, Kerala',
    alt: 'Serene Kerala backwaters with traditional houseboat surrounded by lush greenery',
  },
];

export default function HeroSlideshow() {
  const [activeIndex, setActiveIndex] = useState(0);

  // Auto transition every 5.5 seconds, continuously looping
  useEffect(() => {
    const timer = setInterval(() => {
      setActiveIndex((prev) => (prev + 1) % HERO_SLIDES.length);
    }, 5500);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="hero-slideshow" aria-label="Travel destination slideshow">
      {HERO_SLIDES.map((slide, idx) => {
        const isActive = idx === activeIndex;
        return (
          <div
            key={slide.id}
            className={`hero-slideshow__slide ${isActive ? 'hero-slideshow__slide--active' : ''}`}
            aria-hidden={!isActive}
          >
            <img
              src={slide.src}
              alt={slide.alt}
              className="hero-slideshow__image"
              loading={idx === 0 ? 'eager' : 'lazy'}
            />
          </div>
        );
      })}

      {/* Multi-layer gradient overlay for readability & tropical mood */}
      <div className="hero-slideshow__overlay" />

      {/* Active slide destination pill & indicators */}
      <div className="hero-slideshow__meta">
        <div className="hero-slideshow__location-badge">
          <MapPin size={13} className="hero-slideshow__pin-icon" />
          <span>{HERO_SLIDES[activeIndex].title} &bull; {HERO_SLIDES[activeIndex].location}</span>
        </div>
        <div className="hero-slideshow__dots">
          {HERO_SLIDES.map((slide, idx) => (
            <button
              key={slide.id}
              className={`hero-slideshow__dot ${idx === activeIndex ? 'hero-slideshow__dot--active' : ''}`}
              onClick={() => setActiveIndex(idx)}
              aria-label={`Go to slide ${idx + 1}: ${slide.title}`}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

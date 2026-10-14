import { useEffect, useRef, useState } from 'react';

/**
 * Custom hook for continuous scroll-triggered reveal animations.
 * Automatically animates elements entering from below when scrolling down,
 * and entering from above when scrolling up.
 * Loops repeatedly every time the user scrolls through the page.
 *
 * @param {object} options
 * @param {number}  options.threshold  — Visibility threshold (0–1). Default 0.08
 * @param {string}  options.rootMargin — Root margin. Default '0px 0px -40px 0px'
 */
export function useScrollReveal({ threshold = 0.08, rootMargin = '0px 0px -40px 0px' } = {}) {
  const ref = useRef(null);
  const [isVisible, setIsVisible] = useState(false);
  const [offsetDirection, setOffsetDirection] = useState('below'); // 'below' | 'above'

  useEffect(() => {
    // Respect prefers-reduced-motion
    const motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    if (motionQuery.matches) {
      setIsVisible(true);
      return;
    }

    const el = ref.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
        } else {
          const rect = entry.boundingClientRect;
          // Determine whether element is above or below the viewport
          if (rect.bottom <= 0 || rect.top < 0) {
            setOffsetDirection('above');
          } else {
            setOffsetDirection('below');
          }
          setIsVisible(false);
        }
      },
      { threshold, rootMargin }
    );

    observer.observe(el);

    return () => observer.disconnect();
  }, [threshold, rootMargin]);

  return { ref, isVisible, offsetDirection };
}

import { useScrollReveal } from '../../hooks/useScrollReveal';

/**
 * Slide component that applies repeatable scroll-triggered reveal animations.
 * When scrolling down, slides up smoothly into view.
 * When scrolling up, slides down smoothly into view.
 * Loops repeatedly every time the user scrolls up or down.
 *
 * @param {number}  delay      - Animation delay in ms. Default 0
 * @param {number}  duration   - Animation duration in ms. Default 650
 * @param {number}  distance   - Translation distance in px. Default 45
 * @param {string}  className  - Additional CSS classes
 * @param {object}  style      - Additional inline styles
 */
export default function ScrollReveal({
  children,
  delay = 0,
  duration = 650,
  distance = 45,
  className = '',
  style = {},
  ...rest
}) {
  const { ref, isVisible, offsetDirection } = useScrollReveal({ threshold: 0.08 });

  const initialTransform = offsetDirection === 'above'
    ? `translateY(-${distance}px)`
    : `translateY(${distance}px)`;

  const baseStyle = {
    opacity: isVisible ? 1 : 0,
    transform: isVisible ? 'translateY(0)' : initialTransform,
    transition: isVisible
      ? `opacity ${duration}ms cubic-bezier(0.16, 1, 0.3, 1) ${delay}ms, transform ${duration}ms cubic-bezier(0.16, 1, 0.3, 1) ${delay}ms`
      : 'opacity 300ms ease, transform 0ms',
    willChange: 'opacity, transform',
    ...style,
  };

  return (
    <div ref={ref} className={`travel-slide ${className}`} style={baseStyle} {...rest}>
      {children}
    </div>
  );
}

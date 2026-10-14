import { useState, useEffect } from 'react';
import './TypewriterText.css';

/**
 * TypewriterText — animates text line-by-line, character-by-character in a continuous smooth loop.
 *
 * Supports multi-line pairs (e.g. Line 1: Plan Smarter., Line 2: Travel Better.),
 * pauses at full display, smoothly deletes back, and cycles continuously
 * without causing any layout shift (CLS = 0).
 */
export default function TypewriterText({
  phrasePairs,
  segments,
  lines,
  speed = 80,
  deleteSpeed = 35,
  pauseBetweenLines = 350,
  pauseAtEnd = 2400,
  pauseBeforeNext = 400,
}) {
  // Support phrasePairs, or single pair from segments/lines, or default travel pairs
  const pairs = phrasePairs || [
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
      { text: 'Unforgettable Journeys.', className: 'gradient-text' },
    ],
  ];

  const [pairIndex, setPairIndex] = useState(0);
  const currentPair = pairs[pairIndex % pairs.length];

  // Animation states: 'TYPING_LINE_1' | 'PAUSE_LINE_1' | 'TYPING_LINE_2' | 'PAUSED_FULL' | 'DELETING_LINE_2' | 'DELETING_LINE_1' | 'PAUSED_EMPTY'
  const [phase, setPhase] = useState('TYPING_LINE_1');
  const [line1Chars, setLine1Chars] = useState(0);
  const [line2Chars, setLine2Chars] = useState(0);

  const line1Text = currentPair[0]?.text || '';
  const line2Text = currentPair[1]?.text || '';

  useEffect(() => {
    let timer;

    if (phase === 'TYPING_LINE_1') {
      if (line1Chars < line1Text.length) {
        const char = line1Text[line1Chars];
        const delay = (char === '.' || char === '!' || char === '?') ? speed + 100 : speed;
        timer = setTimeout(() => {
          setLine1Chars((c) => c + 1);
        }, delay);
      } else {
        setPhase('PAUSE_LINE_1');
      }
    } else if (phase === 'PAUSE_LINE_1') {
      timer = setTimeout(() => {
        setPhase('TYPING_LINE_2');
      }, pauseBetweenLines);
    } else if (phase === 'TYPING_LINE_2') {
      if (line2Chars < line2Text.length) {
        const char = line2Text[line2Chars];
        const delay = (char === '.' || char === '!' || char === '?') ? speed + 100 : speed;
        timer = setTimeout(() => {
          setLine2Chars((c) => c + 1);
        }, delay);
      } else {
        setPhase('PAUSED_FULL');
      }
    } else if (phase === 'PAUSED_FULL') {
      // Pause at full text display before looping back
      timer = setTimeout(() => {
        setPhase('DELETING_LINE_2');
      }, pauseAtEnd);
    } else if (phase === 'DELETING_LINE_2') {
      if (line2Chars > 0) {
        timer = setTimeout(() => {
          setLine2Chars((c) => c - 1);
        }, deleteSpeed);
      } else {
        setPhase('DELETING_LINE_1');
      }
    } else if (phase === 'DELETING_LINE_1') {
      if (line1Chars > 0) {
        timer = setTimeout(() => {
          setLine1Chars((c) => c - 1);
        }, deleteSpeed);
      } else {
        setPhase('PAUSED_EMPTY');
      }
    } else if (phase === 'PAUSED_EMPTY') {
      // Pause slightly when empty before typing next pair
      timer = setTimeout(() => {
        setPairIndex((prev) => (prev + 1) % pairs.length);
        setPhase('TYPING_LINE_1');
      }, pauseBeforeNext);
    }

    return () => clearTimeout(timer);
  }, [
    phase,
    line1Chars,
    line2Chars,
    line1Text,
    line2Text,
    speed,
    deleteSpeed,
    pauseBetweenLines,
    pauseAtEnd,
    pauseBeforeNext,
    pairs.length,
  ]);

  // Determine which line the cursor should be rendered on
  const showCursorOnLine1 =
    phase === 'TYPING_LINE_1' ||
    phase === 'PAUSE_LINE_1' ||
    phase === 'DELETING_LINE_1' ||
    phase === 'PAUSED_EMPTY';

  const showCursorOnLine2 =
    phase === 'TYPING_LINE_2' ||
    phase === 'PAUSED_FULL' ||
    phase === 'DELETING_LINE_2';

  return (
    <span className="typewriter">
      {/* Line 1 */}
      <span className={`typewriter__line ${currentPair[0]?.className || ''}`}>
        {line1Text.slice(0, line1Chars)}
        {showCursorOnLine1 && (
          <span className="typewriter__cursor" aria-hidden="true">|</span>
        )}
      </span>

      {/* Line 2 */}
      <span className={`typewriter__line ${currentPair[1]?.className || ''}`}>
        {line2Text.slice(0, line2Chars)}
        {showCursorOnLine2 && (
          <span className="typewriter__cursor" aria-hidden="true">|</span>
        )}
      </span>
    </span>
  );
}

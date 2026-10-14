/**
 * Dynamic Date Utilities for Travel Planning
 * Respects local user calendar dates, timezones, and prevents historical/past dates.
 */

/**
 * Format a Date object to YYYY-MM-DD using local calendar year, month, and day.
 * Avoids UTC timezone shifts.
 */
export function formatLocalDate(date) {
  if (!date || !(date instanceof Date) || isNaN(date.getTime())) {
    return '';
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Returns tomorrow's date in user's local calendar as YYYY-MM-DD.
 * New trips must not allow today or yesterday.
 */
export function getLocalTomorrowDate() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return formatLocalDate(d);
}

/**
 * Returns a future date N days from today in local calendar as YYYY-MM-DD.
 */
export function getLocalFutureDate(daysAhead = 5) {
  const d = new Date();
  d.setDate(d.getDate() + daysAhead);
  return formatLocalDate(d);
}

/**
 * Given a start date string (YYYY-MM-DD), calculate a sensible default end date (e.g. +4 days).
 */
export function getDefaultEndDate(startDateStr, durationDays = 4) {
  if (!startDateStr) {
    return getLocalFutureDate(5);
  }
  const parts = startDateStr.split('-').map(Number);
  if (parts.length === 3) {
    const d = new Date(parts[0], parts[1] - 1, parts[2]);
    d.setDate(d.getDate() + durationDays);
    return formatLocalDate(d);
  }
  return getLocalFutureDate(5);
}

/**
 * Checks whether a given YYYY-MM-DD date string is on or after tomorrow in the local calendar.
 */
export function isValidFutureDate(dateStr) {
  if (!dateStr) return false;
  const tomorrowStr = getLocalTomorrowDate();
  return dateStr >= tomorrowStr;
}

/**
 * Currency & date formatting utilities configured for Indian Rupees (₹).
 */

export function formatCurrency(amount) {
  if (amount == null || isNaN(amount)) return '₹0';
  return `₹${Number(amount).toLocaleString('en-IN')}`;
}

export function formatINR(amount) {
  return formatCurrency(amount);
}

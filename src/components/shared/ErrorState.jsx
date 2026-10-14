import { AlertTriangle } from 'lucide-react';
import './ErrorState.css';

export default function ErrorState({ title = 'Something went wrong', message, onRetry }) {
  return (
    <div className="error-state animate-fadeInUp">
      <div className="error-state__icon-wrap">
        <AlertTriangle size={40} />
      </div>
      <h3 className="error-state__title">{title}</h3>
      <p className="error-state__message">
        {message || 'An unexpected error occurred. Please try again.'}
      </p>
      {onRetry && (
        <button className="btn btn-primary" onClick={onRetry}>
          Try Again
        </button>
      )}
    </div>
  );
}

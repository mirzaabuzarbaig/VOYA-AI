import { Loader } from 'lucide-react';
import './LoadingSpinner.css';

export default function LoadingSpinner({ size = 'md', message = 'Loading...' }) {
  const sizes = { sm: 20, md: 32, lg: 48 };
  return (
    <div className={`loading-spinner loading-spinner--${size}`}>
      <Loader size={sizes[size]} className="loading-spinner__icon" />
      {message && <p className="loading-spinner__message">{message}</p>}
    </div>
  );
}

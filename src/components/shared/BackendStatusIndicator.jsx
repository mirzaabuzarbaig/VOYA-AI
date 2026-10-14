import { useState, useEffect } from 'react';
import { checkBackendHealth } from '../../services/api';
import { Database, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';
import './BackendStatusIndicator.css';

export default function BackendStatusIndicator() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [minimized, setMinimized] = useState(false);

  const fetchHealth = async () => {
    setLoading(true);
    try {
      const res = await checkBackendHealth();
      setHealth(res);
    } catch (err) {
      setHealth({ status: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  const isConnected = health?.status === 'ok';

  if (minimized) {
    return (
      <button
        className="backend-status-minimized"
        onClick={() => setMinimized(false)}
        title="Show Backend Status"
        aria-label="Show Backend Status"
      >
        <span className={`backend-status__dot ${isConnected ? 'backend-status__dot--online' : 'backend-status__dot--offline'}`} />
        <Database size={14} />
      </button>
    );
  }

  return (
    <aside className="backend-status-banner" aria-label="Backend Status">
      <div className="backend-status__header">
        <div className="backend-status__title-wrap">
          <span className={`backend-status__dot ${isConnected ? 'backend-status__dot--online' : 'backend-status__dot--offline'}`} />
          <Database size={14} className="backend-status__icon" />
          <span className="backend-status__title">
            {isConnected ? 'Spring Boot + PostgreSQL' : 'Backend Offline'}
          </span>
        </div>
        <div className="backend-status__actions">
          <button
            onClick={fetchHealth}
            disabled={loading}
            className={`backend-status__btn-icon ${loading ? 'backend-status__btn-icon--spin' : ''}`}
            title="Refresh health check"
            aria-label="Refresh health check"
          >
            <RefreshCw size={12} />
          </button>
          <button
            onClick={() => setMinimized(true)}
            className="backend-status__btn-close"
            title="Minimize"
            aria-label="Minimize"
          >
            &times;
          </button>
        </div>
      </div>

      <div className="backend-status__body">
        {isConnected ? (
          <div className="backend-status__detail">
            <CheckCircle2 size={13} className="text-emerald" />
            <span>Service: <strong>{health?.service || 'VoyaAI backend'}</strong></span>
            <span className="backend-status__badge">HTTP 200</span>
          </div>
        ) : (
          <div className="backend-status__detail">
            <AlertCircle size={13} className="text-amber" />
            <span>Mock mode active (Spring Boot: 8080)</span>
          </div>
        )}
      </div>
    </aside>
  );
}

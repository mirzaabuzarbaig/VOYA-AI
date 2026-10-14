import './EmptyState.css';

export default function EmptyState({ icon: Icon, title, message, action }) {
  return (
    <div className="empty-state animate-fadeInUp">
      {Icon && (
        <div className="empty-state__icon-wrap">
          <Icon size={40} />
        </div>
      )}
      <h3 className="empty-state__title">{title}</h3>
      <p className="empty-state__message">{message}</p>
      {action && (
        <div className="empty-state__action">{action}</div>
      )}
    </div>
  );
}

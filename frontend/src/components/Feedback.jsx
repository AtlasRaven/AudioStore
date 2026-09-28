export function Loading({ label = 'Đang tải dữ liệu…' }) { return <div className="page-state" role="status">{label}</div>; }
export function ErrorMessage({ message, onRetry }) {
  return <div className="notice notice--error" role="alert"><span>{message}</span>{onRetry && <button className="button button--small button--outline" onClick={onRetry}>Thử lại</button>}</div>;
}
export function EmptyState({ title, detail }) { return <div className="empty-state"><h2>{title}</h2>{detail && <p>{detail}</p>}</div>; }

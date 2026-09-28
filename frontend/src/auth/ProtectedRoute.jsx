import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

export default function ProtectedRoute({ roles }) {
  const { user, loading } = useAuth(); const location = useLocation();
  if (loading) return <main className="page-state">Đang tải tài khoản…</main>;
  if (!user) return <Navigate to="/tai-khoan/dang-nhap" state={{ from: location }} replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/khong-co-quyen" replace />;
  return <Outlet />;
}

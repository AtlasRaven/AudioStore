import { NavLink, Outlet, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { adminNavigation, canSee, roles } from '../auth/permissions';

export default function AdminLayout() {
  const { user, signOut } = useAuth();
  return <div className="admin-shell">
    <aside className="admin-sidebar">
      <Link className="admin-brand" to="/admin"><span className="brand-mark__box">AS</span><span>AudioStore<br /><small>Quản lý cửa hàng</small></span></Link>
      <nav className="admin-nav" aria-label="Quản trị">
        {adminNavigation.filter((item) => canSee(user, item.roles)).map((item) => <NavLink end={item.to === '/admin'} key={item.to} to={item.to}>{item.label}</NavLink>)}
      </nav>
      <div className="admin-sidebar__bottom"><Link to="/">Về cửa hàng</Link><button className="text-button" onClick={signOut}>Đăng xuất</button></div>
    </aside>
    <div className="admin-main"><header className="admin-topbar"><div><span className="muted">Khu vực quản trị</span><strong>{user?.fullName}</strong></div><span className="role-tag">{roles[user?.role]}</span></header><main className="admin-content"><Outlet /></main></div>
  </div>;
}

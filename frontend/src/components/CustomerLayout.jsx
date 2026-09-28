import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { getCart } from '../utils/cart';
import { request } from '../api/client';

export default function CustomerLayout() {
  const { user, signOut } = useAuth(); const [count, setCount] = useState(() => getCart().reduce((sum, line) => sum + line.quantity, 0)); const [categories,setCategories]=useState([]);
  useEffect(() => {
    const update = () => setCount(getCart().reduce((sum, line) => sum + line.quantity, 0));
    window.addEventListener('audiostore:cart', update); return () => window.removeEventListener('audiostore:cart', update);
  }, []);
  useEffect(()=>{request({url:'/api/categories'}).then(setCategories).catch(()=>{});},[]);
  return <div className="site-shell">
    <div className="store-topline"><div className="container store-topline__inner"><span>Thiết bị âm thanh, chọn theo nhu cầu</span><span>COD hoặc chuyển khoản</span></div></div>
    <header className="store-header"><div className="container store-header__main">
      <Link className="brand-mark" to="/" aria-label="AudioStore, trang chủ"><span className="brand-mark__box">AS</span><span>AudioStore</span></Link>
      <nav className="main-nav" aria-label="Điều hướng chính">
        <NavLink to="/san-pham">Sản phẩm</NavLink>{categories.slice(0,3).map(category=><NavLink key={category.id} to={`/san-pham?category=${category.id}`}>{category.name}</NavLink>)}<a href="/#thuong-hieu">Thương hiệu</a>
      </nav>
      <div className="store-actions"><Link to="/gio-hang" className="cart-link">Giỏ hàng <span>{count}</span></Link>
        {user ? <div className="user-menu"><Link to={user.role === 'CUSTOMER' ? '/tai-khoan/ho-so' : '/admin'}>{user.fullName}</Link>{user.role==='CUSTOMER'&&<Link to="/tai-khoan/don-hang">Đơn hàng</Link>}<button className="text-button" onClick={signOut}>Đăng xuất</button></div> : <Link className="text-link" to="/tai-khoan/dang-nhap">Đăng nhập</Link>}
      </div>
    </div><div className="container store-header__sub"><span>Giao hàng toàn quốc</span><span>Đổi trả theo chính sách cửa hàng</span><Link to="/tu-van">Cần chọn thiết bị? Nhắn với tư vấn viên</Link></div></header>
    <main><Outlet /></main>
    <footer className="site-footer"><div className="container footer-grid"><div><Link className="brand-mark brand-mark--light" to="/">AudioStore</Link><p>Thiết bị âm thanh cho nhu cầu nghe nhạc, làm việc và thu âm tại nhà.</p></div><div><h2>Hỗ trợ mua hàng</h2><p>Thanh toán COD hoặc chuyển khoản</p><Link to="/tu-van">Nhờ tư vấn chọn thiết bị</Link></div><div><h2>Tài khoản</h2><Link to="/tai-khoan/don-hang">Đơn hàng của tôi</Link><Link to="/tai-khoan/dia-chi">Địa chỉ giao hàng</Link></div></div><div className="container footer-bottom">© AudioStore · Sản phẩm và giá bán được cập nhật tại cửa hàng</div></footer>
  </div>;
}

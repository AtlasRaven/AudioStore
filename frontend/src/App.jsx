import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import { adminRoles } from './auth/permissions';
import CustomerLayout from './components/CustomerLayout';
import AdminLayout from './components/AdminLayout';
import { AddressesPage, CartPage, CheckoutPage, LoginPage, OrdersPage, RegisterPage } from './pages/AccountPages';
import { CatalogPage, HomePage as StoreHomePage, ProductDetailPage as StoreProductPage } from './pages/StorePages';
import AdvisorPage from './pages/AdvisorPage';
import ProfilePage from './pages/ProfilePage';
import { AdminDashboard, AdminProducts, AiLogsPage, InventoryPage, OrdersAdminPage, PromotionsPage, PurchasesPage, ReportsPage, ReviewsPage, UsersPage } from './pages/AdminPages';

function NotFound(){return <section className="container auth-page"><div className="auth-card"><h1>Không tìm thấy trang</h1><p>Đường dẫn này không có trong AudioStore.</p><a className="button" href="/">Về trang chủ</a></div></section>;}
function Forbidden(){return <section className="container auth-page"><div className="auth-card"><h1>Bạn chưa được cấp quyền</h1><p>Hãy đăng nhập bằng tài khoản phù hợp để mở trang này.</p><a className="button" href="/">Về trang chủ</a></div></section>;}

export default function App(){
  return <BrowserRouter><AuthProvider><Routes>
    <Route element={<CustomerLayout/>}>
      <Route index element={<StoreHomePage/>}/>
      <Route path="san-pham" element={<CatalogPage/>}/>
      <Route path="san-pham/:id" element={<StoreProductPage/>}/>
      <Route path="gio-hang" element={<CartPage/>}/>
      <Route path="tu-van" element={<AdvisorPage/>}/>
      <Route path="tai-khoan/dang-nhap" element={<LoginPage/>}/>
      <Route path="tai-khoan/dang-ky" element={<RegisterPage/>}/>
      <Route element={<ProtectedRoute roles={['CUSTOMER']}/> }>
        <Route path="thanh-toan" element={<CheckoutPage/>}/>
        <Route path="tai-khoan/don-hang" element={<div className="container page-section"><OrdersPage/></div>}/>
        <Route path="tai-khoan/dia-chi" element={<div className="container page-section"><AddressesPage/></div>}/>
        <Route path="tai-khoan/ho-so" element={<div className="container page-section"><ProfilePage/></div>}/>
      </Route>
    </Route>
    <Route element={<ProtectedRoute roles={adminRoles}/> }><Route path="admin" element={<AdminLayout/>}>
      <Route index element={<AdminDashboard/>}/><Route path="san-pham" element={<AdminProducts/>}/><Route path="kho" element={<InventoryPage/>}/><Route path="nhap-hang" element={<PurchasesPage/>}/><Route path="don-hang" element={<OrdersAdminPage/>}/><Route path="khuyen-mai" element={<PromotionsPage/>}/><Route path="bao-cao" element={<ReportsPage/>}/>
      <Route element={<ProtectedRoute roles={['ADMIN']}/> }><Route path="tai-khoan" element={<UsersPage/>}/></Route>
      <Route element={<ProtectedRoute roles={['ADMIN','MARKETING_CSKH']}/> }><Route path="danh-gia" element={<ReviewsPage/>}/><Route path="ai" element={<AiLogsPage/>}/></Route>
    </Route></Route>
    <Route path="khong-co-quyen" element={<Forbidden/>}/><Route path="*" element={<NotFound/>}/>
  </Routes></AuthProvider></BrowserRouter>;
}

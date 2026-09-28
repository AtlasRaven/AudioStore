export const roles = {
  ADMIN: 'Quản trị viên',
  STAFF_KHO: 'Nhân viên kho',
  STAFF_BAN_HANG: 'Nhân viên bán hàng',
  KE_TOAN: 'Kế toán',
  MARKETING_CSKH: 'Marketing và chăm sóc khách',
  CUSTOMER: 'Khách hàng',
};

export const adminRoles = ['ADMIN', 'STAFF_KHO', 'STAFF_BAN_HANG', 'KE_TOAN', 'MARKETING_CSKH'];
export const canSee = (user, allowed) => Boolean(user && allowed.includes(user.role));

export const adminNavigation = [
  { label: 'Tổng quan', to: '/admin', roles: adminRoles },
  { label: 'Sản phẩm', to: '/admin/san-pham', roles: adminRoles },
  { label: 'Kho hàng', to: '/admin/kho', roles: ['ADMIN', 'STAFF_KHO', 'STAFF_BAN_HANG', 'KE_TOAN'] },
  { label: 'Nhập hàng', to: '/admin/nhap-hang', roles: ['ADMIN', 'STAFF_KHO', 'KE_TOAN'] },
  { label: 'Đơn hàng', to: '/admin/don-hang', roles: adminRoles },
  { label: 'Khuyến mãi', to: '/admin/khuyen-mai', roles: ['ADMIN', 'STAFF_BAN_HANG', 'KE_TOAN', 'MARKETING_CSKH'] },
  { label: 'Báo cáo', to: '/admin/bao-cao', roles: ['ADMIN', 'STAFF_BAN_HANG', 'KE_TOAN'] },
  { label: 'Tài khoản', to: '/admin/tai-khoan', roles: ['ADMIN'] },
  { label: 'Đánh giá', to: '/admin/danh-gia', roles: ['ADMIN', 'MARKETING_CSKH'] },
  { label: 'Nhật ký AI', to: '/admin/ai', roles: ['ADMIN', 'MARKETING_CSKH'] },
];

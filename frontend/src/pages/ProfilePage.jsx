import { useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { request } from '../api/client';
import { ErrorMessage } from '../components/Feedback';
import PageTitle from '../components/PageTitle';

export default function ProfilePage(){
  const {user}=useAuth();const [form,setForm]=useState({oldPassword:'',newPassword:''});const [message,setMessage]=useState('');const [error,setError]=useState('');
  const submit=async(e)=>{e.preventDefault();setError('');setMessage('');try{await request({method:'POST',url:'/api/auth/change-password',data:form});setMessage('Đã đổi mật khẩu.');setForm({oldPassword:'',newPassword:''});}catch(err){setError(err.message);}};
  return <section className="stack"><PageTitle title="Thông tin tài khoản" detail="Thông tin đăng nhập của bạn."/><div className="panel stack"><p><strong>Họ tên:</strong> {user?.fullName}</p><p><strong>Email:</strong> {user?.email}</p><p><strong>Số điện thoại:</strong> {user?.phone||'Chưa cập nhật'}</p></div><form className="panel stack" onSubmit={submit}><h2>Đổi mật khẩu</h2>{error&&<ErrorMessage message={error}/>}{message&&<p className="status-pill status-pill--success">{message}</p>}<div className="field"><label htmlFor="old-password">Mật khẩu hiện tại</label><input id="old-password" type="password" required value={form.oldPassword} onChange={e=>setForm({...form,oldPassword:e.target.value})}/></div><div className="field"><label htmlFor="new-password">Mật khẩu mới</label><input id="new-password" type="password" minLength="8" maxLength="72" required value={form.newPassword} onChange={e=>setForm({...form,newPassword:e.target.value})}/></div><button className="button">Đổi mật khẩu</button></form></section>;
}

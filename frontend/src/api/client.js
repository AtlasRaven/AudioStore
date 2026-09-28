import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
export const http = axios.create({ baseURL, timeout: 15000, headers: { 'Content-Type': 'application/json' } });

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('audiostore.token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || 'Không kết nối được máy chủ. Vui lòng thử lại.';
    if (error.response?.status === 401 && !error.config?.url?.includes('/api/auth/login')) {
      localStorage.removeItem('audiostore.token');
      localStorage.removeItem('audiostore.user');
      window.dispatchEvent(new Event('audiostore:signout'));
    }
    return Promise.reject(new Error(message));
  },
);

export async function request(config) {
  const response = await http(config);
  if (response.data?.success === false) throw new Error(response.data.message || 'Yêu cầu chưa thực hiện được.');
  return response.data?.data;
}

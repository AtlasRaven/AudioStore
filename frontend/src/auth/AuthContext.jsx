import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { request } from '../api/client';

const AuthContext = createContext(null);
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('audiostore.user') || 'null'); } catch { return null; }
  });
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('audiostore.token')));

  useEffect(() => {
    const signOut = () => { setUser(null); setLoading(false); };
    window.addEventListener('audiostore:signout', signOut);
    if (localStorage.getItem('audiostore.token')) {
      request({ url: '/api/auth/me' }).then((next) => {
        setUser(next); localStorage.setItem('audiostore.user', JSON.stringify(next));
      }).catch(() => setUser(null)).finally(() => setLoading(false));
    }
    return () => window.removeEventListener('audiostore:signout', signOut);
  }, []);

  const value = useMemo(() => ({
    user, loading,
    async signIn(email, password) {
      const result = await request({ method: 'POST', url: '/api/auth/login', data: { email, password } });
      localStorage.setItem('audiostore.token', result.token);
      localStorage.setItem('audiostore.user', JSON.stringify(result.user));
      setUser(result.user);
      return result.user;
    },
    async register(values) {
      const result = await request({ method: 'POST', url: '/api/auth/register', data: values });
      localStorage.setItem('audiostore.token', result.token);
      localStorage.setItem('audiostore.user', JSON.stringify(result.user));
      setUser(result.user);
      return result.user;
    },
    signOut() {
      localStorage.removeItem('audiostore.token'); localStorage.removeItem('audiostore.user'); setUser(null);
    },
  }), [user, loading]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() { return useContext(AuthContext); }

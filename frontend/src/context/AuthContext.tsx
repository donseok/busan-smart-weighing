import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';

type UserRole = 'ADMIN' | 'MANAGER' | 'DRIVER';

interface User {
  loginId: string;
  name: string;
  role: UserRole;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  hasRole: (role: UserRole | UserRole[]) => boolean;
  login: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

function parseJwt(token: string): Record<string, unknown> | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

function extractUser(token: string): User | null {
  const payload = parseJwt(token);
  if (!payload) return null;
  return {
    loginId: (payload.sub as string) || '',
    name: (payload.name as string) || (payload.sub as string) || '',
    role: ((payload.role as string) || 'MANAGER') as UserRole,
  };
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const token = localStorage.getItem('accessToken');
    return token ? extractUser(token) : null;
  });

  const isAuthenticated = user !== null;

  const hasRole = useCallback((role: UserRole | UserRole[]) => {
    if (!user) return false;
    if (Array.isArray(role)) return role.includes(user.role);
    return user.role === role;
  }, [user]);

  const login = useCallback((accessToken: string, refreshToken: string) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    setUser(extractUser(accessToken));
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  }, []);

  // Listen for token changes (e.g., after refresh)
  useEffect(() => {
    const checkToken = () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
        const newUser = extractUser(token);
        if (newUser && newUser.loginId !== user?.loginId) {
          setUser(newUser);
        }
      }
    };
    window.addEventListener('storage', checkToken);
    return () => window.removeEventListener('storage', checkToken);
  }, [user]);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, hasRole, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

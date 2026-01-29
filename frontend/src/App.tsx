/**
 * 루트 앱 컴포넌트
 *
 * React Router를 통한 최상위 라우팅을 정의합니다.
 * JWT 토큰 유무에 따라 로그인 페이지와 메인 레이아웃을 분기하며,
 * 인증되지 않은 사용자는 로그인 페이지로 리다이렉트됩니다.
 *
 * @returns 앱 루트 JSX
 */
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import LoginPage from './pages/LoginPage';

/** JWT 토큰 존재 여부로 인증을 확인하는 가드 컴포넌트 */
const RequireAuth: React.FC<{ children: React.ReactElement }> = ({ children }) => {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/*" element={<RequireAuth><MainLayout /></RequireAuth>} />
    </Routes>
  );
};

export default App;

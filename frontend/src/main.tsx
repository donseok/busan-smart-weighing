/**
 * 애플리케이션 엔트리 포인트
 *
 * React 애플리케이션의 진입점으로, 루트 DOM에 앱을 마운트합니다.
 * BrowserRouter, Ant Design ConfigProvider(한국어 로케일),
 * ThemeProvider, TabProvider 등 전역 프로바이더를 설정합니다.
 * 다크/라이트 테마에 따른 Ant Design 테마 설정을 적용합니다.
 *
 * @module main
 */
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import koKR from 'antd/locale/ko_KR';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import { TabProvider } from './context/TabContext';
import { AuthProvider } from './context/AuthContext';
import { darkTheme, lightTheme } from './theme/themeConfig';
import App from './App';
import './styles/tableScroll.css';

// 테마를 동적으로 적용하는 래퍼 컴포넌트
const ThemedApp: React.FC = () => {
  const { themeMode } = useTheme();
  const currentTheme = themeMode === 'dark' ? darkTheme : lightTheme;

  return (
    <ConfigProvider locale={koKR} theme={currentTheme}>
      <BrowserRouter>
        <AuthProvider>
          <TabProvider>
            <App />
          </TabProvider>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
};

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ThemeProvider>
      <ThemedApp />
    </ThemeProvider>
  </React.StrictMode>
);

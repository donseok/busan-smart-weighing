import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import koKR from 'antd/locale/ko_KR';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import { TabProvider } from './context/TabContext';
import { darkTheme, lightTheme } from './theme/themeConfig';
import App from './App';

// 테마를 동적으로 적용하는 래퍼 컴포넌트
const ThemedApp: React.FC = () => {
  const { themeMode } = useTheme();
  const currentTheme = themeMode === 'dark' ? darkTheme : lightTheme;

  return (
    <ConfigProvider locale={koKR} theme={currentTheme}>
      <BrowserRouter>
        <TabProvider>
          <App />
        </TabProvider>
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

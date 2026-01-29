/**
 * 테마 관리 컨텍스트 (ThemeContext)
 *
 * 다크/라이트 모드 전환을 관리하는 React Context입니다.
 * localStorage를 통해 사용자의 테마 설정을 영속화하며,
 * toggleTheme 함수로 모드를 전환할 수 있습니다.
 * Ant Design ConfigProvider와 연동하여 전체 UI 테마를 변경합니다.
 *
 * @module ThemeContext
 */
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

export type ThemeMode = 'light' | 'dark';

/** 테마 컨텍스트 타입 정의 */
interface ThemeContextType {
  themeMode: ThemeMode;                    // 현재 테마 모드
  toggleTheme: () => void;                 // 다크/라이트 토글
  setThemeMode: (mode: ThemeMode) => void; // 직접 모드 설정
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

const THEME_STORAGE_KEY = 'busan-weighing-theme'; // localStorage 영속화 키

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [themeMode, setThemeModeState] = useState<ThemeMode>(() => {
    const saved = localStorage.getItem(THEME_STORAGE_KEY);
    return (saved as ThemeMode) || 'dark';
  });

  useEffect(() => {
    localStorage.setItem(THEME_STORAGE_KEY, themeMode);
    // HTML 속성 업데이트 (CSS 변수용)
    document.documentElement.setAttribute('data-theme', themeMode);
  }, [themeMode]);

  const toggleTheme = () => {
    setThemeModeState((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };

  const setThemeMode = (mode: ThemeMode) => {
    setThemeModeState(mode);
  };

  return (
    <ThemeContext.Provider value={{ themeMode, toggleTheme, setThemeMode }}>
      {children}
    </ThemeContext.Provider>
  );
};

/** 테마 컨텍스트 사용 훅 (ThemeProvider 내부에서만 사용 가능) */
export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

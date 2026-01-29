import React, {
  createContext,
  useContext,
  useState,
  useCallback,
  useEffect,
  useRef,
  Suspense,
} from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { message, Spin } from 'antd';
import { PAGE_REGISTRY, PINNED_TABS, MAX_TABS } from '../config/pageRegistry';

export interface TabItem {
  key: string;
  title: string;
  icon: React.ReactNode;
  closable: boolean;
  component: React.ReactNode;
}

interface TabContextType {
  tabs: TabItem[];
  activeKey: string;
  openTab: (key: string) => void;
  closeTab: (key: string) => void;
  closeOtherTabs: (key: string) => void;
  closeAllClosable: () => void;
  closeRightTabs: (key: string) => void;
  setActiveTab: (key: string) => void;
  moveTab: (dragKey: string, hoverKey: string) => void;
}

const TabContext = createContext<TabContextType | null>(null);

const SESSION_KEY = 'bsw_open_tabs';
const ACTIVE_KEY = 'bsw_active_tab';

function loadSessionTabs(): string[] {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveSessionTabs(keys: string[]) {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(keys));
}

function loadActiveKey(): string | null {
  return sessionStorage.getItem(ACTIVE_KEY);
}

function saveActiveKey(key: string) {
  sessionStorage.setItem(ACTIVE_KEY, key);
}

function createTabItem(key: string): TabItem | null {
  const config = PAGE_REGISTRY[key];
  if (!config) return null;
  const Component = config.component;
  return {
    key,
    title: config.title,
    icon: config.icon,
    closable: config.closable,
    component: (
      <Suspense fallback={<div style={{ textAlign: 'center', padding: 40 }}><Spin /></div>}>
        <Component />
      </Suspense>
    ),
  };
}

export const TabProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const isInternalNav = useRef(false);

  // 초기 탭 구성: sessionStorage 복원 또는 pinned 탭만
  const [tabs, setTabs] = useState<TabItem[]>(() => {
    const savedKeys = loadSessionTabs();
    const initialKeys = savedKeys.length > 0
      ? savedKeys
      : [...PINNED_TABS];
    // pinned 탭이 항상 포함되도록 보장
    const allKeys = [...new Set([...PINNED_TABS, ...initialKeys])];
    return allKeys
      .map(createTabItem)
      .filter((t): t is TabItem => t !== null);
  });

  const [activeKey, setActiveKeyState] = useState<string>(() => {
    const savedActive = loadActiveKey();
    if (savedActive && PAGE_REGISTRY[savedActive]) return savedActive;
    return PINNED_TABS[0] || '/weighing-station';
  });

  // sessionStorage 동기화
  useEffect(() => {
    saveSessionTabs(tabs.map(t => t.key));
  }, [tabs]);

  useEffect(() => {
    saveActiveKey(activeKey);
  }, [activeKey]);

  const openTab = useCallback((key: string) => {
    const config = PAGE_REGISTRY[key];
    if (!config) return;

    setTabs(prev => {
      const exists = prev.find(t => t.key === key);
      if (exists) {
        // 이미 열린 탭이면 활성화만
        setActiveKeyState(key);
        isInternalNav.current = true;
        navigate(key);
        return prev;
      }
      // 최대 탭 수 체크
      if (prev.length >= MAX_TABS) {
        message.warning(`최대 ${MAX_TABS}개까지 탭을 열 수 있습니다`);
        return prev;
      }
      const newTab = createTabItem(key);
      if (!newTab) return prev;
      setActiveKeyState(key);
      isInternalNav.current = true;
      navigate(key);
      return [...prev, newTab];
    });
  }, [navigate]);

  const closeTab = useCallback((key: string) => {
    setTabs(prev => {
      const target = prev.find(t => t.key === key);
      if (!target || !target.closable) return prev;

      const idx = prev.findIndex(t => t.key === key);
      const next = prev.filter(t => t.key !== key);

      // 닫는 탭이 활성 탭이면 인접 탭으로 이동
      setActiveKeyState(current => {
        if (current === key) {
          const newActive = next[Math.min(idx, next.length - 1)]?.key
            || PINNED_TABS[0]
            || '/weighing-station';
          isInternalNav.current = true;
          navigate(newActive);
          return newActive;
        }
        return current;
      });

      return next;
    });
  }, [navigate]);

  const closeOtherTabs = useCallback((key: string) => {
    setTabs(prev => prev.filter(t => t.key === key || !t.closable));
    setActiveKeyState(key);
    isInternalNav.current = true;
    navigate(key);
  }, [navigate]);

  const closeAllClosable = useCallback(() => {
    setTabs(prev => {
      const remaining = prev.filter(t => !t.closable);
      const newActive = remaining[0]?.key || PINNED_TABS[0] || '/weighing-station';
      setActiveKeyState(newActive);
      isInternalNav.current = true;
      navigate(newActive);
      return remaining;
    });
  }, [navigate]);

  const closeRightTabs = useCallback((key: string) => {
    setTabs(prev => {
      const idx = prev.findIndex(t => t.key === key);
      if (idx === -1) return prev;
      const right = prev.slice(idx + 1).filter(t => t.closable);
      if (right.length === 0) return prev;
      const rightKeys = new Set(right.map(t => t.key));
      const next = prev.filter(t => !rightKeys.has(t.key));
      // 활성 탭이 닫힌 경우 기준 탭으로 이동
      setActiveKeyState(current => {
        if (rightKeys.has(current)) {
          isInternalNav.current = true;
          navigate(key);
          return key;
        }
        return current;
      });
      return next;
    });
  }, [navigate]);

  const setActiveTab = useCallback((key: string) => {
    setActiveKeyState(key);
    isInternalNav.current = true;
    navigate(key);
  }, [navigate]);

  const moveTab = useCallback((dragKey: string, hoverKey: string) => {
    if (dragKey === hoverKey) return;
    setTabs(prev => {
      const dragIdx = prev.findIndex(t => t.key === dragKey);
      const hoverIdx = prev.findIndex(t => t.key === hoverKey);
      if (dragIdx === -1 || hoverIdx === -1) return prev;
      const next = [...prev];
      const [dragged] = next.splice(dragIdx, 1);
      next.splice(hoverIdx, 0, dragged);
      return next;
    });
  }, []);

  // 브라우저 뒤로/앞으로 처리: URL 변경 감지 → 해당 탭 활성화
  useEffect(() => {
    if (isInternalNav.current) {
      isInternalNav.current = false;
      return;
    }
    const path = location.pathname;
    if (!PAGE_REGISTRY[path]) return;

    // 탭이 없으면 열기
    setTabs(prev => {
      const exists = prev.find(t => t.key === path);
      if (exists) {
        setActiveKeyState(path);
        return prev;
      }
      if (prev.length >= MAX_TABS) return prev;
      const newTab = createTabItem(path);
      if (!newTab) return prev;
      setActiveKeyState(path);
      return [...prev, newTab];
    });
  }, [location.pathname]);

  // 앱 시작 시 초기 URL 동기화
  useEffect(() => {
    const path = location.pathname;
    if (path === '/' || path === '') {
      isInternalNav.current = true;
      navigate(activeKey);
    } else if (PAGE_REGISTRY[path]) {
      // URL에 해당하는 탭이 있으면 활성화
      setTabs(prev => {
        const exists = prev.find(t => t.key === path);
        if (exists) {
          setActiveKeyState(path);
          return prev;
        }
        const newTab = createTabItem(path);
        if (!newTab) return prev;
        setActiveKeyState(path);
        return [...prev, newTab];
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <TabContext.Provider
      value={{ tabs, activeKey, openTab, closeTab, closeOtherTabs, closeAllClosable, closeRightTabs, setActiveTab, moveTab }}
    >
      {children}
    </TabContext.Provider>
  );
};

export function useTab(): TabContextType {
  const ctx = useContext(TabContext);
  if (!ctx) throw new Error('useTab must be used within TabProvider');
  return ctx;
}

export function clearTabSession() {
  sessionStorage.removeItem(SESSION_KEY);
  sessionStorage.removeItem(ACTIVE_KEY);
}

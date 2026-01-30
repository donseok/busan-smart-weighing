import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Layout, Menu, Typography, Button, Tooltip, Switch, Popover, Tabs, Dropdown } from 'antd';
import type { MenuProps } from 'antd';
import {
  DashboardOutlined,
  CarOutlined,
  ExperimentOutlined,
  DatabaseOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  BankOutlined,
  ToolOutlined,
  LogoutOutlined,
  SunOutlined,
  MoonOutlined,
  SettingOutlined,
  UserOutlined,
  ControlOutlined,
  FileSearchOutlined,
  NotificationOutlined,
  TagsOutlined,
  StarOutlined,
  BarChartOutlined,
  MonitorOutlined,
  QuestionCircleOutlined,
  DesktopOutlined,
} from '@ant-design/icons';
import { darkColors, lightColors, spacing, typography } from '../theme/themeConfig';
import { useTheme } from '../context/ThemeContext';
import { useTab, clearTabSession } from '../context/TabContext';
import { useAuth } from '../context/AuthContext';
import { PAGE_REGISTRY } from '../config/pageRegistry';
import { useKeyboardShortcuts } from '../hooks/useKeyboardShortcuts';
import FavoritesList from '../components/FavoritesList';
import FavoriteButton from '../components/FavoriteButton';
import OnboardingTour from '../components/OnboardingTour';

const { Header, Sider, Content } = Layout;

/**
 * 사이드바 메뉴 항목 정의
 *
 * 애플리케이션의 전체 메뉴 구조를 정의합니다.
 * 각 항목은 경로(key), 아이콘(icon), 라벨(label)로 구성되며,
 * '기준정보 관리'와 '시스템 관리'는 하위 메뉴(children)를 포함합니다.
 */
const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '대시보드' },
  { key: '/weighing-station', icon: <DesktopOutlined />, label: '계량소 관제' },
  { key: '/dispatch', icon: <CarOutlined />, label: '배차 관리' },
  { key: '/weighing', icon: <ExperimentOutlined />, label: '계량 현황' },
  { key: '/inquiry', icon: <FileSearchOutlined />, label: '계량 조회' },
  { key: '/gate-pass', icon: <CheckCircleOutlined />, label: '출문 관리' },
  { key: '/slips', icon: <FileTextOutlined />, label: '전자 계량표' },
  { key: '/statistics', icon: <BarChartOutlined />, label: '통계/보고서' },
  { key: '/monitoring', icon: <MonitorOutlined />, label: '장비 관제' },
  {
    key: 'master',
    icon: <DatabaseOutlined />,
    label: '기준정보 관리',
    children: [
      { key: '/master/codes', icon: <TagsOutlined />, label: '코드 관리' },
      { key: '/master/companies', icon: <BankOutlined />, label: '운송사' },
      { key: '/master/vehicles', icon: <CarOutlined />, label: '차량' },
      { key: '/master/scales', icon: <ToolOutlined />, label: '계량대' },
    ],
  },
  { key: '/notices', icon: <NotificationOutlined />, label: '공지사항' },
  { key: '/help', icon: <QuestionCircleOutlined />, label: '이용 안내' },
  {
    key: 'admin',
    icon: <SettingOutlined />,
    label: '시스템 관리',
    children: [
      { key: '/admin/users', icon: <UserOutlined />, label: '사용자 관리' },
      { key: '/admin/settings', icon: <ControlOutlined />, label: '시스템 설정' },
      { key: '/admin/audit-logs', icon: <FileSearchOutlined />, label: '감사 로그' },
    ],
  },
];

/**
 * 메인 레이아웃 컴포넌트
 *
 * 애플리케이션의 전체 레이아웃을 구성하는 최상위 레이아웃 컴포넌트입니다.
 * - 왼쪽 사이드바: 접이식 네비게이션 메뉴 (로고, 메뉴 항목)
 * - 상단 헤더: 즐겨찾기, 테마 전환, 사용자 정보, 로그아웃
 * - 탭 바: 멀티 탭 인터페이스 (드래그 재배치, 컨텍스트 메뉴 지원)
 * - 콘텐츠 영역: 활성 탭의 페이지 콘텐츠 표시
 *
 * 모든 탭 컴포넌트를 동시에 렌더링하고 display로 전환하여
 * 탭 변경 시 컴포넌트가 언마운트되지 않도록 합니다.
 *
 * @returns 메인 레이아웃 JSX
 */
const MainLayout: React.FC = () => {
  /** 사이드바 접힘 상태 */
  const [collapsed, setCollapsed] = useState(false);
  /** 즐겨찾기 팝오버 열림 상태 */
  const [favoritesOpen, setFavoritesOpen] = useState(false);
  const navigate = useNavigate();
  /** 테마 모드(dark/light) 및 전환 함수 */
  const { themeMode, toggleTheme } = useTheme();
  /** 탭 관리 컨텍스트 (열기, 닫기, 이동 등) */
  const { tabs, activeKey, openTab, closeTab, closeOtherTabs, closeAllClosable, closeRightTabs, setActiveTab, moveTab } = useTab();
  /** 인증 컨텍스트 (사용자 정보 및 역할 확인) */
  const { user, logout } = useAuth();
  /** 탭 드래그 시 드래그 중인 탭 키를 저장하는 ref */
  const dragKeyRef = useRef<string | null>(null);
  /** 온보딩 투어 대상 요소 refs */
  const siderRef = useRef<HTMLDivElement>(null);
  const headerRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);

  /**
   * 현재 활성 탭의 경로에 따라 열어야 할 서브메뉴 키를 계산
   *
   * 하위 메뉴 항목이 활성 상태일 때 해당 상위 메뉴를 자동으로 펼칩니다.
   *
   * @returns 열어야 할 서브메뉴 키 배열
   */
  const getOpenKeys = () => {
    const keys: string[] = [];
    for (const item of menuItems) {
      if ('children' in item && item.children) {
        for (const child of item.children) {
          if (child.key === activeKey) {
            keys.push(item.key);
          }
        }
      }
    }
    return keys;
  };

  /** 서브메뉴 열림 상태 (초기값: 현재 경로의 상위 메뉴 또는 기준정보) */
  const [openKeys, setOpenKeys] = useState<string[]>(() => {
    const pathKeys = getOpenKeys();
    return pathKeys.length > 0 ? pathKeys : ['master'];
  });

  /** 활성 탭 변경 시 해당 서브메뉴를 자동으로 펼침 */
  useEffect(() => {
    const pathKeys = getOpenKeys();
    if (pathKeys.length > 0) {
      setOpenKeys(prev => {
        const merged = new Set([...prev, ...pathKeys]);
        return Array.from(merged);
      });
    }
  }, [activeKey]);

  /**
   * 현재 활성 탭에 해당하는 메뉴 항목 정보
   * (즐겨찾기 버튼에 현재 페이지 이름과 경로를 전달하기 위해 사용)
   */
  const currentPageInfo = menuItems.flatMap(item =>
    'children' in item && item.children ? item.children : [item]
  ).find(item => item?.key === activeKey);

  /** 현재 테마에 맞는 색상 팔레트 선택 */
  const colors = themeMode === 'dark' ? darkColors : lightColors;
  /** 다크 모드 여부 */
  const isDark = themeMode === 'dark';

  /** 역할 기반 메뉴 필터링 - 사용자 역할에 따라 접근 가능한 메뉴만 표시 (미인증 시 전체 표시) */
  const filteredMenuItems = useMemo(() => {
    if (!user) return menuItems;
    return menuItems.map(item => {
      if ('children' in item && item.children) {
        const filteredChildren = item.children.filter(child => {
          const config = PAGE_REGISTRY[child.key];
          if (!config?.roles) return true;
          return config.roles.includes(user.role);
        });
        if (filteredChildren.length === 0) return null;
        return { ...item, children: filteredChildren };
      }
      const config = PAGE_REGISTRY[item.key];
      if (config?.roles && !config.roles.includes(user.role)) return null;
      return item;
    }).filter(Boolean);
  }, [user]);

  /** 탭 전환 시 fadeIn 애니메이션 스타일 주입 */
  useEffect(() => {
    const style = document.createElement('style');
    style.textContent = `
      @keyframes fadeIn {
        from { opacity: 0; transform: translateY(4px); }
        to { opacity: 1; transform: translateY(0); }
      }
    `;
    document.head.appendChild(style);
    return () => { document.head.removeChild(style); };
  }, []);

  useKeyboardShortcuts([
    {
      key: 'w',
      ctrl: true,
      handler: () => { if (activeKey) closeTab(activeKey); },
      description: '현재 탭 닫기',
    },
    {
      key: 'Tab',
      ctrl: true,
      handler: () => {
        const currentIdx = tabs.findIndex(t => t.key === activeKey);
        const nextIdx = (currentIdx + 1) % tabs.length;
        setActiveTab(tabs[nextIdx].key);
      },
      description: '다음 탭으로 이동',
    },
  ]);

  /**
   * 로그아웃 핸들러
   *
   * 로컬스토리지의 인증 토큰과 탭 세션을 삭제하고
   * 로그인 페이지로 이동합니다.
   */
  const handleLogout = () => {
    logout();
    clearTabSession();
    navigate('/login');
  };

  /**
   * 탭 편집(닫기) 핸들러
   *
   * Ant Design Tabs의 onEdit 콜백으로 사용됩니다.
   * 'remove' 액션일 때 해당 탭을 닫습니다.
   *
   * @param targetKey - 편집 대상 탭 키
   * @param action - 편집 액션 ('add' 또는 'remove')
   */
  const handleTabEdit = (
    targetKey: React.MouseEvent | React.KeyboardEvent | string,
    action: 'add' | 'remove',
  ) => {
    if (action === 'remove' && typeof targetKey === 'string') {
      closeTab(targetKey);
    }
  };

  return (
    <Layout style={{ height: '100vh', overflow: 'hidden' }}>
      {/* 왼쪽 사이드바: 접이식 네비게이션 메뉴 */}
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={240}
        theme={isDark ? 'dark' : 'light'}
        style={{
          borderRight: `1px solid ${colors.border}`,
          background: colors.bgSider,
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {/* 로고 영역: 클릭 시 대시보드 탭으로 이동 */}
        <div
          onClick={() => openTab('/dashboard')}
          style={{
            height: 80,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: `1px solid ${colors.border}`,
            gap: spacing.sm,
            background: colors.bgSider,
            cursor: 'pointer',
            transition: 'opacity 0.2s',
          }}
          onMouseEnter={(e) => (e.currentTarget.style.opacity = '0.8')}
          onMouseLeave={(e) => (e.currentTarget.style.opacity = '1')}
        >
          {/* 로고 아이콘 (그라디언트 배경) */}
          <div
            style={{
              width: 32,
              height: 32,
              borderRadius: 8,
              background: `linear-gradient(135deg, ${colors.primary}, ${isDark ? '#0891B2' : '#06B6D4'})`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 13,
              fontWeight: 700,
              color: '#fff',
              flexShrink: 0,
            }}
          >
            DK
          </div>
          {/* 사이드바가 펼쳐진 상태에서만 텍스트 표시 */}
          {!collapsed && (
            <Typography.Text
              strong
              style={{
                color: colors.textPrimary,
                fontSize: typography.bodyMd.fontSize,
                letterSpacing: '-0.02em',
                whiteSpace: 'nowrap',
              }}
            >
              동국씨엠 스마트 계량
            </Typography.Text>
          )}
        </div>

        {/* 네비게이션 메뉴 (스크롤 가능 영역) */}
        <div ref={siderRef} style={{ flex: 1, overflow: 'auto' }}>
          <Menu
            theme={isDark ? 'dark' : 'light'}
            selectedKeys={[activeKey]}
            openKeys={openKeys}
            onOpenChange={(keys) => setOpenKeys(keys)}
            mode="inline"
            items={filteredMenuItems as MenuProps['items']}
            onClick={({ key }) => openTab(key)}
            style={{
              marginTop: 8,
              border: 'none',
              background: 'transparent',
            }}
          />
        </div>
      </Sider>

      <Layout style={{ background: colors.bgBase, overflow: 'hidden' }}>
        {/* 상단 헤더: 즐겨찾기, 테마 전환, 사용자 메뉴 */}
        <Header
          ref={headerRef}
          style={{
            padding: `0 ${spacing.xl}px`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            gap: spacing.lg,
            borderBottom: `1px solid ${colors.border}`,
            backdropFilter: 'blur(12px)',
            background: isDark ? 'rgba(30, 41, 59, 0.8)' : 'rgba(255, 255, 255, 0.9)',
          }}
        >
          {/* 현재 페이지 즐겨찾기 추가/해제 버튼 */}
          {currentPageInfo && (
            <FavoriteButton
              favoriteType="MENU"
              targetPath={activeKey}
              displayName={currentPageInfo.label as string}
              icon={currentPageInfo.key}
            />
          )}

          {/* 즐겨찾기 목록 팝오버 */}
          <Popover
            content={<FavoritesList onNavigate={() => setFavoritesOpen(false)} />}
            title="즐겨찾기"
            trigger="click"
            open={favoritesOpen}
            onOpenChange={setFavoritesOpen}
            placement="bottomRight"
            overlayStyle={{ width: 300 }}
          >
            <Button
              type="text"
              icon={<StarOutlined style={{ color: '#faad14' }} />}
              style={{ color: colors.textSecondary }}
            >
              즐겨찾기
            </Button>
          </Popover>

          {/* 구분선 */}
          <div style={{ width: 1, height: 20, background: colors.border }} />

          {/* 다크/라이트 테마 전환 스위치 */}
          <Tooltip title={isDark ? '라이트 모드로 전환' : '다크 모드로 전환'}>
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
              <SunOutlined style={{
                color: isDark ? colors.textSecondary : colors.warning,
                fontSize: 16,
              }} />
              <Switch
                checked={isDark}
                onChange={toggleTheme}
                size="small"
                style={{
                  background: isDark ? colors.primary : colors.border,
                }}
              />
              <MoonOutlined style={{
                color: isDark ? colors.primary : colors.textSecondary,
                fontSize: 16,
              }} />
            </div>
          </Tooltip>

          {/* 구분선 */}
          <div style={{
            width: 1,
            height: 20,
            background: colors.border,
          }} />

          {/* 사용자 정보 및 로그아웃 */}
          <Typography.Text style={{ color: colors.textSecondary, fontSize: typography.bodySm.fontSize }}>
            {user?.name || '사용자'}
          </Typography.Text>
          <Button
            type="text"
            icon={<UserOutlined />}
            onClick={() => openTab('/mypage')}
            style={{ color: colors.textSecondary }}
            size="small"
          >
            마이페이지
          </Button>
          <Button
            type="text"
            icon={<LogoutOutlined />}
            onClick={handleLogout}
            style={{ color: colors.textSecondary }}
            size="small"
          >
            로그아웃
          </Button>
        </Header>

        {/* 멀티 탭 바: 드래그 재배치, 컨텍스트 메뉴(우클릭) 지원 */}
        <div ref={contentRef} style={{
          background: isDark ? colors.bgSider : '#fafafa',
          borderBottom: `1px solid ${colors.border}`,
          paddingLeft: 8,
          paddingRight: 8,
        }}>
          <Tabs
            type="editable-card"
            hideAdd
            activeKey={activeKey}
            onChange={(key) => setActiveTab(key)}
            onEdit={handleTabEdit}
            size="small"
            items={tabs.map(tab => {
              /** 탭 우클릭 컨텍스트 메뉴 항목 정의 */
              const contextMenuItems: MenuProps['items'] = [
                ...(tab.closable ? [{
                  key: 'close',
                  label: '탭 닫기',
                }] : []),
                {
                  key: 'closeOthers',
                  label: '다른 탭 모두 닫기',
                },
                {
                  key: 'closeRight',
                  label: '오른쪽 탭 닫기',
                },
                { type: 'divider' as const },
                {
                  key: 'closeAll',
                  label: '닫을 수 있는 탭 모두 닫기',
                },
              ];
              return {
                key: tab.key,
                label: (
                  /* 우클릭 컨텍스트 메뉴가 연결된 드래그 가능한 탭 라벨 */
                  <Dropdown
                    menu={{
                      items: contextMenuItems,
                      onClick: ({ key: action }) => {
                        switch (action) {
                          case 'close': closeTab(tab.key); break;
                          case 'closeOthers': closeOtherTabs(tab.key); break;
                          case 'closeRight': closeRightTabs(tab.key); break;
                          case 'closeAll': closeAllClosable(); break;
                        }
                      },
                    }}
                    trigger={['contextMenu']}
                  >
                    {/* 드래그앤드롭으로 탭 순서 변경 가능 */}
                    <span
                      draggable
                      onDragStart={(e) => {
                        dragKeyRef.current = tab.key;
                        e.dataTransfer.effectAllowed = 'move';
                      }}
                      onDragOver={(e) => {
                        e.preventDefault();
                        e.dataTransfer.dropEffect = 'move';
                      }}
                      onDrop={(e) => {
                        e.preventDefault();
                        if (dragKeyRef.current && dragKeyRef.current !== tab.key) {
                          moveTab(dragKeyRef.current, tab.key);
                        }
                        dragKeyRef.current = null;
                      }}
                      onDragEnd={() => { dragKeyRef.current = null; }}
                      style={{ fontSize: typography.caption.fontSize, display: 'flex', alignItems: 'center', gap: spacing.xs, cursor: 'grab' }}
                    >
                      {tab.icon}
                      {tab.title}
                    </span>
                  </Dropdown>
                ),
                closable: tab.closable,
              };
            })}
            style={{ marginBottom: 0 }}
          />
        </div>

        {/* 콘텐츠 영역: 모든 탭 컴포넌트를 동시 렌더링하고 display로 전환하여 언마운트 방지 */}
        <Content style={{ margin: spacing.lg, flex: 1, overflow: 'hidden' }}>
          <div
            style={{
              padding: spacing.xl,
              height: '100%',
              background: colors.bgSurface,
              borderRadius: 12,
              border: `1px solid ${colors.border}`,
              display: 'flex',
              flexDirection: 'column',
              overflow: 'hidden',
              boxSizing: 'border-box',
            }}
          >
            {/* 활성 탭만 display:block, 나머지는 display:none으로 숨김 (전환 시 fadeIn 애니메이션) */}
            {tabs.map(tab => (
              <div
                key={tab.key}
                style={{
                  display: tab.key === activeKey ? 'block' : 'none',
                  flex: 1,
                  minHeight: 0,
                  overflow: 'auto',
                  animation: tab.key === activeKey ? 'fadeIn 0.2s ease-in' : undefined,
                }}
              >
                {tab.component}
              </div>
            ))}
          </div>
        </Content>
      </Layout>
      <OnboardingTour siderRef={siderRef} headerRef={headerRef} contentRef={contentRef} />
    </Layout>
  );
};

export default MainLayout;

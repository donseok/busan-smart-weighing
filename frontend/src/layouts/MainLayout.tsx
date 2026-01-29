import React, { useState, useEffect, useRef } from 'react';
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
import { darkColors, lightColors } from '../theme/themeConfig';
import { useTheme } from '../context/ThemeContext';
import { useTab, clearTabSession } from '../context/TabContext';
import FavoritesList from '../components/FavoritesList';
import FavoriteButton from '../components/FavoriteButton';

const { Header, Sider, Content } = Layout;

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

const MainLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [favoritesOpen, setFavoritesOpen] = useState(false);
  const navigate = useNavigate();
  const { themeMode, toggleTheme } = useTheme();
  const { tabs, activeKey, openTab, closeTab, closeOtherTabs, closeAllClosable, closeRightTabs, setActiveTab, moveTab } = useTab();
  const dragKeyRef = useRef<string | null>(null);

  // 현재 경로에 따라 열려야 할 서브메뉴 계산
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

  const [openKeys, setOpenKeys] = useState<string[]>(() => {
    const pathKeys = getOpenKeys();
    return pathKeys.length > 0 ? pathKeys : ['master'];
  });

  // 활성 탭 변경 시 해당 서브메뉴 자동 열기
  useEffect(() => {
    const pathKeys = getOpenKeys();
    if (pathKeys.length > 0) {
      setOpenKeys(prev => {
        const merged = new Set([...prev, ...pathKeys]);
        return Array.from(merged);
      });
    }
  }, [activeKey]);

  // 현재 페이지 정보 (즐겨찾기 버튼용)
  const currentPageInfo = menuItems.flatMap(item =>
    'children' in item && item.children ? item.children : [item]
  ).find(item => item?.key === activeKey);

  // 현재 테마에 맞는 색상 선택
  const colors = themeMode === 'dark' ? darkColors : lightColors;
  const isDark = themeMode === 'dark';

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    clearTabSession();
    navigate('/login');
  };

  // 탭 편집 (닫기) 핸들러
  const handleTabEdit = (
    targetKey: React.MouseEvent | React.KeyboardEvent | string,
    action: 'add' | 'remove',
  ) => {
    if (action === 'remove' && typeof targetKey === 'string') {
      closeTab(targetKey);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
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
        {/* 로고 영역 */}
        <div
          onClick={() => openTab('/dashboard')}
          style={{
            height: 80,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: `1px solid ${colors.border}`,
            gap: 8,
            background: colors.bgSider,
            cursor: 'pointer',
            transition: 'opacity 0.2s',
          }}
          onMouseEnter={(e) => (e.currentTarget.style.opacity = '0.8')}
          onMouseLeave={(e) => (e.currentTarget.style.opacity = '1')}
        >
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
          {!collapsed && (
            <Typography.Text
              strong
              style={{
                color: colors.textPrimary,
                fontSize: 15,
                letterSpacing: '-0.02em',
                whiteSpace: 'nowrap',
              }}
            >
              동국씨엠 스마트 계량
            </Typography.Text>
          )}
        </div>

        <div style={{ flex: 1, overflow: 'auto' }}>
          <Menu
            theme={isDark ? 'dark' : 'light'}
            selectedKeys={[activeKey]}
            openKeys={openKeys}
            onOpenChange={(keys) => setOpenKeys(keys)}
            mode="inline"
            items={menuItems}
            onClick={({ key }) => openTab(key)}
            style={{
              marginTop: 8,
              border: 'none',
              background: 'transparent',
            }}
          />
        </div>
      </Sider>

      <Layout style={{ background: colors.bgBase }}>
        <Header
          style={{
            padding: '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            gap: 16,
            borderBottom: `1px solid ${colors.border}`,
            backdropFilter: 'blur(12px)',
            background: isDark ? 'rgba(30, 41, 59, 0.8)' : 'rgba(255, 255, 255, 0.9)',
          }}
        >
          {/* 현재 페이지 즐겨찾기 버튼 */}
          {currentPageInfo && (
            <FavoriteButton
              favoriteType="MENU"
              targetPath={activeKey}
              displayName={currentPageInfo.label as string}
              icon={currentPageInfo.key}
            />
          )}

          {/* 즐겨찾기 목록 */}
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

          <div style={{ width: 1, height: 20, background: colors.border }} />

          {/* 테마 토글 스위치 */}
          <Tooltip title={isDark ? '라이트 모드로 전환' : '다크 모드로 전환'}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
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

          <div style={{
            width: 1,
            height: 20,
            background: colors.border,
          }} />

          <Typography.Text style={{ color: colors.textSecondary, fontSize: 13 }}>
            관리자
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

        {/* 탭 바 */}
        <div style={{
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
                      style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 4, cursor: 'grab' }}
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

        <Content style={{ margin: 16, overflow: 'auto' }}>
          <div
            style={{
              padding: 24,
              minHeight: 360,
              background: colors.bgSurface,
              borderRadius: 12,
              border: `1px solid ${colors.border}`,
            }}
          >
            {/* 모든 탭 컴포넌트를 동시 렌더링 — display로 전환하여 unmount 방지 */}
            {tabs.map(tab => (
              <div
                key={tab.key}
                style={{ display: tab.key === activeKey ? 'block' : 'none' }}
              >
                {tab.component}
              </div>
            ))}
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;

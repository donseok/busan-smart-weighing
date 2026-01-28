import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Typography, Button } from 'antd';
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
} from '@ant-design/icons';
import { colors } from '../theme/themeConfig';

const { Header, Sider, Content } = Layout;

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '대시보드' },
  { key: '/dispatch', icon: <CarOutlined />, label: '배차 관리' },
  { key: '/weighing', icon: <ExperimentOutlined />, label: '계량 현황' },
  {
    key: 'master',
    icon: <DatabaseOutlined />,
    label: '기준정보 관리',
    children: [
      { key: '/master/companies', icon: <BankOutlined />, label: '운송사' },
      { key: '/master/vehicles', icon: <CarOutlined />, label: '차량' },
      { key: '/master/scales', icon: <ToolOutlined />, label: '계량대' },
    ],
  },
  { key: '/gate-pass', icon: <CheckCircleOutlined />, label: '출문 관리' },
  { key: '/slips', icon: <FileTextOutlined />, label: '전자 계량표' },
];

const MainLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={240}
        style={{
          borderRight: `1px solid ${colors.border}`,
        }}
      >
        {/* 로고 영역 */}
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: `1px solid ${colors.border}`,
            gap: 8,
          }}
        >
          <div
            style={{
              width: 32,
              height: 32,
              borderRadius: 8,
              background: `linear-gradient(135deg, ${colors.primary}, #0891B2)`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 16,
              fontWeight: 700,
              color: '#fff',
              flexShrink: 0,
            }}
          >
            B
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
              부산 스마트 계량
            </Typography.Text>
          )}
        </div>

        <Menu
          theme="dark"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={['master']}
          mode="inline"
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ marginTop: 8, border: 'none' }}
        />
      </Sider>

      <Layout>
        <Header
          style={{
            padding: '0 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            gap: 16,
            borderBottom: `1px solid ${colors.border}`,
            backdropFilter: 'blur(12px)',
            background: 'rgba(30, 41, 59, 0.8)',
          }}
        >
          <Typography.Text style={{ color: colors.textSecondary, fontSize: 13 }}>
            관리자
          </Typography.Text>
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
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;

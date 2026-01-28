import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, theme, Typography } from 'antd';
import {
  DashboardOutlined,
  CarOutlined,
  ExperimentOutlined,
  DatabaseOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  BankOutlined,
  ToolOutlined,
} from '@ant-design/icons';

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
  const { token: { colorBgContainer, borderRadiusLG } } = theme.useToken();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div style={{ height: 32, margin: 16, textAlign: 'center' }}>
          <Typography.Text strong style={{ color: '#fff', fontSize: collapsed ? 12 : 16 }}>
            {collapsed ? 'BSW' : '부산 스마트 계량'}
          </Typography.Text>
        </div>
        <Menu
          theme="dark"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={['master']}
          mode="inline"
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: '0 24px', background: colorBgContainer, display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
          <Typography.Text type="secondary">관리자</Typography.Text>
        </Header>
        <Content style={{ margin: 16 }}>
          <div style={{ padding: 24, minHeight: 360, background: colorBgContainer, borderRadius: borderRadiusLG }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;

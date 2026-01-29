import React from 'react';
import {
  DashboardOutlined,
  CarOutlined,
  ExperimentOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  BankOutlined,
  ToolOutlined,
  UserOutlined,
  ControlOutlined,
  FileSearchOutlined,
  NotificationOutlined,
  TagsOutlined,
  BarChartOutlined,
  MonitorOutlined,
  QuestionCircleOutlined,
  DesktopOutlined,
  IdcardOutlined,
} from '@ant-design/icons';

// 페이지 컴포넌트 lazy import
const DashboardPage = React.lazy(() => import('../pages/DashboardPage'));
const DispatchPage = React.lazy(() => import('../pages/DispatchPage'));
const WeighingPage = React.lazy(() => import('../pages/WeighingPage'));
const InquiryPage = React.lazy(() => import('../pages/InquiryPage'));
const GatePassPage = React.lazy(() => import('../pages/GatePassPage'));
const SlipPage = React.lazy(() => import('../pages/SlipPage'));
const StatisticsPage = React.lazy(() => import('../pages/StatisticsPage'));
const WeighingStationPage = React.lazy(() => import('../pages/WeighingStationPage'));
const MonitoringPage = React.lazy(() => import('../pages/MonitoringPage'));
const MasterCodePage = React.lazy(() => import('../pages/master/MasterCodePage'));
const MasterCompanyPage = React.lazy(() => import('../pages/master/MasterCompanyPage'));
const MasterVehiclePage = React.lazy(() => import('../pages/master/MasterVehiclePage'));
const MasterScalePage = React.lazy(() => import('../pages/master/MasterScalePage'));
const NoticePage = React.lazy(() => import('../pages/NoticePage'));
const HelpPage = React.lazy(() => import('../pages/HelpPage'));
const AdminUserPage = React.lazy(() => import('../pages/admin/AdminUserPage'));
const AdminSettingsPage = React.lazy(() => import('../pages/admin/AdminSettingsPage'));
const AdminAuditLogPage = React.lazy(() => import('../pages/admin/AdminAuditLogPage'));
const MyPage = React.lazy(() => import('../pages/MyPage'));

export interface PageConfig {
  component: React.LazyExoticComponent<React.FC>;
  title: string;
  icon: React.ReactNode;
  closable: boolean;
}

export const PAGE_REGISTRY: Record<string, PageConfig> = {
  '/dashboard': {
    component: DashboardPage,
    title: '대시보드',
    icon: React.createElement(DashboardOutlined),
    closable: true,
  },
  '/dispatch': {
    component: DispatchPage,
    title: '배차 관리',
    icon: React.createElement(CarOutlined),
    closable: true,
  },
  '/weighing': {
    component: WeighingPage,
    title: '계량 현황',
    icon: React.createElement(ExperimentOutlined),
    closable: true,
  },
  '/inquiry': {
    component: InquiryPage,
    title: '계량 조회',
    icon: React.createElement(FileSearchOutlined),
    closable: true,
  },
  '/gate-pass': {
    component: GatePassPage,
    title: '출문 관리',
    icon: React.createElement(CheckCircleOutlined),
    closable: true,
  },
  '/slips': {
    component: SlipPage,
    title: '전자 계량표',
    icon: React.createElement(FileTextOutlined),
    closable: true,
  },
  '/statistics': {
    component: StatisticsPage,
    title: '통계/보고서',
    icon: React.createElement(BarChartOutlined),
    closable: true,
  },
  '/weighing-station': {
    component: WeighingStationPage,
    title: '계량소 관제',
    icon: React.createElement(DesktopOutlined),
    closable: false,
  },
  '/monitoring': {
    component: MonitoringPage,
    title: '장비 관제',
    icon: React.createElement(MonitorOutlined),
    closable: true,
  },
  '/master/codes': {
    component: MasterCodePage,
    title: '코드 관리',
    icon: React.createElement(TagsOutlined),
    closable: true,
  },
  '/master/companies': {
    component: MasterCompanyPage,
    title: '운송사',
    icon: React.createElement(BankOutlined),
    closable: true,
  },
  '/master/vehicles': {
    component: MasterVehiclePage,
    title: '차량',
    icon: React.createElement(CarOutlined),
    closable: true,
  },
  '/master/scales': {
    component: MasterScalePage,
    title: '계량대',
    icon: React.createElement(ToolOutlined),
    closable: true,
  },
  '/notices': {
    component: NoticePage,
    title: '공지사항',
    icon: React.createElement(NotificationOutlined),
    closable: true,
  },
  '/help': {
    component: HelpPage,
    title: '이용 안내',
    icon: React.createElement(QuestionCircleOutlined),
    closable: true,
  },
  '/admin/users': {
    component: AdminUserPage,
    title: '사용자 관리',
    icon: React.createElement(UserOutlined),
    closable: true,
  },
  '/admin/settings': {
    component: AdminSettingsPage,
    title: '시스템 설정',
    icon: React.createElement(ControlOutlined),
    closable: true,
  },
  '/admin/audit-logs': {
    component: AdminAuditLogPage,
    title: '감사 로그',
    icon: React.createElement(FileSearchOutlined),
    closable: true,
  },
  '/mypage': {
    component: MyPage,
    title: '마이페이지',
    icon: React.createElement(IdcardOutlined),
    closable: true,
  },
};

// 앱 시작 시 자동 고정되는 탭
export const PINNED_TABS = ['/weighing-station'];

// 최대 탭 수
export const MAX_TABS = 10;

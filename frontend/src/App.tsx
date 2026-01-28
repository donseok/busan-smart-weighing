import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import DispatchPage from './pages/DispatchPage';
import WeighingPage from './pages/WeighingPage';
import MasterCodePage from './pages/master/MasterCodePage';
import MasterCompanyPage from './pages/master/MasterCompanyPage';
import MasterVehiclePage from './pages/master/MasterVehiclePage';
import MasterScalePage from './pages/master/MasterScalePage';
import GatePassPage from './pages/GatePassPage';
import SlipPage from './pages/SlipPage';
import NoticePage from './pages/NoticePage';
import AdminUserPage from './pages/admin/AdminUserPage';
import AdminSettingsPage from './pages/admin/AdminSettingsPage';
import AdminAuditLogPage from './pages/admin/AdminAuditLogPage';
import StatisticsPage from './pages/StatisticsPage';
import MonitoringPage from './pages/MonitoringPage';
import MyPage from './pages/MyPage';
import HelpPage from './pages/HelpPage';

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<MainLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="dispatch" element={<DispatchPage />} />
        <Route path="weighing" element={<WeighingPage />} />
        <Route path="master/codes" element={<MasterCodePage />} />
        <Route path="master/companies" element={<MasterCompanyPage />} />
        <Route path="master/vehicles" element={<MasterVehiclePage />} />
        <Route path="master/scales" element={<MasterScalePage />} />
        <Route path="gate-pass" element={<GatePassPage />} />
        <Route path="slips" element={<SlipPage />} />
        <Route path="notices" element={<NoticePage />} />
        <Route path="admin/users" element={<AdminUserPage />} />
        <Route path="admin/settings" element={<AdminSettingsPage />} />
        <Route path="admin/audit-logs" element={<AdminAuditLogPage />} />
        <Route path="statistics" element={<StatisticsPage />} />
        <Route path="monitoring" element={<MonitoringPage />} />
        <Route path="mypage" element={<MyPage />} />
        <Route path="help" element={<HelpPage />} />
      </Route>
    </Routes>
  );
};

export default App;

import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import DispatchPage from './pages/DispatchPage';
import WeighingPage from './pages/WeighingPage';
import MasterCompanyPage from './pages/master/MasterCompanyPage';
import MasterVehiclePage from './pages/master/MasterVehiclePage';
import MasterScalePage from './pages/master/MasterScalePage';
import GatePassPage from './pages/GatePassPage';
import SlipPage from './pages/SlipPage';

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<MainLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="dispatch" element={<DispatchPage />} />
        <Route path="weighing" element={<WeighingPage />} />
        <Route path="master/companies" element={<MasterCompanyPage />} />
        <Route path="master/vehicles" element={<MasterVehiclePage />} />
        <Route path="master/scales" element={<MasterScalePage />} />
        <Route path="gate-pass" element={<GatePassPage />} />
        <Route path="slips" element={<SlipPage />} />
      </Route>
    </Routes>
  );
};

export default App;

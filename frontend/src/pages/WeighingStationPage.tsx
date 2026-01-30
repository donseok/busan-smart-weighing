/**
 * 계량소 관제 페이지 컴포넌트
 *
 * 계량소의 실시간 관제 화면으로, 중량 표시, 차량 정보,
 * 장치 연결 상태, 계량 이력 등의 표시 패널과
 * 모드 전환, 수동 계량, 차단기 제어, 시뮬레이터 등의
 * 제어 패널로 구성됩니다.
 * WebSocket을 통해 실시간 계량 데이터를 수신하며,
 * useWeighingStation 훅으로 상태 관리를 수행합니다.
 *
 * @returns 계량소 관제 페이지 JSX
 */
import React, { useEffect } from 'react';
import { Row, Col, Typography, theme } from 'antd';
import { useWeighingStation } from '../hooks/useWeighingStation';
import WeightDisplay from '../components/weighing-station/WeightDisplay';
import VehicleInfoPanel from '../components/weighing-station/VehicleInfoPanel';
import ConnectionStatusBar from '../components/weighing-station/ConnectionStatusBar';
import WeighingHistoryTable from '../components/weighing-station/WeighingHistoryTable';
import ModeToggle from '../components/weighing-station/ModeToggle';
import ManualControls from '../components/weighing-station/ManualControls';
import ActionButtons from '../components/weighing-station/ActionButtons';
import ProcessStateBar from '../components/weighing-station/ProcessStateBar';
import StatusLog from '../components/weighing-station/StatusLog';
import SimulatorPanel from '../components/weighing-station/SimulatorPanel';

const WeighingStationPage: React.FC = () => {
  const { token } = theme.useToken();

  const {
    mode,
    processState,
    weight,
    vehicle,
    devices,
    logs,
    history,
    searchResults,
    selectedDispatchId,
    simulatorEnabled,
    searchLoading,
    weighingDisplay,
    changeMode,
    handleSearch,
    handleConfirmWeight,
    handleReset,
    handleBarrierOpen,
    setSelectedDispatchId,
    setSimulatorEnabled,
    loadHistory,
    handleSimTriggerSensor,
    handleSimCaptureLpr,
    handleSimTogglePosition,
    handleSimSetWeight,
  } = useWeighingStation();

  // 초기 이력 로드
  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  return (
    <div style={{ margin: -24 }}>
      {/* 헤더 */}
      <div
        style={{
          padding: '16px 24px',
          borderBottom: `1px solid ${token.colorBorder}`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: `linear-gradient(135deg, ${token.colorPrimary}08, ${token.colorPrimary}15, transparent)`,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* 악센트 라인 */}
        <div style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: 2,
          background: `linear-gradient(90deg, ${token.colorPrimary}, ${token.colorSuccess}, transparent)`,
        }} />
        <Typography.Title level={4} style={{ margin: 0 }}>
          계량소 관제
        </Typography.Title>
        <Typography.Text style={{ color: token.colorTextSecondary, fontSize: 13 }}>
          Weighing Station Control
        </Typography.Text>
      </div>

      {/* 메인 그리드 */}
      <div style={{ padding: 16 }}>
        <Row gutter={16}>
          {/* ─── 좌측 패널 (표시 영역) ─── */}
          <Col xs={24} lg={10}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {/* 중량 표시 */}
              <WeightDisplay weight={weight} weighingInfo={weighingDisplay} />

              {/* 차량/배차 정보 */}
              <VehicleInfoPanel vehicle={vehicle} />

              {/* 장치 연결 상태 */}
              <ConnectionStatusBar devices={devices} />

              {/* 최근 계량 기록 */}
              <WeighingHistoryTable history={history} />
            </div>
          </Col>

          {/* ─── 우측 패널 (제어 영역) ─── */}
          <Col xs={24} lg={14}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {/* 계량 모드 토글 */}
              <ModeToggle mode={mode} onChange={changeMode} />

              {/* 수동 계량 제어 */}
              <ManualControls
                mode={mode}
                searchResults={searchResults}
                selectedDispatchId={selectedDispatchId}
                searchLoading={searchLoading}
                onSearch={handleSearch}
                onSelectDispatch={setSelectedDispatchId}
                onConfirmWeight={handleConfirmWeight}
              />

              {/* 작업 제어 버튼 */}
              <ActionButtons
                onReset={handleReset}
                onBarrierOpen={handleBarrierOpen}
              />

              {/* 프로세스 상태 바 */}
              <ProcessStateBar processState={processState} />

              {/* 상태 로그 */}
              <StatusLog logs={logs} />

              {/* 시뮬레이터 패널 */}
              <SimulatorPanel
                enabled={simulatorEnabled}
                onToggle={setSimulatorEnabled}
                onTriggerSensor={handleSimTriggerSensor}
                onCaptureLpr={handleSimCaptureLpr}
                onTogglePosition={handleSimTogglePosition}
                onSetWeight={handleSimSetWeight}
              />
            </div>
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default WeighingStationPage;

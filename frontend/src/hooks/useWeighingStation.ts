import { useState, useCallback, useRef } from 'react';
import { message } from 'antd';
import { useWeighingStationSocket } from './useWeighingStationSocket';
import {
  searchDispatches,
  createWeighing,
  fetchWeighingHistory,
  openBarrier,
  resetProcess,
  triggerSensor,
  captureLpr,
  togglePosition,
  setSimulatorWeight,
} from '../api/weighingStationApi';
import type {
  WeighingMode,
  ProcessState,
  WeightData,
  VehicleInfo,
  DeviceConnectionState,
  StatusLogEntry,
  WeighingHistoryRecord,
  DispatchSearchResult,
  ScaleStatusMessage,
  WeighingUpdateMessage,
  DeviceStatusMessage,
} from '../types/weighingStation';
import {
  EMPTY_VEHICLE,
  INITIAL_WEIGHT,
  INITIAL_DEVICES,
} from '../types/weighingStation';

let logIdCounter = 0;

export function useWeighingStation() {
  // ─── 상태 ───
  const [mode, setMode] = useState<WeighingMode>('AUTO');
  const [processState, setProcessState] = useState<ProcessState>('IDLE');
  const [weight, setWeight] = useState<WeightData>(INITIAL_WEIGHT);
  const [vehicle, setVehicle] = useState<VehicleInfo>(EMPTY_VEHICLE);
  const [devices, setDevices] = useState<DeviceConnectionState>(INITIAL_DEVICES);
  const [logs, setLogs] = useState<StatusLogEntry[]>([]);
  const [history, setHistory] = useState<WeighingHistoryRecord[]>([]);
  const [searchResults, setSearchResults] = useState<DispatchSearchResult[]>([]);
  const [selectedDispatchId, setSelectedDispatchId] = useState<number | null>(null);
  const [simulatorEnabled, setSimulatorEnabled] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);

  const logsRef = useRef(logs);
  logsRef.current = logs;

  // ─── 로그 추가 ───
  const addLog = useCallback((msg: string, level: StatusLogEntry['level'] = 'info') => {
    const entry: StatusLogEntry = {
      id: String(++logIdCounter),
      timestamp: new Date().toLocaleTimeString('ko-KR', { hour12: false }),
      message: msg,
      level,
    };
    setLogs((prev) => {
      const next = [entry, ...prev];
      return next.length > 200 ? next.slice(0, 200) : next;
    });
  }, []);

  // ─── WebSocket 핸들러 ───
  const handleScaleStatus = useCallback((msg: ScaleStatusMessage) => {
    setWeight({
      currentWeight: msg.currentWeight,
      stability: msg.stabilityStatus,
      unit: msg.unit || 'kg',
    });
    // 네트워크 장치 온라인 표시
    setDevices((prev) => ({ ...prev, scale: 'ONLINE' }));
  }, []);

  const handleWeighingUpdate = useCallback((msg: WeighingUpdateMessage) => {
    setProcessState(msg.processState);

    if (msg.plateNumber || msg.companyName) {
      setVehicle({
        plateNumber: msg.plateNumber || '-',
        companyName: msg.companyName || '-',
        itemName: msg.itemName || '-',
        dispatchId: msg.dispatchId ?? null,
        driverName: msg.driverName || '-',
      });
    }

    if (msg.message) {
      const level = msg.processState === 'ERROR' ? 'error'
        : msg.processState === 'COMPLETE' ? 'success'
        : 'info';
      addLog(msg.message, level);
    }

    // 완료 시 기록 새로고침
    if (msg.processState === 'COMPLETE') {
      loadHistory();
    }
  }, [addLog]);

  const handleDeviceStatus = useCallback((msg: DeviceStatusMessage) => {
    const key = msg.deviceType === 'SCALE' ? 'scale'
      : msg.deviceType === 'DISPLAY' ? 'display'
      : msg.deviceType === 'BARRIER' ? 'barrier'
      : 'network';
    setDevices((prev) => ({ ...prev, [key]: msg.status }));

    if (msg.status === 'ERROR' && msg.message) {
      addLog(`[${msg.deviceName}] ${msg.message}`, 'error');
    }
  }, [addLog]);

  // WebSocket 연결
  useWeighingStationSocket({
    onScaleStatus: handleScaleStatus,
    onWeighingUpdate: handleWeighingUpdate,
    onDeviceStatus: handleDeviceStatus,
  });

  // ─── 이력 로드 ───
  const loadHistory = useCallback(async () => {
    try {
      const records = await fetchWeighingHistory(50);
      setHistory(records);
    } catch {
      // 조용히 실패
    }
  }, []);

  // ─── 모드 변경 ───
  const changeMode = useCallback((newMode: WeighingMode) => {
    setMode(newMode);
    setSearchResults([]);
    setSelectedDispatchId(null);
    addLog(`모드 변경: ${newMode === 'AUTO' ? '자동(LPR)' : '수동'}`, 'info');
  }, [addLog]);

  // ─── 배차 검색 ───
  const handleSearch = useCallback(async (plateNumber: string) => {
    if (!plateNumber.trim()) {
      message.warning('차량번호를 입력하세요');
      return;
    }
    setSearchLoading(true);
    try {
      const results = await searchDispatches(plateNumber.trim());
      setSearchResults(results);
      if (results.length === 0) {
        message.info('검색 결과가 없습니다');
        addLog(`배차 검색 결과 없음: ${plateNumber}`, 'warning');
      } else {
        addLog(`배차 ${results.length}건 검색됨: ${plateNumber}`, 'info');
      }
    } catch {
      message.error('배차 검색 실패');
      addLog(`배차 검색 실패: ${plateNumber}`, 'error');
    } finally {
      setSearchLoading(false);
    }
  }, [addLog]);

  // ─── 수동 계량 확인 ───
  const handleConfirmWeight = useCallback(async () => {
    if (!selectedDispatchId) {
      message.warning('배차를 선택하세요');
      return;
    }
    const dispatch = searchResults.find((d) => d.dispatchId === selectedDispatchId);
    if (!dispatch) return;

    try {
      await createWeighing({
        dispatchId: dispatch.dispatchId,
        weighingMode: 'MANUAL',
        plateNumber: dispatch.plateNumber,
        grossWeight: weight.currentWeight,
      });
      addLog(`수동 계량 시작: ${dispatch.plateNumber} (배차#${dispatch.dispatchId})`, 'success');
      message.success('계량이 시작되었습니다');
    } catch {
      message.error('계량 시작 실패');
      addLog('수동 계량 시작 실패', 'error');
    }
  }, [selectedDispatchId, searchResults, weight.currentWeight, addLog]);

  // ─── 초기화 ───
  const handleReset = useCallback(async () => {
    try {
      await resetProcess();
      setProcessState('IDLE');
      setVehicle(EMPTY_VEHICLE);
      setSearchResults([]);
      setSelectedDispatchId(null);
      addLog('프로세스 초기화', 'info');
    } catch {
      message.error('초기화 실패');
    }
  }, [addLog]);

  // ─── 차단기 열기 ───
  const handleBarrierOpen = useCallback(async () => {
    try {
      await openBarrier();
      addLog('차단기 수동 열기', 'success');
    } catch {
      message.error('차단기 열기 실패');
      addLog('차단기 열기 실패', 'error');
    }
  }, [addLog]);

  // ─── 시뮬레이터 ───
  const handleSimTriggerSensor = useCallback(async () => {
    try {
      await triggerSensor();
      addLog('[시뮬] 차량 감지 트리거', 'info');
    } catch {
      addLog('[시뮬] 센서 트리거 실패', 'error');
    }
  }, [addLog]);

  const handleSimCaptureLpr = useCallback(async () => {
    try {
      await captureLpr();
      addLog('[시뮬] LPR 촬영 요청', 'info');
    } catch {
      addLog('[시뮬] LPR 촬영 실패', 'error');
    }
  }, [addLog]);

  const handleSimTogglePosition = useCallback(async () => {
    try {
      await togglePosition();
      addLog('[시뮬] 정위치 토글', 'info');
    } catch {
      addLog('[시뮬] 정위치 토글 실패', 'error');
    }
  }, [addLog]);

  const handleSimSetWeight = useCallback(async (w: number) => {
    try {
      await setSimulatorWeight(w);
      addLog(`[시뮬] 중량 설정: ${w.toLocaleString()} kg`, 'info');
    } catch {
      addLog('[시뮬] 중량 설정 실패', 'error');
    }
  }, [addLog]);

  return {
    // 상태
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

    // 액션
    changeMode,
    handleSearch,
    handleConfirmWeight,
    handleReset,
    handleBarrierOpen,
    setSelectedDispatchId,
    setSimulatorEnabled,
    loadHistory,
    addLog,

    // 시뮬레이터
    handleSimTriggerSensor,
    handleSimCaptureLpr,
    handleSimTogglePosition,
    handleSimSetWeight,
  };
}

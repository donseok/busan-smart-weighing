/**
 * 계량소 관제 상태 관리 훅
 *
 * 계량소의 전체 상태(계량 데이터, 장치 상태, 프로세스 단계)를
 * 통합 관리하는 커스텀 React 훅입니다.
 * WebSocket을 통한 실시간 데이터 수신, 배차 검색, 수동 계량,
 * 차단기 제어, 시뮬레이터 조작 등의 기능을 제공합니다.
 *
 * @returns 계량소 상태 및 제어 함수 객체
 */

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
  WeighingDisplayInfo,
} from '../types/weighingStation';
import {
  EMPTY_VEHICLE,
  INITIAL_WEIGHT,
  INITIAL_DEVICES,
  INITIAL_WEIGHING_DISPLAY,
} from '../types/weighingStation';

/** 로그 항목 ID 자동 증가 카운터 */
let logIdCounter = 0;

export function useWeighingStation() {
  // ─── 상태 정의 ───
  const [mode, setMode] = useState<WeighingMode>('AUTO');               // 계량 모드 (자동/수동)
  const [processState, setProcessState] = useState<ProcessState>('IDLE'); // 프로세스 상태
  const [weight, setWeight] = useState<WeightData>(INITIAL_WEIGHT);     // 실시간 중량
  const [vehicle, setVehicle] = useState<VehicleInfo>(EMPTY_VEHICLE);   // 현재 차량 정보
  const [devices, setDevices] = useState<DeviceConnectionState>(INITIAL_DEVICES); // 장치 연결 상태
  const [logs, setLogs] = useState<StatusLogEntry[]>([]);               // 상태 로그 목록
  const [history, setHistory] = useState<WeighingHistoryRecord[]>([]);  // 최근 계량 이력
  const [searchResults, setSearchResults] = useState<DispatchSearchResult[]>([]); // 배차 검색 결과
  const [selectedDispatchId, setSelectedDispatchId] = useState<number | null>(null); // 선택된 배차 ID
  const [simulatorEnabled, setSimulatorEnabled] = useState(false);      // 시뮬레이터 활성화 여부
  const [searchLoading, setSearchLoading] = useState(false);            // 검색 로딩 상태
  const [weighingDisplay, setWeighingDisplay] = useState<WeighingDisplayInfo>(INITIAL_WEIGHING_DISPLAY); // 다중 중량 표시

  // 로그 상태의 최신값을 참조하기 위한 ref (콜백 내에서 클로저 문제 방지)
  const logsRef = useRef(logs);
  logsRef.current = logs;

  // ─── 로그 추가 ───
  /**
   * 상태 로그에 새 항목을 추가하는 함수
   * 최대 200개까지 유지하며, 초과 시 오래된 로그를 제거합니다.
   *
   * @param msg - 로그 메시지
   * @param level - 로그 레벨 (기본값: 'info')
   */
  const addLog = useCallback((msg: string, level: StatusLogEntry['level'] = 'info') => {
    const entry: StatusLogEntry = {
      id: String(++logIdCounter),
      timestamp: new Date().toLocaleTimeString('ko-KR', { hour12: false }),
      message: msg,
      level,
    };
    setLogs((prev) => {
      const next = [entry, ...prev];
      // 로그가 200개를 초과하면 오래된 항목 제거
      return next.length > 200 ? next.slice(0, 200) : next;
    });
  }, []);

  // ─── WebSocket 핸들러 ───

  /**
   * 계량대 상태 메시지 수신 핸들러
   * 실시간 중량 데이터를 업데이트하고 계량대 연결 상태를 ONLINE으로 설정합니다.
   */
  const handleScaleStatus = useCallback((msg: ScaleStatusMessage) => {
    setWeight({
      currentWeight: msg.currentWeight,
      stability: msg.stabilityStatus,
      unit: msg.unit || 'kg',
    });
    // 중량 데이터 수신 시 계량대 장치를 온라인으로 표시
    setDevices((prev) => ({ ...prev, scale: 'ONLINE' }));

    // 계량 중일 때 현재 중량을 2차중량으로 반영하고 실중량 자동 계산
    setWeighingDisplay((prev) => {
      if (prev.firstWeight > 0 || prev.secondWeight > 0) {
        const second = msg.currentWeight;
        const net = prev.firstWeight > 0 ? Math.abs(second - prev.firstWeight) : 0;
        return { ...prev, secondWeight: second, netWeight: net };
      }
      return prev;
    });
  }, []);

  /**
   * 계량 업데이트 메시지 수신 핸들러
   * 프로세스 상태, 차량 정보를 업데이트하고 상태 로그를 기록합니다.
   * 계량 완료 시 이력을 자동 새로고침합니다.
   */
  const handleWeighingUpdate = useCallback((msg: WeighingUpdateMessage) => {
    setProcessState(msg.processState);

    // 차량 정보가 있으면 차량 패널 업데이트
    if (msg.plateNumber || msg.companyName) {
      setVehicle({
        plateNumber: msg.plateNumber || '-',
        companyName: msg.companyName || '-',
        itemName: msg.itemName || '-',
        dispatchId: msg.dispatchId ?? null,
        driverName: msg.driverName || '-',
      });
    }

    // 다중 중량 표시 업데이트
    setWeighingDisplay((prev) => ({
      ...prev,
      firstWeight: msg.tareWeight ?? prev.firstWeight,
      secondWeight: msg.grossWeight ?? prev.secondWeight,
      netWeight: msg.netWeight ?? prev.netWeight,
      notification: msg.message ?? prev.notification,
    }));

    // 메시지가 있으면 상태 로그에 기록
    if (msg.message) {
      const level = msg.processState === 'ERROR' ? 'error'
        : msg.processState === 'COMPLETE' ? 'success'
        : 'info';
      addLog(msg.message, level);
    }

    // 계량 완료 시 이력 테이블 새로고침
    if (msg.processState === 'COMPLETE') {
      loadHistory();
    }
  }, [addLog]);

  /**
   * 장치 상태 메시지 수신 핸들러
   * 장치 유형에 따라 해당 장치의 연결 상태를 업데이트합니다.
   * 오류 발생 시 에러 로그를 기록합니다.
   */
  const handleDeviceStatus = useCallback((msg: DeviceStatusMessage) => {
    // 장치 유형을 상태 키로 매핑
    const key = msg.deviceType === 'SCALE' ? 'scale'
      : msg.deviceType === 'DISPLAY' ? 'display'
      : msg.deviceType === 'BARRIER' ? 'barrier'
      : 'network';
    setDevices((prev) => ({ ...prev, [key]: msg.status }));

    // 오류 상태인 경우 에러 로그 기록
    if (msg.status === 'ERROR' && msg.message) {
      addLog(`[${msg.deviceName}] ${msg.message}`, 'error');
    }
  }, [addLog]);

  // WebSocket 연결 및 토픽 구독
  useWeighingStationSocket({
    onScaleStatus: handleScaleStatus,
    onWeighingUpdate: handleWeighingUpdate,
    onDeviceStatus: handleDeviceStatus,
  });

  // ─── 이력 로드 ───
  /** 최근 계량 이력을 서버에서 조회하여 상태를 업데이트합니다 */
  const loadHistory = useCallback(async () => {
    try {
      const records = await fetchWeighingHistory(50);
      setHistory(records);
    } catch {
      // 이력 로드 실패 시 조용히 무시 (UI에 영향 없음)
    }
  }, []);

  // ─── 모드 변경 ───
  /**
   * 계량 모드(자동/수동) 전환
   * 모드 변경 시 검색 결과와 선택된 배차를 초기화합니다.
   */
  const changeMode = useCallback((newMode: WeighingMode) => {
    setMode(newMode);
    setSearchResults([]);
    setSelectedDispatchId(null);
    addLog(`모드 변경: ${newMode === 'AUTO' ? '자동(LPR)' : '수동'}`, 'info');
  }, [addLog]);

  // ─── 배차 검색 ───
  /**
   * 차량번호로 배차를 검색하는 함수
   * 수동 계량 모드에서 계량 대상 배차를 찾기 위해 사용합니다.
   *
   * @param plateNumber - 검색할 차량번호
   */
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
  /**
   * 수동 모드에서 선택된 배차에 대해 계량을 시작하는 함수
   * 배차를 선택하지 않으면 경고 메시지를 표시합니다.
   */
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

      // 배차 정보의 이론중량을 다중 중량 표시에 반영
      // Note: DispatchSearchResult does not have expectedWeight/tareWeight fields,
      // so these will be populated via WebSocket WeighingUpdateMessage once the backend processes it.
      setWeighingDisplay((prev) => ({
        ...prev,
        notification: `수동 계량: ${dispatch.plateNumber}`,
      }));
    } catch {
      message.error('계량 시작 실패');
      addLog('수동 계량 시작 실패', 'error');
    }
  }, [selectedDispatchId, searchResults, weight.currentWeight, addLog]);

  // ─── 초기화 ───
  /** 계량 프로세스를 초기 상태(IDLE)로 리셋합니다 */
  const handleReset = useCallback(async () => {
    try {
      await resetProcess();
      setProcessState('IDLE');
      setVehicle(EMPTY_VEHICLE);
      setSearchResults([]);
      setSelectedDispatchId(null);
      setWeighingDisplay(INITIAL_WEIGHING_DISPLAY);
      addLog('프로세스 초기화', 'info');
    } catch {
      message.error('초기화 실패');
    }
  }, [addLog]);

  // ─── 차단기 열기 ───
  /** 차단기를 수동으로 개방하는 함수 */
  const handleBarrierOpen = useCallback(async () => {
    try {
      await openBarrier();
      addLog('차단기 수동 열기', 'success');
    } catch {
      message.error('차단기 열기 실패');
      addLog('차단기 열기 실패', 'error');
    }
  }, [addLog]);

  // ─── 시뮬레이터 제어 함수들 ───

  /** [시뮬레이터] 차량 감지 센서 트리거 */
  const handleSimTriggerSensor = useCallback(async () => {
    try {
      await triggerSensor();
      addLog('[시뮬] 차량 감지 트리거', 'info');
    } catch {
      addLog('[시뮬] 센서 트리거 실패', 'error');
    }
  }, [addLog]);

  /** [시뮬레이터] LPR 카메라 촬영 요청 */
  const handleSimCaptureLpr = useCallback(async () => {
    try {
      await captureLpr();
      addLog('[시뮬] LPR 촬영 요청', 'info');
    } catch {
      addLog('[시뮬] LPR 촬영 실패', 'error');
    }
  }, [addLog]);

  /** [시뮬레이터] 차량 정위치 토글 */
  const handleSimTogglePosition = useCallback(async () => {
    try {
      await togglePosition();
      addLog('[시뮬] 정위치 토글', 'info');
    } catch {
      addLog('[시뮬] 정위치 토글 실패', 'error');
    }
  }, [addLog]);

  /**
   * [시뮬레이터] 중량 설정
   * @param w - 설정할 중량 값 (kg)
   */
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
    weighingDisplay,

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

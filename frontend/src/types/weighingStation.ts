// ─── 계량소 관제 화면 전용 타입 정의 ───

// 프로세스 상태
export type ProcessState = 'IDLE' | 'WEIGHING' | 'STABILIZING' | 'COMPLETE' | 'ERROR';

// 계량 모드
export type WeighingMode = 'AUTO' | 'MANUAL';

// 중량 안정성 상태
export type StabilityStatus = 'UNSTABLE' | 'STABLE' | 'ERROR' | 'DISCONNECTED';

// 장치 연결 상태
export type ConnectionStatus = 'ONLINE' | 'OFFLINE' | 'ERROR';

// ─── WebSocket 메시지 타입 ───

// /topic/scale-status
export interface ScaleStatusMessage {
  scaleId: number;
  currentWeight: number;
  unit: string;
  isStable: boolean;
  stabilityStatus: StabilityStatus;
  timestamp: string;
}

// /topic/weighing-updates
export interface WeighingUpdateMessage {
  weighingId: number;
  dispatchId?: number;
  processState: ProcessState;
  weighingMode: WeighingMode;
  plateNumber?: string;
  grossWeight?: number;
  tareWeight?: number;
  netWeight?: number;
  companyName?: string;
  itemName?: string;
  driverName?: string;
  message?: string;
  timestamp: string;
}

// /topic/device-status
export interface DeviceStatusMessage {
  deviceType: 'SCALE' | 'DISPLAY' | 'BARRIER' | 'NETWORK';
  deviceName: string;
  status: ConnectionStatus;
  message?: string;
  timestamp: string;
}

// ─── 상태 관리 타입 ───

export interface DeviceConnectionState {
  scale: ConnectionStatus;
  display: ConnectionStatus;
  barrier: ConnectionStatus;
  network: ConnectionStatus;
}

export interface VehicleInfo {
  plateNumber: string;
  companyName: string;
  itemName: string;
  dispatchId: number | null;
  driverName: string;
}

export interface WeightData {
  currentWeight: number;
  stability: StabilityStatus;
  unit: string;
}

export interface StatusLogEntry {
  id: string;
  timestamp: string;
  message: string;
  level: 'info' | 'success' | 'warning' | 'error';
}

// ─── API 응답 타입 ───

export interface DispatchSearchResult {
  dispatchId: number;
  plateNumber: string;
  companyId: number;
  companyName: string;
  itemType: string;
  itemName: string;
  driverName: string;
  dispatchDate: string;
  dispatchStatus: string;
}

export interface WeighingHistoryRecord {
  weighingId: number;
  plateNumber: string;
  grossWeight: number;
  tareWeight?: number;
  netWeight?: number;
  weighingMode: string;
  weighingStatus: string;
  companyName?: string;
  itemName?: string;
  createdAt: string;
}

export interface CreateWeighingRequest {
  dispatchId: number;
  scaleId?: number;
  weighingMode: WeighingMode;
  plateNumber?: string;
  plateConfidence?: number;
  grossWeight?: number;
}

export interface SimulatorCommand {
  command: 'TRIGGER_SENSOR' | 'CAPTURE_LPR' | 'TOGGLE_POSITION' | 'SET_WEIGHT';
  params?: Record<string, unknown>;
}

// ─── 계량소 전체 상태 ───

export interface WeighingStationState {
  mode: WeighingMode;
  processState: ProcessState;
  weight: WeightData;
  vehicle: VehicleInfo;
  devices: DeviceConnectionState;
  logs: StatusLogEntry[];
  history: WeighingHistoryRecord[];
  searchResults: DispatchSearchResult[];
  selectedDispatchId: number | null;
  simulatorEnabled: boolean;
}

// ─── 초기 상태 ───

export const EMPTY_VEHICLE: VehicleInfo = {
  plateNumber: '-',
  companyName: '-',
  itemName: '-',
  dispatchId: null,
  driverName: '-',
};

export const INITIAL_WEIGHT: WeightData = {
  currentWeight: 0,
  stability: 'DISCONNECTED',
  unit: 'kg',
};

export const INITIAL_DEVICES: DeviceConnectionState = {
  scale: 'OFFLINE',
  display: 'OFFLINE',
  barrier: 'OFFLINE',
  network: 'OFFLINE',
};

export const PROCESS_STATE_LABELS: Record<ProcessState, string> = {
  IDLE: '대기',
  WEIGHING: '계량 중',
  STABILIZING: '안정화 중',
  COMPLETE: '완료',
  ERROR: '오류',
};

export const STABILITY_LABELS: Record<StabilityStatus, string> = {
  UNSTABLE: '불안정',
  STABLE: '안정',
  ERROR: '오류',
  DISCONNECTED: '미연결',
};

export const WEIGHING_MODE_LABELS: Record<string, string> = {
  LPR_AUTO: 'LPR자동',
  AUTO: '자동',
  MOBILE_OTP: '모바일OTP',
  MANUAL: '수동',
  RE_WEIGH: '재계량',
};

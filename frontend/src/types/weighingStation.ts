/**
 * 계량소 관제 화면 전용 타입 정의
 *
 * 계량소 관제(WeighingStation) 페이지에서 사용되는
 * 프로세스 상태, WebSocket 메시지, 장치 상태 등의 타입을 정의합니다.
 *
 * @module types/weighingStation
 */

// ─── 계량소 관제 화면 전용 타입 정의 ───

/** 계량 프로세스 상태 - 현재 계량 작업의 진행 단계 */
export type ProcessState = 'IDLE' | 'WEIGHING' | 'STABILIZING' | 'COMPLETE' | 'ERROR';

/** 계량 모드 - 자동(LPR 기반) 또는 수동 계량 */
export type WeighingMode = 'AUTO' | 'MANUAL';

/** 중량 안정성 상태 - 계량대 위의 중량값 안정 여부 */
export type StabilityStatus = 'UNSTABLE' | 'STABLE' | 'ERROR' | 'DISCONNECTED';

/** 장치 연결 상태 - 계량소 장비의 네트워크 연결 상태 */
export type ConnectionStatus = 'ONLINE' | 'OFFLINE' | 'ERROR';

// ─── WebSocket 메시지 타입 ───

/**
 * 계량대 상태 WebSocket 메시지 (/topic/scale-status)
 *
 * 계량대에서 실시간으로 전송되는 중량 데이터입니다.
 * 약 500ms 간격으로 현재 중량과 안정성 상태를 수신합니다.
 *
 * @property scaleId - 계량대 ID
 * @property currentWeight - 현재 측정 중량 (kg)
 * @property unit - 중량 단위 (기본 'kg')
 * @property isStable - 중량 안정 여부
 * @property stabilityStatus - 안정성 상태 상세
 * @property timestamp - 측정 시각
 */
export interface ScaleStatusMessage {
  scaleId: number;
  currentWeight: number;
  unit: string;
  isStable: boolean;
  stabilityStatus: StabilityStatus;
  timestamp: string;
}

/**
 * 계량 업데이트 WebSocket 메시지 (/topic/weighing-updates)
 *
 * 계량 프로세스의 상태 변경(차량 감지, 계량 시작, 완료 등) 시
 * 서버에서 푸시되는 메시지입니다.
 *
 * @property weighingId - 계량 기록 ID
 * @property dispatchId - 연관 배차 ID (선택)
 * @property processState - 현재 프로세스 상태
 * @property weighingMode - 계량 모드
 * @property plateNumber - 차량번호 (선택)
 * @property grossWeight - 총중량 (선택)
 * @property tareWeight - 공차중량 (선택)
 * @property netWeight - 순중량 (선택)
 * @property companyName - 운송사명 (선택)
 * @property itemName - 품목명 (선택)
 * @property driverName - 기사명 (선택)
 * @property message - 상태 메시지 (선택)
 * @property timestamp - 이벤트 발생 시각
 */
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

/**
 * 장치 상태 WebSocket 메시지 (/topic/device-status)
 *
 * 계량소 장비(계량대, 전광판, 차단기, 네트워크)의
 * 연결 상태 변경 시 푸시되는 메시지입니다.
 *
 * @property deviceType - 장치 유형 (SCALE, DISPLAY, BARRIER, NETWORK)
 * @property deviceName - 장치 이름
 * @property status - 연결 상태
 * @property message - 상태 메시지 (오류 시, 선택)
 * @property timestamp - 이벤트 발생 시각
 */
export interface DeviceStatusMessage {
  deviceType: 'SCALE' | 'DISPLAY' | 'BARRIER' | 'NETWORK';
  deviceName: string;
  status: ConnectionStatus;
  message?: string;
  timestamp: string;
}

// ─── 상태 관리 타입 ───

/**
 * 장치 연결 상태 종합
 *
 * 계량소의 모든 장치(계량대, 전광판, 차단기, 네트워크)의
 * 연결 상태를 한 객체로 관리합니다.
 *
 * @property scale - 계량대 연결 상태
 * @property display - 전광판 연결 상태
 * @property barrier - 차단기 연결 상태
 * @property network - 네트워크 연결 상태
 */
export interface DeviceConnectionState {
  scale: ConnectionStatus;
  display: ConnectionStatus;
  barrier: ConnectionStatus;
  network: ConnectionStatus;
}

/**
 * 차량 정보 타입
 *
 * 현재 계량 중인 차량의 기본 정보입니다.
 *
 * @property plateNumber - 차량번호
 * @property companyName - 운송사명
 * @property itemName - 품목명
 * @property dispatchId - 연관 배차 ID (미확인 시 null)
 * @property driverName - 기사명
 */
export interface VehicleInfo {
  plateNumber: string;
  companyName: string;
  itemName: string;
  dispatchId: number | null;
  driverName: string;
}

/**
 * 중량 데이터 타입
 *
 * 계량대에서 실시간 수신되는 중량 값과 안정성 상태입니다.
 *
 * @property currentWeight - 현재 중량 (kg)
 * @property stability - 중량 안정성 상태
 * @property unit - 중량 단위
 */
export interface WeightData {
  currentWeight: number;
  stability: StabilityStatus;
  unit: string;
}

/**
 * 상태 로그 항목 타입
 *
 * 계량소 관제 화면 하단의 실시간 로그에 표시되는 개별 항목입니다.
 *
 * @property id - 로그 고유 ID
 * @property timestamp - 로그 발생 시각 (HH:mm:ss)
 * @property message - 로그 메시지
 * @property level - 로그 레벨 (info, success, warning, error)
 */
export interface StatusLogEntry {
  id: string;
  timestamp: string;
  message: string;
  level: 'info' | 'success' | 'warning' | 'error';
}

// ─── API 응답 타입 ───

/**
 * 배차 검색 결과 타입
 *
 * 수동 계량 모드에서 차량번호로 배차를 검색할 때 반환되는 결과입니다.
 *
 * @property dispatchId - 배차 ID
 * @property plateNumber - 차량번호
 * @property companyId - 운송사 ID
 * @property companyName - 운송사명
 * @property itemType - 품목 유형
 * @property itemName - 품목명
 * @property driverName - 기사명
 * @property dispatchDate - 배차일
 * @property dispatchStatus - 배차 상태
 */
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

/**
 * 계량 이력 기록 타입
 *
 * 계량소 관제 화면의 최근 계량 이력 테이블에 표시되는 데이터입니다.
 *
 * @property weighingId - 계량 ID
 * @property plateNumber - 차량번호
 * @property grossWeight - 총중량 (kg)
 * @property tareWeight - 공차중량 (kg, 선택)
 * @property netWeight - 순중량 (kg, 선택)
 * @property weighingMode - 계량 방식
 * @property weighingStatus - 계량 상태
 * @property companyName - 운송사명 (선택)
 * @property itemName - 품목명 (선택)
 * @property createdAt - 계량 일시
 */
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

/**
 * 계량 기록 생성 요청 타입
 *
 * 수동 계량 시 서버에 계량 기록 생성을 요청하는 데이터입니다.
 *
 * @property dispatchId - 연관 배차 ID
 * @property scaleId - 사용할 계량대 ID (선택)
 * @property weighingMode - 계량 모드 (AUTO 또는 MANUAL)
 * @property plateNumber - 차량번호 (선택)
 * @property plateConfidence - LPR 인식 신뢰도 (선택)
 * @property grossWeight - 총중량 (선택)
 */
export interface CreateWeighingRequest {
  dispatchId: number;
  scaleId?: number;
  weighingMode: WeighingMode;
  plateNumber?: string;
  plateConfidence?: number;
  grossWeight?: number;
}

/**
 * 시뮬레이터 명령 타입
 *
 * 개발/테스트 환경에서 계량 프로세스를 시뮬레이션하기 위한 명령입니다.
 *
 * @property command - 시뮬레이터 명령 (TRIGGER_SENSOR, CAPTURE_LPR, TOGGLE_POSITION, SET_WEIGHT)
 * @property params - 명령 매개변수 (SET_WEIGHT 시 weight 값 등, 선택)
 */
export interface SimulatorCommand {
  command: 'TRIGGER_SENSOR' | 'CAPTURE_LPR' | 'TOGGLE_POSITION' | 'SET_WEIGHT';
  params?: Record<string, unknown>;
}

// ─── 계량소 전체 상태 ───

/**
 * 계량소 전체 상태 타입
 *
 * useWeighingStation 훅에서 관리하는 계량소의 종합 상태입니다.
 *
 * @property mode - 현재 계량 모드 (AUTO/MANUAL)
 * @property processState - 현재 프로세스 상태
 * @property weight - 실시간 중량 데이터
 * @property vehicle - 현재 차량 정보
 * @property devices - 장치 연결 상태
 * @property logs - 상태 로그 목록
 * @property history - 최근 계량 이력
 * @property searchResults - 배차 검색 결과
 * @property selectedDispatchId - 선택된 배차 ID
 * @property simulatorEnabled - 시뮬레이터 활성화 여부
 */
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

// ─── 초기 상태 상수 ───

/** 빈 차량 정보 (계량 대기 상태에서 사용) */
export const EMPTY_VEHICLE: VehicleInfo = {
  plateNumber: '-',
  companyName: '-',
  itemName: '-',
  dispatchId: null,
  driverName: '-',
};

/** 초기 중량 데이터 (연결 전 기본값) */
export const INITIAL_WEIGHT: WeightData = {
  currentWeight: 0,
  stability: 'DISCONNECTED',
  unit: 'kg',
};

/** 초기 장치 연결 상태 (모든 장치 오프라인) */
export const INITIAL_DEVICES: DeviceConnectionState = {
  scale: 'OFFLINE',
  display: 'OFFLINE',
  barrier: 'OFFLINE',
  network: 'OFFLINE',
};

/** 프로세스 상태별 한글 라벨 매핑 */
export const PROCESS_STATE_LABELS: Record<ProcessState, string> = {
  IDLE: '대기',
  WEIGHING: '계량 중',
  STABILIZING: '안정화 중',
  COMPLETE: '완료',
  ERROR: '오류',
};

/** 중량 안정성 상태별 한글 라벨 매핑 */
export const STABILITY_LABELS: Record<StabilityStatus, string> = {
  UNSTABLE: '불안정',
  STABLE: '안정',
  ERROR: '오류',
  DISCONNECTED: '미연결',
};

/** 계량 방식별 한글 라벨 매핑 */
export const WEIGHING_MODE_LABELS: Record<string, string> = {
  LPR_AUTO: 'LPR자동',
  AUTO: '자동',
  MOBILE_OTP: '모바일OTP',
  MANUAL: '수동',
  RE_WEIGH: '재계량',
};

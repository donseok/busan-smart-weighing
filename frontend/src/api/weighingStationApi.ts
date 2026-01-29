import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types';
import type {
  DispatchSearchResult,
  WeighingHistoryRecord,
  CreateWeighingRequest,
  SimulatorCommand,
} from '../types/weighingStation';

// 배차 검색 (차량번호로)
export const searchDispatches = async (plateNumber: string): Promise<DispatchSearchResult[]> => {
  const response = await apiClient.get<ApiResponse<DispatchSearchResult[]>>(
    '/dispatches',
    { params: { plateNumber, status: 'REGISTERED' } },
  );
  return response.data.data || [];
};

// 계량 기록 생성
export const createWeighing = async (data: CreateWeighingRequest): Promise<unknown> => {
  const response = await apiClient.post<ApiResponse<unknown>>('/weighings', data);
  return response.data.data;
};

// 계량 확인 (중량 확정)
export const confirmWeighing = async (
  weighingId: number,
  grossWeight: number,
): Promise<unknown> => {
  const response = await apiClient.post<ApiResponse<unknown>>(
    `/weighings/${weighingId}/confirm`,
    { grossWeight },
  );
  return response.data.data;
};

// 재계량 요청
export const reWeigh = async (
  weighingId: number,
  reason: string,
): Promise<unknown> => {
  const response = await apiClient.post<ApiResponse<unknown>>(
    `/weighings/${weighingId}/reweigh`,
    { reason },
  );
  return response.data.data;
};

// 최근 계량 기록 조회
export const fetchWeighingHistory = async (
  size: number = 50,
): Promise<WeighingHistoryRecord[]> => {
  const response = await apiClient.get<ApiResponse<PageResponse<WeighingHistoryRecord>>>(
    '/weighings',
    { params: { size, sort: 'createdAt,desc' } },
  );
  const page = response.data.data;
  return page?.content || [];
};

// 차단기 열기 명령
export const openBarrier = async (): Promise<void> => {
  await apiClient.post('/monitoring/devices/cmd', {
    deviceType: 'BARRIER_GATE',
    command: 'OPEN',
  });
};

// 프로세스 초기화
export const resetProcess = async (scaleId: number = 1): Promise<void> => {
  await apiClient.post(`/weighings/reset`, { scaleId });
};

// ─── 시뮬레이터 명령 ───

export const sendSimulatorCommand = async (cmd: SimulatorCommand): Promise<void> => {
  await apiClient.post('/lpr/simulator', cmd);
};

export const triggerSensor = () =>
  sendSimulatorCommand({ command: 'TRIGGER_SENSOR' });

export const captureLpr = () =>
  sendSimulatorCommand({ command: 'CAPTURE_LPR' });

export const togglePosition = () =>
  sendSimulatorCommand({ command: 'TOGGLE_POSITION' });

export const setSimulatorWeight = (weight: number) =>
  sendSimulatorCommand({ command: 'SET_WEIGHT', params: { weight } });

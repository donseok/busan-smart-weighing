/**
 * 계량소 관제 API 모듈
 *
 * 계량소 관제(WeighingStation) 페이지에서 사용하는 REST API 호출 함수들입니다.
 * 배차 검색, 계량 기록 생성/확인, 차단기 제어, 시뮬레이터 명령 등을 제공합니다.
 *
 * @module api/weighingStationApi
 */

import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types';
import type {
  DispatchSearchResult,
  WeighingHistoryRecord,
  CreateWeighingRequest,
  SimulatorCommand,
} from '../types/weighingStation';

/**
 * 배차 검색 (차량번호 기준)
 *
 * 수동 계량 모드에서 차량번호를 입력하여 등록 상태인 배차를 검색합니다.
 *
 * @param plateNumber - 검색할 차량번호
 * @returns 검색된 배차 목록
 */
export const searchDispatches = async (plateNumber: string): Promise<DispatchSearchResult[]> => {
  const response = await apiClient.get<ApiResponse<DispatchSearchResult[]>>(
    '/dispatches',
    { params: { plateNumber, status: 'REGISTERED' } },
  );
  return response.data.data || [];
};

/**
 * 계량 기록 생성
 *
 * 수동 계량 시 새로운 계량 기록을 서버에 생성합니다.
 *
 * @param data - 계량 생성 요청 데이터 (배차 ID, 계량 모드, 차량번호 등)
 * @returns 생성된 계량 기록 데이터
 */
export const createWeighing = async (data: CreateWeighingRequest): Promise<unknown> => {
  const response = await apiClient.post<ApiResponse<unknown>>('/weighings', data);
  return response.data.data;
};

/**
 * 계량 확인 (중량 확정)
 *
 * 계량 중인 기록의 총중량을 확정하여 계량을 완료 처리합니다.
 *
 * @param weighingId - 확정할 계량 기록 ID
 * @param grossWeight - 확정할 총중량 (kg)
 * @returns 업데이트된 계량 기록 데이터
 */
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

/**
 * 재계량 요청
 *
 * 기존 계량 기록에 대해 재계량을 요청합니다.
 * 사유를 필수로 입력해야 합니다.
 *
 * @param weighingId - 재계량할 계량 기록 ID
 * @param reason - 재계량 사유
 * @returns 업데이트된 계량 기록 데이터
 */
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

/**
 * 최근 계량 기록 조회
 *
 * 계량소 관제 화면의 이력 테이블에 표시할 최근 계량 기록을 조회합니다.
 * 생성일시 역순으로 정렬됩니다.
 *
 * @param size - 조회할 기록 수 (기본값: 50)
 * @returns 최근 계량 기록 배열
 */
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

/**
 * 차단기 열기 명령
 *
 * 계량 완료 후 차량이 나갈 수 있도록 차단기를 수동으로 개방합니다.
 */
export const openBarrier = async (): Promise<void> => {
  await apiClient.post('/monitoring/devices/cmd', {
    deviceType: 'BARRIER_GATE',
    command: 'OPEN',
  });
};

/**
 * 프로세스 초기화
 *
 * 계량소의 현재 프로세스 상태를 초기(IDLE)로 리셋합니다.
 * 오류 발생 시 또는 수동으로 프로세스를 재시작할 때 사용합니다.
 *
 * @param scaleId - 초기화할 계량대 ID (기본값: 1)
 */
export const resetProcess = async (scaleId: number = 1): Promise<void> => {
  await apiClient.post(`/weighings/reset`, { scaleId });
};

// ─── 시뮬레이터 명령 ───

/**
 * 시뮬레이터 명령 전송
 *
 * 개발/테스트 환경에서 LPR 및 계량 프로세스를 시뮬레이션합니다.
 *
 * @param cmd - 시뮬레이터 명령 객체
 */
export const sendSimulatorCommand = async (cmd: SimulatorCommand): Promise<void> => {
  await apiClient.post('/lpr/simulator', cmd);
};

/** 차량 감지 센서 트리거 (시뮬레이터) */
export const triggerSensor = () =>
  sendSimulatorCommand({ command: 'TRIGGER_SENSOR' });

/** LPR 카메라 촬영 요청 (시뮬레이터) */
export const captureLpr = () =>
  sendSimulatorCommand({ command: 'CAPTURE_LPR' });

/** 차량 정위치 토글 (시뮬레이터) */
export const togglePosition = () =>
  sendSimulatorCommand({ command: 'TOGGLE_POSITION' });

/**
 * 시뮬레이터 중량 설정
 *
 * @param weight - 설정할 중량 값 (kg)
 */
export const setSimulatorWeight = (weight: number) =>
  sendSimulatorCommand({ command: 'SET_WEIGHT', params: { weight } });

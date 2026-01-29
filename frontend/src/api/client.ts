/**
 * API 클라이언트 모듈
 *
 * Axios 기반의 HTTP 클라이언트로, 모든 REST API 호출에 사용됩니다.
 * 요청/응답 인터셉터를 통해 다음 기능을 자동 처리합니다:
 * - JWT 토큰 자동 첨부 (Authorization 헤더)
 * - camelCase ↔ snake_case 키 자동 변환
 * - 401 응답 시 토큰 자동 갱신 (리프레시 토큰 사용)
 *
 * @module api/client
 */

import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

/**
 * snake_case 문자열을 camelCase로 변환
 * @param str - 변환할 문자열 (예: 'dispatch_status')
 * @returns camelCase 문자열 (예: 'dispatchStatus')
 */
const snakeToCamel = (str: string): string =>
  str.replace(/_([a-z])/g, (_, c: string) => c.toUpperCase());

/**
 * camelCase 문자열을 snake_case로 변환
 * @param str - 변환할 문자열 (예: 'dispatchStatus')
 * @returns snake_case 문자열 (예: 'dispatch_status')
 */
const camelToSnake = (str: string): string =>
  str.replace(/[A-Z]/g, (c) => `_${c.toLowerCase()}`);

/**
 * 객체의 모든 키를 재귀적으로 변환하는 유틸리티 함수
 *
 * 배열, 중첩 객체도 재귀적으로 처리하여 모든 키 이름을 변환합니다.
 *
 * @param obj - 키를 변환할 대상 객체/배열
 * @param converter - 키 변환 함수 (snakeToCamel 또는 camelToSnake)
 * @returns 키가 변환된 새 객체
 */
const convertKeys = (obj: unknown, converter: (key: string) => string): unknown => {
  // 배열인 경우 각 요소에 대해 재귀 변환
  if (Array.isArray(obj)) return obj.map((item) => convertKeys(item, converter));
  // 객체인 경우 모든 키를 변환하고 값도 재귀 처리
  if (obj !== null && typeof obj === 'object') {
    return Object.entries(obj as Record<string, unknown>).reduce(
      (acc, [key, value]) => {
        acc[converter(key)] = convertKeys(value, converter);
        return acc;
      },
      {} as Record<string, unknown>,
    );
  }
  // 기본 타입은 그대로 반환
  return obj;
};

/** Axios 인스턴스 생성 (기본 설정: baseURL, 타임아웃 10초, JSON 헤더) */
const apiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

/** 토큰 갱신 중 여부 플래그 (동시 갱신 방지) */
let isRefreshing = false;

/** 토큰 갱신 중 대기하는 요청 큐 */
let failedQueue: {
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}[] = [];

/**
 * 대기 중인 요청 큐를 처리하는 함수
 *
 * 토큰 갱신이 완료되면 대기 중이던 모든 요청을 재실행하거나
 * 갱신 실패 시 모든 요청을 거부합니다.
 *
 * @param error - 갱신 실패 시 에러 객체 (성공 시 null)
 * @param token - 새로 발급된 액세스 토큰 (실패 시 null)
 */
const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token!);
    }
  });
  failedQueue = [];
};

// ─── 요청 인터셉터 ───
// 모든 요청에 JWT 토큰 첨부 및 요청 데이터의 키를 snake_case로 변환
apiClient.interceptors.request.use((config) => {
  // localStorage에서 액세스 토큰을 가져와 Authorization 헤더에 첨부
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // 요청 본문(body)의 키를 camelCase → snake_case로 변환
  if (config.data && typeof config.data === 'object') {
    config.data = convertKeys(config.data, camelToSnake);
  }
  // 쿼리 파라미터의 키도 snake_case로 변환
  if (config.params && typeof config.params === 'object') {
    config.params = convertKeys(config.params, camelToSnake);
  }
  return config;
});

// ─── 응답 인터셉터 ───
// 응답 데이터의 키를 camelCase로 변환하고, 401 에러 시 토큰 자동 갱신
apiClient.interceptors.response.use(
  (response) => {
    // JSON 응답의 키를 snake_case → camelCase로 변환
    const contentType = response.headers['content-type'] || '';
    if (response.data && typeof response.data === 'object' && contentType.includes('application/json')) {
      response.data = convertKeys(response.data, snakeToCamel);
    }
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // 401 Unauthorized 응답 시 토큰 자동 갱신 처리
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      // 이미 갱신 중인 경우 대기 큐에 추가
      if (isRefreshing) {
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      // 토큰 갱신 시작
      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');

      // 리프레시 토큰이 없으면 갱신 불가
      if (!refreshToken) {
        isRefreshing = false;
        return Promise.reject(error);
      }

      try {
        // 리프레시 토큰으로 새 액세스 토큰 발급 요청
        const response = await axios.post('/api/v1/auth/refresh', {
          refresh_token: refreshToken,
        });

        const { access_token: newAccessToken, refresh_token: newRefreshToken } =
          response.data?.data ?? response.data;

        // 새 토큰을 localStorage에 저장
        localStorage.setItem('accessToken', newAccessToken);
        if (newRefreshToken) {
          localStorage.setItem('refreshToken', newRefreshToken);
        }

        // 대기 중인 요청들에 새 토큰 전달
        processQueue(null, newAccessToken);

        // 원래 요청 재실행
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // 갱신 실패 시 대기 요청 모두 거부하고 로그인 페이지로 이동
        processQueue(refreshError, null);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  },
);

/**
 * 구조화된 API 에러 인터페이스
 *
 * 에러 유형별로 분류하여 UI에서 적절한 메시지를 표시할 수 있도록 합니다.
 * - NETWORK: 서버 연결 불가 (오프라인, 타임아웃 등)
 * - BUSINESS: 비즈니스 로직 에러 (중복, 유효성 등)
 * - AUTH: 인증/권한 에러 (401, 403)
 * - UNKNOWN: 분류 불가 에러
 */
export interface ApiError {
  type: 'NETWORK' | 'BUSINESS' | 'AUTH' | 'UNKNOWN';
  code?: string;
  message: string;
  status?: number;
}

/**
 * API 에러를 구조화된 ApiError 객체로 파싱하는 함수
 *
 * Axios 에러, 네트워크 에러, 비즈니스 에러 등을 분류하여
 * 일관된 에러 객체로 변환합니다.
 *
 * @param error - catch 블록에서 받은 에러 객체
 * @returns 구조화된 ApiError 객체
 */
export function parseApiError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ error?: { code?: string; message?: string }; message?: string }>;

    if (!axiosError.response) {
      return {
        type: 'NETWORK',
        message: '서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.',
      };
    }

    const status = axiosError.response.status;
    const data = axiosError.response.data;

    if (status === 401 || status === 403) {
      return {
        type: 'AUTH',
        code: data?.error?.code,
        message: data?.error?.message || '인증이 만료되었습니다. 다시 로그인해주세요.',
        status,
      };
    }

    if (data?.error) {
      return {
        type: 'BUSINESS',
        code: data.error.code,
        message: data.error.message || '요청을 처리할 수 없습니다.',
        status,
      };
    }

    return {
      type: 'UNKNOWN',
      message: data?.message || `서버 오류가 발생했습니다. (${status})`,
      status,
    };
  }

  return {
    type: 'UNKNOWN',
    message: '알 수 없는 오류가 발생했습니다.',
  };
}

export default apiClient;

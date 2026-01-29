/**
 * @fileoverview API 호출 공통 래퍼 커스텀 훅
 *
 * 로딩 상태 관리, 구조화된 에러 처리, 사용자 메시지 표시를 캡슐화하여
 * 반복적인 try/catch/finally 패턴을 제거합니다.
 *
 * @module hooks/useApiCall
 */
import { useState, useCallback } from 'react';
import { message } from 'antd';
import { parseApiError, type ApiError } from '../api/client';

/**
 * API 호출 공통 래퍼 훅
 *
 * `setLoading(true); try { ... } catch { } setLoading(false)` 패턴을 캡슐화하고,
 * 에러 발생 시 구조화된 ApiError로 파싱하여 적절한 메시지를 표시합니다.
 *
 * @returns execute: API 함수 실행기, loading: 로딩 상태, error: 마지막 에러, clearError: 에러 초기화
 */
export function useApiCall() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  const execute = useCallback(
    async <T>(fn: () => Promise<T>, options?: { showError?: boolean }): Promise<T | undefined> => {
      setLoading(true);
      setError(null);
      try {
        const result = await fn();
        return result;
      } catch (err) {
        const apiError = parseApiError(err);
        setError(apiError);
        if (options?.showError !== false) {
          if (apiError.type === 'NETWORK') {
            message.error({ content: apiError.message, key: 'network-error', duration: 5 });
          } else {
            message.error(apiError.message);
          }
        }
        return undefined;
      } finally {
        setLoading(false);
      }
    },
    [],
  );

  const clearError = useCallback(() => setError(null), []);

  return { execute, loading, error, clearError };
}

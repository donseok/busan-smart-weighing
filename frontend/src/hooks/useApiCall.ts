import { useState, useCallback } from 'react';

/**
 * API 호출 공통 래퍼 훅
 *
 * `setLoading(true); try { ... } catch { } setLoading(false)` 패턴을 캡슐화합니다.
 *
 * @returns [execute, loading] - execute: API 함수를 감싸서 실행, loading: 로딩 상태
 */
export function useApiCall() {
  const [loading, setLoading] = useState(false);

  const execute = useCallback(
    async <T>(fn: () => Promise<T>): Promise<T | undefined> => {
      setLoading(true);
      try {
        const result = await fn();
        return result;
      } catch (error) {
        console.error('[API Error]', error);
        return undefined;
      } finally {
        setLoading(false);
      }
    },
    [],
  );

  return [execute, loading] as const;
}

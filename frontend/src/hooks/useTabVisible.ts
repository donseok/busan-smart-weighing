import { useState, useEffect } from 'react';
import { useTab } from '../context/TabContext';

/**
 * 탭 활성 상태 감지 훅
 *
 * 현재 탭이 활성 상태인지 감지합니다.
 * 비활성 탭에서 불필요한 데이터 페칭이나 WebSocket 처리를 중단할 수 있습니다.
 *
 * @param tabKey - 감시할 탭의 경로 키
 * @returns isVisible - 탭이 현재 활성 상태인지 여부
 */
export function useTabVisible(tabKey: string): boolean {
  const { activeKey } = useTab();
  const [isVisible, setIsVisible] = useState(activeKey === tabKey);

  useEffect(() => {
    setIsVisible(activeKey === tabKey);
  }, [activeKey, tabKey]);

  return isVisible;
}

/**
 * 탭 활성 시에만 콜백을 실행하는 훅
 *
 * @param tabKey - 감시할 탭의 경로 키
 * @param callback - 탭이 활성화될 때 실행할 콜백
 */
export function useTabActivation(tabKey: string, callback: () => void) {
  const { activeKey } = useTab();

  useEffect(() => {
    if (activeKey === tabKey) {
      callback();
    }
  }, [activeKey, tabKey, callback]);
}

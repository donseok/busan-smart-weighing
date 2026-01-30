/**
 * @fileoverview 숫자 애니메이션 컴포넌트
 *
 * 숫자 값이 변경될 때 부드러운 카운팅 애니메이션을 적용하는 컴포넌트입니다.
 * 대시보드 KPI, 계량 중량 표시 등에서 시각적 효과를 위해 사용됩니다.
 * requestAnimationFrame을 사용하여 60fps 부드러운 애니메이션을 구현합니다.
 *
 * @module components/AnimatedNumber
 */
import React, { useEffect, useState, useRef } from 'react';

/**
 * 숫자 애니메이션 컴포넌트의 속성 인터페이스
 *
 * @property value - 표시할 목표 숫자 값
 * @property duration - 애니메이션 지속 시간 (밀리초, 기본값: 600ms)
 * @property decimals - 소수점 이하 자릿수 (기본값: 0)
 * @property suffix - 숫자 뒤에 표시할 접미사 (예: 'kg', '%')
 * @property style - 추가 CSS 스타일
 */
interface AnimatedNumberProps {
  value: number;
  duration?: number;
  decimals?: number;
  suffix?: string;
  style?: React.CSSProperties;
}

/**
 * 숫자 애니메이션 컴포넌트
 *
 * 숫자 값이 변경되면 이전 값에서 새 값까지 부드럽게 카운팅되는
 * 애니메이션 효과를 보여주는 컴포넌트입니다.
 * Ease-out Cubic 이징 함수를 사용하여 자연스러운 감속 효과를 구현합니다.
 *
 * @example
 * ```tsx
 * // 기본 사용법: 0에서 1000까지 애니메이션
 * <AnimatedNumber value={1000} />
 *
 * // 소수점 2자리, 단위 포함
 * <AnimatedNumber value={85.5} decimals={2} suffix="%" />
 *
 * // 커스텀 지속 시간 (1초)
 * <AnimatedNumber value={5000} duration={1000} suffix="kg" />
 * ```
 *
 * @param props - 컴포넌트 속성
 * @returns 애니메이션되는 숫자를 포함한 span 요소
 */
const AnimatedNumber: React.FC<AnimatedNumberProps> = ({
  value,
  duration = 600,
  decimals = 0,
  suffix = '',
  style,
}) => {
  /** 화면에 표시되는 현재 숫자 값 (애니메이션 중간값) */
  const [displayValue, setDisplayValue] = useState(value);

  /** 이전 값을 저장하여 다음 애니메이션의 시작점으로 사용 */
  const prevValue = useRef(value);

  /** requestAnimationFrame ID를 저장하여 정리(cleanup) 시 취소 가능하게 함 */
  const frameRef = useRef<number>(0);

  useEffect(() => {
    // 애니메이션 시작값과 목표값 설정
    const start = prevValue.current;
    const end = value;
    const diff = end - start;

    // 값이 변경되지 않으면 애니메이션 스킵
    if (diff === 0) return;

    // 애니메이션 시작 시간 기록
    const startTime = performance.now();

    /**
     * requestAnimationFrame 콜백 함수
     *
     * 매 프레임마다 호출되어 현재 진행률을 계산하고
     * Ease-out Cubic 이징을 적용한 값을 화면에 표시합니다.
     *
     * @param currentTime - 현재 타임스탬프 (performance.now())
     */
    const animate = (currentTime: number) => {
      // 경과 시간 계산
      const elapsed = currentTime - startTime;

      // 진행률 (0~1), duration을 초과하면 1로 고정
      const progress = Math.min(elapsed / duration, 1);

      /**
       * Ease-out Cubic 이징 함수
       *
       * 수학식: 1 - (1 - x)^3
       * 시작할 때 빠르고 끝날 때 느려지는 자연스러운 감속 효과를 제공합니다.
       * 일반적으로 UI 애니메이션에서 가장 자연스러운 효과를 냅니다.
       */
      const eased = 1 - Math.pow(1 - progress, 3);

      // 이징이 적용된 현재 값을 계산하여 화면에 표시
      setDisplayValue(start + diff * eased);

      // 애니메이션이 완료되지 않았으면 다음 프레임 예약
      if (progress < 1) {
        frameRef.current = requestAnimationFrame(animate);
      } else {
        // 애니메이션 완료 시 현재 값을 다음 애니메이션의 시작점으로 저장
        prevValue.current = end;
      }
    };

    // 첫 번째 애니메이션 프레임 예약
    frameRef.current = requestAnimationFrame(animate);

    // 컴포넌트 언마운트 또는 value 변경 시 진행 중인 애니메이션 정리
    return () => {
      if (frameRef.current) {
        cancelAnimationFrame(frameRef.current);
      }
    };
  }, [value, duration]);

  return (
    <span style={style}>
      {/* 천 단위 구분 쉼표 적용: 정규식으로 3자리마다 쉼표 삽입 */}
      {displayValue.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
      {suffix}
    </span>
  );
};

export default AnimatedNumber;

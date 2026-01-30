import React from 'react';
import { Tag, theme } from 'antd';
import type { WeightData, StabilityStatus } from '../../types/weighingStation';
import { STABILITY_LABELS } from '../../types/weighingStation';
import AnimatedNumber from '../AnimatedNumber';

/**
 * 중량 표시 컴포넌트의 속성 인터페이스
 *
 * @property weight - 현재 중량 데이터 (값, 단위, 안정성 상태 포함)
 */
interface WeightDisplayProps {
  weight: WeightData;
}

/**
 * 안정성 상태별 색상 매핑
 *
 * 각 안정성 상태(STABLE, UNSTABLE, ERROR, DISCONNECTED)에 대해
 * 배경색, 텍스트 색상, 테두리 색상을 정의합니다.
 */
const STABILITY_COLORS: Record<StabilityStatus, { bg: string; text: string; border: string }> = {
  STABLE: { bg: 'rgba(16, 185, 129, 0.15)', text: '#10B981', border: '#10B981' },
  UNSTABLE: { bg: 'rgba(245, 158, 11, 0.15)', text: '#F59E0B', border: '#F59E0B' },
  ERROR: { bg: 'rgba(244, 63, 94, 0.15)', text: '#F43F5E', border: '#F43F5E' },
  DISCONNECTED: { bg: 'rgba(100, 116, 139, 0.15)', text: '#64748B', border: '#64748B' },
};

/**
 * 중량 표시 컴포넌트
 *
 * 계량대에서 측정된 실시간 중량 값을 디지털 디스플레이 형태로 보여줍니다.
 * 안정(STABLE)/불안정(UNSTABLE)/오류(ERROR)/연결 끊김(DISCONNECTED) 상태에 따라
 * 디스플레이 색상과 그림자 효과가 동적으로 변경됩니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.weight - 현재 중량 데이터 객체
 * @returns 중량 디스플레이 JSX
 */
const WeightDisplay: React.FC<WeightDisplayProps> = ({ weight }) => {
  const { token } = theme.useToken();
  /** 현재 안정성 상태에 맞는 색상 스타일 */
  const stabilityStyle = STABILITY_COLORS[weight.stability];

  /** AnimatedNumber 컴포넌트를 사용하여 중량 값을 애니메이션으로 표시 */

  return (
    <div
      style={{
        background: `linear-gradient(180deg, ${token.colorBgContainer}, ${token.colorPrimary}08)`,
        border: `1px solid ${token.colorBorder}`,
        borderRadius: 12,
        padding: '24px 20px',
        textAlign: 'center',
        position: 'relative',
        overflow: 'hidden',
        /* 안정성 상태에 따른 박스 그림자 효과 적용 */
        boxShadow: weight.stability === 'STABLE'
          ? `0 0 24px ${token.colorPrimary}18, inset 0 1px 0 ${token.colorPrimary}30`
          : weight.stability === 'ERROR'
          ? `0 0 24px #F43F5E18, inset 0 1px 0 #F43F5E30`
          : `inset 0 1px 0 ${token.colorBorder}`,
        transition: 'box-shadow 0.4s',
      }}
    >
      {/* 상단 영역: "현재 중량" 라벨 및 안정성 상태 태그 */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: 12,
        }}
      >
        <span
          style={{
            fontSize: 13,
            color: token.colorTextSecondary,
            fontWeight: 500,
            letterSpacing: '0.05em',
            textTransform: 'uppercase',
          }}
        >
          현재 중량
        </span>
        {/* 안정성 상태 표시 태그 (안정/불안정/오류/연결 끊김) */}
        <Tag
          style={{
            background: stabilityStyle.bg,
            color: stabilityStyle.text,
            border: `1px solid ${stabilityStyle.border}`,
            borderRadius: 6,
            fontWeight: 600,
            fontSize: 12,
          }}
        >
          {STABILITY_LABELS[weight.stability]}
        </Tag>
      </div>

      {/* 중량 숫자 디스플레이 (모노스페이스 폰트, 72px 대형 표시) */}
      <div
        role="status"
        aria-live="assertive"
        aria-label={`현재 중량 ${weight.currentWeight.toLocaleString('ko-KR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} ${weight.unit}, 상태: ${STABILITY_LABELS[weight.stability]}`}
        style={{
          fontFamily: "'JetBrains Mono', 'Consolas', 'Courier New', monospace",
          fontSize: weight.currentWeight === 0 ? 96 : 72,
          fontWeight: 700,
          lineHeight: 1,
          /* 상태별 텍스트 색상: 0.0=빨강, 오류=빨강, 연결 끊김=회색, 그 외=기본 테마 색상 */
          color: weight.currentWeight === 0 ? '#F43F5E'
            : weight.stability === 'ERROR' ? '#F43F5E'
            : weight.stability === 'DISCONNECTED' ? '#64748B'
            : token.colorPrimary,
          /* 안정 상태일 때만 글로우 효과 적용 */
          textShadow: weight.stability === 'STABLE'
            ? `0 0 20px ${token.colorPrimary}40`
            : 'none',
          transition: 'color 0.3s, text-shadow 0.3s',
          padding: '16px 0',
        }}
      >
        <AnimatedNumber value={weight.currentWeight} decimals={1} duration={300} />
      </div>

      {/* 중량 단위 표시 (kg 등) */}
      <div
        style={{
          fontSize: 16,
          color: token.colorTextSecondary,
          fontWeight: 500,
          marginTop: 4,
        }}
      >
        {weight.unit}
      </div>
    </div>
  );
};

export default WeightDisplay;

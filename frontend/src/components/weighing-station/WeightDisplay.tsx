import React from 'react';
import { Tag, theme } from 'antd';
import type { WeightData, StabilityStatus, WeighingDisplayInfo } from '../../types/weighingStation';
import { STABILITY_LABELS } from '../../types/weighingStation';
import AnimatedNumber from '../AnimatedNumber';

/**
 * 다중 중량 표시 컴포넌트의 속성 인터페이스
 *
 * @property weight - 현재 중량 데이터 (값, 단위, 안정성 상태 포함)
 * @property weighingInfo - 다중 중량 표시 데이터 (1차/2차/실/이론중량, 알림)
 */
interface WeightDisplayProps {
  weight: WeightData;
  weighingInfo: WeighingDisplayInfo;
}

/** 안정성 상태별 색상 매핑 */
const STABILITY_COLORS: Record<StabilityStatus, { bg: string; text: string; border: string }> = {
  STABLE: { bg: 'rgba(16, 185, 129, 0.15)', text: '#10B981', border: '#10B981' },
  UNSTABLE: { bg: 'rgba(245, 158, 11, 0.15)', text: '#F59E0B', border: '#F59E0B' },
  ERROR: { bg: 'rgba(244, 63, 94, 0.15)', text: '#F43F5E', border: '#F43F5E' },
  DISCONNECTED: { bg: 'rgba(148, 163, 184, 0.15)', text: '#94A3B8', border: '#94A3B8' },
};

/** 중량 값 포맷 (천 단위 구분) */
const formatWeight = (value: number): string => {
  if (value === 0) return '0';
  return value.toLocaleString('ko-KR', { maximumFractionDigits: 0 });
};

/**
 * 다중 중량 표시 컴포넌트
 *
 * 참조 이미지 레이아웃을 따르는 계량소 디스플레이:
 * - 헤더: 회사명 + 공장명
 * - 현재중량: 대형 황금색 숫자
 * - 2x2 그리드: 1차/2차중량 (파란 배경), 실중량/이론중량 (청록 배경)
 * - 알림 바: 녹색 배경 하단 바
 */
const WeightDisplay: React.FC<WeightDisplayProps> = ({ weight, weighingInfo }) => {
  const { token } = theme.useToken();
  const stabilityStyle = STABILITY_COLORS[weight.stability];

  return (
    <div style={{ borderRadius: 12, overflow: 'hidden', border: `1px solid ${token.colorBorder}` }}>
      {/* ── 헤더: 회사명 + 공장명 ── */}
      <div
        style={{
          background: '#1A2332',
          padding: '12px 16px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <span style={{ color: '#fff', fontWeight: 700, fontSize: 14 }}>DK 동국씨엠</span>
        <span style={{ color: '#fff', fontWeight: 700, fontSize: 14 }}>부산공장</span>
      </div>

      {/* ── 현재중량 영역 ── */}
      <div
        style={{
          background: '#0F1923',
          padding: '16px 20px 20px',
          position: 'relative',
        }}
      >
        {/* 상단: 라벨 + 안정성 태그 */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <span style={{ color: '#FF3333', fontWeight: 700, fontSize: 13, letterSpacing: '0.03em' }}>
            현재중량
          </span>
          <Tag
            style={{
              background: stabilityStyle.bg,
              color: stabilityStyle.text,
              border: `1px solid ${stabilityStyle.border}`,
              borderRadius: 6,
              fontWeight: 600,
              fontSize: 11,
              margin: 0,
            }}
          >
            {STABILITY_LABELS[weight.stability]}
          </Tag>
        </div>

        {/* 대형 숫자 디스플레이 */}
        <div
          role="status"
          aria-live="assertive"
          aria-label={`현재 중량 ${weight.currentWeight.toLocaleString('ko-KR')} ${weight.unit}`}
          style={{
            display: 'flex',
            alignItems: 'baseline',
            justifyContent: 'flex-end',
            gap: 8,
          }}
        >
          <span
            style={{
              fontFamily: "'JetBrains Mono', 'Consolas', 'Courier New', monospace",
              fontSize: 64,
              fontWeight: 700,
              lineHeight: 1,
              color: '#FFD700',
              textShadow: weight.stability === 'STABLE'
                ? '0 0 20px rgba(255, 215, 0, 0.3)'
                : 'none',
              transition: 'color 0.3s, text-shadow 0.3s',
            }}
          >
            <AnimatedNumber value={weight.currentWeight} decimals={0} duration={300} />
          </span>
          <span style={{ color: '#B4B4B4', fontSize: 18, fontWeight: 500 }}>
            {weight.unit === 'kg' ? 'Kg' : weight.unit}
          </span>
        </div>
      </div>

      {/* ── 2x2 중량 그리드 ── */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gridTemplateRows: 'auto auto' }}>
        {/* Row 1: 1차중량 / 2차중량 (파란 배경) */}
        <WeightCell
          label="1차중량"
          value={weighingInfo.firstWeight}
          unit={weight.unit}
          bgColor="#1B2A4A"
          labelColor="#64A0FF"
          borderRight
          borderBottom
        />
        <WeightCell
          label="2차중량"
          value={weighingInfo.secondWeight}
          unit={weight.unit}
          bgColor="#1B2A4A"
          labelColor="#64A0FF"
          borderBottom
        />

        {/* Row 2: 실중량 / 이론중량 (청록 배경) */}
        <WeightCell
          label="실중량"
          value={weighingInfo.netWeight}
          unit={weight.unit}
          bgColor="#1A3A3A"
          labelColor="#50C8B4"
          borderRight
        />
        <WeightCell
          label="이론중량"
          value={weighingInfo.theoreticalWeight}
          unit={weight.unit}
          bgColor="#1A3A3A"
          labelColor="#50C8B4"
        />
      </div>

      {/* ── 알림 바 ── */}
      <div
        style={{
          background: '#28A745',
          padding: '10px 16px',
          display: 'flex',
          alignItems: 'center',
          gap: 8,
        }}
      >
        <span style={{ color: '#fff', fontWeight: 600, fontSize: 13 }}>
          {weighingInfo.notification || '알림'}
        </span>
      </div>
    </div>
  );
};

/** 그리드 셀 내부 컴포넌트 */
interface WeightCellProps {
  label: string;
  value: number;
  unit: string;
  bgColor: string;
  labelColor: string;
  borderRight?: boolean;
  borderBottom?: boolean;
}

const WeightCell: React.FC<WeightCellProps> = ({
  label,
  value,
  unit,
  bgColor,
  labelColor,
  borderRight,
  borderBottom,
}) => (
  <div
    style={{
      background: bgColor,
      padding: '10px 14px 14px',
      borderRight: borderRight ? '1px solid rgba(255,255,255,0.1)' : undefined,
      borderBottom: borderBottom ? '1px solid rgba(255,255,255,0.1)' : undefined,
    }}
  >
    <div style={{ color: labelColor, fontWeight: 700, fontSize: 12, marginBottom: 6, letterSpacing: '0.02em' }}>
      {label}
    </div>
    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'flex-end', gap: 6 }}>
      <span
        style={{
          fontFamily: "'JetBrains Mono', 'Consolas', 'Courier New', monospace",
          fontSize: 28,
          fontWeight: 700,
          color: '#fff',
          lineHeight: 1,
        }}
      >
        {formatWeight(value)}
      </span>
      <span style={{ color: '#B4B4B4', fontSize: 13, fontWeight: 500 }}>
        {unit === 'kg' ? 'Kg' : unit}
      </span>
    </div>
  </div>
);

export default WeightDisplay;

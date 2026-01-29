import React from 'react';
import { Tag, theme } from 'antd';
import type { WeightData, StabilityStatus } from '../../types/weighingStation';
import { STABILITY_LABELS } from '../../types/weighingStation';

interface WeightDisplayProps {
  weight: WeightData;
}

const STABILITY_COLORS: Record<StabilityStatus, { bg: string; text: string; border: string }> = {
  STABLE: { bg: 'rgba(16, 185, 129, 0.15)', text: '#10B981', border: '#10B981' },
  UNSTABLE: { bg: 'rgba(245, 158, 11, 0.15)', text: '#F59E0B', border: '#F59E0B' },
  ERROR: { bg: 'rgba(244, 63, 94, 0.15)', text: '#F43F5E', border: '#F43F5E' },
  DISCONNECTED: { bg: 'rgba(100, 116, 139, 0.15)', text: '#64748B', border: '#64748B' },
};

const WeightDisplay: React.FC<WeightDisplayProps> = ({ weight }) => {
  const { token } = theme.useToken();
  const stabilityStyle = STABILITY_COLORS[weight.stability];

  const formattedWeight = weight.currentWeight.toLocaleString('ko-KR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });

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
        boxShadow: weight.stability === 'STABLE'
          ? `0 0 24px ${token.colorPrimary}18, inset 0 1px 0 ${token.colorPrimary}30`
          : weight.stability === 'ERROR'
          ? `0 0 24px #F43F5E18, inset 0 1px 0 #F43F5E30`
          : `inset 0 1px 0 ${token.colorBorder}`,
        transition: 'box-shadow 0.4s',
      }}
    >
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

      <div
        style={{
          fontFamily: "'JetBrains Mono', 'Consolas', 'Courier New', monospace",
          fontSize: 72,
          fontWeight: 700,
          lineHeight: 1,
          color: weight.stability === 'ERROR' ? '#F43F5E'
            : weight.stability === 'DISCONNECTED' ? '#64748B'
            : token.colorPrimary,
          textShadow: weight.stability === 'STABLE'
            ? `0 0 20px ${token.colorPrimary}40`
            : 'none',
          transition: 'color 0.3s, text-shadow 0.3s',
          padding: '16px 0',
        }}
      >
        {formattedWeight}
      </div>

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

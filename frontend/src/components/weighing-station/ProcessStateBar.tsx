import React from 'react';
import { Tag, theme } from 'antd';
import {
  ClockCircleOutlined,
  LoadingOutlined,
  SyncOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import type { ProcessState } from '../../types/weighingStation';
import { PROCESS_STATE_LABELS } from '../../types/weighingStation';

interface ProcessStateBarProps {
  processState: ProcessState;
}

const STATE_CONFIG: Record<ProcessState, { color: string; icon: React.ReactNode }> = {
  IDLE: { color: '#64748B', icon: <ClockCircleOutlined /> },
  WEIGHING: { color: '#06B6D4', icon: <LoadingOutlined spin /> },
  STABILIZING: { color: '#F59E0B', icon: <SyncOutlined spin /> },
  COMPLETE: { color: '#10B981', icon: <CheckCircleOutlined /> },
  ERROR: { color: '#F43F5E', icon: <CloseCircleOutlined /> },
};

const ProcessStateBar: React.FC<ProcessStateBarProps> = ({ processState }) => {
  const { token } = theme.useToken();
  const config = STATE_CONFIG[processState];

  // 프로그레스 단계
  const steps: ProcessState[] = ['IDLE', 'WEIGHING', 'STABILIZING', 'COMPLETE'];
  const currentIndex = steps.indexOf(processState);
  const isError = processState === 'ERROR';

  return (
    <div
      style={{
        background: `linear-gradient(135deg, ${token.colorBgContainer}, ${config.color}08)`,
        border: `1px solid ${token.colorBorder}`,
        borderLeft: `3px solid ${config.color}`,
        borderRadius: 12,
        padding: '14px 20px',
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        transition: 'all 0.4s',
      }}
    >
      <span style={{ fontSize: 13, color: config.color, fontWeight: 600, flexShrink: 0, transition: 'color 0.4s' }}>
        프로세스 상태
      </span>

      {/* 단계 진행 바 */}
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 4 }}>
        {steps.map((step, i) => {
          const isActive = !isError && i <= currentIndex;
          const isCurrent = !isError && i === currentIndex;
          const stepColor = isError ? '#F43F5E' : isActive ? config.color : `${token.colorTextSecondary}30`;

          return (
            <React.Fragment key={step}>
              <div
                style={{
                  height: 4,
                  flex: 1,
                  borderRadius: 2,
                  background: stepColor,
                  transition: 'background 0.4s',
                  boxShadow: isCurrent ? `0 0 8px ${stepColor}60` : 'none',
                }}
              />
            </React.Fragment>
          );
        })}
      </div>

      <Tag
        icon={config.icon}
        style={{
          background: `${config.color}18`,
          color: config.color,
          border: `1px solid ${config.color}40`,
          borderRadius: 6,
          fontWeight: 600,
          fontSize: 13,
          padding: '2px 12px',
        }}
      >
        {PROCESS_STATE_LABELS[processState]}
      </Tag>
    </div>
  );
};

export default ProcessStateBar;

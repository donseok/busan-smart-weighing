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

/**
 * 프로세스 상태 바 컴포넌트의 속성 인터페이스
 *
 * @property processState - 현재 계량 프로세스 상태
 */
interface ProcessStateBarProps {
  processState: ProcessState;
}

/**
 * 프로세스 상태별 설정 (색상 및 아이콘)
 *
 * - IDLE: 대기 중 (회색, 시계 아이콘)
 * - WEIGHING: 계량 중 (시안, 로딩 스피너)
 * - STABILIZING: 안정화 중 (노란색, 동기화 스피너)
 * - COMPLETE: 완료 (녹색, 체크 아이콘)
 * - ERROR: 오류 (빨간색, 닫기 아이콘)
 */
const STATE_CONFIG: Record<ProcessState, { color: string; icon: React.ReactNode }> = {
  IDLE: { color: '#64748B', icon: <ClockCircleOutlined /> },
  WEIGHING: { color: '#06B6D4', icon: <LoadingOutlined spin /> },
  STABILIZING: { color: '#F59E0B', icon: <SyncOutlined spin /> },
  COMPLETE: { color: '#10B981', icon: <CheckCircleOutlined /> },
  ERROR: { color: '#F43F5E', icon: <CloseCircleOutlined /> },
};

/**
 * 프로세스 상태 바 컴포넌트
 *
 * 계량 프로세스의 현재 진행 상태를 시각적 진행 바와 상태 태그로 표시합니다.
 * 프로세스는 IDLE -> WEIGHING -> STABILIZING -> COMPLETE 순서로 진행되며,
 * 각 단계를 나타내는 세그먼트 바가 활성화됩니다.
 * ERROR 상태일 때는 전체 바가 빨간색으로 표시됩니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.processState - 현재 프로세스 상태
 * @returns 프로세스 상태 바 JSX
 */
const ProcessStateBar: React.FC<ProcessStateBarProps> = ({ processState }) => {
  const { token } = theme.useToken();
  /** 현재 상태에 맞는 색상/아이콘 설정 */
  const config = STATE_CONFIG[processState];

  /** 프로세스 진행 단계 순서 정의 */
  const steps: ProcessState[] = ['IDLE', 'WEIGHING', 'STABILIZING', 'COMPLETE'];
  /** 현재 상태의 단계 인덱스 (진행 바 활성화 기준) */
  const currentIndex = steps.indexOf(processState);
  /** ERROR 상태 여부 */
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
      {/* 프로세스 상태 라벨 */}
      <span style={{ fontSize: 13, color: config.color, fontWeight: 600, flexShrink: 0, transition: 'color 0.4s' }}>
        프로세스 상태
      </span>

      {/* 단계 진행 바: 각 세그먼트가 현재 진행 단계까지 활성화됨 */}
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 4 }}>
        {steps.map((step, i) => {
          /** 현재 단계 이하의 세그먼트는 활성 상태 (ERROR 제외) */
          const isActive = !isError && i <= currentIndex;
          /** 현재 진행 중인 단계 (글로우 효과 적용) */
          const isCurrent = !isError && i === currentIndex;
          /** 세그먼트 색상: ERROR=빨강, 활성=상태 색상, 비활성=투명 회색 */
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

      {/* 현재 프로세스 상태 태그 (아이콘 + 라벨) */}
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

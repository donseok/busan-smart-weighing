import React from 'react';
import { Tooltip, theme } from 'antd';
import {
  DashboardOutlined,
  DesktopOutlined,
  StopOutlined,
  WifiOutlined,
} from '@ant-design/icons';
import type { DeviceConnectionState, ConnectionStatus } from '../../types/weighingStation';

/**
 * 연결 상태 바 컴포넌트의 속성 인터페이스
 *
 * @property devices - 각 장비(계량기, 전광판, 차단기, 네트워크)의 연결 상태
 */
interface ConnectionStatusBarProps {
  devices: DeviceConnectionState;
}

/**
 * 연결 상태별 색상 매핑
 *
 * ONLINE=초록색, OFFLINE=회색, ERROR=빨간색
 */
const STATUS_COLORS: Record<ConnectionStatus, string> = {
  ONLINE: '#10B981',
  OFFLINE: '#64748B',
  ERROR: '#F43F5E',
};

/**
 * 연결 상태별 한국어 라벨 매핑
 */
const STATUS_LABELS: Record<ConnectionStatus, string> = {
  ONLINE: '연결됨',
  OFFLINE: '미연결',
  ERROR: '오류',
};

/**
 * 장비 항목 목록 정의
 *
 * 계량소에 연결된 4가지 장비(계량기, 전광판, 차단기, 네트워크)의
 * 키, 라벨, 아이콘 정보를 정의합니다.
 */
const DEVICE_ITEMS: {
  key: keyof DeviceConnectionState;
  label: string;
  icon: React.ReactNode;
}[] = [
  { key: 'scale', label: '계량기', icon: <DashboardOutlined /> },
  { key: 'display', label: '전광판', icon: <DesktopOutlined /> },
  { key: 'barrier', label: '차단기', icon: <StopOutlined /> },
  { key: 'network', label: '네트워크', icon: <WifiOutlined /> },
];

/**
 * 연결 상태 바 컴포넌트
 *
 * 계량소에 연결된 장비들(계량기, 전광판, 차단기, 네트워크)의
 * 실시간 연결 상태를 시각적으로 표시합니다.
 * 각 장비는 아이콘, 상태 표시등(LED), 라벨로 구성되며,
 * 툴팁으로 상세 상태를 확인할 수 있습니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.devices - 각 장비의 연결 상태 객체
 * @returns 연결 상태 바 JSX
 */
const ConnectionStatusBar: React.FC<ConnectionStatusBarProps> = ({ devices }) => {
  const { token } = theme.useToken();

  return (
    <div
      role="status"
      aria-live="polite"
      aria-label="장비 연결 상태"
      style={{
        background: `linear-gradient(135deg, ${token.colorBgContainer}, ${token.colorSuccess}08)`,
        border: `1px solid ${token.colorBorder}`,
        borderTop: `3px solid ${token.colorSuccess}`,
        borderRadius: 12,
        padding: '12px 20px',
        display: 'flex',
        justifyContent: 'space-around',
        alignItems: 'center',
        gap: 8,
      }}
    >
      {DEVICE_ITEMS.map((item) => {
        /** 해당 장비의 현재 연결 상태 */
        const status = devices[item.key];
        /** 상태에 따른 색상 */
        const color = STATUS_COLORS[status];

        return (
          <Tooltip key={item.key} title={`${item.label}: ${STATUS_LABELS[status]}`}>
            <div
              aria-label={`${item.label}: ${STATUS_LABELS[status]}`}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: 6,
                cursor: 'default',
              }}
            >
              {/* 장비 아이콘 (상태에 따른 배경/테두리 색상 적용) */}
              <div
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: 8,
                  background: `${color}18`,
                  border: `1px solid ${color}40`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 16,
                  color,
                  transition: 'all 0.3s',
                }}
              >
                {item.icon}
              </div>
              {/* 상태 표시등(LED)과 장비 라벨 */}
              <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                {/* ONLINE 상태일 때 글로우 효과가 적용되는 LED 점 */}
                <div
                  style={{
                    width: 6,
                    height: 6,
                    borderRadius: '50%',
                    background: color,
                    boxShadow: status === 'ONLINE' ? `0 0 6px ${color}` : 'none',
                  }}
                />
                <span style={{ fontSize: 11, color: token.colorTextSecondary }}>
                  {item.label}
                </span>
              </div>
            </div>
          </Tooltip>
        );
      })}
    </div>
  );
};

export default ConnectionStatusBar;

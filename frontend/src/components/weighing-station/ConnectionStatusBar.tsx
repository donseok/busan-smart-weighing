import React from 'react';
import { Tooltip, theme } from 'antd';
import {
  DashboardOutlined,
  DesktopOutlined,
  StopOutlined,
  WifiOutlined,
} from '@ant-design/icons';
import type { DeviceConnectionState, ConnectionStatus } from '../../types/weighingStation';

interface ConnectionStatusBarProps {
  devices: DeviceConnectionState;
}

const STATUS_COLORS: Record<ConnectionStatus, string> = {
  ONLINE: '#10B981',
  OFFLINE: '#64748B',
  ERROR: '#F43F5E',
};

const STATUS_LABELS: Record<ConnectionStatus, string> = {
  ONLINE: '연결됨',
  OFFLINE: '미연결',
  ERROR: '오류',
};

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

const ConnectionStatusBar: React.FC<ConnectionStatusBarProps> = ({ devices }) => {
  const { token } = theme.useToken();

  return (
    <div
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
        const status = devices[item.key];
        const color = STATUS_COLORS[status];

        return (
          <Tooltip key={item.key} title={`${item.label}: ${STATUS_LABELS[status]}`}>
            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: 6,
                cursor: 'default',
              }}
            >
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
              <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
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

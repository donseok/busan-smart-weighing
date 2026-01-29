import React from 'react';
import { theme } from 'antd';
import {
  CarOutlined,
  BankOutlined,
  ShoppingOutlined,
  FileTextOutlined,
  UserOutlined,
} from '@ant-design/icons';
import type { VehicleInfo } from '../../types/weighingStation';

interface VehicleInfoPanelProps {
  vehicle: VehicleInfo;
}

const VehicleInfoPanel: React.FC<VehicleInfoPanelProps> = ({ vehicle }) => {
  const { token } = theme.useToken();

  const rows = [
    { label: '차량번호', value: vehicle.plateNumber, icon: <CarOutlined /> },
    { label: '운송사', value: vehicle.companyName, icon: <BankOutlined /> },
    { label: '품목', value: vehicle.itemName, icon: <ShoppingOutlined /> },
    { label: '배차번호', value: vehicle.dispatchId ? `#${vehicle.dispatchId}` : '-', icon: <FileTextOutlined /> },
    { label: '운전자', value: vehicle.driverName, icon: <UserOutlined /> },
  ];

  const isActive = vehicle.plateNumber !== '-';

  return (
    <div
      style={{
        background: token.colorBgContainer,
        border: `1px solid ${token.colorBorder}`,
        borderLeft: `3px solid #6366F1`,
        borderRadius: 12,
        padding: '16px 20px',
      }}
    >
      <div
        style={{
          fontSize: 13,
          color: '#6366F1',
          fontWeight: 600,
          marginBottom: 12,
          letterSpacing: '0.05em',
          textTransform: 'uppercase',
        }}
      >
        차량 / 배차 정보
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {rows.map((row) => (
          <div
            key={row.label}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '6px 0',
              borderBottom: `1px solid ${token.colorBorderSecondary}`,
            }}
          >
            <span style={{ color: '#6366F1', fontSize: 14, width: 18, textAlign: 'center' }}>
              {row.icon}
            </span>
            <span style={{ color: token.colorTextSecondary, fontSize: 13, minWidth: 60, flexShrink: 0 }}>
              {row.label}
            </span>
            <span
              style={{
                color: isActive ? token.colorText : token.colorTextSecondary,
                fontSize: 14,
                fontWeight: isActive ? 600 : 400,
                flex: 1,
                textAlign: 'right',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              }}
            >
              {row.value}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default VehicleInfoPanel;

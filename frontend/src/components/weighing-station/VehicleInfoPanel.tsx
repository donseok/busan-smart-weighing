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

/**
 * 차량 정보 패널 컴포넌트의 속성 인터페이스
 *
 * @property vehicle - 현재 계량 중인 차량 정보 객체
 */
interface VehicleInfoPanelProps {
  vehicle: VehicleInfo;
}

/**
 * 차량 정보 패널 컴포넌트
 *
 * 계량소에 진입한 차량의 상세 정보를 표시합니다.
 * 차량번호, 운송사, 품목, 배차번호, 운전자 정보를 아이콘과 함께
 * 행 단위로 보여주며, 활성/비활성 상태에 따라 텍스트 스타일이 변경됩니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.vehicle - 차량 정보 데이터 객체
 * @returns 차량 정보 패널 JSX
 */
const VehicleInfoPanel: React.FC<VehicleInfoPanelProps> = ({ vehicle }) => {
  const { token } = theme.useToken();

  /** 차량 정보 행 목록 (라벨, 값, 아이콘으로 구성) */
  const rows = [
    { label: '차량번호', value: vehicle.plateNumber, icon: <CarOutlined /> },
    { label: '운송사', value: vehicle.companyName, icon: <BankOutlined /> },
    { label: '품목', value: vehicle.itemName, icon: <ShoppingOutlined /> },
    { label: '배차번호', value: vehicle.dispatchId ? `#${vehicle.dispatchId}` : '-', icon: <FileTextOutlined /> },
    { label: '운전자', value: vehicle.driverName, icon: <UserOutlined /> },
  ];

  /** 차량 정보가 활성 상태인지 판별 (차량번호가 '-'가 아니면 활성) */
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
      {/* 패널 제목 */}
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

      {/* 차량 정보 행 목록 */}
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
            {/* 행 아이콘 */}
            <span style={{ color: '#6366F1', fontSize: 14, width: 18, textAlign: 'center' }}>
              {row.icon}
            </span>
            {/* 행 라벨 */}
            <span style={{ color: token.colorTextSecondary, fontSize: 13, minWidth: 60, flexShrink: 0 }}>
              {row.label}
            </span>
            {/* 행 값 (활성 상태일 때 굵은 글씨로 표시) */}
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

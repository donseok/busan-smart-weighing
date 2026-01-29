import React from 'react';
import { Radio, theme } from 'antd';
import { ScanOutlined, EditOutlined } from '@ant-design/icons';
import type { WeighingMode } from '../../types/weighingStation';

/**
 * 모드 전환 컴포넌트의 속성 인터페이스
 *
 * @property mode - 현재 계량 모드 (AUTO 또는 MANUAL)
 * @property onChange - 모드 변경 시 호출되는 콜백 함수
 */
interface ModeToggleProps {
  mode: WeighingMode;
  onChange: (mode: WeighingMode) => void;
}

/**
 * 모드 전환 컴포넌트
 *
 * 계량소 운영 모드를 자동(AUTO LPR)과 수동(MANUAL) 사이에서 전환합니다.
 * - 자동 모드: LPR(차량번호 자동인식) 카메라로 차량번호를 자동 인식하여 계량 진행
 * - 수동 모드: 관리자가 직접 차량번호를 입력하여 계량 진행
 *
 * 현재 모드에 따라 왼쪽 테두리 색상과 LED 표시등 색상이 동적으로 변경됩니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.mode - 현재 계량 모드
 * @param props.onChange - 모드 변경 콜백 함수
 * @returns 모드 전환 패널 JSX
 */
const ModeToggle: React.FC<ModeToggleProps> = ({ mode, onChange }) => {
  const { token } = theme.useToken();

  /** 모드에 따른 강조 색상 (AUTO=테마 기본색, MANUAL=보라색) */
  const modeColor = mode === 'AUTO' ? token.colorPrimary : '#A855F7';

  return (
    <div
      style={{
        background: token.colorBgContainer,
        border: `1px solid ${token.colorBorder}`,
        borderLeft: `3px solid ${modeColor}`,
        borderRadius: 12,
        padding: '14px 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        transition: 'border-color 0.3s',
      }}
    >
      {/* 왼쪽 영역: LED 표시등 및 "계량 모드" 라벨 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        {/* 모드 활성 상태를 나타내는 LED 점 (글로우 효과 포함) */}
        <div style={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          background: modeColor,
          boxShadow: `0 0 8px ${modeColor}80`,
        }} />
        <span
          style={{
            fontSize: 13,
            color: modeColor,
            fontWeight: 600,
            letterSpacing: '0.05em',
            textTransform: 'uppercase',
          }}
        >
          계량 모드
        </span>
      </div>
      {/* 오른쪽 영역: 자동/수동 모드 라디오 버튼 그룹 */}
      <Radio.Group
        value={mode}
        onChange={(e) => onChange(e.target.value)}
        buttonStyle="solid"
        size="middle"
      >
        <Radio.Button value="AUTO">
          <ScanOutlined style={{ marginRight: 6 }} />
          자동 LPR
        </Radio.Button>
        <Radio.Button value="MANUAL">
          <EditOutlined style={{ marginRight: 6 }} />
          수동
        </Radio.Button>
      </Radio.Group>
    </div>
  );
};

export default ModeToggle;

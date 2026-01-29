import React from 'react';
import { Radio, theme } from 'antd';
import { ScanOutlined, EditOutlined } from '@ant-design/icons';
import type { WeighingMode } from '../../types/weighingStation';

interface ModeToggleProps {
  mode: WeighingMode;
  onChange: (mode: WeighingMode) => void;
}

const ModeToggle: React.FC<ModeToggleProps> = ({ mode, onChange }) => {
  const { token } = theme.useToken();

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
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
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

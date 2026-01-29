import React, { useState } from 'react';
import { Collapse, Button, Space, Switch, InputNumber, theme } from 'antd';
import {
  ExperimentOutlined,
  CameraOutlined,
  AimOutlined,
  DashboardOutlined,
  BugOutlined,
} from '@ant-design/icons';

interface SimulatorPanelProps {
  enabled: boolean;
  onToggle: (enabled: boolean) => void;
  onTriggerSensor: () => void;
  onCaptureLpr: () => void;
  onTogglePosition: () => void;
  onSetWeight: (weight: number) => void;
}

const SimulatorPanel: React.FC<SimulatorPanelProps> = ({
  enabled,
  onToggle,
  onTriggerSensor,
  onCaptureLpr,
  onTogglePosition,
  onSetWeight,
}) => {
  const { token } = theme.useToken();
  const [simWeight, setSimWeight] = useState<number>(15000);

  return (
    <Collapse
      ghost
      items={[
        {
          key: 'simulator',
          label: (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <BugOutlined style={{ color: '#F59E0B' }} />
              <span style={{ color: '#F59E0B', fontSize: 13, fontWeight: 600 }}>
                시뮬레이터
              </span>
              <span style={{
                fontSize: 9,
                fontWeight: 700,
                color: '#F59E0B',
                background: '#F59E0B18',
                border: '1px solid #F59E0B40',
                padding: '0 6px',
                borderRadius: 4,
                letterSpacing: '0.05em',
              }}>
                DEV
              </span>
            </div>
          ),
          children: (
            <div
              style={{
                background: token.colorBgContainer,
                border: `1px solid #F59E0B40`,
                borderLeft: `3px solid #F59E0B`,
                borderRadius: 12,
                padding: 16,
              }}
            >
              {/* 활성화 토글 */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginBottom: 16,
                  paddingBottom: 12,
                  borderBottom: `1px solid ${token.colorBorderSecondary}`,
                }}
              >
                <span style={{ color: token.colorTextSecondary, fontSize: 13 }}>
                  시뮬레이터 모드
                </span>
                <Switch
                  checked={enabled}
                  onChange={onToggle}
                  checkedChildren="ON"
                  unCheckedChildren="OFF"
                />
              </div>

              <Space direction="vertical" style={{ width: '100%' }} size={10}>
                <Button
                  icon={<ExperimentOutlined />}
                  onClick={onTriggerSensor}
                  disabled={!enabled}
                  block
                  size="middle"
                >
                  차량 감지
                </Button>
                <Button
                  icon={<CameraOutlined />}
                  onClick={onCaptureLpr}
                  disabled={!enabled}
                  block
                  size="middle"
                >
                  LPR 촬영
                </Button>
                <Button
                  icon={<AimOutlined />}
                  onClick={onTogglePosition}
                  disabled={!enabled}
                  block
                  size="middle"
                >
                  정위치 토글
                </Button>

                {/* 중량 설정 */}
                <div style={{ display: 'flex', gap: 8 }}>
                  <InputNumber
                    value={simWeight}
                    onChange={(v) => setSimWeight(v ?? 0)}
                    disabled={!enabled}
                    min={0}
                    max={100000}
                    step={1000}
                    addonAfter="kg"
                    style={{ flex: 1 }}
                  />
                  <Button
                    icon={<DashboardOutlined />}
                    onClick={() => onSetWeight(simWeight)}
                    disabled={!enabled}
                  >
                    설정
                  </Button>
                </div>
              </Space>
            </div>
          ),
        },
      ]}
    />
  );
};

export default SimulatorPanel;

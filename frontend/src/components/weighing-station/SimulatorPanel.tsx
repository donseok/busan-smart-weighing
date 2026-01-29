import React, { useState } from 'react';
import { Collapse, Button, Space, Switch, InputNumber, theme } from 'antd';
import {
  ExperimentOutlined,
  CameraOutlined,
  AimOutlined,
  DashboardOutlined,
  BugOutlined,
} from '@ant-design/icons';

/**
 * 시뮬레이터 패널 컴포넌트의 속성 인터페이스
 *
 * @property enabled - 시뮬레이터 활성화 여부
 * @property onToggle - 시뮬레이터 활성/비활성 토글 핸들러
 * @property onTriggerSensor - 차량 감지 센서 트리거 핸들러
 * @property onCaptureLpr - LPR 카메라 촬영 트리거 핸들러
 * @property onTogglePosition - 정위치 상태 토글 핸들러
 * @property onSetWeight - 시뮬레이션 중량 설정 핸들러
 */
interface SimulatorPanelProps {
  enabled: boolean;
  onToggle: (enabled: boolean) => void;
  onTriggerSensor: () => void;
  onCaptureLpr: () => void;
  onTogglePosition: () => void;
  onSetWeight: (weight: number) => void;
}

/**
 * 시뮬레이터 패널 컴포넌트
 *
 * 개발/테스트 환경에서 계량소 장비를 시뮬레이션할 수 있는 패널입니다.
 * 실제 장비 없이도 다음 기능을 수동으로 트리거할 수 있습니다:
 * - 차량 감지: 센서에 차량이 감지된 것으로 시뮬레이션
 * - LPR 촬영: 차량번호 자동인식 카메라 촬영 시뮬레이션
 * - 정위치 토글: 차량이 계량대 정위치에 있는 상태 전환
 * - 중량 설정: 계량기에서 측정되는 중량값을 직접 설정
 *
 * Collapse 컴포넌트로 접을 수 있으며, DEV 배지로 개발용임을 표시합니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.enabled - 시뮬레이터 활성 상태
 * @param props.onToggle - 활성화 토글 콜백
 * @param props.onTriggerSensor - 센서 트리거 콜백
 * @param props.onCaptureLpr - LPR 촬영 콜백
 * @param props.onTogglePosition - 정위치 토글 콜백
 * @param props.onSetWeight - 중량 설정 콜백
 * @returns 시뮬레이터 패널 JSX
 */
const SimulatorPanel: React.FC<SimulatorPanelProps> = ({
  enabled,
  onToggle,
  onTriggerSensor,
  onCaptureLpr,
  onTogglePosition,
  onSetWeight,
}) => {
  const { token } = theme.useToken();
  /** 시뮬레이션 중량 입력값 (기본값 15,000 kg) */
  const [simWeight, setSimWeight] = useState<number>(15000);

  return (
    <Collapse
      ghost
      items={[
        {
          key: 'simulator',
          /* 접이식 패널 헤더: 벌레 아이콘, "시뮬레이터" 라벨, DEV 배지 */
          label: (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <BugOutlined style={{ color: '#F59E0B' }} />
              <span style={{ color: '#F59E0B', fontSize: 13, fontWeight: 600 }}>
                시뮬레이터
              </span>
              {/* 개발 전용 기능임을 나타내는 DEV 배지 */}
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
              {/* 시뮬레이터 활성화 토글 스위치 */}
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
                {/* 차량 감지 시뮬레이션 버튼 */}
                <Button
                  icon={<ExperimentOutlined />}
                  onClick={onTriggerSensor}
                  disabled={!enabled}
                  block
                  size="middle"
                >
                  차량 감지
                </Button>
                {/* LPR 카메라 촬영 시뮬레이션 버튼 */}
                <Button
                  icon={<CameraOutlined />}
                  onClick={onCaptureLpr}
                  disabled={!enabled}
                  block
                  size="middle"
                >
                  LPR 촬영
                </Button>
                {/* 정위치 상태 토글 시뮬레이션 버튼 */}
                <Button
                  icon={<AimOutlined />}
                  onClick={onTogglePosition}
                  disabled={!enabled}
                  block
                  size="middle"
                >
                  정위치 토글
                </Button>

                {/* 시뮬레이션 중량 직접 설정 영역 */}
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

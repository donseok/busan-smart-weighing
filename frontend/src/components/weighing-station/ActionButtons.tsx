import React from 'react';
import { Button, Space, Popconfirm, theme } from 'antd';
import {
  ReloadOutlined,
  ClearOutlined,
  UnlockOutlined,
} from '@ant-design/icons';

/**
 * 동작 버튼 컴포넌트의 속성 인터페이스
 *
 * @property onReWeigh - 재계량 버튼 클릭 핸들러 (선택적, 미구현)
 * @property onReset - 초기화 버튼 클릭 핸들러
 * @property onBarrierOpen - 차단기 열기 버튼 클릭 핸들러
 */
interface ActionButtonsProps {
  onReWeigh?: () => void;
  onReset: () => void;
  onBarrierOpen: () => void;
}

/**
 * 동작 버튼 컴포넌트
 *
 * 계량소 관제 화면에서 관리자가 수행할 수 있는 작업 제어 버튼들을 제공합니다.
 * - 초기화: 현재 계량 프로세스를 초기 상태로 되돌림 (확인 팝업 포함)
 * - 차단기 열기: 계량소 차단기를 수동으로 개방
 * - 재계량: 동일 차량에 대해 재측정 수행 (구현 예정)
 *
 * @param props - 컴포넌트 속성
 * @param props.onReset - 초기화 실행 콜백 함수
 * @param props.onBarrierOpen - 차단기 열기 실행 콜백 함수
 * @returns 동작 버튼 패널 JSX
 */
const ActionButtons: React.FC<ActionButtonsProps> = ({
  onReset,
  onBarrierOpen,
}) => {
  const { token } = theme.useToken();

  return (
    <div
      style={{
        background: token.colorBgContainer,
        border: `1px solid ${token.colorBorder}`,
        borderLeft: `3px solid ${token.colorWarning}`,
        borderRadius: 12,
        padding: '16px 20px',
      }}
    >
      {/* 패널 제목 */}
      <div
        style={{
          fontSize: 13,
          color: token.colorWarning,
          fontWeight: 600,
          marginBottom: 14,
          letterSpacing: '0.05em',
          textTransform: 'uppercase',
        }}
      >
        작업 제어
      </div>
      <Space direction="vertical" style={{ width: '100%' }} size={10}>
        {/* 초기화 버튼: 클릭 시 확인 팝업을 표시한 후 프로세스 초기화 */}
        <Popconfirm
          title="프로세스를 초기화하시겠습니까?"
          description="현재 계량 상태가 초기화됩니다."
          onConfirm={onReset}
          okText="초기화"
          cancelText="취소"
        >
          <Button icon={<ClearOutlined />} block>
            초기화
          </Button>
        </Popconfirm>

        {/* 차단기 수동 열기 버튼 */}
        <Button
          icon={<UnlockOutlined />}
          onClick={onBarrierOpen}
          block
          style={{
            borderColor: '#F59E0B',
            color: '#F59E0B',
          }}
        >
          차단기 열기
        </Button>

        {/* 재계량 버튼 (현재 비활성화 - 구현 예정) */}
        <Button
          icon={<ReloadOutlined />}
          block
          disabled
          title="재계량 (구현 예정)"
        >
          재계량
        </Button>
      </Space>
    </div>
  );
};

export default ActionButtons;

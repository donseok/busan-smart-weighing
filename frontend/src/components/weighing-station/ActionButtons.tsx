import React from 'react';
import { Button, Space, Popconfirm, theme } from 'antd';
import {
  ReloadOutlined,
  ClearOutlined,
  UnlockOutlined,
} from '@ant-design/icons';

interface ActionButtonsProps {
  onReWeigh?: () => void;
  onReset: () => void;
  onBarrierOpen: () => void;
}

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

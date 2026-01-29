/**
 * @fileoverview 빈 상태 표시 공통 컴포넌트
 *
 * 데이터가 없을 때 사용자에게 안내 메시지와 액션 버튼을 제공하는
 * 재사용 가능한 빈 상태 컴포넌트입니다.
 *
 * @module components/EmptyState
 */
import React from 'react';
import { Empty, Button, Typography } from 'antd';
import type { ReactNode } from 'react';

/**
 * EmptyState 컴포넌트 속성 인터페이스
 *
 * @property icon - 커스텀 아이콘 (기본값: Ant Design 기본 빈 이미지)
 * @property title - 제목 메시지 (기본값: '데이터가 없습니다')
 * @property description - 부가 설명 텍스트
 * @property actionText - 액션 버튼 텍스트
 * @property onAction - 액션 버튼 클릭 핸들러
 */
interface EmptyStateProps {
  icon?: ReactNode;
  title?: string;
  description?: string;
  actionText?: string;
  onAction?: () => void;
}

/**
 * 빈 상태 표시 컴포넌트
 *
 * 테이블이나 목록에 데이터가 없을 때 안내 메시지를 표시합니다.
 * 선택적으로 액션 버튼을 제공하여 데이터 등록 등의 동작을 유도할 수 있습니다.
 *
 * @param props - 컴포넌트 속성
 * @returns 빈 상태 안내 JSX
 */
const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title = '데이터가 없습니다',
  description,
  actionText,
  onAction,
}) => {
  return (
    <Empty
      image={icon || Empty.PRESENTED_IMAGE_SIMPLE}
      description={
        <div>
          <Typography.Text strong style={{ fontSize: 15, display: 'block', marginBottom: 4 }}>
            {title}
          </Typography.Text>
          {description && (
            <Typography.Text type="secondary" style={{ fontSize: 13 }}>
              {description}
            </Typography.Text>
          )}
        </div>
      }
    >
      {actionText && onAction && (
        <Button type="primary" onClick={onAction}>
          {actionText}
        </Button>
      )}
    </Empty>
  );
};

export default EmptyState;

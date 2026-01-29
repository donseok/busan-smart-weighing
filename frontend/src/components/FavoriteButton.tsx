import React, { useState, useEffect } from 'react';
import { Button, message, Tooltip } from 'antd';
import { StarOutlined, StarFilled } from '@ant-design/icons';

/**
 * 즐겨찾기 버튼 컴포넌트의 속성 인터페이스
 *
 * @property favoriteType - 즐겨찾기 대상 유형 (메뉴, 배차, 차량, 운송사, 계량대)
 * @property targetId - 즐겨찾기 대상의 고유 ID (데이터 항목용)
 * @property targetPath - 즐겨찾기 대상의 경로 (메뉴 항목용)
 * @property displayName - 즐겨찾기 목록에 표시될 이름
 * @property icon - 즐겨찾기 항목 아이콘 식별자
 * @property size - 버튼 크기 (기본값: 'small')
 * @property showText - 텍스트 라벨 표시 여부 (기본값: false)
 */
interface FavoriteButtonProps {
  favoriteType: 'MENU' | 'DISPATCH' | 'VEHICLE' | 'COMPANY' | 'SCALE';
  targetId?: number;
  targetPath?: string;
  displayName: string;
  icon?: string;
  size?: 'small' | 'middle' | 'large';
  showText?: boolean;
}

/**
 * 즐겨찾기 버튼 컴포넌트
 *
 * 메뉴, 배차, 차량, 운송사, 계량대 등 다양한 항목을 즐겨찾기에
 * 추가/해제할 수 있는 토글 버튼입니다.
 * 마운트 시 API를 호출하여 현재 즐겨찾기 상태를 확인하고,
 * 클릭 시 즐겨찾기 추가/해제를 토글합니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.favoriteType - 즐겨찾기 대상 유형
 * @param props.targetId - 대상 ID
 * @param props.targetPath - 대상 경로
 * @param props.displayName - 표시 이름
 * @param props.icon - 아이콘 식별자
 * @param props.size - 버튼 크기
 * @param props.showText - 텍스트 표시 여부
 * @returns 즐겨찾기 토글 버튼 JSX
 */
const FavoriteButton: React.FC<FavoriteButtonProps> = ({
  favoriteType,
  targetId,
  targetPath,
  displayName,
  icon,
  size = 'small',
  showText = false,
}) => {
  /** 현재 즐겨찾기 등록 여부 */
  const [isFavorite, setIsFavorite] = useState(false);
  /** API 호출 중 로딩 상태 */
  const [loading, setLoading] = useState(false);

  /** 대상 항목 변경 시 즐겨찾기 상태를 API로 확인 */
  useEffect(() => {
    checkFavorite();
  }, [favoriteType, targetId, targetPath]);

  /**
   * 현재 항목의 즐겨찾기 등록 여부를 서버에서 확인
   *
   * POST /api/v1/favorites/check API를 호출하여
   * 해당 항목이 즐겨찾기에 등록되어 있는지 확인합니다.
   */
  const checkFavorite = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/v1/favorites/check', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          favorite_type: favoriteType,
          target_id: targetId,
          target_path: targetPath,
        }),
      });
      const result = await response.json();
      if (result.success) {
        setIsFavorite(result.data.is_favorite);
      }
    } catch (error) {
      console.error('Failed to check favorite:', error);
    }
  };

  /**
   * 즐겨찾기 추가/해제 토글 핸들러
   *
   * POST /api/v1/favorites/toggle API를 호출하여
   * 즐겨찾기 상태를 반전시킵니다.
   */
  const handleToggle = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/v1/favorites/toggle', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          favorite_type: favoriteType,
          target_id: targetId,
          target_path: targetPath,
          display_name: displayName,
          icon: icon,
        }),
      });
      const result = await response.json();
      if (result.success) {
        setIsFavorite(!isFavorite);
        message.success(result.message);
      } else {
        message.error(result.error?.message || '처리에 실패했습니다');
      }
    } catch (error) {
      message.error('처리에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Tooltip title={isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'}>
      <Button
        type="text"
        size={size}
        /* 즐겨찾기 등록 상태에 따라 채워진/빈 별 아이콘 전환 */
        icon={isFavorite ? <StarFilled style={{ color: '#faad14' }} /> : <StarOutlined />}
        onClick={handleToggle}
        loading={loading}
      >
        {showText && (isFavorite ? '즐겨찾기 해제' : '즐겨찾기')}
      </Button>
    </Tooltip>
  );
};

export default FavoriteButton;

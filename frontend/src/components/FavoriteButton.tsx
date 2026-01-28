import React, { useState, useEffect } from 'react';
import { Button, message, Tooltip } from 'antd';
import { StarOutlined, StarFilled } from '@ant-design/icons';

interface FavoriteButtonProps {
  favoriteType: 'MENU' | 'DISPATCH' | 'VEHICLE' | 'COMPANY' | 'SCALE';
  targetId?: number;
  targetPath?: string;
  displayName: string;
  icon?: string;
  size?: 'small' | 'middle' | 'large';
  showText?: boolean;
}

const FavoriteButton: React.FC<FavoriteButtonProps> = ({
  favoriteType,
  targetId,
  targetPath,
  displayName,
  icon,
  size = 'small',
  showText = false,
}) => {
  const [isFavorite, setIsFavorite] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    checkFavorite();
  }, [favoriteType, targetId, targetPath]);

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

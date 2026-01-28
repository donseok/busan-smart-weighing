import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { List, Button, Empty, Spin, Tag, Popconfirm, message } from 'antd';
import {
  StarFilled,
  DeleteOutlined,
  MenuOutlined,
  CarOutlined,
  BankOutlined,
  ToolOutlined,
  FileOutlined,
} from '@ant-design/icons';
import { Favorite, ApiResponse } from '../types';

interface FavoritesListProps {
  onNavigate?: () => void;
}

const iconMap: Record<string, React.ReactNode> = {
  MENU: <MenuOutlined />,
  DISPATCH: <FileOutlined />,
  VEHICLE: <CarOutlined />,
  COMPANY: <BankOutlined />,
  SCALE: <ToolOutlined />,
};

const typeColorMap: Record<string, string> = {
  MENU: 'blue',
  DISPATCH: 'green',
  VEHICLE: 'orange',
  COMPANY: 'purple',
  SCALE: 'cyan',
};

// Helper to convert snake_case response to camelCase for internal use
const mapFavorite = (item: Favorite) => ({
  favoriteId: item.favorite_id,
  favoriteType: item.favorite_type,
  favoriteTypeDesc: item.favorite_type_desc,
  targetId: item.target_id,
  targetPath: item.target_path,
  displayName: item.display_name,
  icon: item.icon,
  sortOrder: item.sort_order,
  createdAt: item.created_at,
});

interface MappedFavorite {
  favoriteId: number;
  favoriteType: string;
  favoriteTypeDesc: string;
  targetId?: number;
  targetPath?: string;
  displayName: string;
  icon?: string;
  sortOrder?: number;
  createdAt: string;
}

const FavoritesList: React.FC<FavoritesListProps> = ({ onNavigate }) => {
  const [favorites, setFavorites] = useState<MappedFavorite[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const fetchFavorites = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/v1/favorites', {
        headers: { Authorization: `Bearer ${token}` },
      });
      const result: ApiResponse<Favorite[]> = await response.json();
      if (result.success) {
        setFavorites(result.data.map(mapFavorite));
      }
    } catch (error) {
      console.error('Failed to fetch favorites:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFavorites();
  }, []);

  const handleClick = (favorite: MappedFavorite) => {
    if (favorite.favoriteType === 'MENU' && favorite.targetPath) {
      navigate(favorite.targetPath);
      onNavigate?.();
    } else if (favorite.targetId) {
      // 데이터 타입에 따른 상세 페이지 이동
      switch (favorite.favoriteType) {
        case 'DISPATCH':
          navigate(`/dispatch?id=${favorite.targetId}`);
          break;
        case 'VEHICLE':
          navigate(`/master/vehicles?id=${favorite.targetId}`);
          break;
        case 'COMPANY':
          navigate(`/master/companies?id=${favorite.targetId}`);
          break;
        case 'SCALE':
          navigate(`/master/scales?id=${favorite.targetId}`);
          break;
      }
      onNavigate?.();
    }
  };

  const handleDelete = async (favoriteId: number) => {
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch(`/api/v1/favorites/${favoriteId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      const result = await response.json();
      if (result.success) {
        message.success('즐겨찾기에서 삭제되었습니다');
        fetchFavorites();
      }
    } catch (error) {
      message.error('삭제에 실패했습니다');
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 20 }}>
        <Spin size="small" />
      </div>
    );
  }

  if (favorites.length === 0) {
    return (
      <Empty
        image={Empty.PRESENTED_IMAGE_SIMPLE}
        description="즐겨찾기가 없습니다"
        style={{ padding: 20 }}
      />
    );
  }

  return (
    <List
      size="small"
      dataSource={favorites}
      renderItem={(item) => (
        <List.Item
          style={{ padding: '8px 12px', cursor: 'pointer' }}
          actions={[
            <Popconfirm
              key="delete"
              title="즐겨찾기에서 삭제하시겠습니까?"
              onConfirm={(e) => {
                e?.stopPropagation();
                handleDelete(item.favoriteId);
              }}
              okText="삭제"
              cancelText="취소"
            >
              <Button
                type="text"
                size="small"
                icon={<DeleteOutlined />}
                onClick={(e) => e.stopPropagation()}
                danger
              />
            </Popconfirm>,
          ]}
          onClick={() => handleClick(item)}
        >
          <List.Item.Meta
            avatar={<StarFilled style={{ color: '#faad14', fontSize: 14 }} />}
            title={
              <span style={{ fontSize: 13 }}>
                {iconMap[item.favoriteType]} {item.displayName}
              </span>
            }
            description={
              <Tag color={typeColorMap[item.favoriteType]} style={{ fontSize: 10 }}>
                {item.favoriteTypeDesc}
              </Tag>
            }
          />
        </List.Item>
      )}
    />
  );
};

export default FavoritesList;

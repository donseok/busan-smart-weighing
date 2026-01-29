import React, { useEffect, useState } from 'react';
// useNavigate no longer needed — navigation handled by openTab
import { List, Button, Empty, Spin, Tag, Popconfirm, message } from 'antd';
import { useTab } from '../context/TabContext';
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

/**
 * 즐겨찾기 목록 컴포넌트의 속성 인터페이스
 *
 * @property onNavigate - 즐겨찾기 항목 클릭으로 이동 후 호출되는 콜백 (팝오버 닫기 등)
 */
interface FavoritesListProps {
  onNavigate?: () => void;
}

/**
 * 즐겨찾기 유형별 아이콘 매핑
 *
 * 각 즐겨찾기 유형(메뉴, 배차, 차량, 운송사, 계량대)에
 * 대응하는 Ant Design 아이콘을 정의합니다.
 */
const iconMap: Record<string, React.ReactNode> = {
  MENU: <MenuOutlined />,
  DISPATCH: <FileOutlined />,
  VEHICLE: <CarOutlined />,
  COMPANY: <BankOutlined />,
  SCALE: <ToolOutlined />,
};

/**
 * 즐겨찾기 유형별 태그 색상 매핑
 */
const typeColorMap: Record<string, string> = {
  MENU: 'blue',
  DISPATCH: 'green',
  VEHICLE: 'orange',
  COMPANY: 'purple',
  SCALE: 'cyan',
};

/**
 * API 응답의 snake_case 필드를 camelCase로 변환하는 헬퍼 함수
 *
 * 서버 API는 snake_case를 사용하고 프론트엔드는 camelCase를 사용하므로,
 * 응답 데이터를 내부 사용에 맞게 변환합니다.
 *
 * @param item - snake_case 형식의 즐겨찾기 API 응답 데이터
 * @returns camelCase로 변환된 즐겨찾기 객체
 */
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

/**
 * 프론트엔드 내부에서 사용하는 camelCase 즐겨찾기 인터페이스
 */
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

/**
 * 즐겨찾기 목록 컴포넌트
 *
 * 사용자가 등록한 즐겨찾기 항목들을 리스트 형태로 표시합니다.
 * 각 항목은 유형별 아이콘과 색상 태그로 구분되며,
 * 클릭 시 해당 메뉴/페이지로 탭 이동을 수행합니다.
 * 삭제 버튼으로 즐겨찾기를 해제할 수 있습니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.onNavigate - 항목 클릭 후 네비게이션 완료 콜백
 * @returns 즐겨찾기 목록 JSX
 */
const FavoritesList: React.FC<FavoritesListProps> = ({ onNavigate }) => {
  /** 즐겨찾기 항목 목록 상태 */
  const [favorites, setFavorites] = useState<MappedFavorite[]>([]);
  /** API 호출 중 로딩 상태 */
  const [loading, setLoading] = useState(false);
  /** 탭 컨텍스트에서 탭 열기 함수 가져오기 */
  const { openTab } = useTab();

  /**
   * 즐겨찾기 목록을 서버에서 조회
   *
   * GET /api/v1/favorites API를 호출하여 현재 사용자의
   * 전체 즐겨찾기 목록을 가져옵니다.
   */
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

  /** 컴포넌트 마운트 시 즐겨찾기 목록 최초 조회 */
  useEffect(() => {
    fetchFavorites();
  }, []);

  /**
   * 즐겨찾기 항목 클릭 핸들러
   *
   * 즐겨찾기 유형에 따라 적절한 탭을 열어 해당 페이지로 이동합니다.
   * - MENU 유형: targetPath 경로로 직접 이동
   * - 데이터 유형(DISPATCH, VEHICLE 등): 해당 관리 페이지로 이동
   *
   * @param favorite - 클릭된 즐겨찾기 항목
   */
  const handleClick = (favorite: MappedFavorite) => {
    if (favorite.favoriteType === 'MENU' && favorite.targetPath) {
      openTab(favorite.targetPath);
      onNavigate?.();
    } else if (favorite.targetId) {
      // 데이터 타입에 따른 상세 페이지 이동 (탭으로 열기)
      switch (favorite.favoriteType) {
        case 'DISPATCH':
          openTab('/dispatch');
          break;
        case 'VEHICLE':
          openTab('/master/vehicles');
          break;
        case 'COMPANY':
          openTab('/master/companies');
          break;
        case 'SCALE':
          openTab('/master/scales');
          break;
      }
      onNavigate?.();
    }
  };

  /**
   * 즐겨찾기 삭제 핸들러
   *
   * DELETE /api/v1/favorites/{id} API를 호출하여 즐겨찾기를 삭제하고,
   * 성공 시 목록을 새로 조회합니다.
   *
   * @param favoriteId - 삭제할 즐겨찾기 ID
   */
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

  /* 로딩 중 스피너 표시 */
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 20 }}>
        <Spin size="small" />
      </div>
    );
  }

  /* 즐겨찾기가 없을 때 빈 상태 표시 */
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
            /* 즐겨찾기 삭제 버튼 (확인 팝업 포함) */
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
            /* 별 아이콘으로 즐겨찾기 항목 표시 */
            avatar={<StarFilled style={{ color: '#faad14', fontSize: 14 }} />}
            title={
              <span style={{ fontSize: 13 }}>
                {iconMap[item.favoriteType]} {item.displayName}
              </span>
            }
            /* 즐겨찾기 유형 태그 (색상으로 구분) */
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

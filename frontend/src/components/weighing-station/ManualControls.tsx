import React, { useState } from 'react';
import { Input, Select, Button, Space, theme } from 'antd';
import { SearchOutlined, CheckCircleOutlined } from '@ant-design/icons';
import type { DispatchSearchResult, WeighingMode } from '../../types/weighingStation';

/**
 * 수동 조작 컴포넌트의 속성 인터페이스
 *
 * @property mode - 현재 계량 모드 (AUTO 또는 MANUAL)
 * @property searchResults - 차량번호 검색 결과 배차 목록
 * @property selectedDispatchId - 현재 선택된 배차 ID
 * @property searchLoading - 검색 중 로딩 상태
 * @property onSearch - 차량번호 검색 실행 핸들러
 * @property onSelectDispatch - 배차 선택 핸들러
 * @property onConfirmWeight - 중량 확인 및 계량 시작 핸들러
 */
interface ManualControlsProps {
  mode: WeighingMode;
  searchResults: DispatchSearchResult[];
  selectedDispatchId: number | null;
  searchLoading: boolean;
  onSearch: (plateNumber: string) => void;
  onSelectDispatch: (id: number | null) => void;
  onConfirmWeight: () => void;
}

/**
 * 수동 조작 컴포넌트
 *
 * 수동 계량 모드에서 관리자가 차량번호를 직접 검색하고,
 * 해당 배차를 선택한 후 중량 확인을 수행할 수 있는 제어 패널입니다.
 * 자동(AUTO) 모드에서는 모든 컨트롤이 비활성화됩니다.
 *
 * 워크플로우: 차량번호 입력 -> 검색 -> 배차 선택 -> 중량 확인 시작
 *
 * @param props - 컴포넌트 속성
 * @param props.mode - 현재 계량 모드
 * @param props.searchResults - 검색된 배차 목록
 * @param props.selectedDispatchId - 선택된 배차 ID
 * @param props.searchLoading - 검색 로딩 상태
 * @param props.onSearch - 검색 콜백 함수
 * @param props.onSelectDispatch - 배차 선택 콜백 함수
 * @param props.onConfirmWeight - 중량 확인 콜백 함수
 * @returns 수동 조작 패널 JSX
 */
const ManualControls: React.FC<ManualControlsProps> = ({
  mode,
  searchResults,
  selectedDispatchId,
  searchLoading,
  onSearch,
  onSelectDispatch,
  onConfirmWeight,
}) => {
  const { token } = theme.useToken();
  /** 차량번호 검색 입력값 상태 */
  const [searchPlate, setSearchPlate] = useState('');
  /** MANUAL 모드가 아닐 때 전체 컨트롤 비활성화 */
  const disabled = mode !== 'MANUAL';

  /** 차량번호 검색 실행 */
  const handleSearch = () => {
    onSearch(searchPlate);
  };

  /** Enter 키 입력 시 검색 실행 */
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  return (
    <div
      style={{
        background: token.colorBgContainer,
        border: `1px solid ${token.colorBorder}`,
        borderLeft: `3px solid #818CF8`,
        borderRadius: 12,
        padding: '16px 20px',
        /* AUTO 모드일 때 투명도를 낮추어 비활성화 상태를 시각적으로 표현 */
        opacity: disabled ? 0.45 : 1,
        transition: 'opacity 0.3s',
      }}
    >
      {/* 패널 제목 */}
      <div
        style={{
          fontSize: 13,
          color: '#818CF8',
          fontWeight: 600,
          marginBottom: 14,
          letterSpacing: '0.05em',
          textTransform: 'uppercase',
        }}
      >
        수동 계량 제어
      </div>

      <Space direction="vertical" style={{ width: '100%' }} size={12}>
        {/* 차량번호 검색 입력 필드 */}
        <Input.Search
          placeholder="차량번호 입력"
          value={searchPlate}
          onChange={(e) => setSearchPlate(e.target.value)}
          onSearch={handleSearch}
          onKeyDown={handleKeyDown}
          enterButton={
            <Button icon={<SearchOutlined />} loading={searchLoading} disabled={disabled}>
              검색
            </Button>
          }
          disabled={disabled}
          allowClear
        />

        {/* 배차 선택 드롭다운 (검색 결과가 없으면 비활성화) */}
        <Select
          placeholder="배차 선택"
          style={{ width: '100%' }}
          value={selectedDispatchId}
          onChange={(val) => onSelectDispatch(val)}
          disabled={disabled || searchResults.length === 0}
          allowClear
          options={searchResults.map((d) => ({
            value: d.dispatchId,
            label: `[#${d.dispatchId}] ${d.itemName} - ${d.companyName}`,
          }))}
          notFoundContent="검색 결과가 없습니다"
        />

        {/* 중량 확인 버튼 (배차가 선택되어야 활성화) */}
        <Button
          type="primary"
          icon={<CheckCircleOutlined />}
          onClick={onConfirmWeight}
          disabled={disabled || !selectedDispatchId}
          block
          size="large"
          style={{ fontWeight: 600 }}
        >
          중량 확인 및 계량 시작
        </Button>
      </Space>
    </div>
  );
};

export default ManualControls;

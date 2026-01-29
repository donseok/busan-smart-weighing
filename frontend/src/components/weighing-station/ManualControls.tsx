import React, { useState } from 'react';
import { Input, Select, Button, Space, theme } from 'antd';
import { SearchOutlined, CheckCircleOutlined } from '@ant-design/icons';
import type { DispatchSearchResult, WeighingMode } from '../../types/weighingStation';

interface ManualControlsProps {
  mode: WeighingMode;
  searchResults: DispatchSearchResult[];
  selectedDispatchId: number | null;
  searchLoading: boolean;
  onSearch: (plateNumber: string) => void;
  onSelectDispatch: (id: number | null) => void;
  onConfirmWeight: () => void;
}

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
  const [searchPlate, setSearchPlate] = useState('');
  const disabled = mode !== 'MANUAL';

  const handleSearch = () => {
    onSearch(searchPlate);
  };

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
        opacity: disabled ? 0.45 : 1,
        transition: 'opacity 0.3s',
      }}
    >
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
        {/* 차량번호 검색 */}
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

        {/* 배차 선택 */}
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

        {/* 중량 확인 버튼 */}
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

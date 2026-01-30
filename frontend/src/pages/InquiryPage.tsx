/**
 * 계량 조회 페이지 컴포넌트
 *
 * 과거 계량 기록을 상세 검색하고 조회하는 페이지입니다.
 * 기간, 차량번호, 품목유형, 업체, 계량상태 등
 * 다양한 조건의 필터 검색과 페이지네이션을 지원하며,
 * 행 클릭 시 계량 상세정보 모달을 통해 전체 데이터를 확인할 수 있습니다.
 *
 * @returns 계량 조회 페이지 JSX
 */
import React, { useState, useCallback } from 'react';
import {
  Typography,
  Button,
  Space,
  Select,
  DatePicker,
  Input,
  Card,
  Modal,
  Descriptions,
  Tag,
  Pagination,
} from 'antd';
import {
  SearchOutlined,
  ClearOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import SortableTable from '../components/SortableTable';
import { TablePageLayout, FixedArea, ScrollArea } from '../components/TablePageLayout';
import apiClient from '../api/client';
import type { WeighingRecord } from '../types';
import dayjs, { type Dayjs } from 'dayjs';
import { colors } from '../theme/themeConfig';

const { RangePicker } = DatePicker;

/** 계량 상태별 태그 색상 매핑 */
const statusColors: Record<string, string> = {
  IN_PROGRESS: colors.warning,
  COMPLETED: colors.success,
  RE_WEIGHING: '#F59E0B',
  ERROR: colors.error,
};

const statusLabels: Record<string, string> = {
  IN_PROGRESS: '진행중',
  COMPLETED: '완료',
  RE_WEIGHING: '재계량',
  ERROR: '오류',
};

const modeLabels: Record<string, string> = {
  LPR_AUTO: 'LPR 자동',
  MOBILE_OTP: '모바일 OTP',
  MANUAL: '수동',
  RE_WEIGH: '재계량',
};

const stepLabels: Record<string, string> = {
  FIRST_WEIGH: '1차 계량',
  SECOND_WEIGH: '2차 계량',
  COMPLETED: '완료',
};

const statusOptions = Object.entries(statusLabels).map(([value, label]) => ({
  value,
  label,
}));

const modeOptions = Object.entries(modeLabels).map(([value, label]) => ({
  value,
  label,
}));

interface SearchFilters {
  dateRange: [Dayjs | null, Dayjs | null] | null;
  lprPlateNumber: string;
  weighingMode: string | undefined;
  status: string | undefined;
}

const initialFilters: SearchFilters = {
  dateRange: null,
  lprPlateNumber: '',
  weighingMode: undefined,
  status: undefined,
};

const InquiryPage: React.FC = () => {
  const [filters, setFilters] = useState<SearchFilters>({ ...initialFilters }); // 검색 필터
  const [data, setData] = useState<WeighingRecord[]>([]);       // 조회 결과 목록
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);               // 조회 실행 여부
  const [totalElements, setTotalElements] = useState(0);         // 전체 결과 수
  const [currentPage, setCurrentPage] = useState(1);             // 현재 페이지 번호
  const [pageSize, setPageSize] = useState(20);                  // 페이지당 건수

  // 계량 상세정보 모달 상태
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<WeighingRecord | null>(
    null,
  );

  const fetchData = useCallback(
    async (page = 1, size = 20) => {
      setLoading(true);
      try {
        const params: Record<string, unknown> = {
          page: page - 1,
          size,
        };

        if (filters.dateRange?.[0]) {
          params.dateFrom = filters.dateRange[0].format('YYYY-MM-DD');
        }
        if (filters.dateRange?.[1]) {
          params.dateTo = filters.dateRange[1].format('YYYY-MM-DD');
        }
        if (filters.weighingMode) {
          params.weighingMode = filters.weighingMode;
        }
        if (filters.status) {
          params.status = filters.status;
        }
        if (filters.lprPlateNumber.trim()) {
          params.lprPlateNumber = filters.lprPlateNumber.trim();
        }

        const res = await apiClient.get('/weighings', { params });
        const pageData = res.data.data;
        setData(pageData.content || []);
        setTotalElements(pageData.totalElements || 0);
        setSearched(true);
      } catch {
        /* ignore */
      }
      setLoading(false);
    },
    [filters],
  );

  const handleSearch = () => {
    setCurrentPage(1);
    fetchData(1, pageSize);
  };

  const handleReset = () => {
    setFilters({ ...initialFilters });
    setData([]);
    setSearched(false);
    setTotalElements(0);
    setCurrentPage(1);
  };

  const handlePageChange = (page: number, size: number) => {
    setCurrentPage(page);
    setPageSize(size);
    fetchData(page, size);
  };

  const openDetail = (record: WeighingRecord) => {
    setSelectedRecord(record);
    setDetailOpen(true);
  };

  /** 검색 입력 필드에서 Enter 키 누를 시 조회 실행 */
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const columns: ColumnsType<WeighingRecord> = [
    { title: 'ID', dataIndex: 'weighingId', width: 80 },
    { title: '배차ID', dataIndex: 'dispatchId', width: 100 },
    {
      title: '차량번호',
      dataIndex: 'lprPlateNumber',
      width: 110,
      render: (v?: string) => v || '-',
    },
    {
      title: '계량방식',
      dataIndex: 'weighingMode',
      width: 120,
      render: (v: string) => modeLabels[v] || v,
    },
    {
      title: '계량단계',
      dataIndex: 'weighingStep',
      width: 110,
      render: (v: string) => stepLabels[v] || v,
    },
    {
      title: '총중량(kg)',
      dataIndex: 'grossWeight',
      width: 120,
      align: 'right',
      render: (v?: number) => v?.toLocaleString() ?? '-',
    },
    {
      title: '공차중량(kg)',
      dataIndex: 'tareWeight',
      width: 130,
      align: 'right',
      render: (v?: number) => v?.toLocaleString() ?? '-',
    },
    {
      title: '순중량(kg)',
      dataIndex: 'netWeight',
      width: 120,
      align: 'right',
      render: (v?: number) => (
        <span style={{ color: colors.primary, fontWeight: 600 }}>
          {v?.toLocaleString() ?? '-'}
        </span>
      ),
    },
    {
      title: '상태',
      dataIndex: 'weighingStatus',
      width: 90,
      render: (v: string) => (
        <Tag color={statusColors[v]}>{statusLabels[v] || v}</Tag>
      ),
    },
    {
      title: '일시',
      dataIndex: 'createdAt',
      width: 160,
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
  ];

  return (
    <TablePageLayout>
      <FixedArea>
      <Typography.Title level={4}>계량 조회</Typography.Title>

      {/* Search Condition Card */}
      <Card
        size="small"
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '16px 24px' } }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <Space size={4}>
            <Typography.Text strong>기간</Typography.Text>
            <RangePicker
              value={filters.dateRange}
              onChange={(dates) => setFilters((prev) => ({ ...prev, dateRange: dates }))}
              placeholder={['시작일', '종료일']}
              allowClear
              style={{ width: 240 }}
            />
          </Space>
          <Space size={4}>
            <Typography.Text strong>차량번호</Typography.Text>
            <Input
              value={filters.lprPlateNumber}
              onChange={(e) => setFilters((prev) => ({ ...prev, lprPlateNumber: e.target.value }))}
              onKeyDown={handleKeyDown}
              placeholder="차량번호"
              allowClear
              style={{ width: 130 }}
            />
          </Space>
          <Space size={4}>
            <Typography.Text strong>계량방식</Typography.Text>
            <Select
              value={filters.weighingMode}
              onChange={(value) => setFilters((prev) => ({ ...prev, weighingMode: value }))}
              placeholder="전체"
              allowClear
              style={{ width: 130 }}
              options={modeOptions}
            />
          </Space>
          <Space size={4}>
            <Typography.Text strong>상태</Typography.Text>
            <Select
              value={filters.status}
              onChange={(value) => setFilters((prev) => ({ ...prev, status: value }))}
              placeholder="전체"
              allowClear
              style={{ width: 110 }}
              options={statusOptions}
            />
          </Space>
          <div style={{ marginLeft: 'auto' }}>
            <Space>
              <Button icon={<ClearOutlined />} onClick={handleReset}>초기화</Button>
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} loading={loading}>조회</Button>
            </Space>
          </div>
        </div>
      </Card>

      {/* Results */}
      {searched && (
        <div style={{ marginBottom: 8 }}>
          <Space>
            <Typography.Text type="secondary">
              조회 결과: <strong>{totalElements.toLocaleString()}</strong>건
            </Typography.Text>
            <Button
              size="small"
              icon={<ReloadOutlined />}
              onClick={() => fetchData(currentPage, pageSize)}
            >
              새로고침
            </Button>
          </Space>
        </div>
      )}
      </FixedArea>
      <ScrollArea>
      <SortableTable
        columns={columns}
        dataSource={data}
        rowKey="weighingId"
        loading={loading}
        size="middle"
        tableKey="inquiry"
        pagination={false}
        scroll={{ y: 1 }}
        onRow={(record) => ({
          onClick: () => openDetail(record),
          style: { cursor: 'pointer' },
        })}
        locale={{
          emptyText: searched
            ? '조회 결과가 없습니다.'
            : '검색 조건을 설정한 후 조회 버튼을 클릭하세요.',
        }}
      />
      </ScrollArea>

      {searched && totalElements > 0 && (
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
          <Pagination
            current={currentPage}
            pageSize={pageSize}
            total={totalElements}
            onChange={handlePageChange}
            showSizeChanger
            showTotal={(total) => `총 ${total.toLocaleString()}건`}
            pageSizeOptions={['10', '20', '50', '100']}
          />
        </div>
      )}

      {/* Detail Modal */}
      <Modal
        title="계량 상세정보"
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={<Button onClick={() => setDetailOpen(false)}>닫기</Button>}
        width={640}
      >
        {selectedRecord && (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="계량 ID">
              {selectedRecord.weighingId}
            </Descriptions.Item>
            <Descriptions.Item label="배차 ID">
              {selectedRecord.dispatchId}
            </Descriptions.Item>
            <Descriptions.Item label="저울 ID">
              {selectedRecord.scaleId}
            </Descriptions.Item>
            <Descriptions.Item label="계량방식">
              {modeLabels[selectedRecord.weighingMode] ||
                selectedRecord.weighingMode}
            </Descriptions.Item>
            <Descriptions.Item label="계량단계">
              {stepLabels[selectedRecord.weighingStep] ||
                selectedRecord.weighingStep}
            </Descriptions.Item>
            <Descriptions.Item label="상태">
              <Tag color={statusColors[selectedRecord.weighingStatus]}>
                {statusLabels[selectedRecord.weighingStatus] ||
                  selectedRecord.weighingStatus}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="차량번호">
              {selectedRecord.lprPlateNumber || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="AI 신뢰도">
              {selectedRecord.aiConfidence != null
                ? `${(selectedRecord.aiConfidence * 100).toFixed(1)}%`
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="총중량(kg)">
              {selectedRecord.grossWeight?.toLocaleString() ?? '-'}
            </Descriptions.Item>
            <Descriptions.Item label="공차중량(kg)">
              {selectedRecord.tareWeight?.toLocaleString() ?? '-'}
            </Descriptions.Item>
            <Descriptions.Item label="순중량(kg)" span={2}>
              <span style={{ color: colors.primary, fontWeight: 600 }}>
                {selectedRecord.netWeight?.toLocaleString() ?? '-'}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="재계량 사유" span={2}>
              {selectedRecord.reWeighReason || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="생성일시">
              {dayjs(selectedRecord.createdAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
            <Descriptions.Item label="수정일시">
              {dayjs(selectedRecord.updatedAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </TablePageLayout>
  );
};

export default InquiryPage;

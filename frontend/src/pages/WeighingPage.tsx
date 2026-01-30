/**
 * 계량 현황 페이지 컴포넌트
 *
 * 계량 기록 목록을 조회하고 상세 정보를 확인하는 페이지입니다.
 * 상태, 계량방식, 기간별 필터 검색을 지원하며,
 * WebSocket을 통해 실시간으로 목록이 갱신됩니다.
 * 행 클릭 시 계량 상세정보 모달을 표시합니다.
 *
 * @returns 계량 현황 페이지 JSX
 */
import React, { useCallback, useEffect, useState } from 'react';
import {
  Typography,
  Tag,
  Button,
  Space,
  Select,
  DatePicker,
  Modal,
  Descriptions,
  Card,
  Row,
  Col,
} from 'antd';
import SortableTable from '../components/SortableTable';
import { TablePageLayout, FixedArea, ScrollArea } from '../components/TablePageLayout';
import { SearchOutlined, ClearOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { WeighingRecord } from '../types';
import { useWebSocket } from '../hooks/useWebSocket';
import dayjs, { type Dayjs } from 'dayjs';
import { colors } from '../theme/themeConfig';

const { RangePicker } = DatePicker;

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

const modeOptions = Object.entries(modeLabels).map(([value, label]) => ({
  value,
  label,
}));

const stepLabels: Record<string, string> = {
  FIRST_WEIGH: '1차 계량',
  SECOND_WEIGH: '2차 계량',
  COMPLETED: '완료',
};

const columns: ColumnsType<WeighingRecord> = [
  { title: 'ID', dataIndex: 'weighingId', width: 80 },
  { title: '배차ID', dataIndex: 'dispatchId', width: 100 },
  {
    title: '계량방식',
    dataIndex: 'weighingMode',
    width: 120,
    render: (v: string) => modeLabels[v] || v,
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
  { title: '차량번호', dataIndex: 'lprPlateNumber', width: 110 },
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

const WeighingPage: React.FC = () => {
  const [data, setData] = useState<WeighingRecord[]>([]);             // 계량 기록 목록
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string | undefined>(); // 상태 필터
  const [modeFilter, setModeFilter] = useState<string | undefined>();     // 계량방식 필터
  const [dateRange, setDateRange] = useState<
    [Dayjs | null, Dayjs | null] | null
  >(null); // 기간 필터
  const [detailOpen, setDetailOpen] = useState(false);                    // 상세 모달 표시
  const [selectedRecord, setSelectedRecord] = useState<WeighingRecord | null>(
    null,
  ); // 상세 보기 대상 레코드

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = {
        status: statusFilter,
        weighingMode: modeFilter,
        size: 20,
      };

      if (dateRange?.[0]) {
        params.dateFrom = dateRange[0].format('YYYY-MM-DD');
      }
      if (dateRange?.[1]) {
        params.dateTo = dateRange[1].format('YYYY-MM-DD');
      }

      const res = await apiClient.get('/weighings', { params });
      setData(res.data.data.content || []);
    } catch {
      /* ignore */
    }
    setLoading(false);
  }, [statusFilter, modeFilter, dateRange]);

  useEffect(() => {
    fetchData();
  }, []);

  const handleReset = () => {
    setStatusFilter(undefined);
    setModeFilter(undefined);
    setDateRange(null);
  };

  /** WebSocket 메시지 수신 시 목록 자동 갱신 */
  const handleWsMessage = useCallback(() => {
    fetchData();
  }, [fetchData]);

  useWebSocket(handleWsMessage);

  const openDetail = (record: WeighingRecord) => {
    setSelectedRecord(record);
    setDetailOpen(true);
  };

  const handleDateRangeChange = (
    dates: [Dayjs | null, Dayjs | null] | null,
  ) => {
    setDateRange(dates);
  };

  return (
    <TablePageLayout>
      <FixedArea>
      <Typography.Title level={4}>계량 현황</Typography.Title>

      <Card
        size="small"
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '16px 24px' } }}
      >
        <Row gutter={[16, 12]} align="middle">
          <Col>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 50, display: 'inline-block' }}>
                기간
              </Typography.Text>
              <RangePicker
                value={dateRange}
                placeholder={['시작일', '종료일']}
                onChange={handleDateRangeChange}
                allowClear
                style={{ width: 260 }}
              />
            </Space>
          </Col>
          <Col>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 50, display: 'inline-block' }}>
                상태
              </Typography.Text>
              <Select
                value={statusFilter}
                placeholder="전체"
                allowClear
                style={{ width: 150 }}
                onChange={setStatusFilter}
                options={Object.entries(statusLabels).map(([k, v]) => ({
                  value: k,
                  label: v,
                }))}
              />
            </Space>
          </Col>
          <Col>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 60, display: 'inline-block' }}>
                계량방식
              </Typography.Text>
              <Select
                value={modeFilter}
                placeholder="전체"
                allowClear
                style={{ width: 160 }}
                onChange={setModeFilter}
                options={modeOptions}
              />
            </Space>
          </Col>
          <Col flex="auto" style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Space>
              <Button icon={<ClearOutlined />} onClick={handleReset}>
                초기화
              </Button>
              <Button type="primary" icon={<SearchOutlined />} onClick={fetchData} loading={loading}>
                조회
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>
      </FixedArea>
      <ScrollArea>
      <SortableTable
        columns={columns}
        dataSource={data}
        rowKey="weighingId"
        loading={loading}
        size="middle"
        tableKey="weighing"
        scroll={{ y: 1 }}
        onRow={(record) => ({
          onClick: () => openDetail(record),
          style: { cursor: 'pointer' },
        })}
      />
      </ScrollArea>

      <Modal
        title="계량 상세정보"
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={
          <Button onClick={() => setDetailOpen(false)}>닫기</Button>
        }
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
            <Descriptions.Item label="총중량(kg)">
              {selectedRecord.grossWeight?.toLocaleString() ?? '-'}
            </Descriptions.Item>
            <Descriptions.Item label="공차중량(kg)">
              {selectedRecord.tareWeight?.toLocaleString() ?? '-'}
            </Descriptions.Item>
            <Descriptions.Item label="순중량(kg)">
              <span style={{ color: colors.primary, fontWeight: 600 }}>
                {selectedRecord.netWeight?.toLocaleString() ?? '-'}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="차량번호">
              {selectedRecord.lprPlateNumber || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="AI 신뢰도">
              {selectedRecord.aiConfidence != null
                ? `${(selectedRecord.aiConfidence * 100).toFixed(1)}%`
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="재계량 사유">
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

export default WeighingPage;

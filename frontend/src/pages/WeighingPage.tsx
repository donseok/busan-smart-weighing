import React, { useCallback, useEffect, useState } from 'react';
import {
  Table,
  Typography,
  Tag,
  Button,
  Space,
  Select,
  DatePicker,
  Modal,
  Descriptions,
} from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
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
  { title: 'ID', dataIndex: 'weighingId', width: 60 },
  { title: '배차ID', dataIndex: 'dispatchId', width: 80 },
  {
    title: '계량방식',
    dataIndex: 'weighingMode',
    render: (v: string) => modeLabels[v] || v,
  },
  {
    title: '총중량(kg)',
    dataIndex: 'grossWeight',
    render: (v?: number) => v?.toLocaleString() ?? '-',
  },
  {
    title: '공차중량(kg)',
    dataIndex: 'tareWeight',
    render: (v?: number) => v?.toLocaleString() ?? '-',
  },
  {
    title: '순중량(kg)',
    dataIndex: 'netWeight',
    render: (v?: number) => (
      <span style={{ color: colors.primary, fontWeight: 600 }}>
        {v?.toLocaleString() ?? '-'}
      </span>
    ),
  },
  { title: '차량번호', dataIndex: 'lprPlateNumber' },
  {
    title: '상태',
    dataIndex: 'weighingStatus',
    render: (v: string) => (
      <Tag color={statusColors[v]}>{statusLabels[v] || v}</Tag>
    ),
  },
  {
    title: '일시',
    dataIndex: 'createdAt',
    render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
  },
];

const WeighingPage: React.FC = () => {
  const [data, setData] = useState<WeighingRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [modeFilter, setModeFilter] = useState<string | undefined>();
  const [dateRange, setDateRange] = useState<
    [Dayjs | null, Dayjs | null] | null
  >(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<WeighingRecord | null>(
    null,
  );

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
  }, [fetchData]);

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
    <>
      <Typography.Title level={4}>계량 현황</Typography.Title>
      <Space style={{ marginBottom: 16 }} wrap>
        <RangePicker
          placeholder={['시작일', '종료일']}
          onChange={handleDateRangeChange}
          allowClear
          style={{ width: 260 }}
        />
        <Select
          placeholder="상태 필터"
          allowClear
          style={{ width: 150 }}
          onChange={setStatusFilter}
          options={Object.entries(statusLabels).map(([k, v]) => ({
            value: k,
            label: v,
          }))}
        />
        <Select
          placeholder="계량방식"
          allowClear
          style={{ width: 160 }}
          onChange={setModeFilter}
          options={modeOptions}
        />
        <Button icon={<ReloadOutlined />} onClick={fetchData}>
          새로고침
        </Button>
      </Space>

      <Table
        columns={columns}
        dataSource={data}
        rowKey="weighingId"
        loading={loading}
        size="middle"
        onRow={(record) => ({
          onClick: () => openDetail(record),
          style: { cursor: 'pointer' },
        })}
      />

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
    </>
  );
};

export default WeighingPage;

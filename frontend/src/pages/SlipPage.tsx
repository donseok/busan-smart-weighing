import React, { useCallback, useEffect, useState } from 'react';
import {
  Table,
  Button,
  Space,
  Typography,
  Tag,
  Modal,
  Select,
  DatePicker,
  Descriptions,
  message,
} from 'antd';
import { ReloadOutlined, ShareAltOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { WeighingSlip } from '../types';
import dayjs, { type Dayjs } from 'dayjs';
import { colors } from '../theme/themeConfig';

const { RangePicker } = DatePicker;

const SlipPage: React.FC = () => {
  const [data, setData] = useState<WeighingSlip[]>([]);
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState<
    [Dayjs | null, Dayjs | null] | null
  >(null);

  // Share modal state
  const [shareModalOpen, setShareModalOpen] = useState(false);
  const [selectedSlipId, setSelectedSlipId] = useState<number | null>(null);
  const [shareMethod, setShareMethod] = useState<string>('KAKAO');

  // Detail modal state
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedSlip, setSelectedSlip] = useState<WeighingSlip | null>(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { size: 20 };

      if (dateRange?.[0]) {
        params.dateFrom = dateRange[0].format('YYYY-MM-DD');
      }
      if (dateRange?.[1]) {
        params.dateTo = dateRange[1].format('YYYY-MM-DD');
      }

      const res = await apiClient.get('/slips', { params });
      setData(res.data.data.content || []);
    } catch {
      /* ignore */
    }
    setLoading(false);
  }, [dateRange]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const openShareModal = (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    setSelectedSlipId(id);
    setShareModalOpen(true);
  };

  const handleShare = async () => {
    if (!selectedSlipId) return;
    try {
      await apiClient.post(`/slips/${selectedSlipId}/share`, {
        method: shareMethod,
      });
      message.success('계량표가 공유되었습니다.');
      setShareModalOpen(false);
      fetchData();
    } catch {
      message.error('공유에 실패했습니다.');
    }
  };

  const openDetail = (record: WeighingSlip) => {
    setSelectedSlip(record);
    setDetailOpen(true);
  };

  const handleDateRangeChange = (
    dates: [Dayjs | null, Dayjs | null] | null,
  ) => {
    setDateRange(dates);
  };

  const columns: ColumnsType<WeighingSlip> = [
    { title: 'ID', dataIndex: 'slipId', width: 60 },
    { title: '전표번호', dataIndex: 'slipNumber' },
    {
      title: '차량번호',
      dataIndex: 'vehiclePlateNumber',
      render: (v?: string) => v || '-',
    },
    {
      title: '운송사',
      dataIndex: 'companyName',
      render: (v?: string) => v || '-',
    },
    {
      title: '품목명',
      dataIndex: 'itemName',
      render: (v?: string) => v || '-',
    },
    {
      title: '총중량(kg)',
      dataIndex: 'grossWeightKg',
      render: (v?: string) => (v ? Number(v).toLocaleString() : '-'),
    },
    {
      title: '차량중량(kg)',
      dataIndex: 'tareWeightKg',
      render: (v?: string) => (v ? Number(v).toLocaleString() : '-'),
    },
    {
      title: '순중량(kg)',
      dataIndex: 'netWeightKg',
      render: (v?: string) => (
        <span style={{ color: colors.primary, fontWeight: 600 }}>
          {v ? Number(v).toLocaleString() : '-'}
        </span>
      ),
    },
    {
      title: '공유',
      dataIndex: 'sharedVia',
      render: (v?: string) => (v ? <Tag color={colors.primary}>{v}</Tag> : '-'),
    },
    {
      title: '발행일',
      dataIndex: 'createdAt',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '공유',
      width: 80,
      render: (_, record) => (
        <Button
          size="small"
          icon={<ShareAltOutlined />}
          onClick={(e) => openShareModal(record.slipId, e)}
        >
          공유
        </Button>
      ),
    },
  ];

  return (
    <>
      <Typography.Title level={4}>전자계량표 관리</Typography.Title>
      <Space style={{ marginBottom: 16 }} wrap>
        <RangePicker
          placeholder={['시작일', '종료일']}
          onChange={handleDateRangeChange}
          allowClear
          style={{ width: 260 }}
        />
        <Button icon={<ReloadOutlined />} onClick={fetchData}>
          새로고침
        </Button>
      </Space>

      <Table
        columns={columns}
        dataSource={data}
        rowKey="slipId"
        loading={loading}
        size="middle"
        scroll={{ x: 1000 }}
        onRow={(record) => ({
          onClick: () => openDetail(record),
          style: { cursor: 'pointer' },
        })}
      />

      {/* Detail Modal */}
      <Modal
        title="계량표 상세정보"
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={
          <Button onClick={() => setDetailOpen(false)}>닫기</Button>
        }
        width={600}
      >
        {selectedSlip && (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="전표 ID">
              {selectedSlip.slipId}
            </Descriptions.Item>
            <Descriptions.Item label="전표번호">
              {selectedSlip.slipNumber}
            </Descriptions.Item>
            <Descriptions.Item label="계량 ID">
              {selectedSlip.weighingId}
            </Descriptions.Item>
            <Descriptions.Item label="배차 ID">
              {selectedSlip.dispatchId}
            </Descriptions.Item>
            <Descriptions.Item label="차량번호">
              {selectedSlip.vehiclePlateNumber || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="운송사">
              {selectedSlip.companyName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="품목명">
              {selectedSlip.itemName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="총중량(kg)">
              {selectedSlip.grossWeightKg
                ? Number(selectedSlip.grossWeightKg).toLocaleString()
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="차량중량(kg)">
              {selectedSlip.tareWeightKg
                ? Number(selectedSlip.tareWeightKg).toLocaleString()
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="순중량(kg)">
              <span style={{ color: colors.primary, fontWeight: 600 }}>
                {selectedSlip.netWeightKg
                  ? Number(selectedSlip.netWeightKg).toLocaleString()
                  : '-'}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="공유방법">
              {selectedSlip.sharedVia ? (
                <Tag color={colors.primary}>{selectedSlip.sharedVia}</Tag>
              ) : (
                '-'
              )}
            </Descriptions.Item>
            <Descriptions.Item label="발행일시">
              {dayjs(selectedSlip.createdAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>

      {/* Share Modal */}
      <Modal
        title="계량표 공유"
        open={shareModalOpen}
        onOk={handleShare}
        onCancel={() => setShareModalOpen(false)}
        okText="공유"
        cancelText="취소"
      >
        <Select
          style={{ width: '100%' }}
          value={shareMethod}
          onChange={setShareMethod}
          options={[
            { value: 'KAKAO', label: '카카오톡' },
            { value: 'SMS', label: 'SMS' },
          ]}
        />
      </Modal>
    </>
  );
};

export default SlipPage;

/**
 * 전자계량표 관리 페이지 컴포넌트
 *
 * 계량 완료 후 생성되는 전자계량표(Weighing Slip)를 관리하는 페이지입니다.
 * 기간, 품목유형, 업체별 필터 검색과 페이지네이션을 지원하며,
 * 계량표 상세 정보 모달, PDF 다운로드, 공유 기능을 제공합니다.
 * 계량 데이터(총중량, 공차중량, 순중량)와 운송 정보를 표시합니다.
 *
 * @returns 전자계량표 관리 페이지 JSX
 */
import React, { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Space,
  Typography,
  Tag,
  Modal,
  Select,
  DatePicker,
  Descriptions,
  message,
  Card,
  Row,
  Col,
} from 'antd';
import SortableTable from '../components/SortableTable';
import { TablePageLayout, FixedArea, ScrollArea } from '../components/TablePageLayout';
import { SearchOutlined, ClearOutlined, ShareAltOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { WeighingSlip } from '../types';
import dayjs, { type Dayjs } from 'dayjs';
import { colors } from '../theme/themeConfig';

const { RangePicker } = DatePicker;

const SlipPage: React.FC = () => {
  const [data, setData] = useState<WeighingSlip[]>([]);                   // 계량표 목록
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState<
    [Dayjs | null, Dayjs | null] | null
  >(null); // 기간 필터

  // 계량표 공유 모달 상태
  const [shareModalOpen, setShareModalOpen] = useState(false);
  const [selectedSlipId, setSelectedSlipId] = useState<number | null>(null); // 공유 대상 ID
  const [shareMethod, setShareMethod] = useState<string>('KAKAO');          // 공유 방식

  // 계량표 상세 모달 상태
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
  }, []);

  const handleReset = () => {
    setDateRange(null);
  };

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
    { title: 'ID', dataIndex: 'slipId', width: 80 },
    { title: '전표번호', dataIndex: 'slipNumber', width: 120 },
    {
      title: '차량번호',
      dataIndex: 'vehiclePlateNumber',
      width: 110,
      render: (v?: string) => v || '-',
    },
    {
      title: '운송사',
      dataIndex: 'companyName',
      width: 100,
      render: (v?: string) => v || '-',
    },
    {
      title: '품목명',
      dataIndex: 'itemName',
      width: 100,
      render: (v?: string) => v || '-',
    },
    {
      title: '총중량(kg)',
      dataIndex: 'grossWeightKg',
      width: 120,
      align: 'right',
      render: (v?: number) => (v != null && !isNaN(v) ? v.toLocaleString() : '-'),
    },
    {
      title: '차량중량(kg)',
      dataIndex: 'tareWeightKg',
      width: 130,
      align: 'right',
      render: (v?: number) => (v != null && !isNaN(v) ? v.toLocaleString() : '-'),
    },
    {
      title: '순중량(kg)',
      dataIndex: 'netWeightKg',
      width: 120,
      align: 'right',
      render: (v?: number) => (
        <span style={{ color: colors.primary, fontWeight: 600 }}>
          {v != null && !isNaN(v) ? v.toLocaleString() : '-'}
        </span>
      ),
    },
    {
      title: '공유',
      dataIndex: 'sharedVia',
      width: 80,
      render: (v?: string) => (v ? <Tag color={colors.primary}>{v}</Tag> : '-'),
    },
    {
      title: '발행일',
      dataIndex: 'createdAt',
      width: 160,
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
    <TablePageLayout>
      <FixedArea>
      <Typography.Title level={4}>전자계량표 관리</Typography.Title>

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
        rowKey="slipId"
        loading={loading}
        size="middle"
        scroll={{ x: 1000, y: 1 }}
        tableKey="slip"
        onRow={(record) => ({
          onClick: () => openDetail(record),
          style: { cursor: 'pointer' },
        })}
      />
      </ScrollArea>

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
              {selectedSlip.grossWeightKg != null && !isNaN(selectedSlip.grossWeightKg)
                ? selectedSlip.grossWeightKg.toLocaleString()
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="차량중량(kg)">
              {selectedSlip.tareWeightKg != null && !isNaN(selectedSlip.tareWeightKg)
                ? selectedSlip.tareWeightKg.toLocaleString()
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="순중량(kg)">
              <span style={{ color: colors.primary, fontWeight: 600 }}>
                {selectedSlip.netWeightKg != null && !isNaN(selectedSlip.netWeightKg)
                  ? selectedSlip.netWeightKg.toLocaleString()
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
    </TablePageLayout>
  );
};

export default SlipPage;

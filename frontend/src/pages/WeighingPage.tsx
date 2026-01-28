import React, { useEffect, useState } from 'react';
import { Table, Typography, Tag, Button, Space, Select } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { WeighingRecord } from '../types';
import dayjs from 'dayjs';

const statusColors: Record<string, string> = {
  IN_PROGRESS: 'processing', COMPLETED: 'success', RE_WEIGHING: 'warning', ERROR: 'error',
};

const statusLabels: Record<string, string> = {
  IN_PROGRESS: '진행중', COMPLETED: '완료', RE_WEIGHING: '재계량', ERROR: '오류',
};

const modeLabels: Record<string, string> = {
  LPR_AUTO: 'LPR 자동', MOBILE_OTP: '모바일 OTP', MANUAL: '수동', RE_WEIGH: '재계량',
};

const columns: ColumnsType<WeighingRecord> = [
  { title: 'ID', dataIndex: 'weighingId', width: 60 },
  { title: '배차ID', dataIndex: 'dispatchId', width: 80 },
  { title: '계량방식', dataIndex: 'weighingMode', render: (v: string) => modeLabels[v] || v },
  { title: '총중량(kg)', dataIndex: 'grossWeight', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '공차중량(kg)', dataIndex: 'tareWeight', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '순중량(kg)', dataIndex: 'netWeight', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '차량번호', dataIndex: 'lprPlateNumber' },
  { title: '상태', dataIndex: 'weighingStatus', render: (v: string) => <Tag color={statusColors[v]}>{statusLabels[v]}</Tag> },
  { title: '일시', dataIndex: 'createdAt', render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm') },
];

const WeighingPage: React.FC = () => {
  const [data, setData] = useState<WeighingRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/weighings', { params: { status: statusFilter, size: 20 } });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, [statusFilter]);

  return (
    <>
      <Typography.Title level={4}>계량 현황</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Select placeholder="상태 필터" allowClear style={{ width: 150 }}
          onChange={setStatusFilter}
          options={Object.entries(statusLabels).map(([k, v]) => ({ value: k, label: v }))} />
        <Button icon={<ReloadOutlined />} onClick={fetchData}>새로고침</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="weighingId" loading={loading} size="middle" />
    </>
  );
};

export default WeighingPage;

import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Typography, Tag, Modal, Select, message } from 'antd';
import { ReloadOutlined, ShareAltOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { WeighingSlip } from '../types';
import dayjs from 'dayjs';

const SlipPage: React.FC = () => {
  const [data, setData] = useState<WeighingSlip[]>([]);
  const [loading, setLoading] = useState(false);
  const [shareModalOpen, setShareModalOpen] = useState(false);
  const [selectedSlipId, setSelectedSlipId] = useState<number | null>(null);
  const [shareMethod, setShareMethod] = useState<string>('KAKAO');

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/slips', { params: { size: 20 } });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  const openShareModal = (id: number) => {
    setSelectedSlipId(id);
    setShareModalOpen(true);
  };

  const handleShare = async () => {
    if (!selectedSlipId) return;
    try {
      await apiClient.post(`/slips/${selectedSlipId}/share`, { method: shareMethod });
      message.success('계량표가 공유되었습니다.');
      setShareModalOpen(false);
      fetchData();
    } catch { message.error('공유에 실패했습니다.'); }
  };

  const columns: ColumnsType<WeighingSlip> = [
    { title: 'ID', dataIndex: 'slipId', width: 60 },
    { title: '전표번호', dataIndex: 'slipNumber' },
    { title: '차량번호', dataIndex: 'vehiclePlateNumber', render: (v?: string) => v || '-' },
    { title: '운송사', dataIndex: 'companyName', render: (v?: string) => v || '-' },
    { title: '품목명', dataIndex: 'itemName', render: (v?: string) => v || '-' },
    { title: '총중량(kg)', dataIndex: 'grossWeightKg', render: (v?: string) => v ? Number(v).toLocaleString() : '-' },
    { title: '차량중량(kg)', dataIndex: 'tareWeightKg', render: (v?: string) => v ? Number(v).toLocaleString() : '-' },
    { title: '순중량(kg)', dataIndex: 'netWeightKg', render: (v?: string) => v ? Number(v).toLocaleString() : '-' },
    { title: '공유', dataIndex: 'sharedVia', render: (v?: string) => v ? <Tag color="blue">{v}</Tag> : '-' },
    { title: '발행일', dataIndex: 'createdAt', render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm') },
    {
      title: '공유', width: 80, render: (_, record) => (
        <Button size="small" icon={<ShareAltOutlined />} onClick={() => openShareModal(record.slipId)}>공유</Button>
      ),
    },
  ];

  return (
    <>
      <Typography.Title level={4}>전자계량표 관리</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ReloadOutlined />} onClick={fetchData}>새로고침</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="slipId" loading={loading} size="middle" scroll={{ x: 1000 }} />

      <Modal title="계량표 공유" open={shareModalOpen} onOk={handleShare} onCancel={() => setShareModalOpen(false)} okText="공유" cancelText="취소">
        <Select style={{ width: '100%' }} value={shareMethod} onChange={setShareMethod}
          options={[{ value: 'KAKAO', label: '카카오톡' }, { value: 'SMS', label: 'SMS' }]} />
      </Modal>
    </>
  );
};

export default SlipPage;

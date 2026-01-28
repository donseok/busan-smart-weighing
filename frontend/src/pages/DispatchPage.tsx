import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Typography, Tag, DatePicker, Select, Modal, Form, Input, message } from 'antd';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { Dispatch } from '../types';
import dayjs from 'dayjs';

const statusColors: Record<string, string> = {
  REGISTERED: 'blue', IN_PROGRESS: 'orange', COMPLETED: 'green', CANCELLED: 'red',
};

const statusLabels: Record<string, string> = {
  REGISTERED: '등록', IN_PROGRESS: '진행중', COMPLETED: '완료', CANCELLED: '취소',
};

const itemTypeLabels: Record<string, string> = {
  BY_PRODUCT: '부산물', WASTE: '폐기물', SUB_MATERIAL: '부재료', EXPORT: '반출', GENERAL: '일반',
};

const columns: ColumnsType<Dispatch> = [
  { title: 'ID', dataIndex: 'dispatchId', width: 60 },
  { title: '품목유형', dataIndex: 'itemType', render: (v: string) => itemTypeLabels[v] || v },
  { title: '품목명', dataIndex: 'itemName' },
  { title: '배차일', dataIndex: 'dispatchDate' },
  { title: '출발지', dataIndex: 'originLocation' },
  { title: '도착지', dataIndex: 'destination' },
  { title: '상태', dataIndex: 'dispatchStatus', render: (v: string) => <Tag color={statusColors[v]}>{statusLabels[v]}</Tag> },
  { title: '등록일', dataIndex: 'createdAt', render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm') },
];

const DispatchPage: React.FC = () => {
  const [data, setData] = useState<Dispatch[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/dispatches', { params: { size: 20 } });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      values.dispatchDate = values.dispatchDate.format('YYYY-MM-DD');
      await apiClient.post('/dispatches', values);
      message.success('배차가 등록되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchData();
    } catch { /* validation error */ }
  };

  return (
    <>
      <Typography.Title level={4}>배차 관리</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>배차 등록</Button>
        <Button icon={<ReloadOutlined />} onClick={fetchData}>새로고침</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="dispatchId" loading={loading} size="middle" />

      <Modal title="배차 등록" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} okText="저장" cancelText="취소">
        <Form form={form} layout="vertical">
          <Form.Item name="vehicleId" label="차량 ID" rules={[{ required: true }]}><Input type="number" /></Form.Item>
          <Form.Item name="companyId" label="운송사 ID" rules={[{ required: true }]}><Input type="number" /></Form.Item>
          <Form.Item name="itemType" label="품목유형" rules={[{ required: true }]}>
            <Select options={Object.entries(itemTypeLabels).map(([k, v]) => ({ value: k, label: v }))} />
          </Form.Item>
          <Form.Item name="itemName" label="품목명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="dispatchDate" label="배차일" rules={[{ required: true }]}><DatePicker style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="originLocation" label="출발지"><Input /></Form.Item>
          <Form.Item name="destination" label="도착지"><Input /></Form.Item>
          <Form.Item name="remarks" label="비고"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default DispatchPage;

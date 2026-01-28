import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Typography, Modal, Form, Input, InputNumber, Tag, message } from 'antd';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { Scale } from '../../types';

const scaleStatusColors: Record<string, string> = {
  IDLE: 'default', WEIGHING: 'processing', COMPLETED: 'success', ERROR: 'error',
};

const columns: ColumnsType<Scale> = [
  { title: 'ID', dataIndex: 'scaleId', width: 60 },
  { title: '계량대명', dataIndex: 'scaleName' },
  { title: '위치', dataIndex: 'location' },
  { title: '최대용량(kg)', dataIndex: 'maxCapacity', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '최소용량(kg)', dataIndex: 'minCapacity', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '상태', dataIndex: 'scaleStatus', render: (v: string) => <Tag color={scaleStatusColors[v]}>{v}</Tag> },
];

const MasterScalePage: React.FC = () => {
  const [data, setData] = useState<Scale[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/master/scales');
      setData(res.data.data || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await apiClient.post('/master/scales', values);
      message.success('계량대가 등록되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchData();
    } catch { /* validation error */ }
  };

  return (
    <>
      <Typography.Title level={4}>계량대 관리</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>계량대 등록</Button>
        <Button icon={<ReloadOutlined />} onClick={fetchData}>새로고침</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="scaleId" loading={loading} size="middle" />

      <Modal title="계량대 등록" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} okText="저장" cancelText="취소">
        <Form form={form} layout="vertical">
          <Form.Item name="scaleName" label="계량대명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="location" label="위치"><Input /></Form.Item>
          <Form.Item name="maxCapacity" label="최대용량(kg)"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="minCapacity" label="최소용량(kg)"><InputNumber style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default MasterScalePage;

import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Typography, Modal, Form, Input, InputNumber, message } from 'antd';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { Vehicle } from '../../types';

const columns: ColumnsType<Vehicle> = [
  { title: 'ID', dataIndex: 'vehicleId', width: 60 },
  { title: '차량번호', dataIndex: 'plateNumber' },
  { title: '차종', dataIndex: 'vehicleType' },
  { title: '기본공차(kg)', dataIndex: 'defaultTareWeight', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '최대적재(kg)', dataIndex: 'maxLoadWeight', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '기사명', dataIndex: 'driverName' },
  { title: '기사연락처', dataIndex: 'driverPhone' },
];

const MasterVehiclePage: React.FC = () => {
  const [data, setData] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/master/vehicles', { params: { size: 50 } });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await apiClient.post('/master/vehicles', values);
      message.success('차량이 등록되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchData();
    } catch { /* validation error */ }
  };

  return (
    <>
      <Typography.Title level={4}>차량 관리</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>차량 등록</Button>
        <Button icon={<ReloadOutlined />} onClick={fetchData}>새로고침</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="vehicleId" loading={loading} size="middle" />

      <Modal title="차량 등록" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} okText="저장" cancelText="취소">
        <Form form={form} layout="vertical">
          <Form.Item name="plateNumber" label="차량번호" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="vehicleType" label="차종" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="companyId" label="운송사 ID"><Input type="number" /></Form.Item>
          <Form.Item name="defaultTareWeight" label="기본 공차중량(kg)"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="maxLoadWeight" label="최대 적재중량(kg)"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="driverName" label="기사명"><Input /></Form.Item>
          <Form.Item name="driverPhone" label="기사 연락처"><Input /></Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default MasterVehiclePage;

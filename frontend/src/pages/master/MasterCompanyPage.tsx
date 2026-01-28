import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Typography, Modal, Form, Input, message } from 'antd';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { Company } from '../../types';

const columns: ColumnsType<Company> = [
  { title: 'ID', dataIndex: 'companyId', width: 60 },
  { title: '운송사명', dataIndex: 'companyName' },
  { title: '유형', dataIndex: 'companyType' },
  { title: '사업자번호', dataIndex: 'businessNumber' },
  { title: '대표자', dataIndex: 'representative' },
  { title: '연락처', dataIndex: 'phoneNumber' },
  { title: '주소', dataIndex: 'address', ellipsis: true },
];

const MasterCompanyPage: React.FC = () => {
  const [data, setData] = useState<Company[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/master/companies', { params: { size: 50 } });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await apiClient.post('/master/companies', values);
      message.success('운송사가 등록되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchData();
    } catch { /* validation error */ }
  };

  return (
    <>
      <Typography.Title level={4}>운송사 관리</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>운송사 등록</Button>
        <Button icon={<ReloadOutlined />} onClick={fetchData}>새로고침</Button>
      </Space>
      <Table columns={columns} dataSource={data} rowKey="companyId" loading={loading} size="middle" />

      <Modal title="운송사 등록" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} okText="저장" cancelText="취소">
        <Form form={form} layout="vertical">
          <Form.Item name="companyName" label="운송사명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="companyType" label="유형" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="businessNumber" label="사업자번호"><Input /></Form.Item>
          <Form.Item name="representative" label="대표자"><Input /></Form.Item>
          <Form.Item name="phoneNumber" label="연락처"><Input /></Form.Item>
          <Form.Item name="address" label="주소"><Input /></Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default MasterCompanyPage;

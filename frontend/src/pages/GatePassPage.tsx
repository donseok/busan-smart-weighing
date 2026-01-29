import React, { useEffect, useState } from 'react';
import { Button, Space, Typography, Tag, Modal, Input, Form, message, Popconfirm, Card, Row, Col } from 'antd';
import SortableTable from '../components/SortableTable';
import { CheckOutlined, CloseOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { GatePass } from '../types';
import dayjs from 'dayjs';
import { colors } from '../theme/themeConfig';

const statusColors: Record<string, string> = {
  PENDING: colors.warning, PASSED: colors.success, REJECTED: colors.error,
};

const statusLabels: Record<string, string> = {
  PENDING: '대기', PASSED: '통과', REJECTED: '반려',
};

const GatePassPage: React.FC = () => {
  const [data, setData] = useState<GatePass[]>([]);
  const [loading, setLoading] = useState(false);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [rejectForm] = Form.useForm();

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/gate-passes', { params: { size: 20 } });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  const handlePass = async (id: number) => {
    try {
      await apiClient.put(`/gate-passes/${id}/pass`);
      message.success('출문이 승인되었습니다.');
      fetchData();
    } catch { message.error('출문 승인에 실패했습니다.'); }
  };

  const handleReject = async (values: { reason: string }) => {
    if (!selectedId) return;
    try {
      await apiClient.put(`/gate-passes/${selectedId}/reject`, { reason: values.reason });
      message.success('출문이 반려되었습니다.');
      setRejectModalOpen(false);
      rejectForm.resetFields();
      setSelectedId(null);
      fetchData();
    } catch { message.error('출문 반려에 실패했습니다.'); }
  };

  const openRejectModal = (id: number) => {
    setSelectedId(id);
    setRejectModalOpen(true);
  };

  const columns: ColumnsType<GatePass> = [
    { title: 'ID', dataIndex: 'gatePassId', width: 80 },
    { title: '계량 ID', dataIndex: 'weighingId', width: 100 },
    { title: '배차 ID', dataIndex: 'dispatchId', width: 100 },
    { title: '상태', dataIndex: 'passStatus', width: 90, render: (v: string) => <Tag color={statusColors[v]}>{statusLabels[v]}</Tag> },
    { title: '처리일시', dataIndex: 'passedAt', width: 160, render: (v?: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-' },
    { title: '반려사유', dataIndex: 'rejectReason', width: 130, render: (v?: string) => v || '-' },
    { title: '등록일', dataIndex: 'createdAt', width: 160, render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm') },
    {
      title: '처리', width: 160, render: (_, record) => record.passStatus === 'PENDING' ? (
        <Space>
          <Popconfirm title="출문을 승인하시겠습니까?" onConfirm={() => handlePass(record.gatePassId)}>
            <Button type="primary" size="small" icon={<CheckOutlined />}>승인</Button>
          </Popconfirm>
          <Button danger size="small" icon={<CloseOutlined />} onClick={() => openRejectModal(record.gatePassId)}>반려</Button>
        </Space>
      ) : null,
    },
  ];

  return (
    <>
      <Typography.Title level={4}>출문 관리</Typography.Title>
      <Card
        size="small"
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '16px 24px' } }}
      >
        <Row align="middle">
          <Col flex="auto" style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button icon={<ReloadOutlined />} onClick={fetchData} loading={loading}>새로고침</Button>
          </Col>
        </Row>
      </Card>
      <SortableTable columns={columns} dataSource={data} rowKey="gatePassId" loading={loading} size="middle" tableKey="gatePass" />

      <Modal title="출문 반려" open={rejectModalOpen} onOk={() => rejectForm.submit()} onCancel={() => { setRejectModalOpen(false); rejectForm.resetFields(); }} okText="반려" cancelText="취소">
        <Form form={rejectForm} onFinish={handleReject}>
          <Form.Item name="reason" rules={[{ required: true, message: '반려 사유를 입력하세요' }]}>
            <Input.TextArea rows={3} placeholder="반려 사유를 입력하세요" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default GatePassPage;

import React, { useEffect, useState, useMemo, useCallback } from 'react';
import { Button, Space, Typography, Modal, Form, Input, InputNumber, Tag, Popconfirm, message } from 'antd';
import SortableTable from '../../components/SortableTable';
import { PlusOutlined, ReloadOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { Scale } from '../../types';
import { colors } from '../../theme/themeConfig';

const scaleStatusColors: Record<string, string> = {
  IDLE: colors.textSecondary, WEIGHING: colors.warning, COMPLETED: colors.success, ERROR: colors.error,
};

const MasterScalePage: React.FC = () => {
  const [data, setData] = useState<Scale[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingScale, setEditingScale] = useState<Scale | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/master/scales');
      setData(res.data.data || []);
    } catch { /* ignore */ }
    setLoading(false);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const filteredData = useMemo(() => {
    if (!searchKeyword) return data;
    const keyword = searchKeyword.toLowerCase();
    return data.filter((item) => item.scaleName.toLowerCase().includes(keyword));
  }, [data, searchKeyword]);

  const handleSearch = (value: string) => {
    setSearchKeyword(value);
  };

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

  const handleEdit = (record: Scale) => {
    setEditingScale(record);
    editForm.setFieldsValue({
      scaleName: record.scaleName,
      location: record.location,
      maxCapacity: record.maxCapacity,
      minCapacity: record.minCapacity,
    });
    setEditModalOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingScale) return;
    try {
      const values = await editForm.validateFields();
      await apiClient.put(`/master/scales/${editingScale.scaleId}`, values);
      message.success('계량대가 수정되었습니다.');
      setEditModalOpen(false);
      setEditingScale(null);
      editForm.resetFields();
      fetchData();
    } catch { /* validation error */ }
  };

  const handleDelete = async (scaleId: number) => {
    try {
      await apiClient.delete(`/master/scales/${scaleId}`);
      message.success('계량대가 삭제되었습니다.');
      fetchData();
    } catch {
      message.error('계량대 삭제에 실패했습니다.');
    }
  };

  const columns: ColumnsType<Scale> = [
    { title: 'ID', dataIndex: 'scaleId', width: 60 },
    { title: '계량대명', dataIndex: 'scaleName' },
    { title: '위치', dataIndex: 'location' },
    { title: '최대용량(kg)', dataIndex: 'maxCapacity', render: (v?: number) => v?.toLocaleString() ?? '-' },
    { title: '최소용량(kg)', dataIndex: 'minCapacity', render: (v?: number) => v?.toLocaleString() ?? '-' },
    { title: '상태', dataIndex: 'scaleStatus', render: (v: string) => <Tag color={scaleStatusColors[v]}>{v}</Tag> },
    {
      title: '관리',
      key: 'actions',
      width: 100,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="삭제 확인"
            description="이 계량대를 삭제하시겠습니까?"
            onConfirm={() => handleDelete(record.scaleId)}
            okText="삭제"
            cancelText="취소"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <Typography.Title level={4}>계량대 관리</Typography.Title>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} align="center">
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>계량대 등록</Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>새로고침</Button>
        </Space>
        <Input.Search
          placeholder="계량대명 검색"
          allowClear
          onSearch={handleSearch}
          onChange={(e) => { if (!e.target.value) setSearchKeyword(''); }}
          style={{ width: 250 }}
          prefix={<SearchOutlined />}
        />
      </Space>
      <SortableTable columns={columns} dataSource={filteredData} rowKey="scaleId" loading={loading} size="middle" tableKey="masterScale" />

      <Modal
        title="계량대 등록"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => { setModalOpen(false); form.resetFields(); }}
        okText="저장"
        cancelText="취소"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="scaleName" label="계량대명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="location" label="위치"><Input /></Form.Item>
          <Form.Item name="maxCapacity" label="최대용량(kg)"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="minCapacity" label="최소용량(kg)"><InputNumber style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>

      <Modal
        title="계량대 수정"
        open={editModalOpen}
        onOk={handleEditSubmit}
        onCancel={() => { setEditModalOpen(false); setEditingScale(null); editForm.resetFields(); }}
        okText="수정"
        cancelText="취소"
      >
        <Form form={editForm} layout="vertical">
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

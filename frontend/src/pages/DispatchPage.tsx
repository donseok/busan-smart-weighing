import React, { useEffect, useState, useCallback } from 'react';
import { Table, Button, Space, Typography, Tag, DatePicker, Select, Modal, Form, Input, Popconfirm, message } from 'antd';
import { PlusOutlined, ReloadOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { Dispatch } from '../types';
import dayjs from 'dayjs';
import { colors } from '../theme/themeConfig';

const { RangePicker } = DatePicker;

const statusColors: Record<string, string> = {
  REGISTERED: colors.primary, IN_PROGRESS: colors.warning, COMPLETED: colors.success, CANCELLED: colors.error,
};

const statusLabels: Record<string, string> = {
  REGISTERED: '등록', IN_PROGRESS: '진행중', COMPLETED: '완료', CANCELLED: '취소',
};

const itemTypeLabels: Record<string, string> = {
  BY_PRODUCT: '부산물', WASTE: '폐기물', SUB_MATERIAL: '부재료', EXPORT: '반출', GENERAL: '일반',
};

const statusOptions = Object.entries(statusLabels).map(([value, label]) => ({ value, label }));
const itemTypeOptions = Object.entries(itemTypeLabels).map(([value, label]) => ({ value, label }));

interface FilterParams {
  startDate?: string;
  endDate?: string;
  itemType?: string;
  dispatchStatus?: string;
}

const DispatchPage: React.FC = () => {
  const [data, setData] = useState<Dispatch[]>([]);
  const [loading, setLoading] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<Dispatch | null>(null);
  const [filters, setFilters] = useState<FilterParams>({});
  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();

  const fetchData = useCallback(async (currentFilters?: FilterParams) => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { size: 20 };
      const appliedFilters = currentFilters ?? filters;
      if (appliedFilters.startDate) params.startDate = appliedFilters.startDate;
      if (appliedFilters.endDate) params.endDate = appliedFilters.endDate;
      if (appliedFilters.itemType) params.itemType = appliedFilters.itemType;
      if (appliedFilters.dispatchStatus) params.dispatchStatus = appliedFilters.dispatchStatus;
      const res = await apiClient.get('/dispatches', { params });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  }, [filters]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    try {
      const values = await createForm.validateFields();
      values.dispatchDate = values.dispatchDate.format('YYYY-MM-DD');
      await apiClient.post('/dispatches', values);
      message.success('배차가 등록되었습니다.');
      setCreateModalOpen(false);
      createForm.resetFields();
      fetchData();
    } catch { /* validation error */ }
  };

  const handleEdit = (record: Dispatch) => {
    setEditingRecord(record);
    editForm.setFieldsValue({
      vehicleId: record.vehicleId,
      companyId: record.companyId,
      itemType: record.itemType,
      itemName: record.itemName,
      dispatchDate: dayjs(record.dispatchDate),
      originLocation: record.originLocation,
      destination: record.destination,
      remarks: record.remarks,
    });
    setEditModalOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingRecord) return;
    try {
      const values = await editForm.validateFields();
      values.dispatchDate = values.dispatchDate.format('YYYY-MM-DD');
      await apiClient.put(`/dispatches/${editingRecord.dispatchId}`, values);
      message.success('배차가 수정되었습니다.');
      setEditModalOpen(false);
      setEditingRecord(null);
      editForm.resetFields();
      fetchData();
    } catch { /* validation error */ }
  };

  const handleDelete = async (record: Dispatch) => {
    try {
      await apiClient.delete(`/dispatches/${record.dispatchId}`);
      message.success('배차가 삭제되었습니다.');
      fetchData();
    } catch {
      message.error('삭제에 실패했습니다.');
    }
  };

  const handleDateRangeChange = (_: unknown, dateStrings: [string, string]) => {
    const updated: FilterParams = {
      ...filters,
      startDate: dateStrings[0] || undefined,
      endDate: dateStrings[1] || undefined,
    };
    setFilters(updated);
    fetchData(updated);
  };

  const handleItemTypeChange = (value: string | undefined) => {
    const updated: FilterParams = { ...filters, itemType: value };
    setFilters(updated);
    fetchData(updated);
  };

  const handleStatusChange = (value: string | undefined) => {
    const updated: FilterParams = { ...filters, dispatchStatus: value };
    setFilters(updated);
    fetchData(updated);
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
    {
      title: '작업',
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
          {record.dispatchStatus === 'REGISTERED' && (
            <Popconfirm
              title="배차 삭제"
              description="이 배차를 삭제하시겠습니까?"
              onConfirm={() => handleDelete(record)}
              okText="삭제"
              cancelText="취소"
            >
              <Button type="link" size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const formFields = (
    <>
      <Form.Item name="vehicleId" label="차량 ID" rules={[{ required: true }]}><Input type="number" /></Form.Item>
      <Form.Item name="companyId" label="운송사 ID" rules={[{ required: true }]}><Input type="number" /></Form.Item>
      <Form.Item name="itemType" label="품목유형" rules={[{ required: true }]}>
        <Select options={itemTypeOptions} />
      </Form.Item>
      <Form.Item name="itemName" label="품목명" rules={[{ required: true }]}><Input /></Form.Item>
      <Form.Item name="dispatchDate" label="배차일" rules={[{ required: true }]}><DatePicker style={{ width: '100%' }} /></Form.Item>
      <Form.Item name="originLocation" label="출발지"><Input /></Form.Item>
      <Form.Item name="destination" label="도착지"><Input /></Form.Item>
      <Form.Item name="remarks" label="비고"><Input.TextArea rows={2} /></Form.Item>
    </>
  );

  return (
    <>
      <Typography.Title level={4}>배차 관리</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalOpen(true)}>배차 등록</Button>
        <Button icon={<ReloadOutlined />} onClick={() => fetchData()}>새로고침</Button>
      </Space>

      <Space style={{ marginBottom: 16, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        <RangePicker
          onChange={handleDateRangeChange}
          placeholder={['시작일', '종료일']}
          style={{ minWidth: 240 }}
        />
        <Select
          allowClear
          placeholder="품목유형"
          options={itemTypeOptions}
          onChange={handleItemTypeChange}
          style={{ minWidth: 140 }}
        />
        <Select
          allowClear
          placeholder="배차상태"
          options={statusOptions}
          onChange={handleStatusChange}
          style={{ minWidth: 140 }}
        />
      </Space>

      <Table columns={columns} dataSource={data} rowKey="dispatchId" loading={loading} size="middle" />

      <Modal
        title="배차 등록"
        open={createModalOpen}
        onOk={handleCreate}
        onCancel={() => { setCreateModalOpen(false); createForm.resetFields(); }}
        okText="저장"
        cancelText="취소"
      >
        <Form form={createForm} layout="vertical">
          {formFields}
        </Form>
      </Modal>

      <Modal
        title="배차 수정"
        open={editModalOpen}
        onOk={handleEditSubmit}
        onCancel={() => { setEditModalOpen(false); setEditingRecord(null); editForm.resetFields(); }}
        okText="저장"
        cancelText="취소"
      >
        <Form form={editForm} layout="vertical">
          {formFields}
        </Form>
      </Modal>
    </>
  );
};

export default DispatchPage;

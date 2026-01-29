import React, { useEffect, useState, useCallback } from 'react';
import { Button, Space, Typography, Modal, Form, Input, InputNumber, Popconfirm, message } from 'antd';
import SortableTable from '../../components/SortableTable';
import { PlusOutlined, ReloadOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { Vehicle } from '../../types';
import { maxLengthRule, plateNumberRule, phoneNumberRule, positiveNumberRule, mustBeGreaterThanField } from '../../utils/validators';

const MasterVehiclePage: React.FC = () => {
  const [data, setData] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();

  const fetchData = useCallback(async (keyword?: string) => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { size: 50 };
      if (keyword) {
        params.filter = keyword;
      }
      const res = await apiClient.get('/master/vehicles', { params });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSearch = () => {
    fetchData(searchKeyword);
  };

  const handleReset = () => {
    setSearchKeyword('');
  };

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await apiClient.post('/master/vehicles', values);
      message.success('차량이 등록되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchData(searchKeyword);
    } catch { /* validation error */ }
  };

  const handleEdit = (record: Vehicle) => {
    setEditingVehicle(record);
    editForm.setFieldsValue({
      plateNumber: record.plateNumber,
      vehicleType: record.vehicleType,
      companyId: record.companyId,
      defaultTareWeight: record.defaultTareWeight,
      maxLoadWeight: record.maxLoadWeight,
      driverName: record.driverName,
      driverPhone: record.driverPhone,
    });
    setEditModalOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingVehicle) return;
    try {
      const values = await editForm.validateFields();
      await apiClient.put(`/master/vehicles/${editingVehicle.vehicleId}`, values);
      message.success('차량이 수정되었습니다.');
      setEditModalOpen(false);
      setEditingVehicle(null);
      editForm.resetFields();
      fetchData(searchKeyword);
    } catch { /* validation error */ }
  };

  const handleDelete = async (vehicleId: number) => {
    try {
      await apiClient.delete(`/master/vehicles/${vehicleId}`);
      message.success('차량이 삭제되었습니다.');
      fetchData(searchKeyword);
    } catch {
      message.error('차량 삭제에 실패했습니다.');
    }
  };

  const columns: ColumnsType<Vehicle> = [
    { title: 'ID', dataIndex: 'vehicleId', width: 80 },
    { title: '차량번호', dataIndex: 'plateNumber', width: 110 },
    { title: '차종', dataIndex: 'vehicleType', width: 90 },
    { title: '기본공차(kg)', dataIndex: 'defaultTareWeight', width: 130, align: 'right', render: (v?: number) => v?.toLocaleString() ?? '-' },
    { title: '최대적재(kg)', dataIndex: 'maxLoadWeight', width: 130, align: 'right', render: (v?: number) => v?.toLocaleString() ?? '-' },
    { title: '기사명', dataIndex: 'driverName', width: 100 },
    { title: '기사연락처', dataIndex: 'driverPhone', width: 130 },
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
            description="이 차량을 삭제하시겠습니까?"
            onConfirm={() => handleDelete(record.vehicleId)}
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
      <Typography.Title level={4}>차량 관리</Typography.Title>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} align="center">
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>차량 등록</Button>
        </Space>
        <Space>
          <Input
            placeholder="차량번호 검색"
            allowClear
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            prefix={<SearchOutlined />}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>조회</Button>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>초기화</Button>
        </Space>
      </Space>
      <SortableTable columns={columns} dataSource={data} rowKey="vehicleId" loading={loading} size="middle" tableKey="masterVehicle" />

      <Modal
        title="차량 등록"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => { setModalOpen(false); form.resetFields(); }}
        okText="저장"
        cancelText="취소"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="plateNumber" label="차량번호" rules={[{ required: true }, maxLengthRule(20), plateNumberRule]}><Input /></Form.Item>
          <Form.Item name="vehicleType" label="차종" rules={[{ required: true }, maxLengthRule(20)]}><Input /></Form.Item>
          <Form.Item name="companyId" label="운송사 ID"><Input type="number" /></Form.Item>
          <Form.Item name="defaultTareWeight" label="기본 공차중량(kg)" rules={[positiveNumberRule]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="maxLoadWeight" label="최대 적재중량(kg)" dependencies={['defaultTareWeight']} rules={[positiveNumberRule, mustBeGreaterThanField('defaultTareWeight', '기본 공차중량')]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="driverName" label="기사명" rules={[maxLengthRule(50)]}><Input /></Form.Item>
          <Form.Item name="driverPhone" label="기사 연락처" rules={[phoneNumberRule]}><Input placeholder="010-0000-0000" /></Form.Item>
        </Form>
      </Modal>

      <Modal
        title="차량 수정"
        open={editModalOpen}
        onOk={handleEditSubmit}
        onCancel={() => { setEditModalOpen(false); setEditingVehicle(null); editForm.resetFields(); }}
        okText="수정"
        cancelText="취소"
      >
        <Form form={editForm} layout="vertical">
          <Form.Item name="plateNumber" label="차량번호" rules={[{ required: true }, maxLengthRule(20), plateNumberRule]}><Input /></Form.Item>
          <Form.Item name="vehicleType" label="차종" rules={[{ required: true }, maxLengthRule(20)]}><Input /></Form.Item>
          <Form.Item name="companyId" label="운송사 ID"><Input type="number" /></Form.Item>
          <Form.Item name="defaultTareWeight" label="기본 공차중량(kg)" rules={[positiveNumberRule]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="maxLoadWeight" label="최대 적재중량(kg)" dependencies={['defaultTareWeight']} rules={[positiveNumberRule, mustBeGreaterThanField('defaultTareWeight', '기본 공차중량')]}><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="driverName" label="기사명" rules={[maxLengthRule(50)]}><Input /></Form.Item>
          <Form.Item name="driverPhone" label="기사 연락처" rules={[phoneNumberRule]}><Input placeholder="010-0000-0000" /></Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default MasterVehiclePage;

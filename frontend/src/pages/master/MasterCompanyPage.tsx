import React, { useEffect, useState, useCallback } from 'react';
import { Button, Space, Typography, Modal, Form, Input, Popconfirm, message } from 'antd';
import SortableTable from '../../components/SortableTable';
import { PlusOutlined, ReloadOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { Company } from '../../types';

const MasterCompanyPage: React.FC = () => {
  const [data, setData] = useState<Company[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingCompany, setEditingCompany] = useState<Company | null>(null);
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
      const res = await apiClient.get('/master/companies', { params });
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
      await apiClient.post('/master/companies', values);
      message.success('운송사가 등록되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchData(searchKeyword);
    } catch { /* validation error */ }
  };

  const handleEdit = (record: Company) => {
    setEditingCompany(record);
    editForm.setFieldsValue({
      companyName: record.companyName,
      companyType: record.companyType,
      businessNumber: record.businessNumber,
      representative: record.representative,
      phoneNumber: record.phoneNumber,
      address: record.address,
    });
    setEditModalOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingCompany) return;
    try {
      const values = await editForm.validateFields();
      await apiClient.put(`/master/companies/${editingCompany.companyId}`, values);
      message.success('운송사가 수정되었습니다.');
      setEditModalOpen(false);
      setEditingCompany(null);
      editForm.resetFields();
      fetchData(searchKeyword);
    } catch { /* validation error */ }
  };

  const handleDelete = async (companyId: number) => {
    try {
      await apiClient.delete(`/master/companies/${companyId}`);
      message.success('운송사가 삭제되었습니다.');
      fetchData(searchKeyword);
    } catch {
      message.error('운송사 삭제에 실패했습니다.');
    }
  };

  const columns: ColumnsType<Company> = [
    { title: 'ID', dataIndex: 'companyId', width: 60 },
    { title: '운송사명', dataIndex: 'companyName' },
    { title: '유형', dataIndex: 'companyType' },
    { title: '사업자번호', dataIndex: 'businessNumber' },
    { title: '대표자', dataIndex: 'representative' },
    { title: '연락처', dataIndex: 'phoneNumber' },
    { title: '주소', dataIndex: 'address', ellipsis: true },
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
            description="이 운송사를 삭제하시겠습니까?"
            onConfirm={() => handleDelete(record.companyId)}
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
      <Typography.Title level={4}>운송사 관리</Typography.Title>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} align="center">
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>운송사 등록</Button>
        </Space>
        <Space>
          <Input
            placeholder="운송사명 검색"
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
      <SortableTable columns={columns} dataSource={data} rowKey="companyId" loading={loading} size="middle" tableKey="masterCompany" />

      <Modal
        title="운송사 등록"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => { setModalOpen(false); form.resetFields(); }}
        okText="저장"
        cancelText="취소"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="companyName" label="운송사명" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="companyType" label="유형" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="businessNumber" label="사업자번호"><Input /></Form.Item>
          <Form.Item name="representative" label="대표자"><Input /></Form.Item>
          <Form.Item name="phoneNumber" label="연락처"><Input /></Form.Item>
          <Form.Item name="address" label="주소"><Input /></Form.Item>
        </Form>
      </Modal>

      <Modal
        title="운송사 수정"
        open={editModalOpen}
        onOk={handleEditSubmit}
        onCancel={() => { setEditModalOpen(false); setEditingCompany(null); editForm.resetFields(); }}
        okText="수정"
        cancelText="취소"
      >
        <Form form={editForm} layout="vertical">
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

import React, { useEffect, useState, useCallback } from 'react';
import { Button, Space, Typography, Modal, Form, Input, InputNumber, Popconfirm, message, Select, Tag } from 'antd';
import SortableTable from '../../components/SortableTable';
import { PlusOutlined, ReloadOutlined, SearchOutlined, EditOutlined, DeleteOutlined, FilterOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../../api/client';
import type { CommonCode } from '../../types';

const MasterCodePage: React.FC = () => {
  const [data, setData] = useState<CommonCode[]>([]);
  const [codeGroups, setCodeGroups] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingCode, setEditingCode] = useState<CommonCode | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedGroup, setSelectedGroup] = useState<string | undefined>(undefined);
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();

  const fetchCodeGroups = useCallback(async () => {
    try {
      const res = await apiClient.get('/master/codes/groups');
      setCodeGroups(res.data.data || []);
    } catch { /* ignore */ }
  }, []);

  const fetchData = useCallback(async (keyword?: string, group?: string) => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { size: 100 };
      if (group) {
        params.codeGroup = group;
      } else if (keyword) {
        params.filter = keyword;
      }
      const res = await apiClient.get('/master/codes', { params });
      setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  }, []);

  useEffect(() => {
    fetchCodeGroups();
    fetchData();
  }, [fetchCodeGroups, fetchData]);

  const handleSearch = () => {
    fetchData(searchKeyword, selectedGroup);
  };

  const handleReset = () => {
    setSearchKeyword('');
    setSelectedGroup(undefined);
  };

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await apiClient.post('/master/codes', values);
      message.success('코드가 등록되었습니다.');
      setModalOpen(false);
      form.resetFields();
      fetchData(searchKeyword, selectedGroup);
      fetchCodeGroups();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { error?: { message?: string } } } };
      if (err.response?.data?.error?.message) {
        message.error(err.response.data.error.message);
      }
    }
  };

  const handleEdit = (record: CommonCode) => {
    setEditingCode(record);
    editForm.setFieldsValue({
      codeGroup: record.codeGroup,
      codeValue: record.codeValue,
      codeName: record.codeName,
      sortOrder: record.sortOrder,
    });
    setEditModalOpen(true);
  };

  const handleEditSubmit = async () => {
    if (!editingCode) return;
    try {
      const values = await editForm.validateFields();
      await apiClient.put(`/master/codes/${editingCode.codeId}`, values);
      message.success('코드가 수정되었습니다.');
      setEditModalOpen(false);
      setEditingCode(null);
      editForm.resetFields();
      fetchData(searchKeyword, selectedGroup);
    } catch { /* validation error */ }
  };

  const handleDelete = async (codeId: number) => {
    try {
      await apiClient.delete(`/master/codes/${codeId}`);
      message.success('코드가 비활성화되었습니다.');
      fetchData(searchKeyword, selectedGroup);
    } catch {
      message.error('코드 삭제에 실패했습니다.');
    }
  };

  const getGroupColor = (group: string) => {
    const colors: Record<string, string> = {
      'ITEM_TYPE': 'blue',
      'VEHICLE_TYPE': 'green',
      'WEIGHING_MODE': 'orange',
      'COMPANY_TYPE': 'purple',
      'DISPATCH_STATUS': 'cyan',
      'WEIGHING_STATUS': 'magenta',
    };
    return colors[group] || 'default';
  };

  const columns: ColumnsType<CommonCode> = [
    { title: 'ID', dataIndex: 'codeId', width: 80 },
    {
      title: '코드그룹',
      dataIndex: 'codeGroup',
      width: 150,
      render: (group: string) => <Tag color={getGroupColor(group)}>{group}</Tag>,
    },
    { title: '코드값', dataIndex: 'codeValue', width: 120 },
    { title: '코드명', dataIndex: 'codeName' },
    { title: '정렬순서', dataIndex: 'sortOrder', width: 100, align: 'center' },
    {
      title: '상태',
      dataIndex: 'isActive',
      width: 80,
      align: 'center',
      render: (isActive: boolean) => (
        <Tag color={isActive ? 'success' : 'default'}>{isActive ? '활성' : '비활성'}</Tag>
      ),
    },
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
            description="이 코드를 비활성화하시겠습니까?"
            onConfirm={() => handleDelete(record.codeId)}
            okText="비활성화"
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
      <Typography.Title level={4}>코드 관리</Typography.Title>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} align="center">
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>코드 등록</Button>
        <Space>
          <Select
            style={{ width: 180 }}
            placeholder="코드그룹 필터"
            allowClear
            value={selectedGroup}
            onChange={setSelectedGroup}
            options={codeGroups.map(g => ({ label: g, value: g }))}
            suffixIcon={<FilterOutlined />}
          />
          <Input
            placeholder="코드그룹/코드명 검색"
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
      <SortableTable columns={columns} dataSource={data} rowKey="codeId" loading={loading} size="middle" tableKey="masterCode" />

      <Modal
        title="코드 등록"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => { setModalOpen(false); form.resetFields(); }}
        okText="저장"
        cancelText="취소"
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="codeGroup"
            label="코드그룹"
            rules={[{ required: true, message: '코드그룹을 입력하세요' }]}
            extra="예: ITEM_TYPE, VEHICLE_TYPE, COMPANY_TYPE"
          >
            <Input placeholder="대문자와 언더스코어 사용 권장" />
          </Form.Item>
          <Form.Item
            name="codeValue"
            label="코드값"
            rules={[{ required: true, message: '코드값을 입력하세요' }]}
            extra="예: BY_PRODUCT, CARGO, TRANSPORT"
          >
            <Input placeholder="대문자와 언더스코어 사용 권장" />
          </Form.Item>
          <Form.Item
            name="codeName"
            label="코드명"
            rules={[{ required: true, message: '코드명을 입력하세요' }]}
          >
            <Input placeholder="사용자에게 표시될 이름" />
          </Form.Item>
          <Form.Item name="sortOrder" label="정렬순서">
            <InputNumber min={1} placeholder="숫자가 작을수록 먼저 표시" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="코드 수정"
        open={editModalOpen}
        onOk={handleEditSubmit}
        onCancel={() => { setEditModalOpen(false); setEditingCode(null); editForm.resetFields(); }}
        okText="수정"
        cancelText="취소"
        width={500}
      >
        <Form form={editForm} layout="vertical">
          <Form.Item name="codeGroup" label="코드그룹">
            <Input disabled />
          </Form.Item>
          <Form.Item name="codeValue" label="코드값">
            <Input disabled />
          </Form.Item>
          <Form.Item
            name="codeName"
            label="코드명"
            rules={[{ required: true, message: '코드명을 입력하세요' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="sortOrder" label="정렬순서">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default MasterCodePage;

/**
 * 배차 관리 페이지 컴포넌트
 *
 * 배차(Dispatch) 데이터의 CRUD 기능을 제공하는 관리 페이지입니다.
 * 기간, 품목유형, 배차상태별 필터 검색과 페이지네이션을 지원하며,
 * 배차 등록/수정/삭제 모달을 통해 데이터를 관리합니다.
 *
 * @returns 배차 관리 페이지 JSX
 */
import React, { useState, useCallback } from 'react';
import { Button, Space, Typography, Tag, DatePicker, Select, Modal, Form, Input, Popconfirm, message, Card, Row, Col, Pagination } from 'antd';
import { PlusOutlined, ReloadOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ClearOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import type { Dispatch } from '../types';
import dayjs, { type Dayjs } from 'dayjs';
import SortableTable from '../components/SortableTable';
import { TablePageLayout, FixedArea, ScrollArea } from '../components/TablePageLayout';
import { maxLengthRule, futureOrPresentDateValidator } from '../utils/validators';
import {
  ITEM_TYPE_LABELS,
  ITEM_TYPE_OPTIONS,
  DISPATCH_STATUS_LABELS,
  DISPATCH_STATUS_OPTIONS,
  DISPATCH_STATUS_COLORS,
} from '../constants/labels';

const { RangePicker } = DatePicker;

interface FilterParams {
  dateRange: [Dayjs | null, Dayjs | null] | null;
  itemType?: string;
  dispatchStatus?: string;
}

const initialFilters: FilterParams = {
  dateRange: null,
  itemType: undefined,
  dispatchStatus: undefined,
};

const DispatchPage: React.FC = () => {
  const [data, setData] = useState<Dispatch[]>([]);                       // 배차 목록
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);                        // 조회 실행 여부
  const [totalElements, setTotalElements] = useState(0);                  // 전체 결과 수
  const [currentPage, setCurrentPage] = useState(1);                      // 현재 페이지
  const [pageSize, setPageSize] = useState(20);                           // 페이지당 건수
  const [createModalOpen, setCreateModalOpen] = useState(false);          // 등록 모달
  const [editModalOpen, setEditModalOpen] = useState(false);              // 수정 모달
  const [editingRecord, setEditingRecord] = useState<Dispatch | null>(null); // 수정 대상
  const [filters, setFilters] = useState<FilterParams>({ ...initialFilters }); // 검색 필터
  const [createForm] = Form.useForm();  // 등록 폼
  const [editForm] = Form.useForm();    // 수정 폼

  const fetchData = useCallback(async (page = 1, size = 20) => {
    setLoading(true);
    try {
      const params: Record<string, unknown> = { page: page - 1, size };
      if (filters.dateRange?.[0]) params.startDate = filters.dateRange[0].format('YYYY-MM-DD');
      if (filters.dateRange?.[1]) params.endDate = filters.dateRange[1].format('YYYY-MM-DD');
      if (filters.itemType) params.itemType = filters.itemType;
      if (filters.dispatchStatus) params.dispatchStatus = filters.dispatchStatus;
      const res = await apiClient.get('/dispatches', { params });
      const pageData = res.data.data;
      setData(pageData.content || []);
      setTotalElements(pageData.totalElements || 0);
      setSearched(true);
    } catch { /* ignore */ }
    setLoading(false);
  }, [filters]);

  const handleSearch = () => {
    setCurrentPage(1);
    fetchData(1, pageSize);
  };

  const handleReset = () => {
    setFilters({ ...initialFilters });
    setData([]);
    setSearched(false);
    setTotalElements(0);
    setCurrentPage(1);
  };

  const handlePageChange = (page: number, size: number) => {
    setCurrentPage(page);
    setPageSize(size);
    fetchData(page, size);
  };

  const handleCreate = async () => {
    try {
      const values = await createForm.validateFields();
      values.dispatchDate = values.dispatchDate.format('YYYY-MM-DD');
      await apiClient.post('/dispatches', values);
      message.success('배차가 등록되었습니다.');
      setCreateModalOpen(false);
      createForm.resetFields();
      if (searched) fetchData(currentPage, pageSize);
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
      if (searched) fetchData(currentPage, pageSize);
    } catch { /* validation error */ }
  };

  const handleDelete = async (record: Dispatch) => {
    try {
      await apiClient.delete(`/dispatches/${record.dispatchId}`);
      message.success('배차가 삭제되었습니다.');
      if (searched) fetchData(currentPage, pageSize);
    } catch {
      message.error('삭제에 실패했습니다.');
    }
  };

  const columns: ColumnsType<Dispatch> = [
    { title: 'ID', dataIndex: 'dispatchId', width: 80 },
    { title: '품목유형', dataIndex: 'itemType', width: 100, render: (v: string) => ITEM_TYPE_LABELS[v] || v },
    { title: '품목명', dataIndex: 'itemName', width: 110 },
    { title: '배차일', dataIndex: 'dispatchDate', width: 110 },
    { title: '출발지', dataIndex: 'originLocation', width: 110 },
    { title: '도착지', dataIndex: 'destination', width: 110 },
    { title: '상태', dataIndex: 'dispatchStatus', width: 90, render: (v: string) => <Tag color={DISPATCH_STATUS_COLORS[v]}>{DISPATCH_STATUS_LABELS[v]}</Tag> },
    { title: '등록일', dataIndex: 'createdAt', width: 160, render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm') },
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
        <Select options={ITEM_TYPE_OPTIONS} />
      </Form.Item>
      <Form.Item name="itemName" label="품목명" rules={[{ required: true }, maxLengthRule(100)]}><Input /></Form.Item>
      <Form.Item name="dispatchDate" label="배차일" rules={[{ required: true }, { validator: futureOrPresentDateValidator }]}><DatePicker style={{ width: '100%' }} /></Form.Item>
      <Form.Item name="originLocation" label="출발지" rules={[maxLengthRule(100)]}><Input /></Form.Item>
      <Form.Item name="destination" label="도착지" rules={[maxLengthRule(100)]}><Input /></Form.Item>
      <Form.Item name="remarks" label="비고"><Input.TextArea rows={2} /></Form.Item>
    </>
  );

  return (
    <TablePageLayout>
      <FixedArea>
      <Typography.Title level={4}>배차 관리</Typography.Title>

      {/* Search Condition Card */}
      <Card
        size="small"
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '16px 24px' } }}
      >
        <Row gutter={[16, 12]} align="middle">
          <Col>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 50, display: 'inline-block' }}>
                기간
              </Typography.Text>
              <RangePicker
                value={filters.dateRange}
                onChange={(dates) => setFilters((prev) => ({ ...prev, dateRange: dates }))}
                placeholder={['시작일', '종료일']}
                allowClear
                style={{ width: 260 }}
              />
            </Space>
          </Col>
          <Col>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 60, display: 'inline-block' }}>
                품목유형
              </Typography.Text>
              <Select
                value={filters.itemType}
                onChange={(value) => setFilters((prev) => ({ ...prev, itemType: value }))}
                placeholder="전체"
                allowClear
                style={{ width: 140 }}
                options={ITEM_TYPE_OPTIONS}
              />
            </Space>
          </Col>
          <Col>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 60, display: 'inline-block' }}>
                배차상태
              </Typography.Text>
              <Select
                value={filters.dispatchStatus}
                onChange={(value) => setFilters((prev) => ({ ...prev, dispatchStatus: value }))}
                placeholder="전체"
                allowClear
                style={{ width: 130 }}
                options={DISPATCH_STATUS_OPTIONS}
              />
            </Space>
          </Col>
          <Col flex="auto" style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Space>
              <Button icon={<ClearOutlined />} onClick={handleReset}>
                초기화
              </Button>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleSearch}
                loading={loading}
              >
                조회
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* Results header */}
      <div style={{ marginBottom: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Space>
          {searched && (
            <Typography.Text type="secondary">
              조회 결과: <strong>{totalElements.toLocaleString()}</strong>건
            </Typography.Text>
          )}
          {searched && (
            <Button size="small" icon={<ReloadOutlined />} onClick={() => fetchData(currentPage, pageSize)}>
              새로고침
            </Button>
          )}
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalOpen(true)}>
          배차 등록
        </Button>
      </div>
      </FixedArea>
      <ScrollArea>
      <SortableTable
        columns={columns}
        dataSource={data}
        rowKey="dispatchId"
        loading={loading}
        size="middle"
        tableKey="dispatch"
        pagination={false}
        locale={{
          emptyText: searched
            ? '조회 결과가 없습니다.'
            : '검색 조건을 설정한 후 조회 버튼을 클릭하세요.',
        }}
      />
      </ScrollArea>

      {searched && totalElements > 0 && (
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
          <Pagination
            current={currentPage}
            pageSize={pageSize}
            total={totalElements}
            onChange={handlePageChange}
            showSizeChanger
            showTotal={(total) => `총 ${total.toLocaleString()}건`}
            pageSizeOptions={['10', '20', '50', '100']}
          />
        </div>
      )}

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
    </TablePageLayout>
  );
};

export default DispatchPage;

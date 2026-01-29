import React, { useEffect, useCallback, type ReactNode } from 'react';
import { Button, Space, Typography, Modal, Form, Popconfirm, message, Input } from 'antd';
import SortableTable from './SortableTable';
import { PlusOutlined, ReloadOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import { useCrudState } from '../hooks/useCrudState';

/** 개별 Master 페이지에서 전달하는 설정 */
export interface MasterCrudPageConfig<T extends Record<string, unknown>> {
  /** 페이지 제목 (e.g. "운송사 관리") */
  title: string;
  /** API 엔드포인트 (e.g. "/master/companies") */
  endpoint: string;
  /** 레코드의 PK 필드명 (e.g. "companyId") */
  rowKey: string;
  /** SortableTable 키 */
  tableKey: string;
  /** 엔티티 한국어 이름 (e.g. "운송사") */
  entityName: string;
  /** 검색 placeholder (e.g. "운송사명 검색") */
  searchPlaceholder: string;
  /** 테이블 컬럼 (관리 컬럼 제외) */
  columns: ColumnsType<T>;
  /** 등록/수정 폼 필드 JSX */
  formFields: ReactNode;
  /** 수정 폼 필드 JSX (등록과 다른 경우) */
  editFormFields?: ReactNode;
  /** 수정 모달에 set할 필드값을 레코드로부터 추출 */
  getEditFieldValues: (record: T) => Record<string, unknown>;
  /** 응답에서 data를 추출 (기본: res.data.data.content || []) */
  extractData?: (resData: unknown) => T[];
  /** fetchData 파라미터 변환 (기본: { size: 50, filter: keyword }) */
  buildFetchParams?: (keyword?: string) => Record<string, unknown>;
  /** 모달 너비 */
  modalWidth?: number;
  /** 삭제 확인 메시지 (e.g. "이 운송사를 삭제하시겠습니까?") */
  deleteConfirmMessage?: string;
  /** 삭제 확인 버튼 텍스트 */
  deleteOkText?: string;
  /** 추가 테이블 dataSource 가공 (e.g. 프론트 필터링) */
  filterData?: (data: T[], keyword: string) => T[];
}

export default function MasterCrudPage<T extends Record<string, unknown>>({
  title,
  endpoint,
  rowKey,
  tableKey,
  entityName,
  searchPlaceholder,
  columns,
  formFields,
  editFormFields,
  getEditFieldValues,
  extractData,
  buildFetchParams,
  modalWidth,
  deleteConfirmMessage,
  deleteOkText = '삭제',
  filterData,
}: MasterCrudPageConfig<T>) {
  const {
    data, setData, loading, setLoading,
    modalOpen, editModalOpen, editingRecord,
    searchKeyword, setSearchKeyword,
    form, editForm,
    openCreateModal, closeCreateModal,
    openEditModal, closeEditModal,
    resetSearch,
  } = useCrudState<T>();

  const defaultExtract = (resData: unknown): T[] => {
    const d = resData as { data?: { content?: T[] } | T[] };
    if (Array.isArray(d.data)) return d.data;
    return (d.data as { content?: T[] })?.content || [];
  };

  const extract = extractData || defaultExtract;

  const fetchData = useCallback(async (keyword?: string) => {
    setLoading(true);
    try {
      const params = buildFetchParams
        ? buildFetchParams(keyword)
        : { size: 50, ...(keyword ? { filter: keyword } : {}) };
      const res = await apiClient.get(endpoint, { params });
      setData(extract(res.data));
    } catch {
      console.error(`[${entityName}] 데이터 조회 실패`);
    }
    setLoading(false);
  }, [endpoint, buildFetchParams, entityName, extract, setData, setLoading]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSearch = () => fetchData(searchKeyword);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await apiClient.post(endpoint, values);
      message.success(`${entityName}가 등록되었습니다.`);
      closeCreateModal();
      fetchData(searchKeyword);
    } catch { /* validation error */ }
  };

  const handleEdit = (record: T) => {
    openEditModal(record, getEditFieldValues(record));
  };

  const handleEditSubmit = async () => {
    if (!editingRecord) return;
    try {
      const values = await editForm.validateFields();
      await apiClient.put(`${endpoint}/${(editingRecord as Record<string, unknown>)[rowKey]}`, values);
      message.success(`${entityName}가 수정되었습니다.`);
      closeEditModal();
      fetchData(searchKeyword);
    } catch { /* validation error */ }
  };

  const handleDelete = async (id: number) => {
    try {
      await apiClient.delete(`${endpoint}/${id}`);
      message.success(`${entityName}가 삭제되었습니다.`);
      fetchData(searchKeyword);
    } catch {
      message.error(`${entityName} 삭제에 실패했습니다.`);
    }
  };

  const actionColumn: ColumnsType<T>[number] = {
    title: '관리',
    key: 'actions',
    width: 100,
    render: (_: unknown, record: T) => (
      <Space size="small">
        <Button
          type="link"
          size="small"
          icon={<EditOutlined />}
          onClick={() => handleEdit(record)}
        />
        <Popconfirm
          title="삭제 확인"
          description={deleteConfirmMessage || `이 ${entityName}를 삭제하시겠습니까?`}
          onConfirm={() => handleDelete((record as Record<string, unknown>)[rowKey] as number)}
          okText={deleteOkText}
          cancelText="취소"
        >
          <Button type="link" size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      </Space>
    ),
  };

  const displayData = filterData ? filterData(data, searchKeyword) : data;

  return (
    <>
      <Typography.Title level={4}>{title}</Typography.Title>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} align="center">
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            {entityName} 등록
          </Button>
        </Space>
        <Space>
          <Input
            placeholder={searchPlaceholder}
            allowClear
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            prefix={<SearchOutlined />}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={filterData ? () => fetchData() : handleSearch}>
            조회
          </Button>
          <Button icon={<ReloadOutlined />} onClick={resetSearch}>초기화</Button>
        </Space>
      </Space>
      <SortableTable
        columns={[...columns, actionColumn] as ColumnsType<T>}
        dataSource={displayData}
        rowKey={rowKey}
        loading={loading}
        size="middle"
        tableKey={tableKey}
      />

      <Modal
        title={`${entityName} 등록`}
        open={modalOpen}
        onOk={handleCreate}
        onCancel={closeCreateModal}
        okText="저장"
        cancelText="취소"
        width={modalWidth}
      >
        <Form form={form} layout="vertical">
          {formFields}
        </Form>
      </Modal>

      <Modal
        title={`${entityName} 수정`}
        open={editModalOpen}
        onOk={handleEditSubmit}
        onCancel={closeEditModal}
        okText="수정"
        cancelText="취소"
        width={modalWidth}
      >
        <Form form={editForm} layout="vertical">
          {editFormFields || formFields}
        </Form>
      </Modal>
    </>
  );
}

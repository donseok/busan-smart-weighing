/**
 * @fileoverview 마스터 데이터 생성/조회/수정/삭제 범용 페이지 컴포넌트
 *
 * 운송사, 차량, 품목, 코드 등 마스터 데이터 관리 페이지의 공통 레이아웃과
 * CRUD 로직을 제공하는 제네릭 컴포넌트입니다.
 * 각 마스터 페이지는 MasterCrudPageConfig를 통해 설정만 전달하면 됩니다.
 *
 * @module components/MasterCrudPage
 */
import { useEffect, useCallback, useRef, type ReactNode } from 'react';
import { Button, Space, Typography, Modal, Form, Popconfirm, message, Input } from 'antd';
import SortableTable from './SortableTable';
import EmptyState from './EmptyState';
import { PlusOutlined, ReloadOutlined, SearchOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient, { parseApiError } from '../api/client';
import { useCrudState } from '../hooks/useCrudState';
import { useKeyboardShortcuts } from '../hooks/useKeyboardShortcuts';

/** 개별 Master 페이지에서 전달하는 설정 */
export interface MasterCrudPageConfig<T extends object> {
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

/**
 * 마스터 데이터 생성/조회/수정/삭제 범용 페이지 컴포넌트
 *
 * 테이블 조회, 검색, 등록/수정 모달, 삭제 확인 등 공통 CRUD UI를 제공합니다.
 * 제네릭 타입 T를 통해 다양한 마스터 데이터 엔티티에 재사용할 수 있습니다.
 *
 * @template T - 마스터 데이터 레코드 타입
 * @param config - 페이지 설정 (엔드포인트, 컬럼, 폼 필드 등)
 * @returns 마스터 데이터 관리 페이지 JSX
 */
export default function MasterCrudPage<T extends object>({
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

  const extractRef = useRef(extractData);
  extractRef.current = extractData;
  const buildParamsRef = useRef(buildFetchParams);
  buildParamsRef.current = buildFetchParams;

  const defaultExtract = useCallback((resData: unknown): T[] => {
    const d = resData as { data?: { content?: T[] } | T[] };
    if (Array.isArray(d.data)) return d.data;
    return (d.data as { content?: T[] })?.content || [];
  }, []);

  const fetchData = useCallback(async (keyword?: string) => {
    setLoading(true);
    try {
      const params = buildParamsRef.current
        ? buildParamsRef.current(keyword)
        : { size: 50, ...(keyword ? { filter: keyword } : {}) };
      const res = await apiClient.get(endpoint, { params });
      const extract = extractRef.current || defaultExtract;
      setData(extract(res.data));
    } catch (err) {
      const apiError = parseApiError(err);
      message.error(apiError.message);
    }
    setLoading(false);
  }, [endpoint, defaultExtract, setData, setLoading]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSearch = () => fetchData(searchKeyword);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await apiClient.post(endpoint, values);
      message.success(`${entityName}가 등록되었습니다.`);
      closeCreateModal();
      fetchData(searchKeyword);
    } catch (err) {
      if (err && typeof err === 'object' && 'errorFields' in err) return;
      const apiError = parseApiError(err);
      message.error(apiError.message);
    }
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
    } catch (err) {
      if (err && typeof err === 'object' && 'errorFields' in err) return;
      const apiError = parseApiError(err);
      message.error(apiError.message);
    }
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

  useKeyboardShortcuts([
    {
      key: 'n',
      ctrl: true,
      handler: openCreateModal,
      description: `${entityName} 등록`,
    },
  ]);

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
        locale={{
          emptyText: (
            <EmptyState
              title={`등록된 ${entityName}가 없습니다`}
              description={`${entityName} 등록 버튼을 클릭하여 새로운 ${entityName}를 추가하세요.`}
              actionText={`${entityName} 등록`}
              onAction={openCreateModal}
            />
          ),
        }}
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

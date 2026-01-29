import { useState } from 'react';
import { Form } from 'antd';
import type { FormInstance } from 'antd';

/**
 * Master CRUD 페이지 공통 상태 훅
 *
 * 4개 Master 페이지에서 동일하게 반복되는 상태를 하나로 추출합니다.
 *
 * @template T - 데이터 레코드 타입
 */
export function useCrudState<T>() {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<T | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();

  const openCreateModal = () => setModalOpen(true);

  const closeCreateModal = () => {
    setModalOpen(false);
    form.resetFields();
  };

  const openEditModal = (record: T, fieldValues: Record<string, unknown>) => {
    setEditingRecord(record);
    editForm.setFieldsValue(fieldValues);
    setEditModalOpen(true);
  };

  const closeEditModal = () => {
    setEditModalOpen(false);
    setEditingRecord(null);
    editForm.resetFields();
  };

  const resetSearch = () => {
    setSearchKeyword('');
  };

  return {
    data,
    setData,
    loading,
    setLoading,
    modalOpen,
    editModalOpen,
    editingRecord,
    searchKeyword,
    setSearchKeyword,
    form: form as FormInstance,
    editForm: editForm as FormInstance,
    openCreateModal,
    closeCreateModal,
    openEditModal,
    closeEditModal,
    resetSearch,
  };
}

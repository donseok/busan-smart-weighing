/**
 * 공지사항 관리 페이지 컴포넌트
 *
 * 시스템 공지사항의 CRUD 기능을 제공하는 관리 페이지입니다.
 * 공지사항 등록/수정/삭제, 게시/비게시 전환, 상단 고정(Pin) 토글,
 * 키워드 검색 등의 기능을 지원합니다.
 * 관리자만 등록/수정/삭제가 가능하며, 일반 사용자는 조회만 가능합니다.
 *
 * @returns 공지사항 관리 페이지 JSX
 */
import React, { useState, useEffect } from 'react';
import {
  Button,
  Modal,
  Form,
  Input,
  Select,
  Space,
  Tag,
  Typography,
  message,
  Popconfirm,
  Switch,
  Card,
} from 'antd';
import SortableTable from '../components/SortableTable';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PushpinOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import { darkColors, lightColors } from '../theme/themeConfig';
import { useTheme } from '../context/ThemeContext';
import { useAuth } from '../context/AuthContext';
import type { Notice } from '../types';
import { NOTICE_CATEGORY_OPTIONS, NOTICE_CATEGORY_COLORS } from '../constants/labels';

const { TextArea } = Input;
const { Title } = Typography;

const NoticePage: React.FC = () => {
  const [notices, setNotices] = useState<Notice[]>([]);                // 공지사항 목록
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);             // 등록/수정 모달
  const [detailModalVisible, setDetailModalVisible] = useState(false); // 상세 보기 모달
  const [editingNotice, setEditingNotice] = useState<Notice | null>(null);   // 수정 대상 공지
  const [selectedNotice, setSelectedNotice] = useState<Notice | null>(null); // 상세 보기 대상
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [searchKeyword, setSearchKeyword] = useState('');              // 제목 검색어
  const [form] = Form.useForm();

  const { themeMode } = useTheme();
  const colors = themeMode === 'dark' ? darkColors : lightColors;
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const fetchNotices = async (page = 1, size = 10) => {
    setLoading(true);
    try {
      const endpoint = isAdmin ? '/notices/admin' : '/notices';
      const res = await apiClient.get(endpoint, {
        params: { page: page - 1, size },
      });
      setNotices(res.data.data.content);
      setPagination({
        current: page,
        pageSize: size,
        total: res.data.data.totalElements,
      });
    } catch {
      message.error('공지사항 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotices();
  }, []);

  const handleCreate = () => {
    setEditingNotice(null);
    form.resetFields();
    form.setFieldsValue({ isPublished: true, isPinned: false });
    setModalVisible(true);
  };

  const handleEdit = (record: Notice) => {
    setEditingNotice(record);
    form.setFieldsValue({
      title: record.title,
      content: record.content,
      category: record.category,
      isPinned: record.isPinned,
    });
    setModalVisible(true);
  };

  const handleViewDetail = async (noticeId: number) => {
    try {
      const res = await apiClient.get(`/notices/${noticeId}`);
      setSelectedNotice(res.data.data);
      setDetailModalVisible(true);
    } catch {
      message.error('공지사항을 불러오는데 실패했습니다.');
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      if (editingNotice) {
        await apiClient.put(`/notices/${editingNotice.noticeId}`, {
          title: values.title,
          content: values.content,
          category: values.category,
          isPinned: values.isPinned,
        });
        message.success('공지사항이 수정되었습니다.');
      } else {
        await apiClient.post('/notices', values);
        message.success('공지사항이 등록되었습니다.');
      }
      setModalVisible(false);
      fetchNotices(pagination.current, pagination.pageSize);
    } catch {
      message.error('저장에 실패했습니다.');
    }
  };

  const handleDelete = async (noticeId: number) => {
    try {
      await apiClient.delete(`/notices/${noticeId}`);
      message.success('공지사항이 삭제되었습니다.');
      fetchNotices(pagination.current, pagination.pageSize);
    } catch {
      message.error('삭제에 실패했습니다.');
    }
  };

  /** 공지사항 게시/비게시 상태 토글 */
  const handleTogglePublish = async (noticeId: number) => {
    try {
      await apiClient.patch(`/notices/${noticeId}/publish`);
      message.success('발행 상태가 변경되었습니다.');
      fetchNotices(pagination.current, pagination.pageSize);
    } catch {
      message.error('상태 변경에 실패했습니다.');
    }
  };

  /** 공지사항 상단 고정/해제 토글 */
  const handleTogglePin = async (noticeId: number) => {
    try {
      await apiClient.patch(`/notices/${noticeId}/pin`);
      message.success('고정 상태가 변경되었습니다.');
      fetchNotices(pagination.current, pagination.pageSize);
    } catch {
      message.error('상태 변경에 실패했습니다.');
    }
  };

  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      fetchNotices();
      return;
    }
    setLoading(true);
    try {
      const res = await apiClient.get('/notices/search', {
        params: { keyword: searchKeyword, page: 0, size: pagination.pageSize },
      });
      setNotices(res.data.data.content);
      setPagination({
        ...pagination,
        current: 1,
        total: res.data.data.totalElements,
      });
    } catch {
      message.error('검색에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const columns: ColumnsType<Notice> = [
    ...(isAdmin ? [{
      title: '상태',
      dataIndex: 'isPublished',
      key: 'isPublished',
      width: 90,
      render: (isPublished: boolean, record: Notice) => (
        <Space direction="vertical" size={2}>
          {record.isPinned && <Tag color="gold"><PushpinOutlined /> 고정</Tag>}
          <Tag color={isPublished ? 'green' : 'default'}>
            {isPublished ? '공개' : '비공개'}
          </Tag>
        </Space>
      ),
    } as const] : []),
    {
      title: '카테고리',
      dataIndex: 'categoryDesc',
      key: 'category',
      width: 120,
      render: (text: string, record: Notice) => (
        <Tag color={NOTICE_CATEGORY_COLORS[record.category] || 'default'}>{text}</Tag>
      ),
    },
    {
      title: '제목',
      dataIndex: 'title',
      key: 'title',
      ellipsis: true,
      render: (text: string, record: Notice) => (
        <a onClick={() => handleViewDetail(record.noticeId)} style={{ color: colors.primary }}>
          {text}
        </a>
      ),
    },
    {
      title: '작성자',
      dataIndex: 'authorName',
      key: 'authorName',
      width: 120,
    },
    {
      title: '조회수',
      dataIndex: 'viewCount',
      key: 'viewCount',
      width: 90,
      render: (count: number) => count.toLocaleString(),
    },
    {
      title: '발행일',
      dataIndex: 'publishedAt',
      key: 'publishedAt',
      width: 180,
      render: (date: string) => date ? new Date(date).toLocaleString('ko-KR') : '-',
    },
    {
      title: isAdmin ? '관리' : '보기',
      key: 'actions',
      width: isAdmin ? 180 : 60,
      render: (_, record: Notice) => (
        <Space size="small">
          <Button
            type="text"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record.noticeId)}
          />
          {isAdmin && (
            <>
              <Button
                type="text"
                size="small"
                icon={<PushpinOutlined />}
                onClick={() => handleTogglePin(record.noticeId)}
                style={{ color: record.isPinned ? colors.warning : colors.textSecondary }}
              />
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
              <Popconfirm
                title="정말 삭제하시겠습니까?"
                onConfirm={() => handleDelete(record.noticeId)}
                okText="삭제"
                cancelText="취소"
              >
                <Button type="text" size="small" icon={<DeleteOutlined />} danger />
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={4} style={{ margin: 0, color: colors.textPrimary }}>
          공지사항 관리
        </Title>
        <Space>
          <Input.Search
            placeholder="제목 검색"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onSearch={handleSearch}
            style={{ width: 250 }}
            allowClear
          />
          {isAdmin && (
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              공지 등록
            </Button>
          )}
        </Space>
      </div>

      <SortableTable
        columns={columns}
        dataSource={notices}
        rowKey="noticeId"
        loading={loading}
        tableKey="notice"
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `총 ${total}건`,
          onChange: (page, pageSize) => fetchNotices(page, pageSize),
        }}
      />

      {/* 등록/수정 모달 */}
      <Modal
        title={editingNotice ? '공지사항 수정' : '공지사항 등록'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="title"
            label="제목"
            rules={[{ required: true, message: '제목을 입력하세요' }]}
          >
            <Input maxLength={200} showCount />
          </Form.Item>
          <Form.Item
            name="category"
            label="카테고리"
            rules={[{ required: true, message: '카테고리를 선택하세요' }]}
          >
            <Select options={NOTICE_CATEGORY_OPTIONS} />
          </Form.Item>
          <Form.Item
            name="content"
            label="내용"
            rules={[{ required: true, message: '내용을 입력하세요' }]}
          >
            <TextArea rows={10} />
          </Form.Item>
          <Space>
            {!editingNotice && (
              <Form.Item name="isPublished" valuePropName="checked" style={{ marginBottom: 0 }}>
                <Switch checkedChildren="공개" unCheckedChildren="비공개" />
              </Form.Item>
            )}
            <Form.Item name="isPinned" valuePropName="checked" style={{ marginBottom: 0 }}>
              <Switch checkedChildren="고정" unCheckedChildren="일반" />
            </Form.Item>
          </Space>
          <Form.Item style={{ marginTop: 24, marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setModalVisible(false)}>취소</Button>
              <Button type="primary" htmlType="submit">
                {editingNotice ? '수정' : '등록'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 상세 보기 모달 */}
      <Modal
        title={selectedNotice?.title}
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            닫기
          </Button>,
          ...(isAdmin ? [
            <Button
              key="publish"
              onClick={() => {
                if (selectedNotice) {
                  handleTogglePublish(selectedNotice.noticeId);
                  setDetailModalVisible(false);
                }
              }}
            >
              {selectedNotice?.isPublished ? '비공개로 전환' : '공개로 전환'}
            </Button>,
          ] : []),
        ]}
        width={700}
      >
        {selectedNotice && (
          <div>
            <Space style={{ marginBottom: 16 }}>
              <Tag color={selectedNotice.isPinned ? 'gold' : 'default'}>
                {selectedNotice.isPinned ? '고정' : '일반'}
              </Tag>
              <Tag color={selectedNotice.isPublished ? 'green' : 'default'}>
                {selectedNotice.isPublished ? '공개' : '비공개'}
              </Tag>
              <Tag>{selectedNotice.categoryDesc}</Tag>
            </Space>
            <div style={{ color: colors.textSecondary, marginBottom: 16 }}>
              작성자: {selectedNotice.authorName} | 조회수: {selectedNotice.viewCount} |
              발행일: {selectedNotice.publishedAt ? new Date(selectedNotice.publishedAt).toLocaleString('ko-KR') : '-'}
            </div>
            <Card style={{ background: colors.bgElevated }}>
              <div style={{ whiteSpace: 'pre-wrap', color: colors.textPrimary }}>
                {selectedNotice.content}
              </div>
            </Card>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default NoticePage;

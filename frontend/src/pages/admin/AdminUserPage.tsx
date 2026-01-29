/**
 * 사용자 관리 페이지 컴포넌트 (관리자 전용)
 *
 * 시스템 사용자 계정을 관리하는 페이지입니다.
 * 사용자 등록/수정/삭제, 역할(Role) 변경, 비밀번호 초기화,
 * 계정 잠금/해제 기능을 제공합니다.
 * 사용자 역할은 ADMIN, OPERATOR, VIEWER로 구분되며,
 * 역할에 따라 시스템 접근 권한이 달라집니다.
 *
 * @returns 사용자 관리 페이지 JSX
 */
import React, { useEffect, useState } from 'react';
import {
  Button,
  Tag,
  Space,
  Modal,
  Form,
  Input,
  Select,
  message,
  Popconfirm,
  Typography,
  Switch,
} from 'antd';
import SortableTable from '../../components/SortableTable';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  UnlockOutlined,
  KeyOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { User, ApiResponse, PageResponse } from '../../types';
import apiClient from '../../api/client';
import { minLengthRule, maxLengthRule, passwordStrengthRule, phoneNumberRule } from '../../utils/validators';

const { Title } = Typography;

const roleOptions = [
  { label: '관리자', value: 'ADMIN' },
  { label: '담당자', value: 'MANAGER' },
  { label: '운전자', value: 'DRIVER' },
];

const AdminUserPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);                          // 사용자 목록
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [createModalVisible, setCreateModalVisible] = useState(false);     // 사용자 추가 모달
  const [roleModalVisible, setRoleModalVisible] = useState(false);         // 역할 변경 모달
  const [passwordModalVisible, setPasswordModalVisible] = useState(false); // 비밀번호 초기화 모달
  const [selectedUser, setSelectedUser] = useState<User | null>(null);     // 선택된 사용자
  const [form] = Form.useForm();           // 사용자 추가 폼
  const [roleForm] = Form.useForm();       // 역할 변경 폼
  const [passwordForm] = Form.useForm();   // 비밀번호 초기화 폼

  const fetchUsers = async (page = 1, size = 10) => {
    setLoading(true);
    try {
      const response = await apiClient.get('/users', {
        params: { page: page - 1, size },
      });
      const result: ApiResponse<PageResponse<User>> = response.data;
      if (result.success) {
        setUsers(result.data.content);
        setPagination({
          current: result.data.number + 1,
          pageSize: result.data.size,
          total: result.data.totalElements,
        });
      }
    } catch (error) {
      message.error('사용자 목록을 불러오는데 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleCreate = async (values: any) => {
    try {
      const response = await apiClient.post('/users', values);
      const result = response.data;
      if (result.success) {
        message.success('사용자가 생성되었습니다');
        setCreateModalVisible(false);
        form.resetFields();
        fetchUsers(pagination.current);
      } else {
        message.error(result.error?.message || '생성에 실패했습니다');
      }
    } catch (error) {
      message.error('생성에 실패했습니다');
    }
  };

  /** 사용자 계정 활성/비활성 상태 토글 */
  const handleToggleActive = async (userId: number) => {
    try {
      const response = await apiClient.put(`/users/${userId}/toggle-active`);
      const result = response.data;
      if (result.success) {
        message.success('상태가 변경되었습니다');
        fetchUsers(pagination.current);
      } else {
        message.error(result.error?.message || '상태 변경에 실패했습니다');
      }
    } catch (error) {
      message.error('상태 변경에 실패했습니다');
    }
  };

  /** 로그인 실패 횟수 초과로 잠긴 계정 해제 */
  const handleUnlock = async (userId: number) => {
    try {
      const response = await apiClient.put(`/users/${userId}/unlock`);
      const result = response.data;
      if (result.success) {
        message.success('잠금이 해제되었습니다');
        fetchUsers(pagination.current);
      } else {
        message.error(result.error?.message || '잠금 해제에 실패했습니다');
      }
    } catch (error) {
      message.error('잠금 해제에 실패했습니다');
    }
  };

  /** 선택된 사용자의 역할(ADMIN/MANAGER/DRIVER) 변경 */
  const handleRoleChange = async (values: { userRole: string }) => {
    if (!selectedUser) return;
    try {
      const response = await apiClient.put(`/users/${selectedUser.userId}/role`, values);
      const result = response.data;
      if (result.success) {
        message.success('역할이 변경되었습니다');
        setRoleModalVisible(false);
        roleForm.resetFields();
        fetchUsers(pagination.current);
      } else {
        message.error(result.error?.message || '역할 변경에 실패했습니다');
      }
    } catch (error) {
      message.error('역할 변경에 실패했습니다');
    }
  };

  /** 관리자에 의한 사용자 비밀번호 강제 초기화 */
  const handlePasswordReset = async (values: { newPassword: string }) => {
    if (!selectedUser) return;
    try {
      const response = await apiClient.put(`/users/${selectedUser.userId}/reset-password`, values);
      const result = response.data;
      if (result.success) {
        message.success('비밀번호가 초기화되었습니다');
        setPasswordModalVisible(false);
        passwordForm.resetFields();
      } else {
        message.error(result.error?.message || '비밀번호 초기화에 실패했습니다');
      }
    } catch (error) {
      message.error('비밀번호 초기화에 실패했습니다');
    }
  };

  const handleDelete = async (userId: number) => {
    try {
      const response = await apiClient.delete(`/users/${userId}`);
      const result = response.data;
      if (result.success) {
        message.success('사용자가 삭제되었습니다');
        fetchUsers(pagination.current);
      } else {
        message.error(result.error?.message || '삭제에 실패했습니다');
      }
    } catch (error) {
      message.error('삭제에 실패했습니다');
    }
  };

  const columns: ColumnsType<User> = [
    {
      title: 'ID',
      dataIndex: 'userId',
      width: 80,
    },
    {
      title: '로그인 ID',
      dataIndex: 'loginId',
      width: 130,
    },
    {
      title: '이름',
      dataIndex: 'userName',
      width: 100,
    },
    {
      title: '연락처',
      dataIndex: 'phoneNumber',
      width: 130,
    },
    {
      title: '역할',
      dataIndex: 'userRole',
      width: 100,
      render: (role: string) => {
        const colorMap: Record<string, string> = {
          ADMIN: 'red',
          MANAGER: 'blue',
          DRIVER: 'green',
        };
        const labelMap: Record<string, string> = {
          ADMIN: '관리자',
          MANAGER: '담당자',
          DRIVER: '운전자',
        };
        return <Tag color={colorMap[role]}>{labelMap[role]}</Tag>;
      },
    },
    {
      title: '상태',
      dataIndex: 'isActive',
      width: 80,
      render: (isActive: boolean, record) => (
        <Switch
          checked={isActive}
          onChange={() => handleToggleActive(record.userId)}
          checkedChildren="활성"
          unCheckedChildren="비활성"
          size="small"
        />
      ),
    },
    {
      title: '잠금',
      dataIndex: 'lockedUntil',
      width: 80,
      render: (lockedUntil: string | null, record) => {
        if (!lockedUntil) return <Tag color="green">정상</Tag>;
        const isLocked = new Date(lockedUntil) > new Date();
        return isLocked ? (
          <Button
            type="link"
            size="small"
            icon={<UnlockOutlined />}
            onClick={() => handleUnlock(record.userId)}
            danger
          >
            잠금
          </Button>
        ) : (
          <Tag color="green">정상</Tag>
        );
      },
    },
    {
      title: '작업',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => {
              setSelectedUser(record);
              roleForm.setFieldsValue({ userRole: record.userRole });
              setRoleModalVisible(true);
            }}
          >
            역할
          </Button>
          <Button
            type="text"
            size="small"
            icon={<KeyOutlined />}
            onClick={() => {
              setSelectedUser(record);
              setPasswordModalVisible(true);
            }}
          >
            비밀번호
          </Button>
          <Popconfirm
            title="정말 삭제하시겠습니까?"
            onConfirm={() => handleDelete(record.userId)}
            okText="삭제"
            cancelText="취소"
          >
            <Button type="text" size="small" icon={<DeleteOutlined />} danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>사용자 관리</Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setCreateModalVisible(true)}
        >
          사용자 추가
        </Button>
      </div>

      <SortableTable
        columns={columns}
        dataSource={users}
        rowKey="userId"
        loading={loading}
        tableKey="adminUser"
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `총 ${total}건`,
        }}
        onChange={(p) => fetchUsers(p.current, p.pageSize)}
        size="small"
      />

      {/* 사용자 생성 모달 */}
      <Modal
        title="사용자 추가"
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          form.resetFields();
        }}
        onOk={() => form.submit()}
        okText="생성"
        cancelText="취소"
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="loginId"
            label="로그인 ID"
            rules={[{ required: true, message: '로그인 ID를 입력하세요' }, minLengthRule(3), maxLengthRule(50)]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="password"
            label="비밀번호"
            rules={[
              { required: true, message: '비밀번호를 입력하세요' },
              { min: 8, message: '8자 이상 입력하세요' },
              maxLengthRule(100),
              passwordStrengthRule,
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="userName"
            label="이름"
            rules={[{ required: true, message: '이름을 입력하세요' }, maxLengthRule(50)]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="phoneNumber"
            label="연락처"
            rules={[{ required: true, message: '연락처를 입력하세요' }, phoneNumberRule]}
          >
            <Input placeholder="010-0000-0000" />
          </Form.Item>
          <Form.Item
            name="userRole"
            label="역할"
            rules={[{ required: true, message: '역할을 선택하세요' }]}
          >
            <Select options={roleOptions} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 역할 변경 모달 */}
      <Modal
        title={`역할 변경 - ${selectedUser?.userName}`}
        open={roleModalVisible}
        onCancel={() => {
          setRoleModalVisible(false);
          roleForm.resetFields();
        }}
        onOk={() => roleForm.submit()}
        okText="변경"
        cancelText="취소"
      >
        <Form form={roleForm} layout="vertical" onFinish={handleRoleChange}>
          <Form.Item
            name="userRole"
            label="역할"
            rules={[{ required: true, message: '역할을 선택하세요' }]}
          >
            <Select options={roleOptions} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 비밀번호 초기화 모달 */}
      <Modal
        title={`비밀번호 초기화 - ${selectedUser?.userName}`}
        open={passwordModalVisible}
        onCancel={() => {
          setPasswordModalVisible(false);
          passwordForm.resetFields();
        }}
        onOk={() => passwordForm.submit()}
        okText="초기화"
        cancelText="취소"
      >
        <Form form={passwordForm} layout="vertical" onFinish={handlePasswordReset}>
          <Form.Item
            name="newPassword"
            label="새 비밀번호"
            rules={[
              { required: true, message: '새 비밀번호를 입력하세요' },
              { min: 8, message: '8자 이상 입력하세요' },
              maxLengthRule(100),
              passwordStrengthRule,
            ]}
          >
            <Input.Password />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminUserPage;

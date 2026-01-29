import React, { useCallback, useEffect, useState } from 'react';
import {
  Typography,
  Tabs,
  Form,
  Input,
  Button,
  Switch,
  Card,
  Descriptions,
  message,
  Divider,
} from 'antd';
import {
  UserOutlined,
  LockOutlined,
  BellOutlined,
  SaveOutlined,
} from '@ant-design/icons';
import apiClient from '../api/client';

interface MyProfile {
  userId: number;
  loginId: string;
  userName: string;
  phoneNumber: string;
  email?: string;
  userRole: string;
  userRoleDesc: string;
  companyId?: number;
  companyName?: string;
  pushEnabled: boolean;
  emailEnabled: boolean;
  createdAt: string;
  lastLoginAt?: string;
}

const MyPage: React.FC = () => {
  const [profile, setProfile] = useState<MyProfile | null>(null);
  const [loading, setLoading] = useState(false);
  const [profileForm] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [notificationForm] = Form.useForm();

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/mypage');
      const data = res.data.data;
      setProfile(data);
      profileForm.setFieldsValue({
        userName: data.userName,
        phoneNumber: data.phoneNumber,
        email: data.email,
      });
      notificationForm.setFieldsValue({
        pushEnabled: data.pushEnabled,
        emailEnabled: data.emailEnabled,
      });
    } catch {
      message.error('프로필 정보를 불러오는데 실패했습니다.');
    }
    setLoading(false);
  }, [profileForm, notificationForm]);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const handleProfileSubmit = async (values: {
    userName: string;
    phoneNumber: string;
    email?: string;
  }) => {
    try {
      const res = await apiClient.put('/mypage/profile', values);
      setProfile(res.data.data);
      message.success('프로필이 업데이트되었습니다.');
    } catch {
      message.error('프로필 업데이트에 실패했습니다.');
    }
  };

  const handlePasswordSubmit = async (values: {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
  }) => {
    try {
      await apiClient.put('/mypage/password', values);
      message.success('비밀번호가 변경되었습니다.');
      passwordForm.resetFields();
    } catch (error: unknown) {
      const err = error as { response?: { data?: { error?: { message?: string } } } };
      const errorMsg = err.response?.data?.error?.message || '비밀번호 변경에 실패했습니다.';
      message.error(errorMsg);
    }
  };

  const handleNotificationSubmit = async (values: {
    pushEnabled: boolean;
    emailEnabled: boolean;
  }) => {
    try {
      const res = await apiClient.put('/mypage/notifications', values);
      setProfile(res.data.data);
      message.success('알림 설정이 업데이트되었습니다.');
    } catch {
      message.error('알림 설정 업데이트에 실패했습니다.');
    }
  };

  return (
    <>
      <Typography.Title level={4}>마이페이지</Typography.Title>

      <Tabs
        defaultActiveKey="profile"
        items={[
          {
            key: 'profile',
            label: (
              <span>
                <UserOutlined />
                프로필
              </span>
            ),
            children: (
              <Card loading={loading}>
                {profile && (
                  <>
                    <Descriptions
                      title="기본 정보"
                      bordered
                      column={2}
                      size="small"
                      style={{ marginBottom: 24 }}
                    >
                      <Descriptions.Item label="로그인 ID">
                        {profile.loginId}
                      </Descriptions.Item>
                      <Descriptions.Item label="역할">
                        {profile.userRoleDesc}
                      </Descriptions.Item>
                      <Descriptions.Item label="소속 업체">
                        {profile.companyName || '-'}
                      </Descriptions.Item>
                      <Descriptions.Item label="가입일">
                        {profile.createdAt
                          ? new Date(profile.createdAt).toLocaleDateString()
                          : '-'}
                      </Descriptions.Item>
                    </Descriptions>

                    <Divider />

                    <Typography.Title level={5}>프로필 수정</Typography.Title>
                    <Form
                      form={profileForm}
                      layout="vertical"
                      onFinish={handleProfileSubmit}
                      style={{ maxWidth: 400 }}
                    >
                      <Form.Item
                        name="userName"
                        label="이름"
                        rules={[{ required: true, message: '이름을 입력하세요' }]}
                      >
                        <Input prefix={<UserOutlined />} />
                      </Form.Item>
                      <Form.Item
                        name="phoneNumber"
                        label="연락처"
                        rules={[{ required: true, message: '연락처를 입력하세요' }]}
                      >
                        <Input placeholder="010-0000-0000" />
                      </Form.Item>
                      <Form.Item name="email" label="이메일">
                        <Input type="email" placeholder="email@example.com" />
                      </Form.Item>
                      <Form.Item>
                        <Button
                          type="primary"
                          htmlType="submit"
                          icon={<SaveOutlined />}
                        >
                          저장
                        </Button>
                      </Form.Item>
                    </Form>
                  </>
                )}
              </Card>
            ),
          },
          {
            key: 'password',
            label: (
              <span>
                <LockOutlined />
                비밀번호 변경
              </span>
            ),
            children: (
              <Card>
                <Form
                  form={passwordForm}
                  layout="vertical"
                  onFinish={handlePasswordSubmit}
                  style={{ maxWidth: 400 }}
                >
                  <Form.Item
                    name="currentPassword"
                    label="현재 비밀번호"
                    rules={[
                      { required: true, message: '현재 비밀번호를 입력하세요' },
                    ]}
                  >
                    <Input.Password prefix={<LockOutlined />} />
                  </Form.Item>
                  <Form.Item
                    name="newPassword"
                    label="새 비밀번호"
                    rules={[
                      { required: true, message: '새 비밀번호를 입력하세요' },
                      { min: 8, message: '비밀번호는 8자 이상이어야 합니다' },
                    ]}
                  >
                    <Input.Password prefix={<LockOutlined />} />
                  </Form.Item>
                  <Form.Item
                    name="confirmPassword"
                    label="새 비밀번호 확인"
                    dependencies={['newPassword']}
                    rules={[
                      { required: true, message: '새 비밀번호 확인을 입력하세요' },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue('newPassword') === value) {
                            return Promise.resolve();
                          }
                          return Promise.reject(
                            new Error('비밀번호가 일치하지 않습니다'),
                          );
                        },
                      }),
                    ]}
                  >
                    <Input.Password prefix={<LockOutlined />} />
                  </Form.Item>
                  <Form.Item>
                    <Button
                      type="primary"
                      htmlType="submit"
                      icon={<SaveOutlined />}
                    >
                      비밀번호 변경
                    </Button>
                  </Form.Item>
                </Form>
              </Card>
            ),
          },
          {
            key: 'notifications',
            label: (
              <span>
                <BellOutlined />
                알림 설정
              </span>
            ),
            children: (
              <Card loading={loading}>
                <Form
                  form={notificationForm}
                  layout="vertical"
                  onFinish={handleNotificationSubmit}
                  style={{ maxWidth: 400 }}
                >
                  <Form.Item
                    name="pushEnabled"
                    label="푸시 알림"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="ON" unCheckedChildren="OFF" />
                  </Form.Item>
                  <Typography.Text type="secondary" style={{ display: 'block', marginTop: -16, marginBottom: 24 }}>
                    배차 배정, 계량 완료 등의 알림을 푸시로 받습니다.
                  </Typography.Text>

                  <Form.Item
                    name="emailEnabled"
                    label="이메일 알림"
                    valuePropName="checked"
                  >
                    <Switch checkedChildren="ON" unCheckedChildren="OFF" />
                  </Form.Item>
                  <Typography.Text type="secondary" style={{ display: 'block', marginTop: -16, marginBottom: 24 }}>
                    중요 알림을 이메일로 받습니다.
                  </Typography.Text>

                  <Form.Item>
                    <Button
                      type="primary"
                      htmlType="submit"
                      icon={<SaveOutlined />}
                    >
                      저장
                    </Button>
                  </Form.Item>
                </Form>
              </Card>
            ),
          },
        ]}
      />
    </>
  );
};

export default MyPage;

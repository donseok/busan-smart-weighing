import React from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  const onFinish = async (values: { loginId: string; password: string }) => {
    try {
      const res = await apiClient.post('/auth/login', values);
      localStorage.setItem('accessToken', res.data.data.accessToken);
      navigate('/dashboard');
    } catch {
      message.error('로그인에 실패했습니다.');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 400 }}>
        <Typography.Title level={3} style={{ textAlign: 'center' }}>부산 스마트 계량 시스템</Typography.Title>
        <Form onFinish={onFinish} size="large">
          <Form.Item name="loginId" rules={[{ required: true, message: '아이디를 입력하세요' }]}>
            <Input prefix={<UserOutlined />} placeholder="아이디" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '비밀번호를 입력하세요' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="비밀번호" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>로그인</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;

import React from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { colors } from '../theme/themeConfig';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();

  const onFinish = async (values: { loginId: string; password: string }) => {
    try {
      const res = await apiClient.post('/auth/login', {
        loginId: values.loginId,
        password: values.password,
        deviceType: 'WEB',
      });
      localStorage.setItem('accessToken', res.data.data.accessToken);
      localStorage.setItem('refreshToken', res.data.data.refreshToken);
      navigate('/dashboard');
    } catch {
      message.error('로그인에 실패했습니다.');
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        background: `radial-gradient(ellipse at 50% 0%, rgba(6, 182, 212, 0.08) 0%, ${colors.bgBase} 70%)`,
      }}
    >
      <Card
        style={{
          width: 420,
          background: 'rgba(30, 41, 59, 0.7)',
          backdropFilter: 'blur(20px)',
          border: `1px solid ${colors.border}`,
          borderRadius: 16,
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.4)',
        }}
      >
        {/* 로고 */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div
            style={{
              width: 56,
              height: 56,
              borderRadius: 16,
              background: `linear-gradient(135deg, ${colors.primary}, #0891B2)`,
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 24,
              fontWeight: 700,
              color: '#fff',
              marginBottom: 16,
              boxShadow: `0 4px 16px rgba(6, 182, 212, 0.3)`,
            }}
          >
            B
          </div>
          <Typography.Title
            level={3}
            style={{ margin: 0, color: colors.textPrimary, letterSpacing: '-0.02em' }}
          >
            부산 스마트 계량
          </Typography.Title>
          <Typography.Text style={{ color: colors.textSecondary, fontSize: 13 }}>
            Smart Weighing System
          </Typography.Text>
        </div>

        <Form onFinish={onFinish} size="large">
          <Form.Item
            name="loginId"
            rules={[{ required: true, message: '아이디를 입력하세요' }]}
          >
            <Input
              prefix={<UserOutlined style={{ color: colors.textSecondary }} />}
              placeholder="아이디"
              style={{ borderRadius: 8, height: 44 }}
            />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: '비밀번호를 입력하세요' }]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: colors.textSecondary }} />}
              placeholder="비밀번호"
              style={{ borderRadius: 8, height: 44 }}
            />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button
              type="primary"
              htmlType="submit"
              block
              style={{
                height: 44,
                borderRadius: 8,
                fontWeight: 600,
                fontSize: 15,
              }}
            >
              로그인
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;

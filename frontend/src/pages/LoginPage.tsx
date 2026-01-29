/**
 * 로그인 페이지 컴포넌트
 *
 * JWT 기반 인증을 위한 로그인 폼을 제공하는 페이지입니다.
 * 사용자 ID와 비밀번호를 입력받아 서버 인증 후
 * JWT 토큰을 localStorage에 저장하고 대시보드로 이동합니다.
 * 인증 실패 시 오류 메시지를 표시합니다.
 *
 * @returns 로그인 페이지 JSX
 */
import React from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import { darkColors, lightColors } from '../theme/themeConfig';
import { useTheme } from '../context/ThemeContext';
import { useAuth } from '../context/AuthContext';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { themeMode } = useTheme();
  const { login } = useAuth();
  const colors = themeMode === 'dark' ? darkColors : lightColors;
  const isDark = themeMode === 'dark';

  /** 로그인 폼 제출 - JWT 토큰 저장 후 대시보드로 이동 */
  const onFinish = async (values: { loginId: string; password: string }) => {
    try {
      const res = await apiClient.post('/auth/login', {
        loginId: values.loginId,
        password: values.password,
        deviceType: 'WEB',
      });
      login(res.data.data.accessToken, res.data.data.refreshToken);
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
        background: isDark
          ? `radial-gradient(ellipse at 50% 0%, rgba(6, 182, 212, 0.08) 0%, ${colors.bgBase} 70%)`
          : `radial-gradient(ellipse at 50% 0%, rgba(8, 145, 178, 0.06) 0%, ${colors.bgBase} 70%)`,
        transition: 'background 0.3s ease',
      }}
    >
      <Card
        style={{
          width: 420,
          background: isDark ? 'rgba(30, 41, 59, 0.7)' : 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          border: `1px solid ${colors.border}`,
          borderRadius: 16,
          boxShadow: isDark
            ? '0 8px 32px rgba(0, 0, 0, 0.4)'
            : '0 8px 32px rgba(0, 0, 0, 0.1)',
        }}
      >
        {/* 로고 */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div
            style={{
              width: 56,
              height: 56,
              borderRadius: 16,
              background: `linear-gradient(135deg, ${colors.primary}, ${isDark ? '#0891B2' : '#06B6D4'})`,
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 20,
              fontWeight: 700,
              color: '#fff',
              marginBottom: 16,
              boxShadow: isDark
                ? '0 4px 16px rgba(6, 182, 212, 0.3)'
                : '0 4px 16px rgba(8, 145, 178, 0.25)',
            }}
          >
            DK
          </div>
          <Typography.Title
            level={3}
            style={{ margin: 0, color: colors.textPrimary, letterSpacing: '-0.02em' }}
          >
            동국씨엠 스마트 계량
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

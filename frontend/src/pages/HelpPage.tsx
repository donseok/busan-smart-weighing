/**
 * 도움말 페이지 컴포넌트
 *
 * 시스템 사용 안내와 자주 묻는 질문(FAQ)을 제공하는 페이지입니다.
 * FAQ 탭과 시스템 안내 탭으로 구성되며,
 * FAQ는 카테고리별 아코디언 형태로 질문/답변을 표시합니다.
 * 시스템 안내에서는 주요 기능별 사용 방법을 안내합니다.
 *
 * @returns 도움말 페이지 JSX
 */
import React, { useCallback, useEffect, useState } from 'react';
import {
  Typography,
  Tabs,
  Collapse,
  Select,
  Space,
  Card,
  Divider,
  message,
  Empty,
  Spin,
} from 'antd';
import {
  QuestionCircleOutlined,
  BookOutlined,
  CarOutlined,
  UserOutlined,
  SettingOutlined,
  EllipsisOutlined,
} from '@ant-design/icons';
import apiClient from '../api/client';

interface Faq {
  faqId: number;
  question: string;
  answer: string;
  category: string;
  categoryDesc: string;
  sortOrder: number;
  viewCount: number;
}

const categoryOptions = [
  { value: '', label: '전체', icon: <QuestionCircleOutlined /> },
  { value: 'WEIGHING', label: '계량', icon: <BookOutlined /> },
  { value: 'DISPATCH', label: '배차', icon: <CarOutlined /> },
  { value: 'ACCOUNT', label: '계정', icon: <UserOutlined /> },
  { value: 'SYSTEM', label: '시스템', icon: <SettingOutlined /> },
  { value: 'OTHER', label: '기타', icon: <EllipsisOutlined /> },
];

const HelpPage: React.FC = () => {
  const [faqs, setFaqs] = useState<Faq[]>([]);
  const [loading, setLoading] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState<string>('');

  const fetchFaqs = useCallback(async () => {
    setLoading(true);
    try {
      const url = categoryFilter
        ? `/help/faqs/category/${categoryFilter}`
        : '/help/faqs';
      const res = await apiClient.get(url);
      setFaqs(res.data.data || []);
    } catch {
      message.error('FAQ를 불러오는데 실패했습니다.');
    }
    setLoading(false);
  }, [categoryFilter]);

  useEffect(() => {
    fetchFaqs();
  }, [fetchFaqs]);

  const groupedFaqs = faqs.reduce((acc, faq) => {
    const category = faq.categoryDesc;
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(faq);
    return acc;
  }, {} as Record<string, Faq[]>);

  return (
    <>
      <Typography.Title level={4}>이용 안내</Typography.Title>

      <Tabs
        defaultActiveKey="faq"
        items={[
          {
            key: 'faq',
            label: (
              <span>
                <QuestionCircleOutlined />
                자주 묻는 질문 (FAQ)
              </span>
            ),
            children: (
              <>
                <Space style={{ marginBottom: 16 }}>
                  <Select
                    placeholder="카테고리 선택"
                    style={{ width: 160 }}
                    value={categoryFilter}
                    onChange={setCategoryFilter}
                    options={categoryOptions.map((opt) => ({
                      value: opt.value,
                      label: (
                        <Space>
                          {opt.icon}
                          {opt.label}
                        </Space>
                      ),
                    }))}
                  />
                </Space>

                <Spin spinning={loading}>
                  {faqs.length === 0 ? (
                    <Empty description="등록된 FAQ가 없습니다." />
                  ) : categoryFilter ? (
                    <Collapse
                      accordion
                      items={faqs.map((faq) => ({
                        key: faq.faqId,
                        label: (
                          <span>
                            <QuestionCircleOutlined style={{ marginRight: 8, color: '#1890ff' }} />
                            {faq.question}
                          </span>
                        ),
                        children: (
                          <div style={{ whiteSpace: 'pre-wrap' }}>{faq.answer}</div>
                        ),
                      }))}
                    />
                  ) : (
                    Object.entries(groupedFaqs).map(([category, categoryFaqs]) => (
                      <Card
                        key={category}
                        title={category}
                        size="small"
                        style={{ marginBottom: 16 }}
                      >
                        <Collapse
                          accordion
                          bordered={false}
                          items={categoryFaqs.map((faq) => ({
                            key: faq.faqId,
                            label: (
                              <span>
                                <QuestionCircleOutlined style={{ marginRight: 8, color: '#1890ff' }} />
                                {faq.question}
                              </span>
                            ),
                            children: (
                              <div style={{ whiteSpace: 'pre-wrap' }}>{faq.answer}</div>
                            ),
                          }))}
                        />
                      </Card>
                    ))
                  )}
                </Spin>
              </>
            ),
          },
          {
            key: 'guide',
            label: (
              <span>
                <BookOutlined />
                시스템 이용 안내
              </span>
            ),
            children: (
              <Card>
                <Typography.Title level={5}>동국씨엠 스마트 계량 시스템</Typography.Title>
                <Typography.Paragraph>
                  본 시스템은 동국씨엠 부산공장의 차량 계량 업무를 효율적으로 관리하기 위한
                  통합 플랫폼입니다.
                </Typography.Paragraph>

                <Divider />

                <Typography.Title level={5}>주요 기능</Typography.Title>

                <Typography.Title level={5} style={{ marginTop: 24 }}>
                  1. 배차 관리
                </Typography.Title>
                <Typography.Paragraph>
                  <ul>
                    <li>차량 배차 등록, 수정, 삭제</li>
                    <li>배차 상태 관리 (등록 → 진행중 → 완료)</li>
                    <li>배차 이력 조회 및 검색</li>
                  </ul>
                </Typography.Paragraph>

                <Typography.Title level={5} style={{ marginTop: 24 }}>
                  2. 계량 현황
                </Typography.Title>
                <Typography.Paragraph>
                  <ul>
                    <li>실시간 계량 현황 모니터링</li>
                    <li>LPR 자동 차량번호 인식</li>
                    <li>모바일 OTP 인증 계량</li>
                    <li>계량 이력 조회</li>
                  </ul>
                </Typography.Paragraph>

                <Typography.Title level={5} style={{ marginTop: 24 }}>
                  3. 출문 관리
                </Typography.Title>
                <Typography.Paragraph>
                  <ul>
                    <li>계량 완료 차량 출문 승인/거부</li>
                    <li>출문 이력 관리</li>
                  </ul>
                </Typography.Paragraph>

                <Typography.Title level={5} style={{ marginTop: 24 }}>
                  4. 전자 계량표
                </Typography.Title>
                <Typography.Paragraph>
                  <ul>
                    <li>계량 완료 시 자동 발행</li>
                    <li>PDF 다운로드 및 이메일 공유</li>
                    <li>QR 코드를 통한 진위 확인</li>
                  </ul>
                </Typography.Paragraph>

                <Typography.Title level={5} style={{ marginTop: 24 }}>
                  5. 통계 및 보고서
                </Typography.Title>
                <Typography.Paragraph>
                  <ul>
                    <li>일별/월별 계량 통계</li>
                    <li>업체별, 품목별 분석</li>
                    <li>Excel 다운로드</li>
                  </ul>
                </Typography.Paragraph>

                <Divider />

                <Typography.Title level={5}>문의처</Typography.Title>
                <Typography.Paragraph>
                  <ul>
                    <li>전화: 051-123-4567</li>
                    <li>이메일: support@dongkuk.com</li>
                    <li>운영시간: 평일 08:00 ~ 18:00</li>
                  </ul>
                </Typography.Paragraph>
              </Card>
            ),
          },
        ]}
      />
    </>
  );
};

export default HelpPage;

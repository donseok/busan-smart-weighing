/**
 * @fileoverview 대시보드 페이지 컴포넌트
 *
 * 계량 통계, 실시간 현황, 분석 탭을 포함하는 메인 대시보드 페이지입니다.
 * WebSocket을 통해 실시간 계량 데이터를 수신하고, 차트 옵션을 생성하여
 * 개요(OverviewTab), 실시간(RealtimeTab), 분석(AnalysisTab) 탭에 전달합니다.
 *
 * @module pages/DashboardPage
 */
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Card, Spin, Typography, Tabs, Tag, Space, Modal, Skeleton, Row, Col } from 'antd';
import { NotificationOutlined } from '@ant-design/icons';
import apiClient from '../api/client';
import { useWebSocket } from '../hooks/useWebSocket';
import type { WebSocketMessage } from '../hooks/useWebSocket';
import { useTabVisible } from '../hooks/useTabVisible';
import type {
  ApiResponse,
  WeighingStatistics,
  DashboardSummary,
  CompanyStatistics,
  WeighingRecord,
  Notice,
} from '../types';
import { darkColors, lightColors } from '../theme/themeConfig';
import { useTheme } from '../context/ThemeContext';
import {
  buildLineChartOption,
  buildPieChartOption,
  buildBarChartOption,
  buildCompanyBarChartOption,
  buildCompanyTopChartOption,
} from '../utils/chartOptions';
import OverviewTab from '../components/dashboard/OverviewTab';
import RealtimeTab from '../components/dashboard/RealtimeTab';
import AnalysisTab from '../components/dashboard/AnalysisTab';

/**
 * 대시보드 페이지 컴포넌트
 *
 * 계량 통계, 배차/출문증 현황, 운송사별 통계 등 종합 대시보드를 렌더링합니다.
 * 개요, 실시간 현황, 분석 세 개의 탭으로 구성되며,
 * WebSocket 메시지 수신 시 통계 데이터를 자동 갱신합니다.
 */
const DashboardPage: React.FC = () => {
  const { themeMode } = useTheme();
  const colors = themeMode === 'dark' ? darkColors : lightColors;
  const chartCtx = useMemo(() => ({ colors, themeMode: themeMode as 'dark' | 'light' }), [colors, themeMode]);
  const isVisible = useTabVisible('/dashboard');

  const [statistics, setStatistics] = useState<WeighingStatistics | null>(null);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [companyStats, setCompanyStats] = useState<CompanyStatistics[]>([]);
  const [inProgressWeighings, setInProgressWeighings] = useState<WeighingRecord[]>([]);
  const [pinnedNotices, setPinnedNotices] = useState<Notice[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [noticeModalVisible, setNoticeModalVisible] = useState(false);
  const [selectedNotice, setSelectedNotice] = useState<Notice | null>(null);
  const [noticeLoading, setNoticeLoading] = useState(false);

  const handleNoticeClick = useCallback(async (noticeId: number) => {
    setNoticeLoading(true);
    setNoticeModalVisible(true);
    try {
      const res = await apiClient.get<ApiResponse<Notice>>(`/notices/${noticeId}`);
      setSelectedNotice(res.data.data);
    } catch {
      setSelectedNotice(null);
    } finally {
      setNoticeLoading(false);
    }
  }, []);

  const fetchStatistics = useCallback(async () => {
    try {
      const response = await apiClient.get<ApiResponse<WeighingStatistics>>('/weighings/statistics');
      setStatistics(response.data.data);
    } catch { /* ignore */ }
  }, []);

  const fetchSummary = useCallback(async () => {
    try {
      const response = await apiClient.get<ApiResponse<DashboardSummary>>('/dashboard/summary');
      setSummary(response.data.data);
    } catch { /* ignore */ }
  }, []);

  const fetchCompanyStats = useCallback(async () => {
    try {
      const response = await apiClient.get<ApiResponse<CompanyStatistics[]>>('/dashboard/company-stats');
      setCompanyStats(response.data.data || []);
    } catch { /* ignore */ }
  }, []);

  const fetchInProgressWeighings = useCallback(async () => {
    try {
      const response = await apiClient.get<ApiResponse<WeighingRecord[]>>('/weighings/in-progress');
      setInProgressWeighings(response.data.data || []);
    } catch { /* ignore */ }
  }, []);

  const fetchPinnedNotices = useCallback(async () => {
    try {
      const response = await apiClient.get<ApiResponse<Notice[]>>('/notices/pinned');
      setPinnedNotices(response.data.data || []);
    } catch { /* ignore */ }
  }, []);

  const fetchAllData = useCallback(async () => {
    setLoading(true);
    await Promise.all([
      fetchStatistics(),
      fetchSummary(),
      fetchCompanyStats(),
      fetchInProgressWeighings(),
      fetchPinnedNotices(),
    ]);
    setLoading(false);
  }, [fetchStatistics, fetchSummary, fetchCompanyStats, fetchInProgressWeighings, fetchPinnedNotices]);

  useEffect(() => { fetchAllData(); }, [fetchAllData]);

  const handleWebSocketMessage = useCallback((msg: WebSocketMessage) => {
    if (!isVisible) return;

    if (msg.type === 'DELTA' && msg.topic) {
      // Delta update - refresh only the affected data
      switch (msg.topic) {
        case 'statistics':
          fetchStatistics();
          break;
        case 'summary':
          fetchSummary();
          break;
        case 'in-progress':
          fetchInProgressWeighings();
          break;
        default:
          // Unknown topic - refresh all
          fetchStatistics();
          fetchSummary();
          fetchInProgressWeighings();
      }
    } else {
      // Full update or legacy format - refresh everything
      fetchStatistics();
      fetchSummary();
      fetchInProgressWeighings();
    }
  }, [isVisible, fetchStatistics, fetchSummary, fetchInProgressWeighings]);

  useWebSocket(handleWebSocketMessage);

  const lineChartOption = useMemo(() => buildLineChartOption(statistics, chartCtx), [statistics, chartCtx]);
  const pieChartOption = useMemo(() => buildPieChartOption(statistics, chartCtx), [statistics, chartCtx]);
  const barChartOption = useMemo(() => buildBarChartOption(statistics, chartCtx), [statistics, chartCtx]);
  const companyBarChartOption = useMemo(() => buildCompanyBarChartOption(companyStats, chartCtx), [companyStats, chartCtx]);
  const companyTopChartOption = useMemo(() => buildCompanyTopChartOption(companyStats, chartCtx), [companyStats, chartCtx]);

  const tabItems = [
    {
      key: 'overview',
      label: '개요',
      children: (
        <OverviewTab
          statistics={statistics}
          pinnedNotices={pinnedNotices}
          onNoticeClick={handleNoticeClick}
          lineChartOption={lineChartOption}
          pieChartOption={pieChartOption}
          barChartOption={barChartOption}
          companyTopChartOption={companyTopChartOption}
          colors={colors}
        />
      ),
    },
    {
      key: 'realtime',
      label: '실시간 현황',
      children: (
        <RealtimeTab
          summary={summary}
          inProgressWeighings={inProgressWeighings}
          colors={colors}
        />
      ),
    },
    {
      key: 'analysis',
      label: '분석',
      children: (
        <AnalysisTab
          companyStats={companyStats}
          companyBarChartOption={companyBarChartOption}
          lineChartOption={lineChartOption}
          colors={colors}
        />
      ),
    },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
      {/* 고정 영역: 제목 + 탭 헤더 + 공지사항 */}
      <div style={{ flexShrink: 0 }}>
        <Typography.Title level={4} style={{ marginBottom: 20 }}>대시보드</Typography.Title>
        {!loading && (
          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            items={tabItems.map(item => ({ key: item.key, label: item.label }))}
            style={{ marginBottom: 0 }}
          />
        )}
        {!loading && activeTab === 'overview' && pinnedNotices.length > 0 && (
          <div style={{ marginTop: 12, marginBottom: 4 }}>
            <Alert
              type="info"
              showIcon
              icon={<NotificationOutlined />}
              message={
                <Space style={{ cursor: 'pointer' }}>
                  <Tag color="red">공지</Tag>
                  {pinnedNotices[0].title}
                </Space>
              }
              style={{ borderRadius: 8, cursor: 'pointer' }}
              onClick={() => handleNoticeClick(pinnedNotices[0].noticeId)}
            />
          </div>
        )}
      </div>

      {/* 스크롤 영역: 탭 콘텐츠 */}
      <div style={{ flex: 1, overflowY: 'auto', overflowX: 'hidden', minHeight: 0, paddingTop: 16 }}>
        {loading ? (
          <div>
            <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
              {[1, 2, 3, 4].map(i => (
                <Col span={6} key={i}>
                  <Card><Skeleton active paragraph={{ rows: 1 }} /></Card>
                </Col>
              ))}
            </Row>
            <Row gutter={[16, 16]}>
              <Col span={12}><Card><Skeleton active paragraph={{ rows: 6 }} /></Card></Col>
              <Col span={12}><Card><Skeleton active paragraph={{ rows: 6 }} /></Card></Col>
            </Row>
          </div>
        ) : (
          <>
            {activeTab === 'overview' && (
              <OverviewTab
                statistics={statistics}
                pinnedNotices={[]}
                onNoticeClick={handleNoticeClick}
                lineChartOption={lineChartOption}
                pieChartOption={pieChartOption}
                barChartOption={barChartOption}
                companyTopChartOption={companyTopChartOption}
                colors={colors}
              />
            )}
            {activeTab === 'realtime' && (
              <RealtimeTab
                summary={summary}
                inProgressWeighings={inProgressWeighings}
                colors={colors}
              />
            )}
            {activeTab === 'analysis' && (
              <AnalysisTab
                companyStats={companyStats}
                companyBarChartOption={companyBarChartOption}
                lineChartOption={lineChartOption}
                colors={colors}
              />
            )}
          </>
        )}
      </div>

      <Modal
        title={<Space><NotificationOutlined />{selectedNotice?.title}</Space>}
        open={noticeModalVisible}
        onCancel={() => { setNoticeModalVisible(false); setSelectedNotice(null); }}
        footer={null}
        width={600}
      >
        <Spin spinning={noticeLoading}>
          {selectedNotice ? (
            <div>
              <div style={{ marginBottom: 12, color: colors.textSecondary, fontSize: 13 }}>
                {selectedNotice.publishedAt
                  ? new Date(selectedNotice.publishedAt).toLocaleString('ko-KR')
                  : new Date(selectedNotice.createdAt).toLocaleString('ko-KR')}
              </div>
              <Card style={{ background: colors.bgElevated, border: `1px solid ${colors.border}`, borderRadius: 8 }}>
                <div style={{ whiteSpace: 'pre-wrap', color: colors.textPrimary, lineHeight: 1.7 }}>
                  {selectedNotice.content}
                </div>
              </Card>
            </div>
          ) : (
            !noticeLoading && <div style={{ textAlign: 'center', color: colors.textSecondary, padding: 24 }}>내용을 불러올 수 없습니다.</div>
          )}
        </Spin>
      </Modal>
    </div>
  );
};

export default DashboardPage;

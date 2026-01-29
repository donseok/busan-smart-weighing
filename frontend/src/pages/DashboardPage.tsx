import React, { useCallback, useEffect, useState } from 'react';
import {
  Card,
  Col,
  Row,
  Spin,
  Statistic,
  Typography,
  Tabs,
  Table,
  Tag,
  Alert,
  Space,
  Badge,
  Progress,
  Modal,
} from 'antd';
import {
  CalendarOutlined,
  CarOutlined,
  CheckCircleOutlined,
  DashboardOutlined,
  ExperimentOutlined,
  NotificationOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  LogoutOutlined,
  StopOutlined,
} from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import { useWebSocket } from '../hooks/useWebSocket';
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

const ITEM_TYPE_LABELS: Record<string, string> = {
  BY_PRODUCT: '부산물',
  WASTE: '폐기물',
  SUB_MATERIAL: '부재료',
  EXPORT: '반출',
  GENERAL: '일반',
};

const WEIGHING_MODE_LABELS: Record<string, string> = {
  LPR_AUTO: 'LPR자동',
  MOBILE_OTP: '모바일OTP',
  MANUAL: '수동',
  RE_WEIGH: '재계량',
};

const DashboardPage: React.FC = () => {
  const { themeMode } = useTheme();
  const colors = themeMode === 'dark' ? darkColors : lightColors;

  const chartTextStyle = {
    color: colors.textSecondary,
    fontFamily: "'Inter', sans-serif",
  };

  const chartAxisStyle = {
    axisLine: { lineStyle: { color: colors.border } },
    axisTick: { lineStyle: { color: colors.border } },
    splitLine: { lineStyle: { color: themeMode === 'dark' ? 'rgba(51, 65, 85, 0.5)' : 'rgba(226, 232, 240, 0.8)' } },
    axisLabel: { color: colors.textSecondary },
    nameTextStyle: { color: colors.textSecondary },
  };

  const statCards = [
    { key: 'todayTotal', title: '오늘 전체 건수', icon: <CarOutlined />, field: 'todayTotalCount', suffix: '건', color: colors.primary },
    { key: 'inProgress', title: '계량 진행 중', icon: <ExperimentOutlined />, field: 'todayInProgressCount', suffix: '건', color: colors.warning },
    { key: 'completed', title: '오늘 완료', icon: <CheckCircleOutlined />, field: 'todayCompletedCount', suffix: '건', color: colors.success },
    { key: 'monthTotal', title: '이번 달 전체', icon: <CalendarOutlined />, field: 'monthTotalCount', suffix: '건', color: '#8B5CF6' },
  ] as const;

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

  useEffect(() => {
    fetchAllData();
  }, [fetchAllData]);

  const handleWebSocketMessage = useCallback(() => {
    fetchStatistics();
    fetchSummary();
    fetchInProgressWeighings();
  }, [fetchStatistics, fetchSummary, fetchInProgressWeighings]);

  useWebSocket(handleWebSocketMessage);

  // 실시간 계량 현황 테이블 컬럼
  const inProgressColumns: ColumnsType<WeighingRecord> = [
    { title: 'ID', dataIndex: 'weighingId', width: 60 },
    {
      title: '계량방식',
      dataIndex: 'weighingMode',
      width: 100,
      render: (mode: string) => (
        <Tag color="blue">{WEIGHING_MODE_LABELS[mode] || mode}</Tag>
      ),
    },
    {
      title: '차량번호',
      dataIndex: 'lprPlateNumber',
      width: 120,
      render: (plate: string) => plate || '-',
    },
    {
      title: '총중량(kg)',
      dataIndex: 'grossWeight',
      width: 100,
      align: 'right',
      render: (w: number) => w?.toLocaleString() || '-',
    },
    {
      title: '상태',
      dataIndex: 'weighingStatus',
      width: 80,
      render: () => (
        <Badge status="processing" text="진행중" />
      ),
    },
    {
      title: '시작시간',
      dataIndex: 'createdAt',
      width: 150,
      render: (dt: string) => new Date(dt).toLocaleString('ko-KR'),
    },
  ];

  // 차트 옵션들
  const lineChartOption = React.useMemo(() => {
    const daily = statistics?.dailyStatistics ?? [];
    const areaStartColor = themeMode === 'dark' ? 'rgba(6, 182, 212, 0.25)' : 'rgba(8, 145, 178, 0.18)';
    const areaEndColor = themeMode === 'dark' ? 'rgba(6, 182, 212, 0.02)' : 'rgba(8, 145, 178, 0.02)';
    return {
      title: { text: '일별 계량 추이', left: 'center', textStyle: { ...chartTextStyle, color: colors.textPrimary, fontSize: 15, fontWeight: 500 } },
      tooltip: {
        trigger: 'axis' as const,
        backgroundColor: colors.bgSurface,
        borderColor: colors.border,
        textStyle: { color: colors.textPrimary },
      },
      xAxis: {
        type: 'category' as const,
        data: daily.map((d) => d.date),
        ...chartAxisStyle,
        axisLabel: { rotate: 30, color: colors.textSecondary },
      },
      yAxis: {
        type: 'value' as const,
        name: '건수',
        minInterval: 1,
        ...chartAxisStyle,
      },
      series: [
        {
          name: '계량 건수',
          type: 'line' as const,
          data: daily.map((d) => d.totalCount),
          smooth: true,
          areaStyle: {
            color: {
              type: 'linear' as const,
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: areaStartColor },
                { offset: 1, color: areaEndColor },
              ],
            },
          },
          itemStyle: { color: colors.primary },
          lineStyle: { width: 2.5, color: colors.primary },
        },
      ],
      grid: { left: 50, right: 30, bottom: 60, top: 50 },
      backgroundColor: 'transparent',
    };
  }, [statistics, themeMode, colors, chartTextStyle, chartAxisStyle]);

  const pieChartOption = React.useMemo(() => {
    const byItemType = statistics?.countByItemType ?? {};
    const data = Object.entries(byItemType).map(([key, value]) => ({
      name: ITEM_TYPE_LABELS[key] ?? key,
      value,
    }));
    const pieColors = [colors.primary, colors.success, colors.warning, '#8B5CF6', colors.error];
    return {
      title: { text: '품목별 계량', left: 'center', textStyle: { ...chartTextStyle, color: colors.textPrimary, fontSize: 15, fontWeight: 500 } },
      tooltip: {
        trigger: 'item' as const,
        backgroundColor: colors.bgSurface,
        borderColor: colors.border,
        textStyle: { color: colors.textPrimary },
      },
      legend: { bottom: 0, textStyle: { color: colors.textSecondary } },
      series: [
        {
          type: 'pie' as const,
          radius: ['35%', '60%'],
          data,
          color: pieColors,
          emphasis: {
            itemStyle: {
              shadowBlur: 20,
              shadowOffsetX: 0,
              shadowColor: themeMode === 'dark' ? 'rgba(0, 0, 0, 0.5)' : 'rgba(0, 0, 0, 0.15)',
            },
          },
          label: {
            formatter: '{b}: {c}건 ({d}%)',
            color: colors.textSecondary,
          },
          itemStyle: {
            borderColor: colors.bgSurface,
            borderWidth: 2,
          },
        },
      ],
      backgroundColor: 'transparent',
    };
  }, [statistics, themeMode, colors, chartTextStyle]);

  const barChartOption = React.useMemo(() => {
    const byMode = statistics?.countByWeighingMode ?? {};
    const categories = Object.keys(byMode).map(
      (key) => WEIGHING_MODE_LABELS[key] ?? key,
    );
    const values = Object.values(byMode);
    const barEndColor = themeMode === 'dark' ? 'rgba(16, 185, 129, 0.4)' : 'rgba(5, 150, 105, 0.3)';
    return {
      title: { text: '계량 방식별 현황', left: 'center', textStyle: { ...chartTextStyle, color: colors.textPrimary, fontSize: 15, fontWeight: 500 } },
      tooltip: {
        trigger: 'axis' as const,
        backgroundColor: colors.bgSurface,
        borderColor: colors.border,
        textStyle: { color: colors.textPrimary },
      },
      xAxis: {
        type: 'category' as const,
        data: categories,
        ...chartAxisStyle,
      },
      yAxis: {
        type: 'value' as const,
        name: '건수',
        minInterval: 1,
        ...chartAxisStyle,
      },
      series: [
        {
          type: 'bar' as const,
          data: values,
          itemStyle: {
            color: {
              type: 'linear' as const,
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: colors.success },
                { offset: 1, color: barEndColor },
              ],
            },
            borderRadius: [6, 6, 0, 0],
          },
          barMaxWidth: 50,
        },
      ],
      grid: { left: 50, right: 30, bottom: 40, top: 50 },
      backgroundColor: 'transparent',
    };
  }, [statistics, themeMode, colors, chartTextStyle, chartAxisStyle]);

  const companyBarChartOption = React.useMemo(() => {
    const sortedStats = [...companyStats].slice(0, 10);
    const companyBarEndColor = themeMode === 'dark' ? 'rgba(139, 92, 246, 0.4)' : 'rgba(139, 92, 246, 0.25)';
    return {
      title: { text: '운송사별 계량 현황 (이번 달)', left: 'center', textStyle: { ...chartTextStyle, color: colors.textPrimary, fontSize: 15, fontWeight: 500 } },
      tooltip: {
        trigger: 'axis' as const,
        backgroundColor: colors.bgSurface,
        borderColor: colors.border,
        textStyle: { color: colors.textPrimary },
        formatter: (params: { name: string; value: number; seriesName: string }[]) => {
          const p = params[0];
          const stat = sortedStats.find((s) => s.companyName === p.name);
          return `${p.name}<br/>계량 건수: ${p.value}건<br/>총 중량: ${stat?.totalNetWeightTon.toFixed(2) || 0} 톤`;
        },
      },
      xAxis: {
        type: 'category' as const,
        data: sortedStats.map((s) => s.companyName),
        ...chartAxisStyle,
        axisLabel: { rotate: 30, color: colors.textSecondary, interval: 0 },
      },
      yAxis: {
        type: 'value' as const,
        name: '건수',
        minInterval: 1,
        ...chartAxisStyle,
      },
      series: [
        {
          type: 'bar' as const,
          data: sortedStats.map((s) => s.weighingCount),
          itemStyle: {
            color: {
              type: 'linear' as const,
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#8B5CF6' },
                { offset: 1, color: companyBarEndColor },
              ],
            },
            borderRadius: [6, 6, 0, 0],
          },
          barMaxWidth: 40,
        },
      ],
      grid: { left: 50, right: 30, bottom: 80, top: 50 },
      backgroundColor: 'transparent',
    };
  }, [companyStats, themeMode, colors, chartTextStyle, chartAxisStyle]);

  const companyTopChartOption = React.useMemo(() => {
    const top5 = [...companyStats].sort((a, b) => b.weighingCount - a.weighingCount).slice(0, 5).reverse();
    const barEndColor = themeMode === 'dark' ? 'rgba(139, 92, 246, 0.4)' : 'rgba(139, 92, 246, 0.25)';
    return {
      title: { text: '운송사 Top 5 (이번 달)', left: 'center', textStyle: { ...chartTextStyle, color: colors.textPrimary, fontSize: 15, fontWeight: 500 } },
      tooltip: {
        trigger: 'axis' as const,
        backgroundColor: colors.bgSurface,
        borderColor: colors.border,
        textStyle: { color: colors.textPrimary },
        formatter: (params: { name: string; value: number }[]) => {
          const p = params[0];
          const stat = top5.find((s) => s.companyName === p.name);
          return `${p.name}<br/>계량 건수: ${p.value}건<br/>총 중량: ${stat?.totalNetWeightTon.toFixed(2) || 0} 톤`;
        },
      },
      xAxis: {
        type: 'value' as const,
        name: '건수',
        minInterval: 1,
        ...chartAxisStyle,
      },
      yAxis: {
        type: 'category' as const,
        data: top5.map((s) => s.companyName),
        ...chartAxisStyle,
        axisLabel: { color: colors.textSecondary, width: 80, overflow: 'truncate' as const },
      },
      series: [
        {
          type: 'bar' as const,
          data: top5.map((s) => s.weighingCount),
          itemStyle: {
            color: {
              type: 'linear' as const,
              x: 0, y: 0, x2: 1, y2: 0,
              colorStops: [
                { offset: 0, color: barEndColor },
                { offset: 1, color: '#8B5CF6' },
              ],
            },
            borderRadius: [0, 6, 6, 0],
          },
          barMaxWidth: 28,
        },
      ],
      grid: { left: 90, right: 30, bottom: 40, top: 50 },
      backgroundColor: 'transparent',
    };
  }, [companyStats, themeMode, colors, chartTextStyle, chartAxisStyle]);

  // 개요 탭
  const OverviewTab = () => (
    <>
      {/* 공지사항 배너 */}
      {pinnedNotices.length > 0 && (
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
          style={{ marginBottom: 16, borderRadius: 8, cursor: 'pointer' }}
          onClick={() => handleNoticeClick(pinnedNotices[0].noticeId)}
        />
      )}

      {/* 통계 카드 */}
      <Row gutter={[16, 16]}>
        {statCards.map((card) => (
          <Col xs={24} sm={12} lg={4} xl={4} key={card.key} style={{ flex: '1 1 0', maxWidth: '20%' }}>
            <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
              <Statistic
                title={card.title}
                value={(statistics as unknown as Record<string, unknown>)?.[card.field] as number ?? 0}
                prefix={React.cloneElement(card.icon, { style: { color: card.color } })}
                suffix={card.suffix}
                valueStyle={{ color: card.color, fontWeight: 600 }}
              />
            </Card>
          </Col>
        ))}
        <Col xs={24} sm={12} lg={4} xl={4} style={{ flex: '1 1 0', maxWidth: '20%' }}>
          <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
            <Statistic
              title="오늘 순중량 합계"
              value={statistics?.todayTotalNetWeightTon?.toFixed(2) ?? '0.00'}
              prefix={<DashboardOutlined style={{ color: colors.primary }} />}
              suffix="톤"
              valueStyle={{ color: colors.primary, fontWeight: 600 }}
            />
          </Card>
        </Col>
      </Row>

      {/* 차트 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
            <ReactECharts option={lineChartOption} style={{ height: 350 }} notMerge />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
            <ReactECharts option={pieChartOption} style={{ height: 350 }} notMerge />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16, marginBottom: 24 }}>
        <Col xs={24} lg={12}>
          <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
            <ReactECharts option={barChartOption} style={{ height: 350 }} notMerge />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
            <ReactECharts option={companyTopChartOption} style={{ height: 350 }} notMerge />
          </Card>
        </Col>
      </Row>
    </>
  );

  // 실시간 현황 탭
  const RealtimeTab = () => (
    <>
      {/* 오늘의 현황 요약 */}
      <Row gutter={[16, 16]} align="stretch">
        <Col xs={24} lg={8} style={{ display: 'flex' }}>
          <Card
            title={<><CarOutlined style={{ marginRight: 8 }} />배차 현황</>}
            style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated, width: '100%' }}
          >
            <Space direction="vertical" style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><ClockCircleOutlined style={{ marginRight: 4, color: colors.warning }} />대기</span>
                <strong>{summary?.dispatchRegistered ?? 0}건</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><ExperimentOutlined style={{ marginRight: 4, color: colors.primary }} />진행중</span>
                <strong>{summary?.dispatchInProgress ?? 0}건</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><CheckCircleOutlined style={{ marginRight: 4, color: colors.success }} />완료</span>
                <strong>{summary?.dispatchCompleted ?? 0}건</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><StopOutlined style={{ marginRight: 4, color: colors.error }} />취소</span>
                <strong>{summary?.dispatchCancelled ?? 0}건</strong>
              </div>
              <Progress
                percent={
                  summary
                    ? Math.round(
                        (summary.dispatchCompleted /
                          (summary.dispatchRegistered + summary.dispatchInProgress + summary.dispatchCompleted + summary.dispatchCancelled || 1)) *
                          100
                      )
                    : 0
                }
                status="active"
                strokeColor={colors.success}
              />
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={8} style={{ display: 'flex' }}>
          <Card
            title={<><LogoutOutlined style={{ marginRight: 8 }} />출문 현황</>}
            style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated, width: '100%' }}
          >
            <Space direction="vertical" style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><ClockCircleOutlined style={{ marginRight: 4, color: colors.warning }} />대기</span>
                <strong>{summary?.gatePassPending ?? 0}건</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><CheckCircleOutlined style={{ marginRight: 4, color: colors.success }} />통과</span>
                <strong>{summary?.gatePassPassed ?? 0}건</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><StopOutlined style={{ marginRight: 4, color: colors.error }} />거부</span>
                <strong>{summary?.gatePassRejected ?? 0}건</strong>
              </div>
              <Progress
                percent={
                  summary
                    ? Math.round(
                        (summary.gatePassPassed /
                          (summary.gatePassPending + summary.gatePassPassed + summary.gatePassRejected || 1)) *
                          100
                      )
                    : 0
                }
                status="active"
                strokeColor={colors.success}
              />
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={8} style={{ display: 'flex' }}>
          <Card
            title={<><ExperimentOutlined style={{ marginRight: 8 }} />계량 현황</>}
            style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated, width: '100%' }}
          >
            <Space direction="vertical" style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><ClockCircleOutlined style={{ marginRight: 4, color: colors.warning }} />진행중</span>
                <strong>{summary?.weighingInProgress ?? 0}건</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><CheckCircleOutlined style={{ marginRight: 4, color: colors.success }} />완료</span>
                <strong>{summary?.weighingCompleted ?? 0}건</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span><ExperimentOutlined style={{ marginRight: 4, color: colors.primary }} />재계량</span>
                <strong>{summary?.weighingReWeighing ?? 0}건</strong>
              </div>
              <Progress
                percent={
                  summary
                    ? Math.round(
                        (summary.weighingCompleted /
                          (summary.weighingInProgress + summary.weighingCompleted + summary.weighingReWeighing || 1)) *
                          100
                      )
                    : 0
                }
                status="active"
                strokeColor={colors.success}
              />
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 실시간 계량 테이블 */}
      <Card
        title={
          <Space>
            <Badge status="processing" />
            실시간 계량 현황
            <Tag color="blue">{inProgressWeighings.length}건 진행 중</Tag>
          </Space>
        }
        style={{ marginTop: 16, borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}
      >
        <Table
          columns={inProgressColumns}
          dataSource={inProgressWeighings}
          rowKey="weighingId"
          pagination={false}
          size="middle"
          locale={{ emptyText: '진행 중인 계량이 없습니다' }}
        />
      </Card>
    </>
  );

  // 분석 탭
  const AnalysisTab = () => (
    <>
      <Row gutter={[16, 16]}>
        <Col xs={24}>
          <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
            <ReactECharts option={companyBarChartOption} style={{ height: 400 }} notMerge />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }} align="stretch">
        <Col xs={24} lg={12} style={{ display: 'flex' }}>
          <Card
            title={<><TeamOutlined style={{ marginRight: 8 }} />운송사별 상세</>}
            style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated, width: '100%', minHeight: 410 }}
          >
            <Table
              dataSource={companyStats}
              rowKey="companyId"
              pagination={{ pageSize: 5 }}
              size="small"
              columns={[
                { title: '운송사', dataIndex: 'companyName' },
                { title: '계량 건수', dataIndex: 'weighingCount', align: 'right', render: (v: number) => `${v}건` },
                { title: '총 중량', dataIndex: 'totalNetWeightTon', align: 'right', render: (v: number) => `${v.toFixed(2)} 톤` },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} lg={12} style={{ display: 'flex' }}>
          <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated, width: '100%' }}>
            <ReactECharts option={lineChartOption} style={{ height: 350 }} notMerge />
          </Card>
        </Col>
      </Row>
    </>
  );

  const tabItems = [
    { key: 'overview', label: '개요', children: <OverviewTab /> },
    { key: 'realtime', label: '실시간 현황', children: <RealtimeTab /> },
    { key: 'analysis', label: '분석', children: <AnalysisTab /> },
  ];

  return (
    <>
      <Typography.Title level={4} style={{ marginBottom: 20 }}>
        대시보드
      </Typography.Title>
      <Spin spinning={loading}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
          style={{ marginBottom: 24 }}
        />
      </Spin>

      {/* 공지사항 상세 팝업 */}
      <Modal
        title={
          <Space>
            <NotificationOutlined />
            {selectedNotice?.title}
          </Space>
        }
        open={noticeModalVisible}
        onCancel={() => {
          setNoticeModalVisible(false);
          setSelectedNotice(null);
        }}
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
              <Card
                style={{
                  background: colors.bgElevated,
                  border: `1px solid ${colors.border}`,
                  borderRadius: 8,
                }}
              >
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
    </>
  );
};

export default DashboardPage;

import React, { useCallback, useEffect, useState } from 'react';
import { Card, Col, Row, Spin, Statistic, Typography } from 'antd';
import {
  CalendarOutlined,
  CarOutlined,
  CheckCircleOutlined,
  DashboardOutlined,
  ExperimentOutlined,
} from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import apiClient from '../api/client';
import { useWebSocket } from '../hooks/useWebSocket';
import type { ApiResponse, WeighingStatistics } from '../types';
import { colors } from '../theme/themeConfig';

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

// 다크 테마 ECharts 공통 설정
const chartTextStyle = {
  color: colors.textSecondary,
  fontFamily: "'Inter', sans-serif",
};

const chartAxisStyle = {
  axisLine: { lineStyle: { color: colors.border } },
  axisTick: { lineStyle: { color: colors.border } },
  splitLine: { lineStyle: { color: 'rgba(51, 65, 85, 0.5)' } },
  axisLabel: { color: colors.textSecondary },
  nameTextStyle: { color: colors.textSecondary },
};

// 스탯 카드 설정
const statCards = [
  { key: 'todayTotal', title: '오늘 전체 건수', icon: <CarOutlined />, field: 'todayTotalCount', suffix: '건', color: colors.primary },
  { key: 'inProgress', title: '계량 진행 중', icon: <ExperimentOutlined />, field: 'todayInProgressCount', suffix: '건', color: colors.warning },
  { key: 'completed', title: '오늘 완료', icon: <CheckCircleOutlined />, field: 'todayCompletedCount', suffix: '건', color: colors.success },
  { key: 'monthTotal', title: '이번 달 전체', icon: <CalendarOutlined />, field: 'monthTotalCount', suffix: '건', color: '#8B5CF6' },
] as const;

const DashboardPage: React.FC = () => {
  const [statistics, setStatistics] = useState<WeighingStatistics | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchStatistics = useCallback(async () => {
    try {
      const response = await apiClient.get<ApiResponse<WeighingStatistics>>(
        '/weighings/statistics',
      );
      setStatistics(response.data.data);
    } catch {
      /* ignore fetch error */
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStatistics();
  }, [fetchStatistics]);

  const handleWebSocketMessage = useCallback(() => {
    fetchStatistics();
  }, [fetchStatistics]);

  useWebSocket(handleWebSocketMessage);

  // --- Chart options (Dark Theme) ---

  const lineChartOption = React.useMemo(() => {
    const daily = statistics?.dailyStatistics ?? [];
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
                { offset: 0, color: 'rgba(6, 182, 212, 0.25)' },
                { offset: 1, color: 'rgba(6, 182, 212, 0.02)' },
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
  }, [statistics]);

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
              shadowColor: 'rgba(0, 0, 0, 0.5)',
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
  }, [statistics]);

  const barChartOption = React.useMemo(() => {
    const byMode = statistics?.countByWeighingMode ?? {};
    const categories = Object.keys(byMode).map(
      (key) => WEIGHING_MODE_LABELS[key] ?? key,
    );
    const values = Object.values(byMode);
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
                { offset: 1, color: 'rgba(16, 185, 129, 0.4)' },
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
  }, [statistics]);

  return (
    <>
      <Typography.Title level={4} style={{ marginBottom: 20 }}>
        대시보드
      </Typography.Title>
      <Spin spinning={loading}>
        {/* Stat cards */}
        <Row gutter={[16, 16]}>
          {statCards.map((card) => (
            <Col xs={24} sm={12} lg={6} key={card.key}>
              <Card
                style={{
                  borderRadius: 12,
                  border: `1px solid ${colors.border}`,
                  background: colors.bgElevated,
                }}
              >
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
        </Row>

        <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card
              style={{
                borderRadius: 12,
                border: `1px solid ${colors.border}`,
                background: colors.bgElevated,
              }}
            >
              <Statistic
                title="오늘 순중량 합계"
                value={
                  statistics?.todayTotalNetWeightTon != null
                    ? statistics.todayTotalNetWeightTon.toFixed(2)
                    : '0.00'
                }
                prefix={<DashboardOutlined style={{ color: colors.primary }} />}
                suffix="톤"
                valueStyle={{ color: colors.primary, fontWeight: 600 }}
              />
            </Card>
          </Col>
        </Row>

        {/* Charts */}
        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24} lg={12}>
            <Card
              style={{
                borderRadius: 12,
                border: `1px solid ${colors.border}`,
                background: colors.bgElevated,
              }}
            >
              <ReactECharts
                option={lineChartOption}
                style={{ height: 350 }}
                notMerge
              />
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card
              style={{
                borderRadius: 12,
                border: `1px solid ${colors.border}`,
                background: colors.bgElevated,
              }}
            >
              <ReactECharts
                option={pieChartOption}
                style={{ height: 350 }}
                notMerge
              />
            </Card>
          </Col>
        </Row>

        <Row gutter={[16, 16]} style={{ marginTop: 16, marginBottom: 24 }}>
          <Col xs={24} lg={12}>
            <Card
              style={{
                borderRadius: 12,
                border: `1px solid ${colors.border}`,
                background: colors.bgElevated,
              }}
            >
              <ReactECharts
                option={barChartOption}
                style={{ height: 350 }}
                notMerge
              />
            </Card>
          </Col>
        </Row>
      </Spin>
    </>
  );
};

export default DashboardPage;

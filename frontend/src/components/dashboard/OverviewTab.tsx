import React from 'react';
import { Card, Col, Row, Statistic, Alert, Space, Tag } from 'antd';
import {
  CalendarOutlined,
  CarOutlined,
  CheckCircleOutlined,
  DashboardOutlined,
  ExperimentOutlined,
  NotificationOutlined,
} from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import type { EChartsOption } from 'echarts';
import type { WeighingStatistics, Notice } from '../../types';

interface OverviewTabProps {
  statistics: WeighingStatistics | null;
  pinnedNotices: Notice[];
  onNoticeClick: (noticeId: number) => void;
  lineChartOption: EChartsOption;
  pieChartOption: EChartsOption;
  barChartOption: EChartsOption;
  companyTopChartOption: EChartsOption;
  colors: {
    primary: string;
    success: string;
    warning: string;
    error: string;
    border: string;
    bgElevated: string;
  };
}

const statCards = [
  { key: 'todayTotal', title: '오늘 전체 건수', icon: <CarOutlined />, field: 'todayTotalCount', suffix: '건', colorKey: 'primary' },
  { key: 'inProgress', title: '계량 진행 중', icon: <ExperimentOutlined />, field: 'todayInProgressCount', suffix: '건', colorKey: 'warning' },
  { key: 'completed', title: '오늘 완료', icon: <CheckCircleOutlined />, field: 'todayCompletedCount', suffix: '건', colorKey: 'success' },
  { key: 'monthTotal', title: '이번 달 전체', icon: <CalendarOutlined />, field: 'monthTotalCount', suffix: '건', colorKey: 'fixed' },
] as const;

const OverviewTab: React.FC<OverviewTabProps> = ({
  statistics,
  pinnedNotices,
  onNoticeClick,
  lineChartOption,
  pieChartOption,
  barChartOption,
  companyTopChartOption,
  colors,
}) => {
  const getColor = (key: string) => key === 'fixed' ? '#8B5CF6' : colors[key as keyof typeof colors];

  return (
    <>
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
          onClick={() => onNoticeClick(pinnedNotices[0].noticeId)}
        />
      )}

      <Row gutter={[16, 16]}>
        {statCards.map((card) => (
          <Col xs={24} sm={12} lg={4} xl={4} key={card.key} style={{ flex: '1 1 0', maxWidth: '20%' }}>
            <Card style={{ borderRadius: 12, border: `1px solid ${colors.border}`, background: colors.bgElevated }}>
              <Statistic
                title={card.title}
                value={(statistics as unknown as Record<string, unknown>)?.[card.field] as number ?? 0}
                prefix={React.cloneElement(card.icon, { style: { color: getColor(card.colorKey) } })}
                suffix={card.suffix}
                valueStyle={{ color: getColor(card.colorKey), fontWeight: 600 }}
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
};

export default OverviewTab;

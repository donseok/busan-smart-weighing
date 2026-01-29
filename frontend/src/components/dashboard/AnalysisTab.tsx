/**
 * @fileoverview 대시보드 분석 탭 컴포넌트
 *
 * 운송사별 계량 현황 바 차트, 운송사별 상세 테이블,
 * 일별 계량 추이 라인 차트를 표시하는 분석 전용 탭입니다.
 *
 * @module components/dashboard/AnalysisTab
 */
import React from 'react';
import { Card, Col, Row, Table } from 'antd';
import { TeamOutlined } from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import type { EChartsOption } from 'echarts';
import type { CompanyStatistics } from '../../types';

interface AnalysisTabProps {
  companyStats: CompanyStatistics[];
  companyBarChartOption: EChartsOption;
  lineChartOption: EChartsOption;
  colors: {
    border: string;
    bgElevated: string;
  };
}

/**
 * 대시보드 분석 탭 컴포넌트
 *
 * @param props - 운송사 통계 데이터, 차트 옵션, 테마 색상
 * @returns 운송사별 분석 차트와 상세 테이블이 포함된 JSX
 */
const AnalysisTab: React.FC<AnalysisTabProps> = ({
  companyStats,
  companyBarChartOption,
  lineChartOption,
  colors,
}) => (
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

export default AnalysisTab;

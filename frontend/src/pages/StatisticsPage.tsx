/**
 * 통계 및 보고서 페이지 컴포넌트
 *
 * 계량 데이터의 일별/월별 통계를 조회하고 분석하는 페이지입니다.
 * 기간, 업체, 품목유형별 필터 검색을 지원하며,
 * 총 계량 건수, 총 중량, 품목별/업체별 최다 항목 등의
 * 요약 통계 카드를 제공합니다.
 * Excel 다운로드 기능으로 통계 데이터를 내보낼 수 있습니다.
 *
 * @returns 통계 및 보고서 페이지 JSX
 */
import React, { useCallback, useEffect, useState } from 'react';
import {
  Typography,
  DatePicker,
  Select,
  Button,
  Space,
  Tabs,
  Card,
  Row,
  Col,
  Statistic,
  message,
} from 'antd';
import {
  DownloadOutlined,
  SearchOutlined,
  BarChartOutlined,
  LineChartOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import SortableTable from '../components/SortableTable';
import { TablePageLayout, FixedArea, ScrollArea } from '../components/TablePageLayout';
import apiClient from '../api/client';
import dayjs, { type Dayjs } from 'dayjs';
import { colors } from '../theme/themeConfig';
import { ITEM_TYPE_OPTIONS } from '../constants/labels';

const { RangePicker } = DatePicker;

/** 일별 통계 데이터 구조 */
interface DailyStatistics {
  date: string;
  companyId?: number;
  companyName: string;
  itemType?: string;
  itemTypeName: string;
  totalCount: number;
  totalWeightKg: number;
  totalWeightTon: number;
}

/** 월별 통계 데이터 구조 */
interface MonthlyStatistics {
  year: number;
  month: number;
  companyId?: number;
  companyName: string;
  itemType?: string;
  itemTypeName: string;
  totalCount: number;
  totalWeightKg: number;
  totalWeightTon: number;
}

/** 통계 요약 데이터 (상단 카드 표시용) */
interface StatisticsSummary {
  totalCount: number;           // 총 계량 건수
  totalWeightKg: number;        // 총 중량 (kg)
  totalWeightTon: number;       // 총 중량 (톤)
  countByItemType: Record<string, number>;   // 품목유형별 건수
  weightByItemType: Record<string, number>;  // 품목유형별 중량
  countByCompany: Record<string, number>;    // 업체별 건수
}

/** 운송사 선택 옵션용 데이터 */
interface Company {
  companyId: number;
  companyName: string;
}

const itemTypeOptions = ITEM_TYPE_OPTIONS;

const dailyColumns: ColumnsType<DailyStatistics> = [
  { title: '날짜', dataIndex: 'date', width: 120 },
  { title: '업체', dataIndex: 'companyName', width: 150 },
  { title: '품목유형', dataIndex: 'itemTypeName', width: 100 },
  {
    title: '건수',
    dataIndex: 'totalCount',
    width: 80,
    align: 'right',
    render: (v: number) => v.toLocaleString(),
  },
  {
    title: '중량(kg)',
    dataIndex: 'totalWeightKg',
    width: 120,
    align: 'right',
    render: (v: number) => v.toLocaleString(undefined, { maximumFractionDigits: 2 }),
  },
  {
    title: '중량(톤)',
    dataIndex: 'totalWeightTon',
    width: 120,
    align: 'right',
    render: (v: number) => v.toLocaleString(undefined, { maximumFractionDigits: 2 }),
  },
];

const monthlyColumns: ColumnsType<MonthlyStatistics> = [
  {
    title: '기간',
    key: 'period',
    width: 100,
    render: (_, record) => `${record.year}년 ${record.month}월`,
  },
  { title: '업체', dataIndex: 'companyName', width: 150 },
  { title: '품목유형', dataIndex: 'itemTypeName', width: 100 },
  {
    title: '건수',
    dataIndex: 'totalCount',
    width: 80,
    align: 'right',
    render: (v: number) => v.toLocaleString(),
  },
  {
    title: '중량(kg)',
    dataIndex: 'totalWeightKg',
    width: 120,
    align: 'right',
    render: (v: number) => v.toLocaleString(undefined, { maximumFractionDigits: 2 }),
  },
  {
    title: '중량(톤)',
    dataIndex: 'totalWeightTon',
    width: 120,
    align: 'right',
    render: (v: number) => v.toLocaleString(undefined, { maximumFractionDigits: 2 }),
  },
];

const StatisticsPage: React.FC = () => {
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs]>([
    dayjs().subtract(30, 'day'),
    dayjs(),
  ]); // 조회 기간 (기본 최근 30일)
  const [companyId, setCompanyId] = useState<number | undefined>();    // 업체 필터
  const [itemType, setItemType] = useState<string | undefined>();      // 품목유형 필터
  const [companies, setCompanies] = useState<Company[]>([]);           // 업체 목록 (Select 옵션)
  const [dailyData, setDailyData] = useState<DailyStatistics[]>([]);   // 일별 통계 데이터
  const [monthlyData, setMonthlyData] = useState<MonthlyStatistics[]>([]); // 월별 통계 데이터
  const [summary, setSummary] = useState<StatisticsSummary | null>(null);  // 요약 통계
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('daily'); // 현재 활성 탭 (daily/monthly)

  /** 운송사 목록 조회 (필터 Select 옵션 데이터) */
  const fetchCompanies = useCallback(async () => {
    try {
      const res = await apiClient.get('/companies');
      setCompanies(res.data.data || []);
    } catch {
      /* ignore */
    }
  }, []);

  /** 일별/월별/요약 통계를 병렬로 조회 */
  const fetchStatistics = useCallback(async () => {
    if (!dateRange[0] || !dateRange[1]) return;

    setLoading(true);
    try {
      const params: Record<string, unknown> = {
        dateFrom: dateRange[0].format('YYYY-MM-DD'),
        dateTo: dateRange[1].format('YYYY-MM-DD'),
        companyId,
        itemType,
      };

      const [dailyRes, monthlyRes, summaryRes] = await Promise.all([
        apiClient.get('/statistics/daily', { params }),
        apiClient.get('/statistics/monthly', { params }),
        apiClient.get('/statistics/summary', { params }),
      ]);

      setDailyData(dailyRes.data.data || []);
      setMonthlyData(monthlyRes.data.data || []);
      setSummary(summaryRes.data.data || null);
    } catch {
      message.error('통계 데이터를 불러오는데 실패했습니다.');
    }
    setLoading(false);
  }, [dateRange, companyId, itemType]);

  useEffect(() => {
    fetchCompanies();
  }, [fetchCompanies]);

  useEffect(() => {
    fetchStatistics();
  }, [fetchStatistics]);

  /** Excel 파일 다운로드 - Blob 응답을 받아 다운로드 링크 생성 */
  const handleExport = async () => {
    if (!dateRange[0] || !dateRange[1]) return;

    try {
      const params = new URLSearchParams();
      params.append('date_from', dateRange[0].format('YYYY-MM-DD'));
      params.append('date_to', dateRange[1].format('YYYY-MM-DD'));
      if (companyId) params.append('company_id', companyId.toString());
      if (itemType) params.append('item_type', itemType);
      params.append('type', activeTab === 'daily' ? 'daily' : activeTab === 'monthly' ? 'monthly' : 'all');

      const response = await apiClient.get(`/statistics/export?${params.toString()}`, {
        responseType: 'blob',
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `statistics_${dateRange[0].format('YYYYMMDD')}_${dateRange[1].format('YYYYMMDD')}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      message.success('Excel 파일이 다운로드되었습니다.');
    } catch {
      message.error('Excel 다운로드에 실패했습니다.');
    }
  };

  const handleDateRangeChange = (
    dates: [Dayjs | null, Dayjs | null] | null,
  ) => {
    if (dates && dates[0] && dates[1]) {
      setDateRange([dates[0], dates[1]]);
    }
  };

  return (
    <TablePageLayout>
      <FixedArea>
        <Typography.Title level={4}>통계 및 보고서</Typography.Title>

        {/* Summary Cards */}
        {summary && (
          <Row gutter={16} style={{ marginBottom: 24 }}>
            <Col span={6}>
              <Card style={{ borderTop: `3px solid ${colors.primary}` }}>
                <Statistic
                  title="총 계량 건수"
                  value={summary.totalCount}
                  prefix={<BarChartOutlined />}
                  suffix="건"
                  valueStyle={{ color: colors.primary }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card style={{ borderTop: `3px solid ${colors.success}` }}>
                <Statistic
                  title="총 중량 (톤)"
                  value={summary.totalWeightTon}
                  prefix={<LineChartOutlined />}
                  precision={2}
                  suffix="톤"
                  valueStyle={{ color: colors.success }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card style={{ borderTop: `3px solid ${colors.warning}` }}>
                <Statistic
                  title="품목별 최다"
                  value={
                    Object.entries(summary.countByItemType).length > 0
                      ? Object.entries(summary.countByItemType).sort((a, b) => b[1] - a[1])[0]?.[0] || '-'
                      : '-'
                  }
                  valueStyle={{ color: colors.warning }}
                />
              </Card>
            </Col>
            <Col span={6}>
              <Card style={{ borderTop: `3px solid ${colors.error}` }}>
                <Statistic
                  title="업체별 최다"
                  value={
                    Object.entries(summary.countByCompany).length > 0
                      ? Object.entries(summary.countByCompany).sort((a, b) => b[1] - a[1])[0]?.[0] || '-'
                      : '-'
                  }
                  valueStyle={{ color: colors.error }}
                />
              </Card>
            </Col>
          </Row>
        )}

        {/* Filters */}
        <Card
          size="small"
          style={{ marginBottom: 16 }}
          styles={{ body: { padding: '16px 24px' } }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 50, display: 'inline-block' }}>기간</Typography.Text>
              <RangePicker
                value={dateRange}
                onChange={handleDateRangeChange}
                allowClear={false}
                style={{ width: 280 }}
              />
            </Space>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 50, display: 'inline-block' }}>업체</Typography.Text>
              <Select
                placeholder="전체"
                allowClear
                style={{ width: 180 }}
                onChange={setCompanyId}
                value={companyId}
                options={companies.map((c) => ({
                  value: c.companyId,
                  label: c.companyName,
                }))}
              />
            </Space>
            <Space size={8}>
              <Typography.Text strong style={{ minWidth: 60, display: 'inline-block' }}>품목유형</Typography.Text>
              <Select
                placeholder="전체"
                allowClear
                style={{ width: 140 }}
                onChange={setItemType}
                value={itemType}
                options={itemTypeOptions}
              />
            </Space>
            <div style={{ marginLeft: 'auto' }}>
              <Space>
                <Button icon={<SearchOutlined />} onClick={fetchStatistics} loading={loading}>조회</Button>
                <Button type="primary" icon={<DownloadOutlined />} onClick={handleExport}>Excel 다운로드</Button>
              </Space>
            </div>
          </div>
        </Card>

        {/* Tab headers only */}
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'daily', label: '일별 통계' },
            { key: 'monthly', label: '월별 통계' },
          ]}
          style={{ marginBottom: 0 }}
        />
      </FixedArea>

      {/* Data Tables - scrollable with fixed column headers */}
      <ScrollArea>
        {activeTab === 'daily' ? (
          <SortableTable
            columns={dailyColumns}
            dataSource={dailyData}
            rowKey={(record: DailyStatistics) =>
              `${record.date}-${record.companyId}-${record.itemType}`
            }
            loading={loading}
            size="middle"
            tableKey="statistics-daily"
            pagination={false}
            scroll={{ y: 1 }}
          />
        ) : (
          <SortableTable
            columns={monthlyColumns}
            dataSource={monthlyData}
            rowKey={(record: MonthlyStatistics) =>
              `${record.year}-${record.month}-${record.companyId}-${record.itemType}`
            }
            loading={loading}
            size="middle"
            tableKey="statistics-monthly"
            pagination={false}
            scroll={{ y: 1 }}
          />
        )}
      </ScrollArea>
    </TablePageLayout>
  );
};

export default StatisticsPage;

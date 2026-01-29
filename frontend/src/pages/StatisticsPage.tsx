import React, { useCallback, useEffect, useState } from 'react';
import {
  Typography,
  DatePicker,
  Select,
  Button,
  Space,
  Tabs,
  Table,
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
import apiClient from '../api/client';
import dayjs, { type Dayjs } from 'dayjs';
import { colors } from '../theme/themeConfig';

const { RangePicker } = DatePicker;

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

interface StatisticsSummary {
  totalCount: number;
  totalWeightKg: number;
  totalWeightTon: number;
  countByItemType: Record<string, number>;
  weightByItemType: Record<string, number>;
  countByCompany: Record<string, number>;
}

interface Company {
  companyId: number;
  companyName: string;
}

const itemTypeOptions = [
  { value: 'BY_PRODUCT', label: '부산물' },
  { value: 'WASTE', label: '폐기물' },
  { value: 'SUB_MATERIAL', label: '부재료' },
  { value: 'EXPORT', label: '반출' },
  { value: 'GENERAL', label: '일반' },
];

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
  ]);
  const [companyId, setCompanyId] = useState<number | undefined>();
  const [itemType, setItemType] = useState<string | undefined>();
  const [companies, setCompanies] = useState<Company[]>([]);
  const [dailyData, setDailyData] = useState<DailyStatistics[]>([]);
  const [monthlyData, setMonthlyData] = useState<MonthlyStatistics[]>([]);
  const [summary, setSummary] = useState<StatisticsSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('daily');

  const fetchCompanies = useCallback(async () => {
    try {
      const res = await apiClient.get('/companies');
      setCompanies(res.data.data || []);
    } catch {
      /* ignore */
    }
  }, []);

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
    <>
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
        <Row gutter={[16, 12]} align="middle">
          <Col>
            <Space size={8}>
              <Typography.Text type="secondary" style={{ minWidth: 50, display: 'inline-block' }}>
                기간
              </Typography.Text>
              <RangePicker
                value={dateRange}
                onChange={handleDateRangeChange}
                allowClear={false}
                style={{ width: 280 }}
              />
            </Space>
          </Col>
          <Col>
            <Space size={8}>
              <Typography.Text type="secondary" style={{ minWidth: 50, display: 'inline-block' }}>
                업체
              </Typography.Text>
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
          </Col>
          <Col>
            <Space size={8}>
              <Typography.Text type="secondary" style={{ minWidth: 60, display: 'inline-block' }}>
                품목유형
              </Typography.Text>
              <Select
                placeholder="전체"
                allowClear
                style={{ width: 140 }}
                onChange={setItemType}
                value={itemType}
                options={itemTypeOptions}
              />
            </Space>
          </Col>
          <Col flex="auto" style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Space>
              <Button icon={<SearchOutlined />} onClick={fetchStatistics} loading={loading}>
                조회
              </Button>
              <Button
                type="primary"
                icon={<DownloadOutlined />}
                onClick={handleExport}
              >
                Excel 다운로드
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* Data Tables */}
      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={[
          {
            key: 'daily',
            label: '일별 통계',
            children: (
              <Table
                columns={dailyColumns}
                dataSource={dailyData}
                rowKey={(record) =>
                  `${record.date}-${record.companyId}-${record.itemType}`
                }
                loading={loading}
                size="middle"
                pagination={{ pageSize: 20, showSizeChanger: true }}
                scroll={{ x: 800 }}
              />
            ),
          },
          {
            key: 'monthly',
            label: '월별 통계',
            children: (
              <Table
                columns={monthlyColumns}
                dataSource={monthlyData}
                rowKey={(record) =>
                  `${record.year}-${record.month}-${record.companyId}-${record.itemType}`
                }
                loading={loading}
                size="middle"
                pagination={{ pageSize: 20, showSizeChanger: true }}
                scroll={{ x: 800 }}
              />
            ),
          },
        ]}
      />
    </>
  );
};

export default StatisticsPage;

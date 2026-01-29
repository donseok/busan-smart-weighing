import React, { useCallback, useEffect, useState } from 'react';
import {
  Typography,
  Card,
  Row,
  Col,
  Tag,
  Space,
  Button,
  Statistic,
  Table,
  Badge,
  Select,
  message,
} from 'antd';
import {
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  DesktopOutlined,
  CameraOutlined,
  DashboardOutlined,
  StopOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import apiClient from '../api/client';
import { useWebSocket } from '../hooks/useWebSocket';
import dayjs from 'dayjs';

interface DeviceStatus {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: string;
  deviceTypeDesc: string;
  location: string;
  connectionStatus: 'ONLINE' | 'OFFLINE' | 'ERROR';
  connectionStatusDesc: string;
  lastConnectedAt?: string;
  ipAddress?: string;
  errorMessage?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface DeviceSummary {
  totalDevices: number;
  onlineCount: number;
  offlineCount: number;
  errorCount: number;
  countByTypeAndStatus: Record<string, Record<string, number>>;
}

const statusColors: Record<string, string> = {
  ONLINE: 'success',
  OFFLINE: 'default',
  ERROR: 'error',
};

const statusIcons: Record<string, React.ReactNode> = {
  ONLINE: <CheckCircleOutlined />,
  OFFLINE: <CloseCircleOutlined />,
  ERROR: <ExclamationCircleOutlined />,
};

const deviceTypeIcons: Record<string, React.ReactNode> = {
  SCALE: <DashboardOutlined />,
  LPR_CAMERA: <CameraOutlined />,
  INDICATOR: <DesktopOutlined />,
  BARRIER_GATE: <StopOutlined />,
};

const deviceTypeOptions = [
  { value: '', label: '전체' },
  { value: 'SCALE', label: '계량대' },
  { value: 'LPR_CAMERA', label: 'LPR 카메라' },
  { value: 'INDICATOR', label: '계량 지시기' },
  { value: 'BARRIER_GATE', label: '차단기' },
];

const columns: ColumnsType<DeviceStatus> = [
  {
    title: '장비',
    key: 'device',
    width: 250,
    render: (_, record) => (
      <Space>
        <span style={{ fontSize: 18 }}>
          {deviceTypeIcons[record.deviceType] || <DesktopOutlined />}
        </span>
        <div>
          <div style={{ fontWeight: 500 }}>{record.deviceName}</div>
          <div style={{ fontSize: 12, color: '#999' }}>{record.deviceCode}</div>
        </div>
      </Space>
    ),
  },
  {
    title: '유형',
    dataIndex: 'deviceTypeDesc',
    width: 120,
  },
  {
    title: '위치',
    dataIndex: 'location',
    width: 150,
  },
  {
    title: 'IP 주소',
    dataIndex: 'ipAddress',
    width: 140,
  },
  {
    title: '상태',
    dataIndex: 'connectionStatus',
    width: 100,
    render: (status: string, record) => (
      <Tag
        icon={statusIcons[status]}
        color={statusColors[status]}
      >
        {record.connectionStatusDesc}
      </Tag>
    ),
  },
  {
    title: '최종 연결',
    dataIndex: 'lastConnectedAt',
    width: 160,
    render: (v?: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-',
  },
  {
    title: '오류 메시지',
    dataIndex: 'errorMessage',
    ellipsis: true,
    render: (v?: string) => v || '-',
  },
];

const MonitoringPage: React.FC = () => {
  const [devices, setDevices] = useState<DeviceStatus[]>([]);
  const [summary, setSummary] = useState<DeviceSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [typeFilter, setTypeFilter] = useState<string>('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [devicesRes, summaryRes] = await Promise.all([
        apiClient.get('/monitoring/devices'),
        apiClient.get('/monitoring/summary'),
      ]);

      setDevices(devicesRes.data.data || []);
      setSummary(summaryRes.data.data || null);
    } catch {
      message.error('장비 정보를 불러오는데 실패했습니다.');
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleWsMessage = useCallback((msg: unknown) => {
    // WebSocket으로 장비 상태 변경 알림 수신 시 데이터 새로고침
    const message = msg as { type?: string };
    if (message?.type === 'device-status') {
      fetchData();
    }
  }, [fetchData]);

  useWebSocket(handleWsMessage);

  const filteredDevices = typeFilter
    ? devices.filter(d => d.deviceType === typeFilter)
    : devices;

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ONLINE':
        return <Badge status="success" />;
      case 'OFFLINE':
        return <Badge status="default" />;
      case 'ERROR':
        return <Badge status="error" />;
      default:
        return <Badge status="default" />;
    }
  };

  return (
    <>
      <Typography.Title level={4}>장비 관제</Typography.Title>

      {/* Summary Cards */}
      {summary && (
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={6}>
            <Card style={{ borderTop: '3px solid #06B6D4', background: 'linear-gradient(135deg, rgba(6,182,212,0.08) 0%, transparent 60%)' }}>
              <Statistic
                title="전체 장비"
                value={summary.totalDevices}
                valueStyle={{ color: '#06B6D4' }}
                suffix="대"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card style={{ borderTop: '3px solid #10B981', background: 'linear-gradient(135deg, rgba(16,185,129,0.08) 0%, transparent 60%)' }}>
              <Statistic
                title={
                  <Space>
                    {getStatusBadge('ONLINE')}
                    온라인
                  </Space>
                }
                value={summary.onlineCount}
                valueStyle={{ color: '#10B981' }}
                suffix="대"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card style={{ borderTop: '3px solid #F59E0B', background: 'linear-gradient(135deg, rgba(245,158,11,0.08) 0%, transparent 60%)' }}>
              <Statistic
                title={
                  <Space>
                    {getStatusBadge('OFFLINE')}
                    오프라인
                  </Space>
                }
                value={summary.offlineCount}
                valueStyle={{ color: '#F59E0B' }}
                suffix="대"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card style={{ borderTop: '3px solid #F43F5E', background: 'linear-gradient(135deg, rgba(244,63,94,0.08) 0%, transparent 60%)' }}>
              <Statistic
                title={
                  <Space>
                    {getStatusBadge('ERROR')}
                    오류
                  </Space>
                }
                value={summary.errorCount}
                valueStyle={{ color: '#F43F5E' }}
                suffix="대"
              />
            </Card>
          </Col>
        </Row>
      )}

      {/* Device Type Cards */}
      {summary && Object.keys(summary.countByTypeAndStatus).length > 0 && (
        <Row gutter={16} style={{ marginBottom: 24 }}>
          {Object.entries(summary.countByTypeAndStatus).map(([type, statusCounts]) => (
            <Col span={6} key={type}>
              <Card
                size="small"
                title={<span style={{ color: '#06B6D4' }}>{type}</span>}
                style={{ borderLeft: '3px solid #06B6D4' }}
              >
                <Space direction="vertical" size={4}>
                  {Object.entries(statusCounts).map(([status, count]) => (
                    <div key={status}>
                      <Badge
                        status={
                          status === '온라인' ? 'success' :
                          status === '오류' ? 'error' : 'default'
                        }
                      />
                      <span style={{
                        marginLeft: 8,
                        color: status === '온라인' ? '#10B981' :
                               status === '오류' ? '#F43F5E' : '#F59E0B',
                      }}>
                        {status}: {count}대
                      </span>
                    </div>
                  ))}
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      {/* Filters */}
      <Space style={{ marginBottom: 16 }}>
        <Select
          placeholder="장비 유형"
          style={{ width: 150 }}
          value={typeFilter}
          onChange={setTypeFilter}
          options={deviceTypeOptions}
        />
        <Button icon={<ReloadOutlined />} onClick={fetchData} loading={loading}>
          새로고침
        </Button>
      </Space>

      {/* Device Table */}
      <Table
        columns={columns}
        dataSource={filteredDevices}
        rowKey="deviceId"
        loading={loading}
        size="middle"
        pagination={false}
        scroll={{ x: 1000 }}
      />
    </>
  );
};

export default MonitoringPage;

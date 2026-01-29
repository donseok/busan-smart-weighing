/**
 * 감사 로그 조회 페이지 컴포넌트 (관리자 전용)
 *
 * 시스템에서 발생한 모든 변경 이력(감사 로그)을 조회하는 페이지입니다.
 * 기간, 사용자, 엔티티 유형, 작업 유형별 필터 검색을 지원하며,
 * 변경 전/후 데이터를 JSON 비교(Diff) 형태로 상세 확인할 수 있습니다.
 * 관리자 권한이 필요합니다.
 *
 * @returns 감사 로그 조회 페이지 JSX
 */
import React, { useEffect, useState } from 'react';
import {
  DatePicker,
  Select,
  Button,
  Space,
  Modal,
  Typography,
  Tag,
  Descriptions,
} from 'antd';
import SortableTable from '../../components/SortableTable';
import { SearchOutlined, ReloadOutlined, EyeOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { AuditLog, ApiResponse, PageResponse } from '../../types';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const actionTypeOptions = [
  { label: '전체', value: '' },
  { label: '로그인', value: 'LOGIN' },
  { label: '로그아웃', value: 'LOGOUT' },
  { label: '생성', value: 'CREATE' },
  { label: '수정', value: 'UPDATE' },
  { label: '삭제', value: 'DELETE' },
  { label: '비밀번호 초기화', value: 'PASSWORD_RESET' },
  { label: '역할 변경', value: 'ROLE_CHANGE' },
  { label: '활성화', value: 'ACTIVATE' },
  { label: '비활성화', value: 'DEACTIVATE' },
  { label: '잠금 해제', value: 'UNLOCK' },
];

const entityTypeOptions = [
  { label: '전체', value: '' },
  { label: '사용자', value: 'USER' },
  { label: '배차', value: 'DISPATCH' },
  { label: '계량', value: 'WEIGHING' },
  { label: '출문', value: 'GATE_PASS' },
  { label: '운송사', value: 'COMPANY' },
  { label: '차량', value: 'VEHICLE' },
  { label: '계량대', value: 'SCALE' },
  { label: '설정', value: 'SETTING' },
];

const AdminAuditLogPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);                      // 감사 로그 목록
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 20, total: 0 });
  const [detailModalVisible, setDetailModalVisible] = useState(false);   // 상세 모달
  const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null); // 상세 보기 대상

  // 검색 필터 상태
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null]>([null, null]); // 기간
  const [actionType, setActionType] = useState('');   // 작업 유형 (CREATE/UPDATE/DELETE 등)
  const [entityType, setEntityType] = useState('');   // 엔티티 유형 (USER/DISPATCH 등)

  const fetchLogs = async (page = 1, size = 20) => {
    setLoading(true);
    try {
      const token = localStorage.getItem('accessToken');
      const params = new URLSearchParams({
        page: String(page - 1),
        size: String(size),
      });

      if (dateRange[0]) params.append('startDate', dateRange[0].format('YYYY-MM-DD'));
      if (dateRange[1]) params.append('endDate', dateRange[1].format('YYYY-MM-DD'));
      if (actionType) params.append('actionType', actionType);
      if (entityType) params.append('entityType', entityType);

      const response = await fetch(`/api/v1/admin/audit-logs?${params.toString()}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const result: ApiResponse<PageResponse<AuditLog>> = await response.json();
      if (result.success) {
        setLogs(result.data.content);
        setPagination({
          current: result.data.number + 1,
          pageSize: result.data.size,
          total: result.data.totalElements,
        });
      }
    } catch (error) {
      console.error('Failed to fetch audit logs:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const handleSearch = () => {
    fetchLogs(1, pagination.pageSize);
  };

  const handleReset = () => {
    setDateRange([null, null]);
    setActionType('');
    setEntityType('');
    fetchLogs(1, pagination.pageSize);
  };

  const renderJsonDiff = (oldValue?: string, newValue?: string) => {
    if (!oldValue && !newValue) return <Text type="secondary">-</Text>;

    const tryParse = (val?: string) => {
      if (!val) return null;
      try {
        return JSON.parse(val);
      } catch {
        return val;
      }
    };

    return (
      <div style={{ display: 'flex', gap: 16 }}>
        {oldValue && (
          <div style={{ flex: 1 }}>
            <Text strong>이전 값:</Text>
            <pre style={{ fontSize: 12, background: '#fff1f0', padding: 8, borderRadius: 4, overflow: 'auto' }}>
              {JSON.stringify(tryParse(oldValue), null, 2)}
            </pre>
          </div>
        )}
        {newValue && (
          <div style={{ flex: 1 }}>
            <Text strong>변경 값:</Text>
            <pre style={{ fontSize: 12, background: '#f6ffed', padding: 8, borderRadius: 4, overflow: 'auto' }}>
              {JSON.stringify(tryParse(newValue), null, 2)}
            </pre>
          </div>
        )}
      </div>
    );
  };

  const columns: ColumnsType<AuditLog> = [
    {
      title: '시간',
      dataIndex: 'createdAt',
      width: 160,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '사용자',
      dataIndex: 'actorName',
      width: 100,
      render: (name: string, record) => name || `ID: ${record.actorId}` || '-',
    },
    {
      title: '액션',
      dataIndex: 'actionTypeDesc',
      width: 120,
      render: (desc: string, record) => {
        const colorMap: Record<string, string> = {
          CREATE: 'green',
          UPDATE: 'blue',
          DELETE: 'red',
          LOGIN: 'cyan',
          LOGOUT: 'default',
          PASSWORD_RESET: 'orange',
          ROLE_CHANGE: 'purple',
          ACTIVATE: 'green',
          DEACTIVATE: 'default',
          UNLOCK: 'gold',
        };
        return <Tag color={colorMap[record.actionType] || 'default'}>{desc}</Tag>;
      },
    },
    {
      title: '대상',
      dataIndex: 'entityTypeDesc',
      width: 100,
    },
    {
      title: '대상 ID',
      dataIndex: 'entityId',
      width: 100,
      render: (id: number) => id ?? '-',
    },
    {
      title: 'IP 주소',
      dataIndex: 'ipAddress',
      width: 130,
      render: (ip: string) => ip || '-',
    },
    {
      title: '상세',
      width: 80,
      render: (_, record) => (
        <Button
          type="text"
          icon={<EyeOutlined />}
          onClick={() => {
            setSelectedLog(record);
            setDetailModalVisible(true);
          }}
        />
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>감사 로그</Title>
      </div>

      {/* 필터 영역 */}
      <div style={{ marginBottom: 16, display: 'flex', gap: 12, flexWrap: 'wrap' }}>
        <RangePicker
          value={dateRange}
          onChange={(dates) => setDateRange(dates as [dayjs.Dayjs | null, dayjs.Dayjs | null])}
          placeholder={['시작일', '종료일']}
        />
        <Select
          value={actionType}
          onChange={setActionType}
          options={actionTypeOptions}
          style={{ width: 150 }}
          placeholder="액션 타입"
        />
        <Select
          value={entityType}
          onChange={setEntityType}
          options={entityTypeOptions}
          style={{ width: 120 }}
          placeholder="대상 타입"
        />
        <Space>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            검색
          </Button>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>
            초기화
          </Button>
        </Space>
      </div>

      <SortableTable
        columns={columns}
        dataSource={logs}
        rowKey="auditLogId"
        loading={loading}
        tableKey="adminAuditLog"
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `총 ${total}건`,
        }}
        onChange={(p) => fetchLogs(p.current, p.pageSize)}
        size="small"
      />

      {/* 상세 모달 */}
      <Modal
        title="감사 로그 상세"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedLog && (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="시간">
              {dayjs(selectedLog.createdAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
            <Descriptions.Item label="사용자">
              {selectedLog.actorName || '-'} (ID: {selectedLog.actorId || '-'})
            </Descriptions.Item>
            <Descriptions.Item label="액션">{selectedLog.actionTypeDesc}</Descriptions.Item>
            <Descriptions.Item label="대상">{selectedLog.entityTypeDesc}</Descriptions.Item>
            <Descriptions.Item label="대상 ID">{selectedLog.entityId || '-'}</Descriptions.Item>
            <Descriptions.Item label="IP 주소">{selectedLog.ipAddress || '-'}</Descriptions.Item>
            <Descriptions.Item label="User Agent" span={2}>
              <Text style={{ fontSize: 12 }}>{selectedLog.userAgent || '-'}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="변경 내용" span={2}>
              {renderJsonDiff(selectedLog.oldValue, selectedLog.newValue)}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default AdminAuditLogPage;

import React from 'react';
import { Table, Tag, theme } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { WeighingHistoryRecord } from '../../types/weighingStation';
import { WEIGHING_MODE_LABELS } from '../../types/weighingStation';

interface WeighingHistoryTableProps {
  history: WeighingHistoryRecord[];
}

const STATUS_TAG_COLORS: Record<string, string> = {
  COMPLETED: 'green',
  IN_PROGRESS: 'blue',
  RE_WEIGHING: 'orange',
  ERROR: 'red',
  CANCELLED: 'default',
};

const STATUS_LABELS: Record<string, string> = {
  COMPLETED: '완료',
  IN_PROGRESS: '진행중',
  RE_WEIGHING: '재계량',
  ERROR: '오류',
  CANCELLED: '취소',
};

const columns: ColumnsType<WeighingHistoryRecord> = [
  {
    title: '시간',
    dataIndex: 'createdAt',
    width: 80,
    render: (dt: string) => {
      try {
        return new Date(dt).toLocaleTimeString('ko-KR', { hour12: false, hour: '2-digit', minute: '2-digit' });
      } catch {
        return '-';
      }
    },
  },
  {
    title: '차량번호',
    dataIndex: 'plateNumber',
    width: 100,
    ellipsis: true,
    render: (v: string) => v || '-',
  },
  {
    title: '중량(kg)',
    dataIndex: 'grossWeight',
    width: 90,
    align: 'right',
    render: (w: number) => w?.toLocaleString() ?? '-',
  },
  {
    title: '모드',
    dataIndex: 'weighingMode',
    width: 80,
    render: (mode: string) => (
      <Tag color="blue" style={{ fontSize: 11 }}>
        {WEIGHING_MODE_LABELS[mode] || mode}
      </Tag>
    ),
  },
  {
    title: '상태',
    dataIndex: 'weighingStatus',
    width: 70,
    render: (status: string) => (
      <Tag color={STATUS_TAG_COLORS[status] || 'default'} style={{ fontSize: 11 }}>
        {STATUS_LABELS[status] || status}
      </Tag>
    ),
  },
];

const WeighingHistoryTable: React.FC<WeighingHistoryTableProps> = ({ history }) => {
  const { token } = theme.useToken();

  return (
    <div
      style={{
        background: token.colorBgContainer,
        border: `1px solid ${token.colorBorder}`,
        borderLeft: `3px solid #64748B`,
        borderRadius: 12,
        padding: '16px 0',
        overflow: 'hidden',
      }}
    >
      <div
        style={{
          fontSize: 13,
          color: '#64748B',
          fontWeight: 600,
          marginBottom: 8,
          padding: '0 20px',
          letterSpacing: '0.05em',
          textTransform: 'uppercase',
        }}
      >
        최근 계량 기록
        <span style={{
          marginLeft: 8,
          fontSize: 11,
          fontWeight: 500,
          background: `${token.colorPrimary}18`,
          color: token.colorPrimary,
          padding: '1px 8px',
          borderRadius: 10,
        }}>
          {history.length}건
        </span>
      </div>
      <Table
        columns={columns}
        dataSource={history}
        rowKey="weighingId"
        size="small"
        pagination={false}
        scroll={{ y: 300 }}
        locale={{ emptyText: '계량 기록이 없습니다' }}
      />
    </div>
  );
};

export default WeighingHistoryTable;

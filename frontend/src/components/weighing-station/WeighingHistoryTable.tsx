import React from 'react';
import { Table, Tag, theme } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { WeighingHistoryRecord } from '../../types/weighingStation';
import { WEIGHING_MODE_LABELS } from '../../types/weighingStation';

/**
 * 계량 이력 테이블 컴포넌트의 속성 인터페이스
 *
 * @property history - 계량 이력 레코드 배열
 */
interface WeighingHistoryTableProps {
  history: WeighingHistoryRecord[];
}

/**
 * 계량 상태별 태그 색상 매핑
 */
const STATUS_TAG_COLORS: Record<string, string> = {
  COMPLETED: 'green',
  IN_PROGRESS: 'blue',
  RE_WEIGHING: 'orange',
  ERROR: 'red',
  CANCELLED: 'default',
};

/**
 * 계량 상태별 한국어 라벨 매핑
 */
const STATUS_LABELS: Record<string, string> = {
  COMPLETED: '완료',
  IN_PROGRESS: '진행중',
  RE_WEIGHING: '재계량',
  ERROR: '오류',
  CANCELLED: '취소',
};

/**
 * 테이블 컬럼 정의
 *
 * - 시간: 생성일시를 HH:mm 형식으로 표시
 * - 차량번호: 차량 번호판 (없으면 '-')
 * - 중량(kg): 총중량을 천 단위 구분하여 표시
 * - 모드: 계량 모드 (자동/수동 등) 태그로 표시
 * - 상태: 계량 상태 (완료/진행중/오류 등) 태그로 표시
 */
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

/**
 * 계량 이력 테이블 컴포넌트
 *
 * 최근 수행된 계량 기록을 테이블 형태로 표시합니다.
 * 시간, 차량번호, 중량, 계량 모드, 상태 정보를 컬럼으로 보여주며,
 * 고정 높이(300px) 내에서 스크롤이 가능합니다.
 * 헤더에 총 기록 건수 배지가 표시됩니다.
 *
 * @param props - 컴포넌트 속성
 * @param props.history - 계량 이력 레코드 배열
 * @returns 계량 이력 테이블 JSX
 */
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
      {/* 패널 제목 및 기록 건수 배지 */}
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
        {/* 기록 건수 표시 배지 */}
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
      {/* Ant Design 테이블 (페이지네이션 없이 스크롤 사용) */}
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

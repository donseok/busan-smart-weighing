import React from 'react';
import { Card, Col, Row, Space, Badge, Tag, Table, Progress } from 'antd';
import {
  CarOutlined,
  CheckCircleOutlined,
  ExperimentOutlined,
  ClockCircleOutlined,
  LogoutOutlined,
  StopOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { DashboardSummary, WeighingRecord } from '../../types';
import { WEIGHING_MODE_LABELS } from '../../constants/labels';

interface RealtimeTabProps {
  summary: DashboardSummary | null;
  inProgressWeighings: WeighingRecord[];
  colors: {
    primary: string;
    success: string;
    warning: string;
    error: string;
    border: string;
    bgElevated: string;
  };
}

const RealtimeTab: React.FC<RealtimeTabProps> = ({ summary, inProgressWeighings, colors }) => {
  const inProgressColumns: ColumnsType<WeighingRecord> = [
    { title: 'ID', dataIndex: 'weighingId', width: 60 },
    {
      title: '계량방식',
      dataIndex: 'weighingMode',
      width: 100,
      render: (mode: string) => <Tag color="blue">{WEIGHING_MODE_LABELS[mode] || mode}</Tag>,
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
      render: () => <Badge status="processing" text="진행중" />,
    },
    {
      title: '시작시간',
      dataIndex: 'createdAt',
      width: 150,
      render: (dt: string) => new Date(dt).toLocaleString('ko-KR'),
    },
  ];

  const calcPercent = (completed: number, total: number) =>
    total > 0 ? Math.round((completed / total) * 100) : 0;

  return (
    <>
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
                    ? calcPercent(
                        summary.dispatchCompleted,
                        summary.dispatchRegistered + summary.dispatchInProgress + summary.dispatchCompleted + summary.dispatchCancelled,
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
                    ? calcPercent(
                        summary.gatePassPassed,
                        summary.gatePassPending + summary.gatePassPassed + summary.gatePassRejected,
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
                    ? calcPercent(
                        summary.weighingCompleted,
                        summary.weighingInProgress + summary.weighingCompleted + summary.weighingReWeighing,
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
};

export default RealtimeTab;

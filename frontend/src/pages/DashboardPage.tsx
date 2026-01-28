import React from 'react';
import { Card, Col, Row, Statistic, Typography } from 'antd';
import { CarOutlined, ExperimentOutlined, CheckCircleOutlined, FileTextOutlined } from '@ant-design/icons';

const DashboardPage: React.FC = () => {
  return (
    <>
      <Typography.Title level={4}>대시보드</Typography.Title>
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic title="오늘 배차" value={0} prefix={<CarOutlined />} suffix="건" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="계량 진행" value={0} prefix={<ExperimentOutlined />} suffix="건" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="계량 완료" value={0} prefix={<CheckCircleOutlined />} suffix="건" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="전자 계량표" value={0} prefix={<FileTextOutlined />} suffix="건" />
          </Card>
        </Col>
      </Row>
    </>
  );
};

export default DashboardPage;

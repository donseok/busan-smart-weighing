/**
 * 차량 관리 페이지 컴포넌트 (기준정보)
 *
 * 계량 시스템에 등록된 차량(Vehicle) 정보를 관리하는 페이지입니다.
 * 차량 등록/수정/삭제 기능과 목록 조회를 제공하며,
 * 차량번호, 차량유형, 공차중량, 최대적재량, 운전자 연락처 등을 관리합니다.
 * MasterCrudPage 공통 컴포넌트를 활용하여 CRUD를 구현합니다.
 *
 * @returns 차량 관리 페이지 JSX
 */
import React from 'react';
import { Input, InputNumber, Form } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Vehicle } from '../../types';
import { maxLengthRule, plateNumberRule, phoneNumberRule, positiveNumberRule, mustBeGreaterThanField } from '../../utils/validators';
import MasterCrudPage from '../../components/MasterCrudPage';

const columns: ColumnsType<Vehicle> = [
  { title: 'ID', dataIndex: 'vehicleId', width: 80 },
  { title: '차량번호', dataIndex: 'plateNumber', width: 110 },
  { title: '차종', dataIndex: 'vehicleType', width: 90 },
  { title: '기본공차(kg)', dataIndex: 'defaultTareWeight', width: 130, align: 'right', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '최대적재(kg)', dataIndex: 'maxLoadWeight', width: 130, align: 'right', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '기사명', dataIndex: 'driverName', width: 100 },
  { title: '기사연락처', dataIndex: 'driverPhone', width: 130 },
];

const formFields = (
  <>
    <Form.Item name="plateNumber" label="차량번호" rules={[{ required: true }, maxLengthRule(20), plateNumberRule]}><Input /></Form.Item>
    <Form.Item name="vehicleType" label="차종" rules={[{ required: true }, maxLengthRule(20)]}><Input /></Form.Item>
    <Form.Item name="companyId" label="운송사 ID"><Input type="number" /></Form.Item>
    <Form.Item name="defaultTareWeight" label="기본 공차중량(kg)" rules={[positiveNumberRule]}><InputNumber style={{ width: '100%' }} /></Form.Item>
    <Form.Item name="maxLoadWeight" label="최대 적재중량(kg)" dependencies={['defaultTareWeight']} rules={[positiveNumberRule, mustBeGreaterThanField('defaultTareWeight', '기본 공차중량')]}><InputNumber style={{ width: '100%' }} /></Form.Item>
    <Form.Item name="driverName" label="기사명" rules={[maxLengthRule(50)]}><Input /></Form.Item>
    <Form.Item name="driverPhone" label="기사 연락처" rules={[phoneNumberRule]}><Input placeholder="010-0000-0000" /></Form.Item>
  </>
);

const MasterVehiclePage: React.FC = () => (
  <MasterCrudPage<Vehicle>
    title="차량 관리"
    endpoint="/master/vehicles"
    rowKey="vehicleId"
    tableKey="masterVehicle"
    entityName="차량"
    searchPlaceholder="차량번호 검색"
    columns={columns}
    formFields={formFields}
    getEditFieldValues={(r) => ({
      plateNumber: r.plateNumber,
      vehicleType: r.vehicleType,
      companyId: r.companyId,
      defaultTareWeight: r.defaultTareWeight,
      maxLoadWeight: r.maxLoadWeight,
      driverName: r.driverName,
      driverPhone: r.driverPhone,
    })}
  />
);

export default MasterVehiclePage;

/**
 * 계량대(저울) 관리 페이지 컴포넌트 (기준정보)
 *
 * 계량소에 설치된 계량대(Scale) 장비 정보를 관리하는 페이지입니다.
 * 계량대 등록/수정/삭제 기능과 목록 조회를 제공하며,
 * 저울명, 위치, 최대용량, 최소감도, 검교정일, 활성 상태 등을 관리합니다.
 * MasterCrudPage 공통 컴포넌트를 활용하여 CRUD를 구현합니다.
 *
 * @returns 계량대 관리 페이지 JSX
 */
import React from 'react';
import { Input, InputNumber, Tag, Form } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Scale } from '../../types';
import { maxLengthRule, positiveNumberRule, mustBeLessThanField } from '../../utils/validators';
import { SCALE_STATUS_COLORS } from '../../constants/labels';
import MasterCrudPage from '../../components/MasterCrudPage';

const columns: ColumnsType<Scale> = [
  { title: 'ID', dataIndex: 'scaleId', width: 80 },
  { title: '계량대명', dataIndex: 'scaleName', width: 130 },
  { title: '위치', dataIndex: 'location', width: 110 },
  { title: '최대용량(kg)', dataIndex: 'maxCapacity', width: 130, align: 'right', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '최소용량(kg)', dataIndex: 'minCapacity', width: 130, align: 'right', render: (v?: number) => v?.toLocaleString() ?? '-' },
  { title: '상태', dataIndex: 'scaleStatus', width: 100, render: (v: string) => <Tag color={SCALE_STATUS_COLORS[v]}>{v}</Tag> },
];

const formFields = (
  <>
    <Form.Item name="scaleName" label="계량대명" rules={[{ required: true }, maxLengthRule(50)]}><Input /></Form.Item>
    <Form.Item name="location" label="위치" rules={[maxLengthRule(100)]}><Input /></Form.Item>
    <Form.Item name="maxCapacity" label="최대용량(kg)" rules={[positiveNumberRule]}><InputNumber style={{ width: '100%' }} /></Form.Item>
    <Form.Item name="minCapacity" label="최소용량(kg)" dependencies={['maxCapacity']} rules={[positiveNumberRule, mustBeLessThanField('maxCapacity', '최대용량')]}><InputNumber style={{ width: '100%' }} /></Form.Item>
  </>
);

const MasterScalePage: React.FC = () => (
  <MasterCrudPage<Scale>
    title="계량대 관리"
    endpoint="/master/scales"
    rowKey="scaleId"
    tableKey="masterScale"
    entityName="계량대"
    searchPlaceholder="계량대명 검색"
    columns={columns}
    formFields={formFields}
    getEditFieldValues={(r) => ({
      scaleName: r.scaleName,
      location: r.location,
      maxCapacity: r.maxCapacity,
      minCapacity: r.minCapacity,
    })}
    extractData={(resData) => {
      const d = resData as { data?: Scale[] };
      return d.data || [];
    }}
    filterData={(data, keyword) => {
      if (!keyword) return data;
      const kw = keyword.toLowerCase();
      return data.filter((item) => item.scaleName.toLowerCase().includes(kw));
    }}
  />
);

export default MasterScalePage;

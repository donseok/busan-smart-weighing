/**
 * 운송사 관리 페이지 컴포넌트 (기준정보)
 *
 * 계량 시스템에 등록된 운송사(Company) 정보를 관리하는 페이지입니다.
 * 운송사 등록/수정/삭제 기능과 목록 조회를 제공하며,
 * 업체명, 사업자번호, 대표자, 연락처, 주소 등의 정보를 관리합니다.
 * MasterCrudPage 공통 컴포넌트를 활용하여 CRUD를 구현합니다.
 *
 * @returns 운송사 관리 페이지 JSX
 */
import React from 'react';
import { Input, Form } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Company } from '../../types';
import { maxLengthRule, businessNumberRule, phoneNumberRule } from '../../utils/validators';
import MasterCrudPage from '../../components/MasterCrudPage';

const columns: ColumnsType<Company> = [
  { title: 'ID', dataIndex: 'companyId', width: 80 },
  { title: '운송사명', dataIndex: 'companyName', width: 130 },
  { title: '유형', dataIndex: 'companyType', width: 100 },
  { title: '사업자번호', dataIndex: 'businessNumber', width: 130 },
  { title: '대표자', dataIndex: 'representative', width: 100 },
  { title: '연락처', dataIndex: 'phoneNumber', width: 130 },
  { title: '주소', dataIndex: 'address', ellipsis: true },
];

const formFields = (
  <>
    <Form.Item name="companyName" label="운송사명" rules={[{ required: true }, maxLengthRule(100)]}><Input /></Form.Item>
    <Form.Item name="companyType" label="유형" rules={[{ required: true }, maxLengthRule(20)]}><Input /></Form.Item>
    <Form.Item name="businessNumber" label="사업자번호" rules={[businessNumberRule]}><Input /></Form.Item>
    <Form.Item name="representative" label="대표자" rules={[maxLengthRule(50)]}><Input /></Form.Item>
    <Form.Item name="phoneNumber" label="연락처" rules={[phoneNumberRule]}><Input placeholder="010-0000-0000" /></Form.Item>
    <Form.Item name="address" label="주소" rules={[maxLengthRule(200)]}><Input /></Form.Item>
  </>
);

const MasterCompanyPage: React.FC = () => (
  <MasterCrudPage<Company>
    title="운송사 관리"
    endpoint="/master/companies"
    rowKey="companyId"
    tableKey="masterCompany"
    entityName="운송사"
    searchPlaceholder="운송사명 검색"
    columns={columns}
    formFields={formFields}
    getEditFieldValues={(r) => ({
      companyName: r.companyName,
      companyType: r.companyType,
      businessNumber: r.businessNumber,
      representative: r.representative,
      phoneNumber: r.phoneNumber,
      address: r.address,
    })}
  />
);

export default MasterCompanyPage;

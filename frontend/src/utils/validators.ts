/**
 * @fileoverview Ant Design Form 유효성 검증 규칙 모음
 *
 * 전화번호, 사업자번호, 비밀번호 강도, 차량번호, 이메일 등
 * 공통 유효성 검증 패턴과 날짜/교차 필드 검증기를 제공합니다.
 *
 * @module utils/validators
 */
import type { RuleObject } from 'antd/es/form';
import dayjs from 'dayjs';

// --- Regex Patterns ---
const PHONE_PATTERN = /^01[016789]-\d{3,4}-\d{4}$/;
const BUSINESS_NUMBER_PATTERN = /^\d{10}$/;
const PASSWORD_STRENGTH_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).+$/;
const PLATE_NUMBER_PATTERN = /^[가-힣]{0,2}\d{2,3}[가-힣]\d{4}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// --- Length Rules ---
/**
 * 최대 글자 수 유효성 검증 규칙을 생성합니다.
 * @param n - 허용 최대 글자 수
 * @returns Ant Design Form 규칙 객체
 */
export const maxLengthRule = (n: number): RuleObject => ({
  max: n,
  message: `${n}자 이하로 입력하세요`,
});

/**
 * 최소 글자 수 유효성 검증 규칙을 생성합니다.
 * @param n - 필수 최소 글자 수
 * @returns Ant Design Form 규칙 객체
 */
export const minLengthRule = (n: number): RuleObject => ({
  min: n,
  message: `${n}자 이상 입력하세요`,
});

// --- Pattern Rules ---
/** 전화번호 형식 유효성 검증 규칙 (예: 010-1234-5678) */
export const phoneNumberRule: RuleObject = {
  pattern: PHONE_PATTERN,
  message: '올바른 전화번호 형식을 입력하세요 (예: 010-1234-5678)',
};

/** 사업자번호 유효성 검증 규칙 (숫자 10자리) */
export const businessNumberRule: RuleObject = {
  pattern: BUSINESS_NUMBER_PATTERN,
  message: '사업자번호는 숫자 10자리를 입력하세요',
};

export const passwordStrengthRule: RuleObject = {
  pattern: PASSWORD_STRENGTH_PATTERN,
  message: '영문과 숫자를 모두 포함해야 합니다',
};

export const plateNumberRule: RuleObject = {
  pattern: PLATE_NUMBER_PATTERN,
  message: '올바른 차량번호 형식을 입력하세요 (예: 12가3456)',
};

export const emailRule: RuleObject = {
  pattern: EMAIL_PATTERN,
  message: '올바른 이메일 형식을 입력하세요',
};

// --- Number Rules ---
export const positiveNumberRule: RuleObject = {
  type: 'number',
  min: 0,
  message: '0 이상의 값을 입력하세요',
};

// --- Date Validators ---
export const futureOrPresentDateValidator = (_: RuleObject, value: unknown) => {
  if (!value) return Promise.resolve();
  const date = dayjs(value as string);
  if (date.isBefore(dayjs(), 'day')) {
    return Promise.reject(new Error('오늘 이전 날짜는 선택할 수 없습니다'));
  }
  return Promise.resolve();
};

// --- Cross-field Validators ---
export const mustBeGreaterThanField = (field: string, label: string) =>
  ({ getFieldValue }: { getFieldValue: (name: string) => unknown }) => ({
    validator(_: RuleObject, value: unknown) {
      if (value == null) return Promise.resolve();
      const other = getFieldValue(field);
      if (other == null) return Promise.resolve();
      if (Number(value) <= Number(other)) {
        return Promise.reject(new Error(`${label}보다 큰 값을 입력하세요`));
      }
      return Promise.resolve();
    },
  });

export const mustBeLessThanField = (field: string, label: string) =>
  ({ getFieldValue }: { getFieldValue: (name: string) => unknown }) => ({
    validator(_: RuleObject, value: unknown) {
      if (value == null) return Promise.resolve();
      const other = getFieldValue(field);
      if (other == null) return Promise.resolve();
      if (Number(value) >= Number(other)) {
        return Promise.reject(new Error(`${label}보다 작은 값을 입력하세요`));
      }
      return Promise.resolve();
    },
  });

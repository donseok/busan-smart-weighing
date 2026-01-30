/**
 * 프론트엔드 전역 라벨/상수 맵
 *
 * 여러 페이지에서 중복 선언되던 상수를 중앙 집중 관리합니다.
 */

// ─── 품목 유형 ───
export const ITEM_TYPE_LABELS: Record<string, string> = {
  BY_PRODUCT: '부산물',
  WASTE: '폐기물',
  SUB_MATERIAL: '부재료',
  EXPORT: '반출',
  GENERAL: '일반',
};

export const ITEM_TYPE_OPTIONS = Object.entries(ITEM_TYPE_LABELS).map(
  ([value, label]) => ({ value, label }),
);

// ─── 계량 방식 ───
export const WEIGHING_MODE_LABELS: Record<string, string> = {
  LPR_AUTO: 'LPR자동',
  MOBILE_OTP: '모바일OTP',
  MANUAL: '수동',
  RE_WEIGH: '재계량',
};

// ─── 배차 상태 ───
export const DISPATCH_STATUS_LABELS: Record<string, string> = {
  REGISTERED: '등록',
  IN_PROGRESS: '진행중',
  COMPLETED: '완료',
  CANCELLED: '취소',
};

export const DISPATCH_STATUS_OPTIONS = Object.entries(DISPATCH_STATUS_LABELS).map(
  ([value, label]) => ({ value, label }),
);

// ─── 상태 색상 (다크 테마 기반, colors 객체의 키 매핑) ───
export const DISPATCH_STATUS_COLORS: Record<string, string> = {
  REGISTERED: '#06B6D4',  // primary
  IN_PROGRESS: '#F59E0B', // warning
  COMPLETED: '#10B981',   // success
  CANCELLED: '#F43F5E',   // error
};

// ─── 계량대 상태 색상 ───
export const SCALE_STATUS_COLORS: Record<string, string> = {
  IDLE: '#CBD5E1',    // textSecondary (밝게 조정)
  WEIGHING: '#F59E0B', // warning
  COMPLETED: '#10B981', // success
  ERROR: '#F43F5E',    // error
};

// ─── 코드그룹 색상 ───
export const CODE_GROUP_COLORS: Record<string, string> = {
  ITEM_TYPE: 'blue',
  VEHICLE_TYPE: 'green',
  WEIGHING_MODE: 'orange',
  COMPANY_TYPE: 'purple',
  DISPATCH_STATUS: 'cyan',
  WEIGHING_STATUS: 'magenta',
};

// ─── 공지 카테고리 ───
export const NOTICE_CATEGORY_OPTIONS = [
  { value: 'SYSTEM', label: '시스템' },
  { value: 'MAINTENANCE', label: '시스템 점검' },
  { value: 'UPDATE', label: '업데이트' },
  { value: 'GENERAL', label: '일반 공지' },
];

export const NOTICE_CATEGORY_COLORS: Record<string, string> = {
  SYSTEM: 'blue',
  MAINTENANCE: 'orange',
  UPDATE: 'purple',
  GENERAL: 'cyan',
};

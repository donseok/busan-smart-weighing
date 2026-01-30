/**
 * Ant Design 테마 설정 모듈
 *
 * 다크 모드와 라이트 모드의 전체 UI 테마를 정의합니다.
 * "Modern Industrial Intelligence" 디자인 컨셉을 기반으로,
 * 다크 모드는 SF 관제 센터, 라이트 모드는 클린 비즈니스 스타일을 적용합니다.
 * 각 모드별 색상 팔레트, 컴포넌트별 토큰, 알고리즘을 설정하며,
 * colors 객체로 공통 시맨틱 색상(primary, success, warning, error)을 제공합니다.
 *
 * @module themeConfig
 */
import { ThemeConfig, theme } from 'antd';

// UI/UX Recommendation: "Modern Industrial Intelligence"
// Dark Mode - SF 관제 센터 컨셉
// Light Mode - 클린 비즈니스 컨셉

// 다크 테마 색상
const darkColors = {
  bgBase: '#0B1120',       // Deep Navy - 메인 배경
  bgSurface: '#1E293B',    // Charcoal - 카드/패널 배경
  bgElevated: '#0F172A',   // 테이블 헤더, 사이드바
  bgSider: '#070D1A',      // 사이드바 배경 (더 깊은 남색)
  primary: '#06B6D4',      // Neon Cyan - 핵심 데이터, 주요 버튼
  success: '#10B981',      // Emerald - 완료, 정상
  warning: '#F59E0B',      // Amber - 주의, 재계량
  error: '#F43F5E',        // Rose - 에러, 통신 두절
  textPrimary: '#F8FAFC',  // 기본 텍스트
  textSecondary: '#CBD5E1', // 보조 텍스트 (Slate 300 - 가독성 향상)
  border: '#334155',       // 테두리 색상
} as const;

// 라이트 테마 색상
const lightColors = {
  bgBase: '#F8FAFC',       // Snow White - 메인 배경
  bgSurface: '#FFFFFF',    // Pure White - 카드/패널 배경
  bgElevated: '#F1F5F9',   // Light Gray - 테이블 헤더
  bgSider: '#FFFFFF',      // White - 사이드바 배경
  primary: '#0891B2',      // Cyan 600 - 더 진한 primary (가독성)
  success: '#059669',      // Emerald 600 - 완료, 정상
  warning: '#D97706',      // Amber 600 - 주의, 재계량
  error: '#E11D48',        // Rose 600 - 에러, 통신 두절
  textPrimary: '#0F172A',  // Dark Slate - 기본 텍스트
  textSecondary: '#64748B', // Slate 500 - 보조 텍스트
  border: '#E2E8F0',       // Slate 200 - 테두리 색상
} as const;

// 기존 호환성을 위한 colors (다크 테마 기본)
const colors = darkColors;

/** 디자인 토큰: 타이포그래피 */
export const typography = {
  /** 페이지 제목 */
  titleLg: { fontSize: 20, fontWeight: 600, lineHeight: 1.4 },
  /** 섹션 제목 */
  titleMd: { fontSize: 16, fontWeight: 600, lineHeight: 1.5 },
  /** 카드/그룹 제목 */
  titleSm: { fontSize: 14, fontWeight: 600, lineHeight: 1.5 },
  /** 본문 기본 */
  bodyMd: { fontSize: 14, lineHeight: 1.6 },
  /** 본문 작은 */
  bodySm: { fontSize: 13, lineHeight: 1.5 },
  /** 캡션/보조 */
  caption: { fontSize: 12, lineHeight: 1.4 },
  /** 라벨 */
  label: { fontSize: 12, fontWeight: 500, letterSpacing: '0.02em' },
} as const;

/** 디자인 토큰: 간격 */
export const spacing = {
  /** 최소 간격 (4px) */
  xs: 4,
  /** 작은 간격 (8px) */
  sm: 8,
  /** 기본 간격 (12px) */
  md: 12,
  /** 넓은 간격 (16px) */
  lg: 16,
  /** 큰 간격 (24px) */
  xl: 24,
  /** 최대 간격 (32px) */
  xxl: 32,
  /** 페이지 마진 */
  page: 24,
  /** 카드 내부 패딩 */
  card: 16,
  /** 섹션 간격 */
  section: 20,
} as const;

/** 디자인 토큰: 아이콘 크기 */
export const iconSize = {
  /** 인라인 아이콘 */
  sm: 14,
  /** 기본 아이콘 */
  md: 16,
  /** 버튼 아이콘 */
  lg: 20,
  /** 헤더/타이틀 아이콘 */
  xl: 24,
} as const;

export const darkTheme: ThemeConfig = {
  algorithm: theme.darkAlgorithm,
  token: {
    colorPrimary: colors.primary,
    colorSuccess: colors.success,
    colorWarning: colors.warning,
    colorError: colors.error,
    colorBgBase: colors.bgBase,
    colorBgContainer: colors.bgSurface,
    colorBgElevated: colors.bgSurface,
    colorBgLayout: colors.bgBase,
    colorTextBase: colors.textPrimary,
    colorBorder: colors.border,
    colorBorderSecondary: '#1E293B',
    borderRadius: 8,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    fontSize: 14,
    colorLink: colors.primary,
    colorLinkHover: '#22D3EE',
    controlHeight: 36,
    controlOutline: 'rgba(6, 182, 212, 0.25)',
    controlOutlineWidth: 2,
    wireframe: false,
  },
  components: {
    Layout: {
      siderBg: colors.bgSider,
      headerBg: colors.bgSurface,
      bodyBg: colors.bgBase,
      triggerBg: '#0F172A',
      triggerColor: colors.textPrimary,
    },
    Menu: {
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemSelectedBg: 'rgba(6, 182, 212, 0.15)',
      darkItemSelectedColor: colors.primary,
      darkItemHoverBg: 'rgba(6, 182, 212, 0.08)',
      darkItemColor: colors.textSecondary,
      itemMarginInline: 8,
      itemBorderRadius: 8,
    },
    Card: {
      colorBgContainer: colors.bgSurface,
      colorBorderSecondary: colors.border,
    },
    Table: {
      colorBgContainer: colors.bgSurface,
      headerBg: colors.bgElevated,
      headerColor: colors.textSecondary,
      rowHoverBg: 'rgba(6, 182, 212, 0.06)',
      borderColor: '#1E293B',
      headerBorderRadius: 8,
    },
    Modal: {
      contentBg: colors.bgSurface,
      headerBg: colors.bgSurface,
      titleColor: colors.textPrimary,
    },
    Button: {
      primaryShadow: '0 2px 8px rgba(6, 182, 212, 0.35)',
      defaultBg: colors.bgElevated,
      defaultBorderColor: colors.border,
      defaultColor: colors.textPrimary,
    },
    Input: {
      colorBgContainer: colors.bgElevated,
      activeBorderColor: colors.primary,
      hoverBorderColor: 'rgba(6, 182, 212, 0.5)',
    },
    Select: {
      colorBgContainer: colors.bgElevated,
      optionSelectedBg: 'rgba(6, 182, 212, 0.15)',
      colorBgElevated: colors.bgSurface,
    },
    DatePicker: {
      colorBgContainer: colors.bgElevated,
      colorBgElevated: colors.bgSurface,
    },
    Statistic: {
      contentFontSize: 28,
    },
    Descriptions: {
      colorBgContainer: colors.bgElevated,
      labelBg: colors.bgElevated,
      colorSplit: colors.border,
    },
    Tag: {
      defaultBg: colors.bgElevated,
    },
    Spin: {
      colorPrimary: colors.primary,
    },
    Typography: {
      colorText: colors.textPrimary,
      colorTextSecondary: colors.textSecondary,
      colorTextHeading: colors.textPrimary,
    },
    Form: {
      labelColor: colors.textSecondary,
    },
    Popconfirm: {
      colorWarning: colors.warning,
    },
  },
};

// 라이트 테마 설정
export const lightTheme: ThemeConfig = {
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: lightColors.primary,
    colorSuccess: lightColors.success,
    colorWarning: lightColors.warning,
    colorError: lightColors.error,
    colorBgBase: lightColors.bgBase,
    colorBgContainer: lightColors.bgSurface,
    colorBgElevated: lightColors.bgSurface,
    colorBgLayout: lightColors.bgBase,
    colorTextBase: lightColors.textPrimary,
    colorBorder: lightColors.border,
    colorBorderSecondary: lightColors.border,
    borderRadius: 8,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    fontSize: 14,
    colorLink: lightColors.primary,
    colorLinkHover: '#0E7490',
    controlHeight: 36,
    controlOutline: 'rgba(8, 145, 178, 0.2)',
    controlOutlineWidth: 2,
    wireframe: false,
  },
  components: {
    Layout: {
      siderBg: lightColors.bgSider,
      headerBg: lightColors.bgSurface,
      bodyBg: lightColors.bgBase,
      triggerBg: lightColors.bgElevated,
      triggerColor: lightColors.textPrimary,
    },
    Menu: {
      itemBg: 'transparent',
      subMenuItemBg: 'transparent',
      itemSelectedBg: 'rgba(8, 145, 178, 0.1)',
      itemSelectedColor: lightColors.primary,
      itemHoverBg: 'rgba(8, 145, 178, 0.05)',
      itemColor: lightColors.textSecondary,
      itemMarginInline: 8,
      itemBorderRadius: 8,
    },
    Card: {
      colorBgContainer: lightColors.bgSurface,
      colorBorderSecondary: lightColors.border,
    },
    Table: {
      colorBgContainer: lightColors.bgSurface,
      headerBg: lightColors.bgElevated,
      headerColor: lightColors.textSecondary,
      rowHoverBg: 'rgba(8, 145, 178, 0.04)',
      borderColor: lightColors.border,
      headerBorderRadius: 8,
    },
    Modal: {
      contentBg: lightColors.bgSurface,
      headerBg: lightColors.bgSurface,
      titleColor: lightColors.textPrimary,
    },
    Button: {
      primaryShadow: '0 2px 8px rgba(8, 145, 178, 0.25)',
      defaultBg: lightColors.bgSurface,
      defaultBorderColor: lightColors.border,
      defaultColor: lightColors.textPrimary,
    },
    Input: {
      colorBgContainer: lightColors.bgSurface,
      activeBorderColor: lightColors.primary,
      hoverBorderColor: 'rgba(8, 145, 178, 0.5)',
    },
    Select: {
      colorBgContainer: lightColors.bgSurface,
      optionSelectedBg: 'rgba(8, 145, 178, 0.1)',
      colorBgElevated: lightColors.bgSurface,
    },
    DatePicker: {
      colorBgContainer: lightColors.bgSurface,
      colorBgElevated: lightColors.bgSurface,
    },
    Statistic: {
      contentFontSize: 28,
    },
    Descriptions: {
      colorBgContainer: lightColors.bgElevated,
      labelBg: lightColors.bgElevated,
      colorSplit: lightColors.border,
    },
    Tag: {
      defaultBg: lightColors.bgElevated,
    },
    Spin: {
      colorPrimary: lightColors.primary,
    },
    Typography: {
      colorText: lightColors.textPrimary,
      colorTextSecondary: lightColors.textSecondary,
      colorTextHeading: lightColors.textPrimary,
    },
    Form: {
      labelColor: lightColors.textSecondary,
    },
    Popconfirm: {
      colorWarning: lightColors.warning,
    },
  },
};

// 테마별 색상 객체 내보내기
export { colors, darkColors, lightColors };
export default darkTheme;

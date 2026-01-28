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
  textSecondary: '#94A3B8', // 보조 텍스트
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

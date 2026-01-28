import { ThemeConfig, theme } from 'antd';

// UI/UX Recommendation: "Modern Industrial Intelligence"
// Dark Mode - SF 관제 센터 컨셉

const colors = {
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

export { colors };
export default darkTheme;

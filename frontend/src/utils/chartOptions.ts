/**
 * 대시보드 차트 옵션 생성 유틸리티
 *
 * DashboardPage에서 ~240 LOC의 차트 설정을 분리합니다.
 */
import type { WeighingStatistics, CompanyStatistics } from '../types';
import { ITEM_TYPE_LABELS, WEIGHING_MODE_LABELS } from '../constants/labels';

interface ThemeColors {
  primary: string;
  success: string;
  warning: string;
  error: string;
  textPrimary: string;
  textSecondary: string;
  bgSurface: string;
  border: string;
  bgElevated: string;
  bgBase: string;
  bgSider: string;
}

interface ChartContext {
  colors: ThemeColors;
  themeMode: 'dark' | 'light';
}

function textStyle(ctx: ChartContext) {
  return {
    color: ctx.colors.textSecondary,
    fontFamily: "'Inter', sans-serif",
  };
}

function axisStyle(ctx: ChartContext) {
  return {
    axisLine: { lineStyle: { color: ctx.colors.border } },
    axisTick: { lineStyle: { color: ctx.colors.border } },
    splitLine: {
      lineStyle: {
        color: ctx.themeMode === 'dark' ? 'rgba(51, 65, 85, 0.5)' : 'rgba(226, 232, 240, 0.8)',
      },
    },
    axisLabel: { color: ctx.colors.textSecondary },
    nameTextStyle: { color: ctx.colors.textSecondary },
  };
}

function titleOpt(ctx: ChartContext, text: string) {
  return {
    text,
    left: 'center' as const,
    textStyle: { ...textStyle(ctx), color: ctx.colors.textPrimary, fontSize: 15, fontWeight: 500 },
  };
}

function tooltipBase(ctx: ChartContext) {
  return {
    backgroundColor: ctx.colors.bgSurface,
    borderColor: ctx.colors.border,
    textStyle: { color: ctx.colors.textPrimary },
  };
}

// ─── 일별 계량 추이 (Line) ───
export function buildLineChartOption(statistics: WeighingStatistics | null, ctx: ChartContext) {
  const daily = statistics?.dailyStatistics ?? [];
  const startColor = ctx.themeMode === 'dark' ? 'rgba(6, 182, 212, 0.25)' : 'rgba(8, 145, 178, 0.18)';
  const endColor = ctx.themeMode === 'dark' ? 'rgba(6, 182, 212, 0.02)' : 'rgba(8, 145, 178, 0.02)';
  return {
    title: titleOpt(ctx, '일별 계량 추이'),
    tooltip: { trigger: 'axis' as const, ...tooltipBase(ctx) },
    xAxis: {
      type: 'category' as const,
      data: daily.map((d) => d.date),
      ...axisStyle(ctx),
      axisLabel: { rotate: 30, color: ctx.colors.textSecondary },
    },
    yAxis: { type: 'value' as const, name: '건수', minInterval: 1, ...axisStyle(ctx) },
    series: [
      {
        name: '계량 건수',
        type: 'line' as const,
        data: daily.map((d) => d.totalCount),
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: startColor },
              { offset: 1, color: endColor },
            ],
          },
        },
        itemStyle: { color: ctx.colors.primary },
        lineStyle: { width: 2.5, color: ctx.colors.primary },
      },
    ],
    grid: { left: 50, right: 30, bottom: 60, top: 50 },
    backgroundColor: 'transparent',
  };
}

// ─── 품목별 계량 (Pie) ───
export function buildPieChartOption(statistics: WeighingStatistics | null, ctx: ChartContext) {
  const byItemType = statistics?.countByItemType ?? {};
  const data = Object.entries(byItemType).map(([key, value]) => ({
    name: ITEM_TYPE_LABELS[key] ?? key,
    value,
  }));
  const pieColors = [ctx.colors.primary, ctx.colors.success, ctx.colors.warning, '#8B5CF6', ctx.colors.error];
  return {
    title: titleOpt(ctx, '품목별 계량'),
    tooltip: { trigger: 'item' as const, ...tooltipBase(ctx) },
    legend: { bottom: 0, textStyle: { color: ctx.colors.textSecondary } },
    series: [
      {
        type: 'pie' as const,
        radius: ['35%', '60%'],
        data,
        color: pieColors,
        emphasis: {
          itemStyle: {
            shadowBlur: 20,
            shadowOffsetX: 0,
            shadowColor: ctx.themeMode === 'dark' ? 'rgba(0, 0, 0, 0.5)' : 'rgba(0, 0, 0, 0.15)',
          },
        },
        label: { formatter: '{b}: {c}건 ({d}%)', color: ctx.colors.textSecondary },
        itemStyle: { borderColor: ctx.colors.bgSurface, borderWidth: 2 },
      },
    ],
    backgroundColor: 'transparent',
  };
}

// ─── 계량 방식별 현황 (Bar) ───
export function buildBarChartOption(statistics: WeighingStatistics | null, ctx: ChartContext) {
  const byMode = statistics?.countByWeighingMode ?? {};
  const categories = Object.keys(byMode).map((key) => WEIGHING_MODE_LABELS[key] ?? key);
  const values = Object.values(byMode);
  const barEnd = ctx.themeMode === 'dark' ? 'rgba(16, 185, 129, 0.4)' : 'rgba(5, 150, 105, 0.3)';
  return {
    title: titleOpt(ctx, '계량 방식별 현황'),
    tooltip: { trigger: 'axis' as const, ...tooltipBase(ctx) },
    xAxis: { type: 'category' as const, data: categories, ...axisStyle(ctx) },
    yAxis: { type: 'value' as const, name: '건수', minInterval: 1, ...axisStyle(ctx) },
    series: [
      {
        type: 'bar' as const,
        data: values,
        itemStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: ctx.colors.success },
              { offset: 1, color: barEnd },
            ],
          },
          borderRadius: [6, 6, 0, 0],
        },
        barMaxWidth: 50,
      },
    ],
    grid: { left: 50, right: 30, bottom: 40, top: 50 },
    backgroundColor: 'transparent',
  };
}

// ─── 운송사별 계량 현황 (Bar) ───
export function buildCompanyBarChartOption(companyStats: CompanyStatistics[], ctx: ChartContext) {
  const sorted = [...companyStats].slice(0, 10);
  const barEnd = ctx.themeMode === 'dark' ? 'rgba(139, 92, 246, 0.4)' : 'rgba(139, 92, 246, 0.25)';
  return {
    title: titleOpt(ctx, '운송사별 계량 현황 (이번 달)'),
    tooltip: {
      trigger: 'axis' as const,
      ...tooltipBase(ctx),
      formatter: (params: { name: string; value: number; seriesName: string }[]) => {
        const p = params[0];
        const stat = sorted.find((s) => s.companyName === p.name);
        return `${p.name}<br/>계량 건수: ${p.value}건<br/>총 중량: ${stat?.totalNetWeightTon.toFixed(2) || 0} 톤`;
      },
    },
    xAxis: {
      type: 'category' as const,
      data: sorted.map((s) => s.companyName),
      ...axisStyle(ctx),
      axisLabel: { rotate: 30, color: ctx.colors.textSecondary, interval: 0 },
    },
    yAxis: { type: 'value' as const, name: '건수', minInterval: 1, ...axisStyle(ctx) },
    series: [
      {
        type: 'bar' as const,
        data: sorted.map((s) => s.weighingCount),
        itemStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#8B5CF6' },
              { offset: 1, color: barEnd },
            ],
          },
          borderRadius: [6, 6, 0, 0],
        },
        barMaxWidth: 40,
      },
    ],
    grid: { left: 50, right: 30, bottom: 80, top: 50 },
    backgroundColor: 'transparent',
  };
}

// ─── 운송사 Top 5 (Horizontal Bar) ───
export function buildCompanyTopChartOption(companyStats: CompanyStatistics[], ctx: ChartContext) {
  const top5 = [...companyStats].sort((a, b) => b.weighingCount - a.weighingCount).slice(0, 5).reverse();
  const barEnd = ctx.themeMode === 'dark' ? 'rgba(139, 92, 246, 0.4)' : 'rgba(139, 92, 246, 0.25)';
  return {
    title: titleOpt(ctx, '운송사 Top 5 (이번 달)'),
    tooltip: {
      trigger: 'axis' as const,
      ...tooltipBase(ctx),
      formatter: (params: { name: string; value: number }[]) => {
        const p = params[0];
        const stat = top5.find((s) => s.companyName === p.name);
        return `${p.name}<br/>계량 건수: ${p.value}건<br/>총 중량: ${stat?.totalNetWeightTon.toFixed(2) || 0} 톤`;
      },
    },
    xAxis: { type: 'value' as const, name: '건수', minInterval: 1, ...axisStyle(ctx) },
    yAxis: {
      type: 'category' as const,
      data: top5.map((s) => s.companyName),
      ...axisStyle(ctx),
      axisLabel: { color: ctx.colors.textSecondary, width: 80, overflow: 'truncate' as const },
    },
    series: [
      {
        type: 'bar' as const,
        data: top5.map((s) => s.weighingCount),
        itemStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: barEnd },
              { offset: 1, color: '#8B5CF6' },
            ],
          },
          borderRadius: [0, 6, 6, 0],
        },
        barMaxWidth: 28,
      },
    ],
    grid: { left: 90, right: 30, bottom: 40, top: 50 },
    backgroundColor: 'transparent',
  };
}

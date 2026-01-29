/**
 * ECharts 트리쉐이킹 설정
 *
 * 전체 echarts (~700KB) 대신 사용하는 차트 타입만 import하여
 * 번들 크기를 ~200KB로 줄입니다.
 */
import * as echarts from 'echarts/core';
import { LineChart, BarChart, PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer,
]);

export default echarts;

# React + Ant Design 전문 에이전트

## 역할
부산 스마트 계량 시스템의 프론트엔드 개발 전문 에이전트입니다.
React 18 / TypeScript strict / Ant Design 5 / Vite 기반의 웹 애플리케이션 개발을 담당합니다.

## 전문 영역
- React 18.3.1 + TypeScript 5.9.3 (strict 모드)
- Ant Design 5.29.3 (다크/라이트 테마)
- Vite 7.3.1 빌드 + 코드 분할
- ECharts 6.0 (tree-shaking)
- STOMP/SockJS WebSocket 클라이언트
- @dnd-kit (드래그 앤 드롭)
- Axios (JWT 자동 첨부, camelCase↔snake_case 변환)

## 프로젝트 컨텍스트

### 디렉토리 구조
```
src/
├── api/client.ts              # Axios (JWT, camelCase↔snake_case, 401 토큰갱신)
├── api/weighingStationApi.ts  # 계량소 전용 API
├── components/
│   ├── MasterCrudPage.tsx     # CRUD 공통 컴포넌트 (테이블+모달+검색)
│   ├── SortableTable.tsx      # @dnd-kit 드래그 정렬 테이블
│   ├── AnimatedNumber.tsx     # 숫자 애니메이션
│   ├── OnboardingTour.tsx     # Ant Design Tour
│   ├── dashboard/             # OverviewTab, RealtimeTab, AnalysisTab
│   └── weighing-station/      # 10개 하위 컴포넌트
├── config/pageRegistry.ts     # 중앙 페이지 레지스트리 (라우트, 아이콘, 권한, lazy)
├── constants/labels.ts        # 한국어 레이블 상수
├── context/                   # AuthContext, TabContext, ThemeContext
├── hooks/
│   ├── useApiCall.ts          # API 호출 래퍼 (로딩/에러 자동관리)
│   ├── useCrudState.ts        # CRUD 상태 관리
│   ├── useWebSocket.ts        # STOMP WebSocket 관리
│   ├── useWeighingStation.ts  # 계량소 비즈니스 로직
│   └── useWeighingStationSocket.ts
├── pages/                     # 17개 페이지 (admin/, master/ 하위 포함)
├── theme/themeConfig.ts       # Ant Design 5 테마 토큰
├── types/index.ts             # 공통 TypeScript 인터페이스
├── types/weighingStation.ts   # 계량소 전용 타입
└── utils/
    ├── validators.ts          # Form 검증 규칙
    ├── chartOptions.ts        # ECharts 옵션
    └── echartsSetup.ts        # ECharts tree-shaking 설정
```

### 필수 규칙
1. **TypeScript strict**: noUnusedLocals, noUnusedParameters → 미사용 변수 에러
2. **Path alias**: `@/*` → `src/*`
3. **페이지 등록**: 반드시 `pageRegistry.ts`에 등록 (path, label, icon, roles, component)
4. **CRUD 패턴**: `MasterCrudPage` 컴포넌트 + `useCrudState` + `useApiCall` 활용
5. **폼 검증**: Ant Design Form + rules 배열 (`validators.ts` 공통 규칙)
6. **API 호출**: `api/client.ts`의 Axios 인스턴스 사용 (camelCase↔snake_case 자동변환)
7. **인증**: `AuthContext`에서 전역 관리 (로그인/로그아웃/토큰 자동갱신)
8. **탭 네비게이션**: `TabContext` (최대 10탭, 고정탭 지원)
9. **테마**: `ThemeContext` (다크/라이트 전환, themeConfig.ts)
10. **코드 분할**: `React.lazy` + pageRegistry (vendor/antd/echarts 청크 분리)

### 페이지 권한 체계
- 전체 접근: dashboard, dispatch, weighing, gate-pass, slips, statistics 등
- ADMIN + MANAGER: master/* (기준정보)
- ADMIN 전용: admin/* (사용자, 설정, 감사로그)

### 주의사항
- `npm run build` 시 TypeScript strict → 미사용 변수/파라미터 에러
- API JSON은 snake_case, 코드는 camelCase (Axios 인터셉터 자동 변환)
- ECharts는 tree-shaking 적용 (`echartsSetup.ts`에서 사용 컴포넌트만 등록)
- WebSocket: JWT 인증 후 사용 가능, 자동 재연결 (5초 딜레이)

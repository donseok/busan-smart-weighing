# 부산 스마트 계량 시스템 (Busan Smart Weighing System)

차량 계량 자동화 시스템. LPR(차량번호인식) 기반 입출문, 실시간 계량, 전표 발행, 모바일 배차 관리를 포함하는 풀스택 모노레포 프로젝트.

## 프로젝트 구조

```
busan-smart-weighing/
├── backend/          # Spring Boot 3.2.5 / Java 17 REST API + WebSocket
├── frontend/         # React 18 / TypeScript / Vite / Ant Design
├── mobile/           # Flutter / Dart 모바일 앱
├── weighing-cs/      # C# .NET 8 WinForms 현장 계량 프로그램
└── docs/             # 기술 문서, 기능 명세, WBS
```

## 기술 스택

| 모듈 | 핵심 기술 | 빌드/배포 |
|------|----------|----------|
| backend | Spring Boot 3.2.5, JPA, Spring Security, JWT(JJWT 0.12.5), WebSocket(STOMP), Redis, PostgreSQL | Gradle / Railway |
| frontend | React 18.3.1, TypeScript 5.9.3, Ant Design 5.29.3, Axios, ECharts, SockJS/STOMP | Vite 7.3.1 / Vercel |
| mobile | Flutter 3.10+, Dart, Provider, Dio, Go Router, Firebase Messaging | Flutter CLI |
| weighing-cs | .NET 8, WinForms, System.IO.Ports, SQLite, Newtonsoft.Json | dotnet CLI |

## 개발 명령어

### 백엔드

```bash
cd backend
./gradlew bootRun                     # 개발 서버 (H2 + embedded Redis, port 8080)
./gradlew test                        # 테스트 실행
./gradlew build                       # JAR 빌드
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
- Profile: dev(기본, H2+Redis), prod(PostgreSQL+Redis), test(H2)

### 프론트엔드

```bash
cd frontend
npm install                           # 의존성 설치
npm run dev                           # 개발 서버 (port 3000, API proxy → 8080)
npm run build                         # tsc + vite build (TypeScript 에러 체크 포함)
```

### 모바일

```bash
cd mobile
flutter pub get                       # 의존성 설치
flutter run                           # 에뮬레이터/실기기 실행
flutter test                          # 테스트 실행
```

- Mock 모드: `lib/config/api_config.dart` → `useMockData = true`

### 데스크톱

```bash
cd weighing-cs
dotnet build                          # 빌드
dotnet run --project WeighingCS       # 실행
dotnet test                           # xUnit 테스트
```

## 백엔드 아키텍처

### 계층 구조

`Controller` → `Service` → `Repository` → `Entity/DB`

### 패키지 구조 (도메인별)

```
com.dongkuk.weighing/
├── auth/           # JWT 인증, 로그인, 토큰 갱신
├── user/           # 사용자 관리 (ADMIN, MANAGER, DRIVER)
├── master/         # 기준정보 (Company, Vehicle, Scale, CommonCode)
├── dispatch/       # 배차 관리
├── weighing/       # 계량 핵심 로직 (WeighingRecord, WeighingStep)
├── gatepass/       # 출문 관리
├── slip/           # 전표 관리 (엑셀 다운로드)
├── lpr/            # 차량번호 인식
├── notification/   # FCM 푸시 알림
├── otp/            # OTP 인증
├── dashboard/      # 대시보드 통계
├── audit/          # 감사 로그
├── websocket/      # STOMP 실시간 메시지
└── global/         # 공통 설정, 예외처리, 유틸, 보안
    ├── config/     # SecurityConfig, CorsConfig, RedisConfig, WebSocketConfig
    ├── common/     # ApiResponse, BusinessException, ErrorCode, GlobalExceptionHandler
    └── audit/      # BaseEntity (createdAt, updatedAt 자동 관리)
```

### API 응답 형식

모든 API는 `ApiResponse<T>` 래퍼를 사용한다:

```json
{ "success": true, "data": { ... }, "error": null }
{ "success": false, "data": null, "error": { "code": "...", "message": "..." } }
```

- JSON 필드: snake_case (Jackson property-naming-strategy)
- 날짜 형식: ISO 8601

### 인증

- JWT Access Token (30분) + Refresh Token (7일)
- 로그아웃 시 Redis 블랙리스트 등록
- 역할: ADMIN, MANAGER, DRIVER

### 실시간 통신

- WebSocket 엔드포인트: `/ws` (SockJS 폴백)
- 구독: `/topic/*` (서버→클라이언트)
- 전송: `/app/*` (클라이언트→서버)

## 프론트엔드 아키텍처

### 디렉토리 구조

```
src/
├── api/            # Axios 인스턴스 (인터셉터: JWT 자동첨부, 401 토큰갱신, camelCase⇄snake_case 변환)
├── components/     # 재사용 컴포넌트 (SortableTable, FavoriteButton, weighing-station/*)
├── constants/      # 상수
├── context/        # React Context (ThemeContext, TabContext)
├── hooks/          # 커스텀 Hook (useWebSocket, useWeighingStation, useApiCall)
├── layouts/        # MainLayout
├── pages/          # 페이지 컴포넌트
│   ├── admin/      # AdminUserPage, AdminAuditLogPage, AdminSettingsPage
│   └── master/     # MasterCompanyPage, MasterVehiclePage, MasterScalePage, MasterCodePage
├── theme/          # Ant Design 테마 (다크/라이트)
├── types/          # TypeScript 인터페이스
└── utils/          # validators.ts (폼 검증 유틸)
```

### 주요 패턴

- Ant Design Form + rules 배열로 폼 검증 (validators.ts에 공통 규칙 정의)
- `Form.useForm()` 으로 폼 상태 관리
- `useCallback` + `useEffect` 조합으로 데이터 로딩
- Vite 개발 서버 프록시: `/api` → `localhost:8080`, `/ws` → `localhost:8080`
- 빌드 시 vendor/antd/echarts 청크 분리

### TypeScript 설정

- strict 모드 활성화
- noUnusedLocals, noUnusedParameters 활성화 → 빌드 시 미사용 변수 에러
- path alias: `@/*` → `src/*`

## 모바일 아키텍처

- Provider 패턴으로 상태 관리 (AuthProvider, DispatchProvider)
- Go Router로 선언적 라우팅 + 인증 리다이렉트
- Dio HTTP 클라이언트 + 인터셉터
- flutter_secure_storage로 토큰 안전 저장
- Mock API 지원 (개발/테스트 용)

## 데스크톱 (WeighingCS) 아키텍처

- 계량대와 시리얼 포트(COM) 통신으로 실시간 중량 데이터 수신
- 전광판/차단기와 TCP 네트워크 통신
- SQLite 로컬 캐시 (오프라인 대비)
- Simulator 클래스로 하드웨어 없이 개발/테스트 가능

## 코드 컨벤션

### 네이밍

| 대상 | 규칙 | 예시 |
|------|------|------|
| Java 클래스 | PascalCase | `DispatchService` |
| Java 메서드/변수 | camelCase | `findByPlateNumber` |
| DB 테이블/컬럼 | snake_case | `weighing_records` |
| React 컴포넌트/파일 | PascalCase | `DispatchPage.tsx` |
| TS 변수/함수 | camelCase | `handleSubmit` |
| API 엔드포인트 | kebab-case 복수형 | `/api/v1/gate-passes` |
| JSON 필드 | snake_case | `dispatch_date` |
| Flutter 파일 | snake_case | `dispatch_list_screen.dart` |

### 커밋 메시지

```
<type>: <한글 설명>

type: feat | fix | refactor | docs | style | test | chore
```

## 환경 변수 (운영)

백엔드 (Railway):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET`, `API_INTERNAL_KEY`, `AES_SECRET_KEY`
- `CORS_ORIGIN_WEB`, `FCM_ENABLED`, `FCM_SERVICE_ACCOUNT_FILE`

## 배포

- **프론트엔드**: Vercel (main push 시 자동 배포, vercel.json으로 SPA 라우팅 + API 프록시)
- **백엔드**: Railway (main push 시 자동 배포, PostgreSQL + Redis 관리형)
- **모바일**: Flutter 빌드 → APK/IPA
- **데스크톱**: dotnet publish → 현장 PC 설치

## 주의사항

- `npm run build` (프론트엔드) 시 TypeScript strict 모드로 미사용 변수/파라미터가 에러 발생
- 백엔드 Entity 수정 시 prod 환경은 `ddl-auto=validate`이므로 DB 마이그레이션 별도 필요
- API 요청/응답 JSON은 snake_case, 프론트엔드 코드는 camelCase (Axios 인터셉터가 자동 변환)
- Redis 연결 실패 시 로그인/로그아웃 불가 (토큰 블랙리스트 의존)
- WebSocket 연결은 JWT 인증 후 사용 가능

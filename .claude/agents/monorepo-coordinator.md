# 모노레포 변경 조율 전문 에이전트

## 역할
부산 스마트 계량 시스템의 4개 모듈(백엔드/프론트엔드/모바일/데스크톱) 간 변경사항을 조율하는 전문 에이전트입니다.
크로스 모듈 변경의 영향도를 분석하고, 일관성 있는 변경 계획을 수립합니다.

## 전문 영역
- 모노레포 구조에서의 크로스 모듈 변경 관리
- 변경 영향도 분석 (ripple effect)
- 배포 순서 결정
- 공유 도메인 모델 일관성 유지
- 코드 컨벤션 일관성 관리

## 프로젝트 구조
```
busan-smart-weighing/
├── backend/          # Spring Boot 3.2.5 / Java 17 (Railway)
├── frontend/         # React 18 / TypeScript / Vite (Vercel)
├── mobile/           # Flutter 3.10+ / Dart
├── weighing-cs/      # .NET 8 WinForms
└── docs/             # 문서 (PRD, TRD, WBS, 설계, 매뉴얼)
```

## 변경 영향 매트릭스

### 백엔드 변경 → 영향 범위
| 변경 유형 | 프론트엔드 | 모바일 | 데스크톱 | DB |
|----------|-----------|--------|---------|-----|
| DTO 필드 추가 | types/ 업데이트 | models/ 업데이트 | Models/ 업데이트 | - |
| DTO 필드 삭제 | ❌ Breaking | ❌ Breaking | ❌ Breaking | - |
| API 경로 변경 | api/ 업데이트 | services/ 업데이트 | ApiService 업데이트 | - |
| Entity 변경 | - | - | - | 마이그레이션 필요 |
| 인증 변경 | AuthContext | auth_provider | - | Redis |
| WebSocket 변경 | useWebSocket | - | - | - |
| 권한 변경 | pageRegistry | GoRouter guard | - | - |

### 프론트엔드 변경 → 영향 범위
- 대부분 프론트엔드 내부에서 완결
- pageRegistry.ts 변경: 라우팅/권한 영향
- API 호출 경로 변경: 백엔드 확인 필요
- WebSocket 구독 변경: 백엔드 토픽 확인 필요

### 모바일 변경 → 영향 범위
- 대부분 모바일 내부에서 완결
- API 호출 변경: 백엔드 확인 필요
- 모델 변경: 백엔드 DTO 확인 필요

### 데스크톱 변경 → 영향 범위
- 하드웨어 인터페이스 변경: 시뮬레이터 동시 업데이트
- API 호출 변경: 백엔드 확인 필요
- 모델 변경: 백엔드 DTO 확인 필요

## 배포 순서 가이드

### 일반 변경 (Breaking Change 없음)
1. backend → 2. frontend → 3. mobile → 4. desktop
- 백엔드 먼저 배포 (하위 호환성 유지)
- 프론트엔드는 Vercel 자동 배포
- 모바일/데스크톱은 별도 배포

### Breaking Change 포함
1. backend (새 엔드포인트 추가 + 구 엔드포인트 유지)
2. frontend/mobile/desktop (새 엔드포인트로 전환)
3. backend (구 엔드포인트 제거)
- 하위 호환성 보장 배포 전략

### DB 스키마 변경 포함
1. DB 마이그레이션 실행 (additive만)
2. backend 배포 (새 스키마 사용)
3. frontend/mobile/desktop 배포
4. DB 정리 (불필요한 컬럼 제거 - 별도 마이그레이션)

## 코드 컨벤션 일관성

### 네이밍 규칙 (모듈별)
| 대상 | 백엔드 | 프론트엔드 | 모바일 | 데스크톱 |
|------|--------|-----------|--------|---------|
| 클래스 | PascalCase | PascalCase | PascalCase | PascalCase |
| 메서드 | camelCase | camelCase | camelCase | PascalCase |
| 변수 | camelCase | camelCase | camelCase | camelCase |
| 파일 | PascalCase.java | PascalCase.tsx | snake_case.dart | PascalCase.cs |
| JSON | snake_case | camelCase (자동변환) | camelCase (수동매핑) | PascalCase ([JsonProperty]) |

### 커밋 메시지 규칙
```
<type>: <한글 설명>

type: feat | fix | refactor | docs | style | test | chore
```

### 변경 범위 태그
- `[backend]`: 백엔드만
- `[frontend]`: 프론트엔드만
- `[mobile]`: 모바일만
- `[desktop]`: 데스크톱만
- `[full-stack]`: 2개 이상 모듈
- `[all]`: 전체 모듈

## 주의사항
- DTO 필드 삭제는 Breaking Change → 반드시 단계적 배포
- Entity 변경은 prod DB 마이그레이션 필수 (ddl-auto=validate)
- API 응답 형식(ApiResponse<T>) 변경은 모든 플랫폼에 영향
- WebSocket 메시지 포맷 변경은 프론트엔드 + 가능하면 모바일에 영향
- 인증 변경(JWT, Redis)은 모든 플랫폼에 영향

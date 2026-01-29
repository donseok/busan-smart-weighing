# 부산 스마트 계량 시스템 - 테스트 결과 보고서

## 1. 테스트 개요

| 항목 | 내용 |
|------|------|
| 프로젝트 | 부산 스마트 계량 시스템 (Busan Smart Weighing System) |
| 실행 일시 | 2026-01-29 |
| 실행 환경 | Windows (win32), Git branch: main |
| 테스트 범위 | Backend, Frontend, Mobile, Desktop (전 모듈) |
| 실행 방법 | 기존 테스트 인프라 그대로 실행 (코드 수정 없음) |

---

## 2. 단위 테스트 결과

### 2.1 Backend (Spring Boot / JUnit 5)

| 항목 | 결과 |
|------|------|
| 상태 | **FAIL** - 컴파일 오류 |
| 테스트 클래스 수 | 16개 (단위 13 + 통합 3) |
| 실행된 테스트 | 0개 (컴파일 단계에서 실패) |

**단위 테스트 클래스 (13개)**:
| # | 클래스 | 도메인 |
|---|--------|--------|
| 1 | `UserTest` | user/domain |
| 2 | `UserRoleTest` | user/domain |
| 3 | `UserServiceTest` | user/service |
| 4 | `UserControllerTest` | user/controller |
| 5 | `AuthServiceTest` | auth/service |
| 6 | `AuthControllerTest` | auth/controller |
| 7 | `JwtTokenProviderTest` | auth/service |
| 8 | `CustomUserDetailsServiceTest` | auth/security |
| 9 | `OtpServiceTest` | otp/service |
| 10 | `OtpControllerTest` | otp/controller |
| 11 | `NoticeServiceTest` | notice/service |
| 12 | `EncryptionUtilTest` | global/util |
| 13 | `MaskingUtilTest` | global/util |

**통합 테스트 클래스 (3개)**:
| # | 클래스 | 도메인 |
|---|--------|--------|
| 1 | `AuthIntegrationTest` | integration |
| 2 | `UserIntegrationTest` | integration |
| 3 | `OtpIntegrationTest` | integration |

**오류 상세**:

```
> Task :compileTestJava FAILED

UserControllerTest.java:50: error: constructor UserResponse in record UserResponse cannot be applied to given types;
  required: Long,String,String,String,String,String,boolean,int,LocalDateTime,LocalDateTime
  found:    long,String,String,String,<null>,boolean,LocalDateTime
  reason: actual and formal argument lists differ in length

UserControllerTest.java:66: error: constructor UserResponse in record UserResponse cannot be applied to given types;
  (동일 오류)
```

**원인 분석**:
- `UserResponse` record가 10개 필드(`userId`, `loginId`, `userName`, `phoneNumber`, `userRole`, `companyName`, `isActive`, `failedLoginCount`, `lockedUntil`, `createdAt`)를 요구하지만, `UserControllerTest`에서 7개 인자만 전달함
- `companyName`(String), `failedLoginCount`(int), `lockedUntil`(LocalDateTime) 필드가 `UserResponse`에 추가된 후 테스트 코드가 업데이트되지 않음

**추가 경고**:
- `JwtTokenProviderTest.java`: deprecated API 사용 경고 (기능에는 영향 없음)

### 2.2 Desktop (C# .NET 8 / xUnit)

| 항목 | 결과 |
|------|------|
| 상태 | **FAIL** - NuGet 복원 오류 |
| 테스트 클래스 수 | 3개 |
| 실행된 테스트 | 0개 (복원 단계에서 실패) |

**테스트 클래스**:
| # | 클래스 | 대상 서비스 |
|---|--------|-----------|
| 1 | `ApiServiceTests` | ApiService |
| 2 | `IndicatorServiceTests` | IndicatorService |
| 3 | `LocalCacheServiceTests` | LocalCacheService |

**오류 상세**:
```
error NU1201: WeighingCS 프로젝트가 net8.0(.NETCoreApp,Version=v8.0)과(와) 호환되지 않습니다.
WeighingCS 프로젝트는 다음을 지원합니다. net8.0-windows7.0(.NETCoreApp,Version=v8.0)
```

**원인 분석**:
- `WeighingCS.csproj`는 `net8.0-windows` TFM(Target Framework Moniker)으로 설정된 WinForms 프로젝트
- `WeighingCS.Tests.csproj`는 `net8.0` (플랫폼 무관)으로 설정되어 있어 TFM 호환성 불일치 발생
- 테스트 프로젝트가 Windows 전용 프로젝트를 참조하므로, 테스트 프로젝트도 `net8.0-windows`로 변경 필요

### 2.3 Mobile (Flutter / flutter_test)

| 항목 | 결과 |
|------|------|
| 상태 | **SKIP** - Flutter SDK 미설치 |
| 테스트 파일 수 | 1개 (`widget_test.dart`) |
| 실행된 테스트 | 0개 |

**비고**: 현재 실행 환경에 Flutter SDK가 설치되어 있지 않아 테스트 실행 불가. CI/CD 환경에서 별도 실행 필요.

### 2.4 Frontend (TypeScript 타입 검증)

| 항목 | 결과 |
|------|------|
| 상태 | **PASS** |
| 검증 방법 | `npx tsc --noEmit` |
| 결과 | 타입 오류 0건 |

**비고**: 테스트 프레임워크(Jest, Vitest 등)가 설치되어 있지 않아 TypeScript 컴파일 검증으로 대체.

---

## 3. 통합 테스트 결과

### 3.1 Backend 통합 테스트

| 항목 | 결과 |
|------|------|
| 상태 | **FAIL** (단위 테스트와 동일 오류로 실행 불가) |
| 테스트 클래스 | AuthIntegrationTest, UserIntegrationTest, OtpIntegrationTest |
| 환경 | H2 + Embedded Redis (IntegrationTestBase) |

단위 테스트 컴파일 오류가 해결되어야 통합 테스트도 실행 가능.

### 3.2 Frontend 빌드 검증

| 항목 | 결과 |
|------|------|
| 상태 | **PASS** |
| 검증 방법 | `npm run build` (tsc + vite build) |
| 빌드 시간 | 50.71초 |
| 변환 모듈 수 | 3,909개 |
| 출력 파일 | 30개 (dist/) |

**빌드 경고**:
- `echarts-C6RcGpra.js` (1,137 KB) - 500KB 초과 청크
- `antd-COUqU2E_.js` (1,150 KB) - 500KB 초과 청크
- 권장: dynamic import 또는 manualChunks 설정으로 코드 분할 개선

---

## 4. E2E 테스트 결과 및 빌드 검증

### 4.1 E2E 프레임워크 현황

| 항목 | 상태 |
|------|------|
| Playwright | **미설치** |
| Cypress | **미설치** |
| Selenium | **미설치** |

E2E 테스트 프레임워크가 프로젝트에 구축되어 있지 않음. 빌드 검증으로 대체.

### 4.2 빌드 검증 결과

| 모듈 | 명령어 | 결과 |
|------|--------|------|
| Backend (메인 소스) | `./gradlew compileJava` | **PASS** |
| Backend (JAR) | `./gradlew build -x test` | **PASS** |
| Frontend | `npm run build` | **PASS** |
| Desktop (메인 프로젝트) | `dotnet build WeighingCS.csproj` | **PASS** |
| Desktop (테스트 프로젝트) | `dotnet test` | **FAIL** (TFM 호환성) |

---

## 5. 테스트 커버리지 현황

| 모듈 | 커버리지 도구 | 상태 | 비고 |
|------|-------------|------|------|
| Backend | JaCoCo | **미설정** | Gradle에 JaCoCo 플러그인 미적용 |
| Frontend | - | **해당 없음** | 테스트 프레임워크 없음 |
| Mobile | flutter_test | **미측정** | Flutter SDK 미설치로 실행 불가 |
| Desktop | coverlet.collector | **미측정** | TFM 호환성 오류로 실행 불가 |

---

## 6. 발견된 문제점

### 6.1 [심각] Backend `UserControllerTest` 컴파일 오류

- **파일**: `backend/src/test/java/.../user/controller/UserControllerTest.java` (line 50, 66)
- **원인**: `UserResponse` record에 `companyName`, `failedLoginCount`, `lockedUntil` 필드가 추가된 후 테스트 코드가 동기화되지 않음
- **영향**: 전체 Backend 테스트 스위트 실행 불가 (16개 클래스 모두)
- **수정 방안**: `UserControllerTest`의 `UserResponse` 생성자 호출을 10개 인자로 업데이트

### 6.2 [심각] Desktop 테스트 프로젝트 TFM 불일치

- **파일**: `weighing-cs/WeighingCS.Tests/WeighingCS.Tests.csproj`
- **원인**: 테스트 프로젝트 TFM이 `net8.0`이고, 참조 프로젝트가 `net8.0-windows`
- **영향**: 테스트 프로젝트 복원/빌드 불가 (3개 테스트 클래스 실행 불가)
- **수정 방안**: 테스트 프로젝트의 TargetFramework를 `net8.0-windows`로 변경

### 6.3 [경고] Backend deprecated API 사용

- **파일**: `JwtTokenProviderTest.java`
- **내용**: deprecated API 사용 경고
- **영향**: 기능 영향 없으나, 향후 API 제거 시 컴파일 오류 가능

### 6.4 [경고] Frontend 번들 사이즈

- **echarts**: 1,137 KB (gzip: 378 KB)
- **antd**: 1,150 KB (gzip: 359 KB)
- **영향**: 초기 로딩 성능에 영향 가능
- **권장**: 추가 코드 분할 또는 dynamic import 적용

---

## 7. 미비 사항 및 권고사항

### 7.1 테스트 인프라 미비 사항

| 항목 | 현황 | 권고 |
|------|------|------|
| Frontend 테스트 프레임워크 | 없음 | Vitest 또는 Jest 도입 권고 |
| Frontend 컴포넌트 테스트 | 없음 | React Testing Library 도입 권고 |
| E2E 테스트 | 없음 | Playwright 도입 권고 (계량 프로세스 핵심 시나리오) |
| Backend 커버리지 | 미측정 | JaCoCo 플러그인 적용 권고 |
| Mobile 테스트 | placeholder 1개 | 핵심 비즈니스 로직 테스트 추가 권고 |
| CI/CD 테스트 자동화 | 없음 | GitHub Actions 파이프라인 구성 권고 |

### 7.2 즉시 수정 필요 사항

1. **Backend `UserControllerTest`**: `UserResponse` 생성자 인자를 현재 record 정의(10개 필드)에 맞게 수정
2. **Desktop `WeighingCS.Tests.csproj`**: TargetFramework를 `net8.0-windows`로 변경

### 7.3 테스트 확대 권고 (우선순위순)

1. Backend 테스트 컴파일 오류 수정 후 전체 테스트 통과 확인
2. Desktop 테스트 TFM 수정 후 전체 테스트 통과 확인
3. Frontend Vitest 도입 + 주요 hook/util 단위 테스트 작성
4. Backend JaCoCo 적용으로 커버리지 가시화
5. Playwright E2E 테스트 (로그인 → 배차 → 계량 → 출문 → 전표 시나리오)
6. CI/CD 파이프라인에 테스트 자동 실행 통합

---

## 8. 종합 요약

| 모듈 | 단위 테스트 | 통합 테스트 | 빌드 검증 | 종합 |
|------|------------|-----------|----------|------|
| Backend | FAIL (컴파일 오류) | FAIL (동일) | PASS | **FAIL** |
| Frontend | PASS (타입 검증) | PASS (빌드) | PASS | **PASS** |
| Mobile | SKIP (SDK 미설치) | - | - | **SKIP** |
| Desktop | FAIL (TFM 불일치) | - | PASS (메인) | **FAIL** |

**전체 판정**: **FAIL** - Backend 테스트 컴파일 오류 및 Desktop 테스트 TFM 불일치 해결 필요

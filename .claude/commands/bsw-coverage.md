# BSW 테스트 커버리지 분석

4개 모듈의 테스트 현황을 분석하고 미커버 영역을 식별합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--module [backend|frontend|mobile|desktop|all]`: 분석 대상 (기본: all)
- `--threshold [0-100]`: 목표 커버리지 (기본: 80)

### 분석 순서

1. **백엔드 테스트 커버리지** (`backend/src/test/`)
   - 테스트 파일 목록 스캔
   - Controller/Service/Domain별 테스트 존재 여부 매핑
   - 현재 테스트 현황:
     - auth: AuthControllerTest, AuthServiceTest, JwtTokenProviderTest, CustomUserDetailsServiceTest ✅
     - user: UserControllerTest, UserServiceTest, UserTest, UserRoleTest ✅
     - otp: OtpControllerTest, OtpServiceTest ✅
     - global/util: EncryptionUtilTest, MaskingUtilTest ✅
     - integration: AuthIntegrationTest, UserIntegrationTest, OtpIntegrationTest ✅
   - 미커버 도메인 식별:
     - dispatch, weighing, gatepass, slip, lpr, notification, dashboard, statistics 등
   - Jacoco 리포트 생성 안내 (`./gradlew jacocoTestReport`)

2. **프론트엔드 테스트 커버리지** (`frontend/`)
   - 현재 상태: 테스트 프레임워크 미설정
   - 권장: Vitest + React Testing Library + @testing-library/jest-dom
   - 테스트 우선순위 제안:
     - AuthContext (인증 로직)
     - useApiCall, useCrudState (공통 훅)
     - MasterCrudPage (공통 CRUD 컴포넌트)
     - validators.ts (폼 검증 유틸)

3. **모바일 테스트 커버리지** (`mobile/test/`)
   - 테스트 파일 스캔
   - 테스트 프레임워크: flutter_test + mockito 5.4.0
   - Provider/Service/Model별 커버리지 분석

4. **데스크톱 테스트 커버리지** (`weighing-cs/WeighingCS.Tests/`)
   - xUnit 테스트 파일 스캔
   - Service별 커버리지 분석

5. **종합 리포트**
   ```
   === BSW 테스트 커버리지 리포트 ===

   | 모듈 | 테스트 수 | 커버 영역 | 미커버 영역 | 커버리지 |
   |------|----------|----------|------------|---------|
   | backend | 18 | auth, user, otp, util | dispatch, weighing 등 | ~35% |
   | frontend | 0 | - | 전체 | 0% |
   | mobile | ? | TBD | TBD | ?% |
   | desktop | ? | TBD | TBD | ?% |

   [우선 테스트 대상 - 비즈니스 임팩트 순]
   1. 🔴 WeighingService (계량 핵심 로직)
   2. 🔴 DispatchService (배차 관리)
   3. 🟡 GatePassService (출문 관리)
   4. 🟡 LprService (차량번호 인식)
   ```

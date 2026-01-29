# BSW 테스트 실행

전체 또는 특정 모듈의 테스트를 실행합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--module [backend|frontend|mobile|desktop|all]`: 테스트 대상 (기본: all)
- `--unit`: 단위 테스트만
- `--integration`: 통합 테스트만
- `--verbose`: 상세 출력

### 테스트 실행

1. **백엔드** (`backend/`)
   ```
   cd backend && ./gradlew test
   ```
   - 단위 테스트: Service, Domain, Util 계층
   - 통합 테스트: `src/test/java/**/integration/` (Testcontainers, EmbeddedRedis)
   - 프로필: test (H2 + embedded Redis)
   - 리포트: `build/reports/tests/test/index.html`

2. **프론트엔드** (`frontend/`)
   ```
   cd frontend && npm run build
   ```
   - TypeScript 컴파일 검증 (strict 모드)
   - 참고: 별도 테스트 프레임워크 미설정 상태 → 빌드 성공 = 타입 검증 통과

3. **모바일** (`mobile/`)
   ```
   cd mobile && flutter test
   ```
   - 테스트 프레임워크: flutter_test + mockito 5.4.0

4. **데스크톱** (`weighing-cs/`)
   ```
   cd weighing-cs && dotnet test
   ```
   - 프레임워크: xUnit
   - 테스트: ApiServiceTests, IndicatorServiceTests, LocalCacheServiceTests

5. **결과 요약**
   - 모듈별 Pass/Fail 카운트
   - 실패 테스트 상세 분석
   - 테스트 커버리지 정보 (가용 시)

# BSW 통합 테스트 스위트 (Skill)

4개 모듈의 테스트를 순차/병렬로 실행하고 종합 리포트를 생성합니다.

## 인자
$ARGUMENTS

## 워크플로우

### 옵션 파싱
- `--parallel`: 모듈별 병렬 실행 (기본: 순차)
- `--module [backend|frontend|mobile|desktop|all]`: 대상 (기본: all)
- `--report`: 상세 리포트 파일 생성
- `--fail-fast`: 첫 실패 시 중단

### Stage 1: 백엔드 테스트
```bash
cd backend && ./gradlew test
```
- 프로필: test (H2 + embedded Redis)
- 단위 테스트: Service, Domain, Util
- 통합 테스트: Controller + Testcontainers
- 리포트: `build/reports/tests/test/index.html`
- 결과 수집: Pass/Fail/Skip 카운트

### Stage 2: 프론트엔드 빌드 검증
```bash
cd frontend && npm run build
```
- TypeScript strict 모드 컴파일 검증
- noUnusedLocals, noUnusedParameters 에러 체크
- 빌드 성공 = 타입 안전성 검증 통과
- 번들 사이즈 리포트

### Stage 3: 모바일 테스트
```bash
cd mobile && flutter test
```
- flutter_test + mockito 프레임워크
- Provider/Service/Model 테스트
- 결과 수집

### Stage 4: 데스크톱 테스트
```bash
cd weighing-cs && dotnet test
```
- xUnit 프레임워크
- ApiServiceTests, IndicatorServiceTests, LocalCacheServiceTests
- 결과 수집

### Stage 5: 종합 리포트

```
╔══════════════════════════════════════════╗
║      BSW 통합 테스트 리포트              ║
╠══════════════════════════════════════════╣
║ 모듈       │ Pass │ Fail │ Skip │ 상태  ║
╠════════════╪══════╪══════╪══════╪═══════╣
║ backend    │  45  │   0  │   2  │  ✅   ║
║ frontend   │  빌드 │ 성공  │   -  │  ✅   ║
║ mobile     │  12  │   1  │   0  │  ❌   ║
║ desktop    │   8  │   0  │   0  │  ✅   ║
╠════════════╧══════╧══════╧══════╧═══════╣
║ 종합: 65 Pass / 1 Fail / 2 Skip        ║
║ 결과: ❌ FAIL (mobile 모듈 실패)         ║
╚══════════════════════════════════════════╝

[실패 상세]
mobile: test/services/api_service_test.dart
  - testFetchDispatches: Expected 3, got 2
```

### 실패 시 조치
- 실패 테스트 상세 로그 출력
- 관련 소스 코드 경로 안내
- 최근 변경사항 (git log) 제공
- 수정 제안

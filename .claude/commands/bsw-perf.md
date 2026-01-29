# BSW 성능 분석

N+1 쿼리, 번들 사이즈, 렌더링 성능 등을 분석합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--module [backend|frontend|all]`: 분석 대상 (기본: all)
- `--focus [query|bundle|render|api]`: 분석 초점
- `--detailed`: 상세 분석

### 백엔드 성능 분석

1. **JPA 쿼리 분석**
   - Repository 메서드의 N+1 쿼리 패턴 탐지
   - `@EntityGraph`, `JOIN FETCH` 사용 여부 확인
   - `FetchType.EAGER` 남용 검사
   - 페이징 쿼리 최적화 확인

2. **API 응답 성능**
   - Controller → Service → Repository 호출 체인 분석
   - 불필요한 데이터 로딩 (DTO 프로젝션 미사용) 탐지
   - Redis 캐시 활용도 분석

3. **인덱스 분석**
   - 검색 조건 필드의 인덱스 존재 여부
   - 복합 인덱스 최적화 제안

### 프론트엔드 성능 분석

1. **번들 사이즈**
   - `npm run build` 후 dist/ 파일 크기 분석
   - vendor/antd/echarts 청크 분리 확인
   - Tree-shaking 효과 분석 (echartsSetup.ts)
   - React.lazy 코드 분할 현황 (pageRegistry.ts)

2. **렌더링 성능**
   - 불필요한 리렌더링 패턴 탐지
   - useCallback/useMemo 적절 사용 여부
   - Context 분리 수준 (AuthContext, TabContext, ThemeContext)

3. **네트워크 성능**
   - API 호출 중복 탐지
   - WebSocket 메시지 빈도 최적화
   - 이미지/리소스 최적화

### 결과 리포트
- 심각도별 (Critical/Warning/Info) 이슈 분류
- 개선 전후 예상 효과
- 우선순위별 최적화 제안

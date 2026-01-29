# BSW CRUD 모듈 일괄 생성 (Skill)

백엔드 CRUD API + 프론트엔드 MasterCrudPage 기반 관리 페이지를 일괄 생성합니다.

## 인자
$ARGUMENTS

## 워크플로우

### 사전 분석
1. 기존 CRUD 모듈 패턴 분석 (MasterCompanyPage, MasterVehiclePage 참조)
2. MasterCrudPage.tsx 컴포넌트 인터페이스 확인
3. useCrudState.ts + useApiCall.ts 훅 사용법 확인
4. pageRegistry.ts 등록 패턴 확인

### Stage 1: 백엔드 도메인 생성
- **Entity** (`backend/src/main/java/com/dongkuk/weighing/{domain}/domain/`)
  - `@Entity`, `@Table(name="snake_case_복수")`, BaseEntity 상속
  - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
  - 필드별 `@Column` 매핑 (nullable, length, precision)
- **Repository**: JpaRepository 상속, 검색 메서드
- **DTO**: CreateRequest(입력검증), UpdateRequest, Response(팩토리메서드), SearchCondition(페이징)
- **Service**: @Service, @Transactional, CRUD + 검색, BusinessException 예외처리
- **Controller**: @RestController, ApiResponse<T> 래핑, @PreAuthorize, Swagger @Operation

### Stage 2: 프론트엔드 페이지 생성
- **타입 정의** (`frontend/src/types/index.ts`)
  - 도메인 interface (camelCase 필드)
  - Request/Response 타입
- **API 함수** (`frontend/src/api/`)
  - Axios CRUD 함수 (GET/POST/PUT/DELETE)
  - 경로: `/api/v1/{kebab-case-복수형}`
- **페이지 컴포넌트** (`frontend/src/pages/master/Master{Name}Page.tsx`)
  - MasterCrudPage 컴포넌트 활용
  - Ant Design Table columns 정의
  - Ant Design Form fields 정의
  - 검색 조건 필터
- **레지스트리 등록** (`frontend/src/config/pageRegistry.ts`)
  - path, label(한국어), icon, roles, component(React.lazy)
- **레이블/검증** (`constants/labels.ts`, `utils/validators.ts`)

### Stage 3: 통합 검증
- 백엔드 DTO ↔ 프론트엔드 타입 필드 매핑 일치
- snake_case ↔ camelCase 변환 확인
- 권한 설정 일치 (@PreAuthorize ↔ pageRegistry roles)
- TypeScript 빌드 (`npm run build`) 에러 없음 확인

### 품질 게이트
- [ ] Entity 필드와 DTO 필드 매핑 완전성
- [ ] Controller 모든 엔드포인트 ApiResponse 래핑
- [ ] 프론트엔드 타입 strict 모드 통과
- [ ] pageRegistry에 라우트 등록 완료
- [ ] 한국어 레이블 추가 완료

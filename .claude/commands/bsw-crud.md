# BSW CRUD 풀세트 생성

백엔드 + 프론트엔드 CRUD 모듈을 일괄 생성합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 인자 파싱
- 첫 번째 인자: 도메인명 (예: `Product`, `Warehouse`)
- `--fields "필드1:타입, 필드2:타입"`: 엔티티 필드 정의
- `--roles [ADMIN|MANAGER|ALL]`: 접근 권한

### 생성 순서

1. **백엔드 생성** (bsw-api 커맨드 로직 적용)
   - Entity + Repository (`domain/`)
   - DTO: CreateRequest, UpdateRequest, Response, SearchCondition (`dto/`)
   - Service: CRUD + 검색/페이징 (`service/`)
   - Controller: REST API + ApiResponse 래핑 (`controller/`)

2. **프론트엔드 생성** (bsw-page 커맨드 로직 적용)
   - 타입 정의 (`types/index.ts`)
   - API 함수 (`api/client.ts`)
   - MasterCrudPage 기반 페이지 컴포넌트 (`pages/master/`)
   - pageRegistry.ts 등록
   - labels.ts, validators.ts 업데이트

3. **통합 검증**
   - 백엔드 DTO 필드 ↔ 프론트엔드 타입 일치 확인
   - snake_case (API) ↔ camelCase (프론트) 매핑 확인
   - 권한 설정 일치 확인 (@PreAuthorize ↔ pageRegistry roles)

4. **테스트 스캐폴딩**
   - 백엔드: Controller 테스트 + Service 테스트 템플릿
   - 프론트: 타입 검증 (npm run build)

### 생성되는 파일 목록
```
backend/src/main/java/com/dongkuk/weighing/{도메인}/
├── controller/{도메인}Controller.java
├── service/{도메인}Service.java
├── domain/{엔티티}.java
├── domain/{엔티티}Repository.java
└── dto/{도메인}CreateRequest.java, {도메인}UpdateRequest.java, {도메인}Response.java, {도메인}SearchCondition.java

frontend/src/
├── pages/master/Master{PageName}Page.tsx
├── types/index.ts (추가)
└── api/client.ts (추가)

+ pageRegistry.ts, labels.ts, validators.ts 업데이트
```

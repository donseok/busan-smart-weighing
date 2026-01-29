# BSW 풀스택 API 생성 (Skill)

백엔드 API + 프론트엔드 타입/API함수 + 모바일 모델을 일괄 생성하는 종합 워크플로우입니다.

## 인자
$ARGUMENTS

## 실행 지침

### 인자 파싱
- 첫 번째 인자: 도메인명 (예: `Report`, `Inventory`)
- `--fields "name:String, quantity:Integer"`: 필드 정의
- `--roles [ADMIN|MANAGER|DRIVER|ALL]`: 접근 권한
- `--platforms [backend|frontend|mobile|all]`: 생성 대상 (기본: all)

### 워크플로우 (7단계)

#### Stage 1: 도메인 분석
- 기존 패키지 구조 확인 (`com.dongkuk.weighing/`)
- 유사 도메인 패턴 참조 (dispatch, weighing 등)
- 네이밍 규칙 결정 (Entity, Table, API Path)

#### Stage 2: Entity + Repository (백엔드)
- `{Domain}.java`: JPA Entity, BaseEntity 상속, snake_case 테이블
- `{Domain}Repository.java`: JpaRepository 상속, 검색 메서드
- `{Domain}Status.java`: 상태 Enum (해당 시)

#### Stage 3: DTO (백엔드)
- `{Domain}CreateRequest.java`: @Valid 입력 검증
- `{Domain}UpdateRequest.java`: 수정 필드
- `{Domain}Response.java`: Entity→DTO 변환
- `{Domain}SearchCondition.java`: 페이징/필터

#### Stage 4: Service + Controller (백엔드)
- `{Domain}Service.java`: @Service, @Transactional, CRUD + 검색
- `{Domain}Controller.java`: @RestController, ApiResponse<T>, @PreAuthorize

#### Stage 5: TypeScript 타입 + API 함수 (프론트엔드)
- `types/index.ts`: interface 추가 (camelCase 필드)
- `api/client.ts` 또는 별도 파일: Axios CRUD 함수
- 자동 camelCase↔snake_case 변환 (Axios 인터셉터)

#### Stage 6: Dart 모델 (모바일)
- `models/{domain}.dart`: fromJson/toJson, camelCase 필드
- `services/{domain}_service.dart`: Dio API 호출

#### Stage 7: 통합 검증
- 4개 플랫폼 필드 매핑 일관성 확인
- TypeScript 빌드 검증 (`npm run build`)
- 권한 설정 일관성 (백엔드 @PreAuthorize ↔ 프론트 pageRegistry)

### 산출물 요약
```
backend/src/main/java/com/dongkuk/weighing/{domain}/
├── controller/{Domain}Controller.java
├── service/{Domain}Service.java
├── domain/{Domain}.java, {Domain}Repository.java
└── dto/{Domain}CreateRequest.java, {Domain}UpdateRequest.java, {Domain}Response.java, {Domain}SearchCondition.java

frontend/src/types/index.ts (인터페이스 추가)
frontend/src/api/{domain}Api.ts (API 함수)

mobile/lib/models/{domain}.dart (모델)
mobile/lib/services/{domain}_service.dart (서비스)
```

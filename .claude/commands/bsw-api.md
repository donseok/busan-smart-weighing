# BSW API 엔드포인트 생성

새 API 엔드포인트의 백엔드 코드를 생성합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 인자 파싱
- 첫 번째 인자: 도메인명 (예: `notification`, `report`)
- `--methods [GET|POST|PUT|DELETE|CRUD]`: HTTP 메서드 (기본: CRUD)
- `--path`: API 경로 (기본: `/api/v1/{도메인-복수형}`)

### 생성 순서

1. **패키지 구조 생성** (`backend/src/main/java/com/dongkuk/weighing/{도메인}/`)
   ```
   {도메인}/
   ├── controller/    {도메인}Controller.java
   ├── service/       {도메인}Service.java
   ├── domain/        {엔티티}.java, {엔티티}Repository.java
   └── dto/           {도메인}Request.java, {도메인}Response.java
   ```

2. **Entity 생성** (`domain/`)
   - `@Entity`, `@Table(name = "snake_case_복수형")`
   - `BaseEntity` 상속 (createdAt, updatedAt 자동 관리)
   - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
   - 필드: `@Column(nullable = ...)` + 적절한 타입

3. **Repository 생성** (`domain/`)
   - `JpaRepository<{Entity}, Long>` 상속
   - 검색 조건에 따른 커스텀 쿼리 메서드

4. **DTO 생성** (`dto/`)
   - `{도메인}CreateRequest`, `{도메인}UpdateRequest`: 입력 DTO
   - `{도메인}Response`: 출력 DTO
   - `{도메인}SearchCondition`: 검색 조건 DTO (페이징, 필터)
   - `@NotBlank`, `@NotNull` 등 Jakarta Validation 적용

5. **Service 생성** (`service/`)
   - `@Service`, `@Transactional`
   - CRUD 메서드: create, update, delete, findById, findAll (페이징)
   - `BusinessException` + `ErrorCode` 사용한 예외 처리

6. **Controller 생성** (`controller/`)
   - `@RestController`, `@RequestMapping("/api/v1/{kebab-case-복수형}")`
   - 모든 응답 `ApiResponse<T>` 래핑
   - `@PreAuthorize` 권한 설정
   - Swagger `@Operation`, `@Tag` 어노테이션

7. **규칙 준수 확인**
   - JSON 필드: snake_case (Jackson 자동 변환)
   - 날짜 형식: ISO 8601
   - 에러 응답: ErrorCode enum 기반

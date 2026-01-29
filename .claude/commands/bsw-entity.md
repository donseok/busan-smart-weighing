# BSW JPA Entity 생성

JPA Entity + Repository + DTO를 생성하고 마이그레이션 SQL을 출력합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 인자 파싱
- 첫 번째 인자: 엔티티명 (PascalCase, 예: `ProductCategory`)
- `--fields "name:String, price:BigDecimal, active:Boolean"`: 필드 정의
- `--relations "ManyToOne:Company, OneToMany:Item"`: 관계 정의
- `--package`: 패키지명 (기본: 엔티티명 소문자)

### 생성 순서

1. **Entity 클래스** (`backend/src/main/java/com/dongkuk/weighing/{패키지}/domain/`)
   - `@Entity`, `@Table(name = "{snake_case_복수형}")`
   - `BaseEntity` 상속 (createdAt, updatedAt)
   - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
   - 필드 타입 매핑:
     - String → `@Column(length = 100)` (조절 가능)
     - BigDecimal → `@Column(precision = 15, scale = 2)`
     - Boolean → `@Column(nullable = false)` + 기본값
     - LocalDateTime → ISO 8601 포맷
     - Enum → `@Enumerated(EnumType.STRING)`
   - 관계 매핑:
     - `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn`
     - `@OneToMany(mappedBy = "...", cascade = CascadeType.ALL)`

2. **Repository** (`domain/`)
   - `JpaRepository<{Entity}, Long>` 상속
   - 주요 필드 기반 findBy 메서드

3. **DTO** (`dto/`)
   - CreateRequest: `@NotBlank`, `@NotNull` 검증
   - UpdateRequest: 수정 가능 필드만
   - Response: Entity → DTO 변환 (정적 팩토리 메서드)

4. **Enum 클래스** (상태 필드 존재 시)
   - 상태 Enum 별도 생성
   - `@Enumerated(EnumType.STRING)` 매핑

5. **마이그레이션 SQL 출력**
   - PostgreSQL 호환 CREATE TABLE
   - 인덱스 생성 (검색 대상 필드)
   - 외래 키 제약조건
   - 롤백 SQL (DROP TABLE)

### 네이밍 규칙
- 테이블: snake_case 복수형 (`product_categories`)
- 컬럼: snake_case (`display_name`)
- 인덱스: `idx_{table}_{column}` (`idx_product_categories_name`)
- 외래키: `fk_{table}_{ref_table}` (`fk_product_categories_company`)

# BSW DB 마이그레이션 생성 (Skill)

Entity 변경을 감지하고 PostgreSQL 마이그레이션 SQL을 자동 생성합니다.

## 인자
$ARGUMENTS

## 워크플로우

### 컨텍스트
- prod 환경: `ddl-auto=validate` → 수동 마이그레이션 필수
- dev 환경: `ddl-auto=update` → JPA 자동 반영
- DB: PostgreSQL (prod), H2 (dev/test)
- 네이밍: 테이블 snake_case 복수형, 컬럼 snake_case

### Stage 1: 변경 감지
- `git diff HEAD~1..HEAD` 또는 `git diff --staged`로 Entity 파일 변경 탐지
- 대상: `backend/src/main/java/com/dongkuk/weighing/**/domain/*.java`
- `@Entity` 어노테이션이 있는 클래스만 필터링

### Stage 2: 변경 분석
각 Entity 변경에 대해:
- **필드 추가**: 새 `@Column` 필드 → `ALTER TABLE ADD COLUMN`
- **필드 삭제**: 제거된 필드 → `ALTER TABLE DROP COLUMN`
- **필드 수정**: 타입/제약조건 변경 → `ALTER TABLE ALTER COLUMN`
- **관계 추가**: `@ManyToOne` 등 → `ALTER TABLE ADD COLUMN` + `ADD CONSTRAINT`
- **관계 삭제**: 관계 제거 → `ALTER TABLE DROP CONSTRAINT` + `DROP COLUMN`
- **인덱스 변경**: `@Index` 추가/삭제
- **새 Entity**: 전체 `CREATE TABLE`
- **Entity 삭제**: `DROP TABLE`

### Stage 3: SQL 생성

**업그레이드 SQL**:
```sql
-- Migration: V{YYYYMMDD}_{순번}__{설명}.sql
-- Generated: {날짜}
-- Entity: {엔티티명}
-- Changes: {변경 요약}

BEGIN;

ALTER TABLE {table_name}
  ADD COLUMN {column_name} {type} {constraints};

CREATE INDEX idx_{table}_{column} ON {table_name}({column_name});

COMMIT;
```

**롤백 SQL**:
```sql
-- Rollback: V{YYYYMMDD}_{순번}__rollback_{설명}.sql

BEGIN;

DROP INDEX IF EXISTS idx_{table}_{column};

ALTER TABLE {table_name}
  DROP COLUMN IF EXISTS {column_name};

COMMIT;
```

### Stage 4: H2 호환성 확인
- PostgreSQL 전용 구문 식별
- H2 대체 구문 제안 (dev 환경용)
- 호환 불가 항목 경고

### Stage 5: 검증
- 생성된 SQL 문법 검증
- 기존 데이터 영향도 분석
  - NOT NULL 컬럼 추가 시 DEFAULT 필요
  - 타입 변경 시 데이터 손실 가능성
  - 인덱스 생성 시 성능 영향
- 외래 키 참조 무결성 확인

### 산출물
- `docs/migrations/V{날짜}_{번호}__{설명}.sql`
- `docs/migrations/V{날짜}_{번호}__rollback_{설명}.sql`
- 변경 요약 리포트 (콘솔 출력)

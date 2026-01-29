# BSW DB 마이그레이션

Entity 변경에 따른 DB 마이그레이션 SQL을 생성합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 컨텍스트
- prod 환경: `ddl-auto=validate` → Entity 변경 시 수동 마이그레이션 필수
- dev 환경: `ddl-auto=update` → 자동 반영
- DB: PostgreSQL (prod), H2 (dev/test)

### 실행 순서

1. **변경 감지**
   - `git diff` 로 `backend/src/main/java/**/domain/` 하위 Entity 변경 확인
   - 변경된 Entity 클래스 목록 추출
   - `@Entity`, `@Table`, `@Column` 어노테이션 분석

2. **스키마 분석**
   - 변경 전 Entity 구조 파악 (git show)
   - 변경 후 Entity 구조 파악
   - 필드 추가/삭제/타입변경/제약조건 변경 식별

3. **마이그레이션 SQL 생성**
   - PostgreSQL 호환 DDL (prod)
   - 네이밍: snake_case (JPA 설정 기반)
   - `ALTER TABLE` / `CREATE TABLE` / `CREATE INDEX` 등
   - 외래 키, 유니크 제약조건 포함

4. **롤백 SQL 생성**
   - 역방향 마이그레이션
   - 데이터 손실 가능성 경고

5. **검증**
   - H2 호환성 확인 (dev 환경)
   - 기존 데이터 영향도 분석

6. **산출물**
   - `V{날짜}_{번호}__{설명}.sql` (업그레이드)
   - `V{날짜}_{번호}__rollback_{설명}.sql` (롤백)

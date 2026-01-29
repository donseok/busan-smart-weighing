# 부산 스마트 계량 시스템 - 신입 개발자 기술 가이드

> **대상**: 프로젝트에 새로 합류하는 신입/주니어 개발자
> **목적**: 프로젝트에 사용된 모든 기술 스택의 핵심 개념과 실무 패턴 이해

---

## 목차

1. [프로젝트 전체 구조](#1-프로젝트-전체-구조)
2. [데이터베이스 (PostgreSQL + Redis)](#2-데이터베이스-postgresql--redis)
3. [백엔드 WAS (Spring Boot)](#3-백엔드-was-spring-boot)
4. [프론트엔드 (React + TypeScript)](#4-프론트엔드-react--typescript)
5. [모바일 앱 (Flutter)](#5-모바일-앱-flutter)
6. [데스크톱 프로그램 (C# .NET WinForms)](#6-데스크톱-프로그램-c-net-winforms)
7. [인증과 보안 (JWT + Spring Security)](#7-인증과-보안-jwt--spring-security)
8. [실시간 통신 (WebSocket / STOMP)](#8-실시간-통신-websocket--stomp)
9. [빌드와 배포 (Vite, Gradle, Vercel, Railway)](#9-빌드와-배포-vite-gradle-vercel-railway)
10. [개발 환경 설정](#10-개발-환경-설정)
11. [코드 컨벤션과 패턴](#11-코드-컨벤션과-패턴)
12. [자주 하는 실수와 주의사항](#12-자주-하는-실수와-주의사항)

---

## 1. 프로젝트 전체 구조

### 1.1 시스템 아키텍처 개요

```
┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
│  React Web   │   │ Flutter App  │   │  C# WinForms     │
│  (Vercel)    │   │ (iOS/Android)│   │  (현장 PC)        │
└──────┬───────┘   └──────┬───────┘   └────────┬─────────┘
       │                  │                     │
       │         HTTPS / WebSocket              │
       └──────────────────┼─────────────────────┘
                          │
                ┌─────────▼─────────┐
                │   Spring Boot     │
                │   (Railway)       │
                │   REST API + WS   │
                └────┬─────────┬────┘
                     │         │
              ┌──────▼──┐  ┌──▼──────┐
              │PostgreSQL│  │  Redis  │
              │ (RDBMS)  │  │ (Cache) │
              └──────────┘  └─────────┘
```

### 1.2 모노레포 디렉토리 구조

```
busan-smart-weighing/
├── backend/                    # Spring Boot 백엔드
│   ├── src/main/java/com/dongkuk/weighing/
│   │   ├── auth/               # 인증 (JWT, 로그인)
│   │   ├── user/               # 사용자 관리
│   │   ├── master/             # 기준정보 (운송사, 차량, 계량대, 코드)
│   │   ├── dispatch/           # 배차 관리
│   │   ├── weighing/           # 계량 핵심 로직
│   │   ├── gatepass/           # 출문 관리
│   │   ├── slip/               # 전표 관리
│   │   ├── notification/       # 푸시 알림 (FCM)
│   │   ├── dashboard/          # 대시보드 통계
│   │   ├── audit/              # 감사 로그
│   │   └── global/             # 공통 설정, 예외처리, 유틸
│   └── src/main/resources/
│       ├── application.yml     # 공통 설정
│       ├── application-dev.yml # 개발 환경
│       └── application-prod.yml# 운영 환경
│
├── frontend/                   # React 웹 프론트엔드
│   ├── src/
│   │   ├── api/                # Axios 클라이언트
│   │   ├── components/         # 재사용 컴포넌트
│   │   ├── context/            # React Context (테마, 탭)
│   │   ├── hooks/              # 커스텀 Hook
│   │   ├── layouts/            # 레이아웃
│   │   ├── pages/              # 페이지 컴포넌트
│   │   ├── theme/              # 테마 설정
│   │   ├── types/              # TypeScript 타입 정의
│   │   └── utils/              # 유틸리티 함수
│   ├── package.json
│   └── vite.config.ts
│
├── mobile/                     # Flutter 모바일 앱
│   ├── lib/
│   │   ├── config/             # API 설정
│   │   ├── models/             # 데이터 모델
│   │   ├── providers/          # 상태 관리
│   │   ├── screens/            # 화면
│   │   ├── services/           # API/알림 서비스
│   │   └── widgets/            # 위젯
│   └── pubspec.yaml
│
└── WeighingCS/                 # C# 데스크톱 프로그램
    ├── Models/
    ├── Services/
    ├── Interfaces/
    └── MainForm.cs
```

### 1.3 주요 기술 버전 요약

| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 | 백엔드 런타임 |
| Spring Boot | 3.2.5 | 백엔드 프레임워크 |
| PostgreSQL | Latest | 관계형 데이터베이스 |
| Redis | Latest | 캐시, 토큰 관리 |
| React | 18.3.1 | 웹 프론트엔드 |
| TypeScript | 5.9.3 | 프론트엔드 타입 시스템 |
| Vite | 7.3.1 | 프론트엔드 빌드 도구 |
| Ant Design | 5.29.3 | UI 컴포넌트 라이브러리 |
| Flutter | 3.10.4+ | 모바일 앱 |
| .NET | 8.0 | 데스크톱 프로그램 |

---

## 2. 데이터베이스 (PostgreSQL + Redis)

### 2.1 PostgreSQL이란?

PostgreSQL(포스트그레스큐엘)은 오픈소스 관계형 데이터베이스(RDBMS)다. MySQL과 비슷하지만, 고급 기능(JSONB, Window Functions, CTE 등)이 더 풍부하다.

### 2.2 꼭 알아야 할 SQL 개념

#### 기본 CRUD

```sql
-- Create: 데이터 삽입
INSERT INTO users (login_id, user_name, phone_number, user_role)
VALUES ('hong123', '홍길동', '010-1234-5678', 'MANAGER');

-- Read: 데이터 조회
SELECT * FROM users WHERE user_role = 'MANAGER';

-- 페이징 처리 (Spring Data JPA가 자동으로 생성하는 쿼리)
SELECT * FROM users
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;  -- 첫 번째 페이지, 20건씩

-- Update: 데이터 수정
UPDATE users SET phone_number = '010-9999-8888' WHERE user_id = 1;

-- Delete: 데이터 삭제
DELETE FROM users WHERE user_id = 1;
```

#### JOIN - 테이블 간 관계 연결

```sql
-- 배차와 차량 정보를 함께 조회
-- INNER JOIN: 양쪽 테이블에 모두 데이터가 있는 경우만 반환
SELECT d.dispatch_id, d.item_name, v.plate_number, v.driver_name
FROM dispatches d
INNER JOIN vehicles v ON d.vehicle_id = v.vehicle_id;

-- LEFT JOIN: 왼쪽 테이블 기준, 오른쪽에 데이터 없으면 NULL
SELECT d.dispatch_id, d.item_name, g.pass_status
FROM dispatches d
LEFT JOIN gate_passes g ON d.dispatch_id = g.dispatch_id;
-- 출문 기록이 없는 배차도 포함됨 (g.pass_status = NULL)
```

#### 인덱스(Index) - 검색 속도 향상

```sql
-- 인덱스는 "책의 목차"와 같다
-- 인덱스 없이 WHERE 조건을 걸면 모든 행을 하나씩 확인 (Full Scan)
-- 인덱스가 있으면 해당 값을 빠르게 찾을 수 있다

CREATE INDEX idx_dispatch_date ON dispatches(dispatch_date);
CREATE INDEX idx_vehicle_plate ON vehicles(plate_number);

-- 복합 인덱스: 여러 컬럼을 조합
CREATE INDEX idx_dispatch_search ON dispatches(dispatch_status, dispatch_date);
```

**인덱스 주의사항:**
- INSERT/UPDATE/DELETE 시 인덱스도 함께 갱신되므로 쓰기 성능은 낮아진다
- WHERE, ORDER BY, JOIN에 자주 쓰이는 컬럼에 생성한다
- 데이터가 적은 테이블에는 불필요하다

#### 트랜잭션(Transaction)

```sql
-- 트랜잭션: "전부 성공하거나 전부 실패하거나"
-- 예: 계량 완료 시 기록 저장 + 상태 변경을 한 묶음으로 처리

BEGIN;  -- 트랜잭션 시작

UPDATE weighing_records SET weighing_status = 'COMPLETED'
WHERE weighing_id = 100;

INSERT INTO weighing_slips (weighing_id, slip_number, issued_at)
VALUES (100, 'SLP-2026-001', NOW());

COMMIT;  -- 두 쿼리 모두 성공 → 확정

-- 만약 중간에 오류 발생 시
ROLLBACK;  -- 두 쿼리 모두 취소, 이전 상태로 복원
```

**Spring에서 트랜잭션 사용 (코드에서 직접 SQL을 작성하지 않는다):**

```java
@Service
public class WeighingService {

    @Transactional  // 이 메서드 전체가 하나의 트랜잭션
    public void completeWeighing(Long weighingId) {
        WeighingRecord record = weighingRepository.findById(weighingId)
            .orElseThrow();
        record.complete();                    // 상태 변경
        weighingRepository.save(record);      // DB 반영

        WeighingSlip slip = WeighingSlip.create(record);
        slipRepository.save(slip);            // 전표 생성

        // 메서드가 정상 종료되면 COMMIT
        // 예외가 발생하면 자동 ROLLBACK
    }
}
```

### 2.3 JPA와 Entity

이 프로젝트에서는 SQL을 직접 작성하지 않고, JPA(Java Persistence API)를 사용한다. Java 객체와 데이터베이스 테이블을 매핑하는 ORM(Object-Relational Mapping) 기술이다.

```java
// Entity: 데이터베이스 테이블과 1:1 매핑되는 Java 클래스

@Entity                      // 이 클래스는 DB 테이블이다
@Table(name = "vehicles")    // 테이블 이름: vehicles
public class Vehicle extends BaseEntity {  // BaseEntity: createdAt, updatedAt 자동 관리

    @Id                      // 이 필드가 Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가 (1, 2, 3...)
    private Long vehicleId;

    @Column(nullable = false, length = 20)  // NOT NULL, 최대 20자
    private String plateNumber;

    @Column(length = 20)     // NULL 허용, 최대 20자
    private String vehicleType;

    @ManyToOne(fetch = FetchType.LAZY)      // N:1 관계, 지연 로딩
    @JoinColumn(name = "company_id")        // FK 컬럼명
    private Company company;

    private BigDecimal defaultTareWeight;
    private BigDecimal maxLoadWeight;
}
```

**핵심 개념:**
- `@Entity` → 이 클래스 = DB 테이블
- `@Id` → Primary Key (고유 식별자)
- `@Column` → 테이블 컬럼 설정 (길이, NULL 허용 등)
- `@ManyToOne` → 다대일 관계 (차량 N : 운송사 1)
- `FetchType.LAZY` → 실제로 접근할 때만 DB에서 조회 (성능 최적화)

### 2.4 Repository - 데이터 접근 계층

```java
// Spring Data JPA: 인터페이스만 선언하면 구현체를 자동 생성
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // 메서드 이름만으로 쿼리 자동 생성!
    // SELECT * FROM vehicles WHERE plate_number = ?
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    // SELECT * FROM vehicles WHERE company_id = ? ORDER BY plate_number ASC
    List<Vehicle> findByCompanyIdOrderByPlateNumberAsc(Long companyId);

    // 복잡한 쿼리는 @Query로 직접 작성
    @Query("SELECT v FROM Vehicle v WHERE v.plateNumber LIKE %:keyword%")
    Page<Vehicle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
```

**메서드 이름 규칙:**
| 키워드 | 의미 | 예시 |
|--------|------|------|
| `findBy` | SELECT WHERE | `findByUserName(name)` |
| `And` | AND 조건 | `findByRoleAndActive(role, active)` |
| `Or` | OR 조건 | `findByNameOrEmail(name, email)` |
| `OrderBy` | 정렬 | `findByRoleOrderByNameAsc(role)` |
| `Between` | 범위 | `findByDateBetween(start, end)` |
| `Like` | 부분 일치 | `findByNameLike(pattern)` |
| `IsNull` | NULL 확인 | `findByDeletedAtIsNull()` |

### 2.5 Redis - 인메모리 캐시

Redis는 메모리 기반의 Key-Value 저장소다. 디스크가 아닌 메모리에 데이터를 저장하므로 극히 빠르다.

**이 프로젝트에서의 Redis 용도:**

```
1. 토큰 블랙리스트: 로그아웃된 JWT 토큰 저장
   KEY: "blacklist:eyJhbGciOi..."  VALUE: "true"  TTL: 30분

2. OTP 코드 저장: 일회용 인증 코드
   KEY: "otp:hong123"  VALUE: "482917"  TTL: 5분

3. Rate Limiting: API 호출 횟수 제한
   KEY: "rate:192.168.1.1"  VALUE: "45"  TTL: 1분
```

**핵심 Redis 명령어 (디버깅 시 필요):**

```bash
# Redis CLI 접속
redis-cli -h localhost -p 6370

# 키 조회
GET "blacklist:token-value-here"

# 키 목록 확인 (주의: 운영에서는 KEYS * 사용 금지)
KEYS "otp:*"

# TTL 확인 (남은 만료 시간, 초 단위)
TTL "otp:hong123"

# 키 삭제
DEL "blacklist:token-value-here"
```

### 2.6 데이터베이스 환경별 설정

| 환경 | DB | DDL 전략 | 용도 |
|------|-----|---------|------|
| **dev** | H2 (인메모리) | `create` | 로컬 개발 (앱 시작 시 테이블 자동 생성) |
| **test** | H2 (인메모리) | `create-drop` | 테스트 (테스트 끝나면 삭제) |
| **prod** | PostgreSQL | `validate` | 운영 (테이블 구조 검증만, 변경 안 함) |

**DDL 전략 설명:**
- `create`: 앱 시작 시 기존 테이블 DROP 후 새로 CREATE (데이터 유실)
- `create-drop`: create + 앱 종료 시 DROP
- `update`: 변경된 Entity만 ALTER TABLE (위험할 수 있음)
- `validate`: DB 스키마와 Entity가 일치하는지 검증만 (운영에 적합)
- `none`: 아무것도 안 함

---

## 3. 백엔드 WAS (Spring Boot)

### 3.1 Spring Boot란?

Spring Boot는 Java 기반의 웹 애플리케이션 프레임워크다. WAS(Web Application Server)로서 Tomcat이 내장되어 있어, JAR 파일 하나로 서버를 실행할 수 있다.

### 3.2 핵심 아키텍처: 계층형 구조

```
HTTP 요청 → Controller → Service → Repository → DB
HTTP 응답 ← Controller ← Service ← Repository ← DB
```

```
┌─────────────────────────────────────────────────┐
│  Controller (표현 계층)                           │
│  - HTTP 요청/응답 처리                            │
│  - 요청 데이터 검증 (Validation)                   │
│  - 응답 형식 변환                                 │
├─────────────────────────────────────────────────┤
│  Service (비즈니스 계층)                           │
│  - 핵심 비즈니스 로직                              │
│  - 트랜잭션 관리 (@Transactional)                 │
│  - 여러 Repository 조합                           │
├─────────────────────────────────────────────────┤
│  Repository (데이터 접근 계층)                     │
│  - DB CRUD 실행                                  │
│  - Spring Data JPA 인터페이스                     │
├─────────────────────────────────────────────────┤
│  Domain/Entity (도메인 계층)                      │
│  - DB 테이블과 매핑되는 객체                        │
│  - 도메인 규칙 포함                               │
└─────────────────────────────────────────────────┘
```

### 3.3 Controller - API 엔드포인트 정의

```java
@RestController                    // JSON 응답을 반환하는 컨트롤러
@RequestMapping("/api/v1/dispatches")  // 기본 URL 경로
@RequiredArgsConstructor           // final 필드에 대한 생성자 자동 생성 (Lombok)
public class DispatchController {

    private final DispatchService dispatchService;  // DI (의존성 주입)

    // GET /api/v1/dispatches?page=0&size=20&startDate=2026-01-01
    @GetMapping
    public ApiResponse<Page<DispatchResponse>> getDispatches(
            DispatchSearchCondition condition,  // 쿼리 파라미터 자동 매핑
            Pageable pageable                   // 페이징 정보 자동 매핑
    ) {
        return ApiResponse.success(dispatchService.search(condition, pageable));
    }

    // POST /api/v1/dispatches (body: JSON)
    @PostMapping
    public ApiResponse<DispatchResponse> create(
            @Valid @RequestBody DispatchCreateRequest request  // JSON → 객체 변환 + 검증
    ) {
        return ApiResponse.success(dispatchService.create(request));
    }

    // PUT /api/v1/dispatches/123
    @PutMapping("/{id}")
    public ApiResponse<DispatchResponse> update(
            @PathVariable Long id,                              // URL에서 ID 추출
            @Valid @RequestBody DispatchUpdateRequest request
    ) {
        return ApiResponse.success(dispatchService.update(id, request));
    }

    // DELETE /api/v1/dispatches/123
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dispatchService.delete(id);
        return ApiResponse.success(null);
    }
}
```

**핵심 어노테이션 정리:**
| 어노테이션 | 의미 |
|-----------|------|
| `@RestController` | REST API 컨트롤러 (JSON 응답) |
| `@RequestMapping` | URL 경로 매핑 |
| `@GetMapping` | HTTP GET 요청 처리 |
| `@PostMapping` | HTTP POST 요청 처리 |
| `@PutMapping` | HTTP PUT 요청 처리 |
| `@DeleteMapping` | HTTP DELETE 요청 처리 |
| `@PathVariable` | URL 경로에서 변수 추출 (`/users/{id}`) |
| `@RequestBody` | HTTP Body의 JSON을 객체로 변환 |
| `@Valid` | Bean Validation 실행 |
| `@RequestParam` | 쿼리 파라미터 추출 (`?name=홍길동`) |

### 3.4 DTO - 데이터 전송 객체

DTO(Data Transfer Object)는 API 요청/응답에 사용되는 데이터 구조체다. Entity를 직접 노출하지 않고, 필요한 데이터만 전달한다.

```java
// 요청 DTO: 클라이언트 → 서버
public record DispatchCreateRequest(
    @NotNull(message = "차량 ID는 필수입니다")
    Long vehicleId,

    @NotNull(message = "운송사 ID는 필수입니다")
    Long companyId,

    @NotBlank(message = "품목유형은 필수입니다")
    String itemType,

    @NotBlank(message = "품목명은 필수입니다")
    @Size(max = 100, message = "품목명은 100자 이하여야 합니다")
    String itemName,

    @NotNull(message = "배차일은 필수입니다")
    LocalDate dispatchDate,

    @Size(max = 100)
    String originLocation,

    @Size(max = 100)
    String destination
) {}

// 응답 DTO: 서버 → 클라이언트
public record DispatchResponse(
    Long dispatchId,
    Long vehicleId,
    String plateNumber,     // Entity에서 가져온 연관 데이터
    String itemType,
    String itemName,
    LocalDate dispatchDate,
    String dispatchStatus,
    LocalDateTime createdAt
) {
    // Entity → DTO 변환 팩토리 메서드
    public static DispatchResponse from(Dispatch dispatch) {
        return new DispatchResponse(
            dispatch.getDispatchId(),
            dispatch.getVehicle().getVehicleId(),
            dispatch.getVehicle().getPlateNumber(),
            dispatch.getItemType().name(),
            dispatch.getItemName(),
            dispatch.getDispatchDate(),
            dispatch.getDispatchStatus().name(),
            dispatch.getCreatedAt()
        );
    }
}
```

**왜 Entity를 직접 반환하지 않는가?**
1. **보안**: 비밀번호, 내부 ID 등 민감 정보가 노출될 수 있다
2. **유연성**: API 스펙과 DB 스키마를 독립적으로 변경할 수 있다
3. **순환 참조**: Entity 간 양방향 관계가 있으면 JSON 직렬화 시 무한 루프 발생

### 3.5 Bean Validation - 요청 데이터 검증

```java
// 자주 사용하는 검증 어노테이션
@NotNull                          // null 불가
@NotBlank                         // null, 빈 문자열, 공백만 불가
@NotEmpty                         // null, 빈 문자열 불가 (공백은 허용)
@Size(min = 3, max = 50)         // 문자열 길이 제한
@Min(0)                          // 최소값
@Max(100)                        // 최대값
@Email                           // 이메일 형식
@Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$")  // 정규식 매칭
@Past                            // 과거 날짜만
@Future                          // 미래 날짜만
```

### 3.6 Service - 비즈니스 로직

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // 기본값: 읽기 전용 (SELECT 최적화)
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final VehicleRepository vehicleRepository;

    // 조회: readOnly = true (기본값)
    public Page<DispatchResponse> search(
            DispatchSearchCondition condition, Pageable pageable) {
        return dispatchRepository.searchByCondition(condition, pageable)
                .map(DispatchResponse::from);
    }

    // 생성: @Transactional로 읽기전용 해제
    @Transactional   // readOnly = false → INSERT/UPDATE/DELETE 가능
    public DispatchResponse create(DispatchCreateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));

        Dispatch dispatch = Dispatch.builder()
                .vehicle(vehicle)
                .itemType(ItemType.valueOf(request.itemType()))
                .itemName(request.itemName())
                .dispatchDate(request.dispatchDate())
                .build();

        return DispatchResponse.from(dispatchRepository.save(dispatch));
    }
}
```

**`@Transactional(readOnly = true)` vs `@Transactional`:**
- `readOnly = true`: SELECT만 실행, Hibernate 변경 감지(Dirty Checking) 비활성화 → 성능 향상
- `readOnly = false` (기본): INSERT/UPDATE/DELETE 가능, 변경 감지 활성화

### 3.7 예외 처리 패턴

```java
// ErrorCode 열거형: 모든 에러 코드를 한 곳에서 관리
public enum ErrorCode {
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "차량을 찾을 수 없습니다"),
    DUPLICATE_PLATE_NUMBER(HttpStatus.CONFLICT, "이미 등록된 차량번호입니다"),
    INVALID_WEIGHING_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 계량 상태입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다");

    private final HttpStatus status;
    private final String message;
}

// BusinessException: 비즈니스 로직에서 발생하는 예외
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// GlobalExceptionHandler: 모든 예외를 잡아서 일관된 응답으로 변환
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ApiResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("VALIDATION_ERROR", message));
    }
}
```

### 3.8 API 응답 표준 형식

```java
// 모든 API 응답은 이 형식으로 통일
public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorInfo error
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message));
    }
}
```

```json
// 성공 응답 예시
{
  "success": true,
  "data": {
    "dispatch_id": 1,
    "item_name": "철근",
    "dispatch_status": "REGISTERED"
  },
  "error": null
}

// 에러 응답 예시
{
  "success": false,
  "data": null,
  "error": {
    "code": "VEHICLE_NOT_FOUND",
    "message": "차량을 찾을 수 없습니다"
  }
}
```

### 3.9 Lombok - 보일러플레이트 코드 제거

```java
// Lombok 없이 (장황한 코드)
public class Vehicle {
    private Long vehicleId;
    private String plateNumber;

    public Vehicle() {}

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long id) { this.vehicleId = id; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String pn) { this.plateNumber = pn; }
}

// Lombok 사용 (깔끔한 코드)
@Getter                        // 모든 필드에 getter 생성
@NoArgsConstructor             // 기본 생성자
@AllArgsConstructor            // 모든 필드를 받는 생성자
@Builder                       // 빌더 패턴 사용 가능
public class Vehicle {
    private Long vehicleId;
    private String plateNumber;
}

// 빌더 패턴 사용
Vehicle vehicle = Vehicle.builder()
    .vehicleId(1L)
    .plateNumber("12가3456")
    .build();
```

| 어노테이션 | 생성하는 코드 |
|-----------|-------------|
| `@Getter` | 모든 필드의 getter |
| `@Setter` | 모든 필드의 setter |
| `@NoArgsConstructor` | 파라미터 없는 생성자 |
| `@AllArgsConstructor` | 모든 필드를 파라미터로 받는 생성자 |
| `@RequiredArgsConstructor` | final 필드만 파라미터로 받는 생성자 |
| `@Builder` | 빌더 패턴 |
| `@ToString` | toString() |
| `@EqualsAndHashCode` | equals(), hashCode() |
| `@Data` | Getter + Setter + ToString + EqualsAndHashCode + RequiredArgs |

### 3.10 의존성 주입 (Dependency Injection)

Spring의 가장 핵심적인 개념이다. 객체를 직접 생성하지 않고, Spring이 대신 생성하고 주입해준다.

```java
// ❌ 나쁜 예: 직접 생성
public class DispatchService {
    private DispatchRepository repo = new DispatchRepository();  // 직접 생성
}

// ✅ 좋은 예: Spring이 주입
@Service
@RequiredArgsConstructor
public class DispatchService {
    private final DispatchRepository repo;  // Spring이 자동으로 주입
}
```

**왜 DI를 사용하는가?**
- 테스트 시 Mock 객체로 교체가 쉽다
- 구현체를 바꿔도 코드를 수정할 필요가 없다
- 객체의 생명주기를 Spring이 관리한다

---

## 4. 프론트엔드 (React + TypeScript)

### 4.1 React란?

React는 사용자 인터페이스(UI)를 구축하기 위한 JavaScript 라이브러리다. **컴포넌트** 기반으로, UI를 작은 조각들로 나누어 조합한다.

### 4.2 TypeScript 기본

TypeScript는 JavaScript에 **타입 시스템**을 추가한 언어다. 코드 작성 시점에 오류를 발견할 수 있다.

```typescript
// JavaScript (타입 없음, 런타임에 오류 발견)
function addUser(user) {
    console.log(user.name);  // user에 name이 있는지 알 수 없음
}

// TypeScript (타입 있음, 코드 작성 시 오류 발견)
interface User {
    userId: number;
    loginId: string;
    userName: string;
    phoneNumber: string;
    userRole: 'ADMIN' | 'MANAGER' | 'DRIVER';  // Union 타입: 이 3개 값만 가능
    isActive: boolean;
}

function addUser(user: User): void {
    console.log(user.userName);  // IDE에서 자동완성 지원
    console.log(user.address);   // ❌ 컴파일 에러! address는 User에 없음
}
```

**자주 사용하는 타입:**

```typescript
// 기본 타입
let name: string = '홍길동';
let age: number = 25;
let isActive: boolean = true;
let data: null = null;
let value: undefined = undefined;

// 배열
let ids: number[] = [1, 2, 3];
let names: string[] = ['홍길동', '김철수'];

// 객체 (interface)
interface Vehicle {
    vehicleId: number;
    plateNumber: string;
    maxLoadWeight?: number;  // ? = 선택적 필드 (없을 수 있음)
}

// 제네릭: 타입을 변수처럼 사용
interface ApiResponse<T> {
    success: boolean;
    data: T;
    error: { code: string; message: string } | null;
}

// 사용
const response: ApiResponse<User[]> = await api.get('/users');
//   response.data = User[] 타입임을 컴파일러가 알고 있다
```

### 4.3 React 함수형 컴포넌트

```tsx
// React.FC: 함수형 컴포넌트 타입
const UserCard: React.FC<{ user: User }> = ({ user }) => {
    return (
        <div>
            <h3>{user.userName}</h3>
            <p>{user.phoneNumber}</p>
        </div>
    );
};

// 사용
<UserCard user={userData} />
```

### 4.4 React Hooks - 핵심 개념

#### useState - 상태 관리

```tsx
const [count, setCount] = useState(0);
//     값     값 변경 함수    초기값

// 값 변경 → 컴포넌트 리렌더링
setCount(5);           // count가 5로 변경, 화면 갱신
setCount(prev => prev + 1);  // 이전 값 기반으로 변경 (안전한 방식)
```

**프로젝트 실제 예시 (DispatchPage.tsx):**

```tsx
const DispatchPage: React.FC = () => {
    // 데이터 상태
    const [data, setData] = useState<Dispatch[]>([]);       // 배차 목록
    const [loading, setLoading] = useState(false);           // 로딩 상태
    const [searched, setSearched] = useState(false);         // 검색 여부
    const [totalElements, setTotalElements] = useState(0);   // 전체 건수
    const [currentPage, setCurrentPage] = useState(1);       // 현재 페이지

    // 모달 상태
    const [createModalOpen, setCreateModalOpen] = useState(false);
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [editingRecord, setEditingRecord] = useState<Dispatch | null>(null);

    // ...
};
```

#### useEffect - 사이드 이펙트 처리

```tsx
// 컴포넌트가 화면에 나타날 때 (마운트) 실행
useEffect(() => {
    fetchData();           // API 호출
}, []);                    // 빈 배열: 최초 1번만 실행

// 특정 값이 변경될 때 실행
useEffect(() => {
    fetchData(searchKeyword);  // searchKeyword가 바뀔 때마다 실행
}, [searchKeyword]);           // 의존성 배열

// 정리(cleanup) 함수: 컴포넌트가 사라질 때 실행
useEffect(() => {
    const ws = new WebSocket('ws://...');
    ws.onmessage = handleMessage;

    return () => {
        ws.close();  // 컴포넌트 언마운트 시 WebSocket 연결 해제
    };
}, []);
```

#### useCallback - 함수 메모이제이션

```tsx
// 문제: 컴포넌트가 리렌더링될 때마다 fetchData 함수가 새로 생성됨
// → useEffect의 의존성 배열에 넣으면 무한 루프 발생 가능

// 해결: useCallback으로 함수를 메모이제이션
const fetchData = useCallback(async (keyword?: string) => {
    setLoading(true);
    try {
        const res = await apiClient.get('/master/companies', { params });
        setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
}, []);  // 의존성 배열이 비어있으므로, 함수가 한 번만 생성됨

// 이제 useEffect에서 안전하게 사용 가능
useEffect(() => { fetchData(); }, [fetchData]);
```

#### Form.useForm - Ant Design 폼 관리

```tsx
const [form] = Form.useForm();   // 폼 인스턴스 생성

// 폼 값 설정 (수정 모달 열 때)
form.setFieldsValue({
    plateNumber: record.plateNumber,
    vehicleType: record.vehicleType,
});

// 폼 검증 후 값 가져오기
const values = await form.validateFields();  // 검증 실패 시 예외 발생

// 폼 초기화
form.resetFields();
```

### 4.5 Ant Design - UI 컴포넌트

이 프로젝트에서 사용하는 주요 Ant Design 컴포넌트:

```tsx
import {
    Button,       // 버튼
    Input,        // 텍스트 입력
    Select,       // 드롭다운 선택
    DatePicker,   // 날짜 선택기
    Form,         // 폼 (검증 포함)
    Table,        // 데이터 테이블
    Modal,        // 모달 대화상자
    message,      // 알림 메시지 (토스트)
    Tag,          // 태그/라벨
    Space,        // 간격 컴포넌트
    Popconfirm,   // 확인 팝업
    Card,         // 카드 레이아웃
    Typography,   // 텍스트 스타일링
    Pagination,   // 페이지네이션
    Switch,       // 토글 스위치
    Tabs,         // 탭 메뉴
} from 'antd';
```

**Form + Validation 예시:**

```tsx
<Form form={form} layout="vertical" onFinish={handleSubmit}>
    <Form.Item
        name="plateNumber"
        label="차량번호"
        rules={[
            { required: true, message: '차량번호를 입력하세요' },
            { max: 20, message: '20자 이하로 입력하세요' },
            { pattern: /^[가-힣]{0,2}\d{2,3}[가-힣]\d{4}$/, message: '올바른 형식이 아닙니다' },
        ]}
    >
        <Input />
    </Form.Item>

    {/* 교차 검증: maxLoadWeight > defaultTareWeight */}
    <Form.Item
        name="maxLoadWeight"
        label="최대 적재중량"
        dependencies={['defaultTareWeight']}  // 이 필드가 바뀌면 재검증
        rules={[
            ({ getFieldValue }) => ({
                validator(_, value) {
                    const tare = getFieldValue('defaultTareWeight');
                    if (value && tare && value <= tare) {
                        return Promise.reject('공차중량보다 커야 합니다');
                    }
                    return Promise.resolve();
                },
            }),
        ]}
    >
        <InputNumber style={{ width: '100%' }} />
    </Form.Item>
</Form>
```

### 4.6 Axios - HTTP 클라이언트

```typescript
// api/client.ts - Axios 인스턴스 설정
import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api/v1',
    timeout: 10000,
});

// 요청 인터셉터: 모든 요청에 JWT 토큰 자동 첨부
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 응답 인터셉터: 401 에러 시 토큰 갱신
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            // accessToken 만료 → refreshToken으로 갱신 시도
            const refreshToken = localStorage.getItem('refreshToken');
            const res = await axios.post('/api/v1/auth/refresh', { refreshToken });
            localStorage.setItem('accessToken', res.data.data.accessToken);

            // 원래 요청 재시도
            error.config.headers.Authorization = `Bearer ${res.data.data.accessToken}`;
            return apiClient(error.config);
        }
        return Promise.reject(error);
    }
);
```

**API 호출 패턴:**

```typescript
// GET 요청 (조회)
const res = await apiClient.get('/dispatches', {
    params: { page: 0, size: 20, startDate: '2026-01-01' }
});
const dispatches: Dispatch[] = res.data.data.content;

// POST 요청 (생성)
await apiClient.post('/dispatches', {
    vehicleId: 1,
    itemName: '철근',
    dispatchDate: '2026-01-29'
});

// PUT 요청 (수정)
await apiClient.put(`/dispatches/${id}`, updatedData);

// DELETE 요청 (삭제)
await apiClient.delete(`/dispatches/${id}`);
```

### 4.7 React Router - 클라이언트 라우팅

```tsx
// SPA(Single Page Application)에서 페이지 전환을 처리
// 실제로는 페이지를 새로 로드하지 않고, 컴포넌트만 교체

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

<BrowserRouter>
    <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<MainLayout />}>  {/* 레이아웃 공유 */}
            <Route index element={<DashboardPage />} />
            <Route path="dispatch" element={<DispatchPage />} />
            <Route path="weighing" element={<WeighingPage />} />
            <Route path="master/vehicles" element={<MasterVehiclePage />} />
            <Route path="admin/users" element={<AdminUserPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" />} />  {/* 404 → 홈 */}
    </Routes>
</BrowserRouter>
```

### 4.8 Context API - 전역 상태 관리

```tsx
// 테마 Context 예시
interface ThemeContextType {
    isDark: boolean;
    toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType>({
    isDark: false,
    toggleTheme: () => {},
});

// Provider: 최상위에서 상태를 제공
export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isDark, setIsDark] = useState(false);

    const toggleTheme = () => {
        setIsDark(prev => !prev);
        localStorage.setItem('theme', !isDark ? 'dark' : 'light');
    };

    return (
        <ThemeContext.Provider value={{ isDark, toggleTheme }}>
            {children}
        </ThemeContext.Provider>
    );
};

// 사용하는 컴포넌트에서:
const { isDark, toggleTheme } = useContext(ThemeContext);
```

### 4.9 JSON 네이밍 변환 (camelCase ↔ snake_case)

```
프론트엔드 (JavaScript):  camelCase   → plateNumber, dispatchDate
백엔드 (Java/JSON):       snake_case  → plate_number, dispatch_date
```

Axios 인터셉터에서 자동 변환한다:
- **요청 시**: camelCase → snake_case
- **응답 시**: snake_case → camelCase

이를 통해 프론트엔드와 백엔드 각각의 네이밍 컨벤션을 유지하면서 통신할 수 있다.

---

## 5. 모바일 앱 (Flutter)

### 5.1 Flutter란?

Google이 만든 크로스 플랫폼 UI 프레임워크다. **하나의 코드**로 iOS, Android, Web 앱을 만들 수 있다. Dart 언어를 사용한다.

### 5.2 Dart 기본 문법

```dart
// 변수 선언
String name = '홍길동';           // 타입 명시
var age = 25;                    // 타입 추론
final loginId = 'hong123';       // 런타임 상수 (한 번 할당 후 변경 불가)
const pi = 3.14;                 // 컴파일타임 상수

// Null Safety (Dart 3.x)
String? nullableName;            // ? = null 가능
String nonNullName = '홍길동';    // null 불가

nullableName?.length;            // ?. = null이면 호출 안 함
nullableName ?? '이름없음';       // ?? = null이면 기본값 사용
nullableName!;                   // ! = null이 아님을 단언 (위험)

// 비동기 프로그래밍
Future<List<Dispatch>> fetchDispatches() async {
    final response = await dio.get('/dispatches');
    return response.data.map((json) => Dispatch.fromJson(json)).toList();
}

// 클래스
class User {
    final int userId;
    final String userName;

    User({required this.userId, required this.userName});

    factory User.fromJson(Map<String, dynamic> json) {
        return User(
            userId: json['user_id'],
            userName: json['user_name'],
        );
    }
}
```

### 5.3 Widget - Flutter의 기본 단위

Flutter에서 화면의 모든 요소는 Widget이다. React의 Component와 비슷하다.

```dart
// StatelessWidget: 상태가 없는 위젯 (정적 UI)
class UserCard extends StatelessWidget {
    final User user;
    const UserCard({super.key, required this.user});

    @override
    Widget build(BuildContext context) {
        return Card(
            child: ListTile(
                title: Text(user.userName),
                subtitle: Text(user.phoneNumber),
                leading: const Icon(Icons.person),
            ),
        );
    }
}

// StatefulWidget: 상태가 있는 위젯 (동적 UI)
class DispatchListScreen extends StatefulWidget {
    const DispatchListScreen({super.key});

    @override
    State<DispatchListScreen> createState() => _DispatchListScreenState();
}

class _DispatchListScreenState extends State<DispatchListScreen> {
    List<Dispatch> _dispatches = [];
    bool _isLoading = false;

    @override
    void initState() {
        super.initState();
        _fetchData();        // 화면 초기화 시 데이터 로드
    }

    Future<void> _fetchData() async {
        setState(() => _isLoading = true);   // React의 setLoading(true) 와 동일
        // ... API 호출 ...
        setState(() => _isLoading = false);  // 화면 갱신
    }

    @override
    Widget build(BuildContext context) {
        if (_isLoading) return const CircularProgressIndicator();

        return ListView.builder(
            itemCount: _dispatches.length,
            itemBuilder: (context, index) => DispatchCard(dispatch: _dispatches[index]),
        );
    }
}
```

### 5.4 Provider - 상태 관리

Provider는 Flutter의 경량 상태 관리 패턴이다. React Context와 유사하다.

```dart
// 상태 클래스 (ChangeNotifier 상속)
class AuthProvider extends ChangeNotifier {
    User? _currentUser;
    bool _isAuthenticated = false;

    User? get currentUser => _currentUser;
    bool get isAuthenticated => _isAuthenticated;

    Future<void> login(String loginId, String password) async {
        final response = await apiService.login(loginId, password);
        _currentUser = response.user;
        _isAuthenticated = true;
        notifyListeners();  // React의 setState() 와 동일 → UI 갱신
    }

    void logout() {
        _currentUser = null;
        _isAuthenticated = false;
        notifyListeners();
    }
}

// 앱 최상위에서 Provider 등록
void main() {
    runApp(
        MultiProvider(
            providers: [
                ChangeNotifierProvider(create: (_) => AuthProvider()),
                ChangeNotifierProvider(create: (_) => DispatchProvider()),
            ],
            child: const MyApp(),
        ),
    );
}

// 위젯에서 상태 사용
class ProfileScreen extends StatelessWidget {
    @override
    Widget build(BuildContext context) {
        // Provider에서 상태 읽기
        final auth = Provider.of<AuthProvider>(context);
        // 또는
        final auth = context.watch<AuthProvider>();  // 변경 시 리빌드
        final auth = context.read<AuthProvider>();   // 변경 시 리빌드 안 함

        return Text('안녕하세요, ${auth.currentUser?.userName}');
    }
}
```

### 5.5 Go Router - 네비게이션

```dart
final router = GoRouter(
    initialLocation: '/login',
    routes: [
        GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
        GoRoute(path: '/home', builder: (_, __) => const HomeScreen()),
        GoRoute(path: '/dispatch/:id', builder: (_, state) {
            final id = state.pathParameters['id']!;
            return DispatchDetailScreen(dispatchId: int.parse(id));
        }),
    ],
    redirect: (context, state) {
        final isLoggedIn = context.read<AuthProvider>().isAuthenticated;
        if (!isLoggedIn && state.matchedLocation != '/login') {
            return '/login';  // 미인증 → 로그인 페이지로 리다이렉트
        }
        return null;  // 정상 진행
    },
);
```

### 5.6 Dio - HTTP 클라이언트

```dart
// Axios와 비슷한 HTTP 클라이언트
class ApiService {
    late final Dio _dio;

    ApiService() {
        _dio = Dio(BaseOptions(
            baseUrl: ApiConfig.baseUrl,     // 'http://..../api/v1'
            connectTimeout: const Duration(seconds: 10),
        ));

        // 인터셉터: 모든 요청에 토큰 자동 첨부
        _dio.interceptors.add(InterceptorsWrapper(
            onRequest: (options, handler) async {
                final token = await _secureStorage.read(key: 'accessToken');
                if (token != null) {
                    options.headers['Authorization'] = 'Bearer $token';
                }
                handler.next(options);
            },
        ));
    }

    Future<List<Dispatch>> getDispatches() async {
        final response = await _dio.get('/dispatches');
        return (response.data['data']['content'] as List)
            .map((json) => Dispatch.fromJson(json))
            .toList();
    }
}
```

### 5.7 React vs Flutter 비교

| 개념 | React | Flutter |
|------|-------|---------|
| 기본 단위 | Component | Widget |
| 상태 관리 | useState | setState / Provider |
| 생명주기 | useEffect | initState / dispose |
| 전역 상태 | Context API | Provider / Riverpod |
| 라우팅 | React Router | Go Router |
| HTTP | Axios | Dio |
| 스타일링 | CSS / Styled | Widget 속성 |
| 리스트 렌더링 | `.map()` | `ListView.builder()` |
| 조건부 렌더링 | `{condition && <Widget>}` | `if (condition) Widget()` |

---

## 6. 데스크톱 프로그램 (C# .NET WinForms)

### 6.1 역할

현장(계량소)에 설치된 Windows PC에서 실행되는 프로그램이다.

- 계량대(저울)와 **시리얼 포트(COM)** 통신으로 실시간 중량 데이터 수신
- 전광판에 계량 결과 표시
- 차단기 제어 (개방/폐쇄)
- 오프라인 시 SQLite에 데이터 캐싱

### 6.2 핵심 구성

```csharp
// 시리얼 포트 통신 (계량대 연결)
using System.IO.Ports;

var port = new SerialPort("COM1", 9600, Parity.None, 8, StopBits.One);
port.DataReceived += (sender, e) => {
    string data = port.ReadLine();
    decimal weight = ParseWeight(data);  // "  1250.5kg" → 1250.5
    UpdateDisplay(weight);
};
port.Open();

// SQLite 로컬 캐시 (오프라인 대비)
using System.Data.SQLite;

var conn = new SQLiteConnection("Data Source=weighing_cache.db");
conn.Open();
var cmd = new SQLiteCommand("INSERT INTO cache (data, synced) VALUES (@d, 0)", conn);
cmd.Parameters.AddWithValue("@d", jsonData);
cmd.ExecuteNonQuery();

// REST API 호출 (백엔드 연동)
using var httpClient = new HttpClient();
httpClient.DefaultRequestHeaders.Authorization =
    new AuthenticationHeaderValue("Bearer", token);

var response = await httpClient.PostAsync(
    "http://server/api/v1/weighing",
    new StringContent(json, Encoding.UTF8, "application/json")
);
```

### 6.3 설정 파일 (appsettings.json)

```json
{
  "Scale": {
    "ScaleId": 1,
    "ComPort": "COM1",
    "BaudRate": 9600,
    "StabilityCount": 5,
    "ToleranceKg": 0.5
  },
  "Api": {
    "BaseUrl": "http://localhost:8080/api/v1",
    "LoginId": "scale-cs",
    "Password": "password"
  },
  "DisplayBoard": {
    "Host": "192.168.1.100",
    "Port": 5000
  },
  "Barrier": {
    "Host": "192.168.1.101",
    "Port": 5001
  }
}
```

### 6.4 모던 UI 시스템 (GDI+ 커스텀 컨트롤)

데스크톱 프로그램은 웹 애플리케이션 수준의 시각적 품질을 위해 **GDI+ 기반 커스텀 컨트롤**을 전면 적용한다. 네이티브 WinForms 컨트롤 대신 `OnPaint`에서 직접 렌더링하여 다크 테마, 라운드 코너, 글로우 효과 등을 구현한다.

#### 6.4.1 Theme 디자인 토큰 시스템

`Controls/Theme.cs`에서 모든 시각적 속성을 중앙 관리한다. Tailwind CSS Slate 팔레트를 기반으로 5단계 배경 계층, 시맨틱 색상, 타이포그래피, 간격 상수를 정의한다.

```csharp
// 배경 계층 (어두운 순)
BgDarkest  #060D1B  → 헤더/푸터
BgBase     #0B1120  → 메인 배경
BgElevated #0F172A  → 입력 필드
BgSurface  #1E293B  → 카드
BgHover    #283548  → 호버 상태

// 폰트 (정적 캐시, Dispose 금지!)
Theme.FontBody      → 9.5pt Segoe UI
Theme.FontBodyBold  → 9.5pt Segoe UI Bold
Theme.FontMono      → 10pt Consolas

// 색상 유틸리티
Theme.WithAlpha(color, alpha)  → 알파 투명도
Theme.Lighten(color, factor)   → 밝게
Theme.Darken(color, factor)    → 어둡게
```

> **주의**: `Theme.FontXxx` 속성은 정적 캐시된 공유 인스턴스이다. `using var font = Theme.FontBody`처럼 사용하면 Dispose 후 **전역적으로 폰트가 무효화**되어 모든 컨트롤에서 "Parameter is not valid" 예외가 발생한다. 반드시 `var font = Theme.FontBody`로 참조만 한다.

#### 6.4.2 커스텀 컨트롤 구성

| 컨트롤 | 설명 | 구현 방식 |
|--------|------|-----------|
| `HeaderBar` | 상단 헤더 (로고, 제목, 연결 LED, 시계) | Control 상속, Timer |
| `StatusFooter` | 하단 상태바 (계량대, 모드, 동기화, 시간) | Control 상속, Timer |
| `WeightDisplayPanel` | 대형 중량 표시 (글로우, 안정성 뱃지) | Control 상속 |
| `CardPanel` | 카드 컨테이너 (유리 효과, 그림자, 액센트) | Panel 상속 |
| `ModernButton` | 버튼 (Primary/Secondary/Danger 3종) | Control 상속 |
| `ModernToggle` | 슬라이딩 토글 (자동/수동 전환, 애니메이션) | Control 상속, Timer |
| `ModernTextBox` | 텍스트 입력 (글로우 테두리, 플레이스홀더) | Control 상속 + TextBox 위임 |
| `ModernComboBox` | 드롭다운 (커스텀 아이템 렌더링) | Control 상속 + ComboBox 위임 |
| `ModernCheckBox` | 체크박스 (커스텀 렌더링, 체크마크) | Control 상속 |
| `ModernListView` | 리스트뷰 (교대 행, 상태 색상화) | ListView 상속 (OwnerDraw) |
| `ProcessStepBar` | 4단계 프로세스 표시 (원형 인디케이터) | Control 상속 |
| `TerminalLogPanel` | 터미널 스타일 로그 출력 | Control 상속 |
| `ModernProgressBar` | 진행바 (스플래시 화면용) | Control 상속 |

#### 6.4.3 레이아웃 구조

메인 폼은 3단 레이아웃으로 구성된다:

```
┌─────────────────────────────────────────────────┐
│  HeaderBar (Dock.Top, 56px)                     │
│  [DK 로고] 부산 스마트 계량 시스템   ● 계량기 ... HH:mm│
├────────────────────┬──┬─────────────────────────┤
│  panelLeftCol      │÷ │  panelRightCol          │
│  (Dock.Left,420px) │1p│  (Dock.Fill)            │
│                    │x │                          │
│  WeightDisplay     │  │  ModeToggle             │
│  (220px)           │  │  ProcessStepBar (64px)  │
│                    │  │  CardManual (185px)      │
│  CardVehicle       │  │  CardActions (88px)     │
│  (190px)           │  │  CardSimulator (90px)   │
│                    │  │                          │
│  CardHistory       │  │  TerminalLog            │
│  (Fill)            │  │  (Fill)                 │
├────────────────────┴──┴─────────────────────────┤
│  StatusFooter (Dock.Bottom, 32px)               │
│  계량대#1 · COM1  ● 자동 모드          v1.0.0 HH:mm:ss│
└─────────────────────────────────────────────────┘
```

#### 6.4.4 렌더링 패턴

모든 커스텀 컨트롤은 다음 패턴을 따른다:

```csharp
public class CustomControl : Control
{
    public CustomControl()
    {
        // 더블 버퍼링 필수
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw, true);
    }

    // 배경 깜빡임 방지
    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        if (Width < 10 || Height < 10) return; // zero-size 방어

        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // RoundedRectHelper로 라운드 사각형 생성
        using var path = RoundedRectHelper.Create(bounds, Theme.RadiusMedium);
        // ... GDI+ 렌더링
    }
}
```

**Wrapper 패턴** (ModernTextBox, ModernComboBox): 네이티브 컨트롤을 내부에 포함하되 외곽 테두리와 포커스 효과만 커스텀 렌더링한다.

```csharp
public class ModernTextBox : Control
{
    private readonly TextBox _inner;  // 내부 네이티브 컨트롤

    // Text, Font 등 속성을 _inner에 위임
    public override string Text
    {
        get => _inner.Text;
        set => _inner.Text = value ?? "";
    }
}
```

---

## 7. 인증과 보안 (JWT + Spring Security)

### 7.1 JWT(JSON Web Token)란?

JWT는 클라이언트에게 발급하는 **디지털 신분증**이다. 서버가 세션을 유지하지 않아도 사용자를 식별할 수 있다.

```
JWT 구조:
xxxxx.yyyyy.zzzzz
  │      │      │
  │      │      └─ Signature (서명): 위변조 방지
  │      └─ Payload (내용): 사용자 정보, 만료시간 등
  └─ Header (헤더): 알고리즘 정보
```

```json
// Payload 예시 (Base64 디코딩 시)
{
  "sub": "hong123",         // 사용자 ID
  "role": "MANAGER",        // 사용자 역할
  "iat": 1737100000,        // 발급 시간
  "exp": 1737101800         // 만료 시간 (30분 후)
}
```

### 7.2 인증 플로우

```
1. 로그인
   클라이언트 → POST /auth/login { loginId, password }
   서버 → { accessToken: "eyJ...", refreshToken: "eyJ..." }

2. API 요청 (인증 필요)
   클라이언트 → GET /dispatches
                Headers: { Authorization: "Bearer eyJ..." }
   서버 → JWT 검증 → 유효 → 데이터 반환

3. 토큰 만료 (30분 후)
   클라이언트 → GET /dispatches → 401 Unauthorized
   클라이언트 → POST /auth/refresh { refreshToken: "eyJ..." }
   서버 → 새로운 { accessToken: "eyJ..." }
   클라이언트 → GET /dispatches (새 토큰으로 재요청)

4. 로그아웃
   클라이언트 → POST /auth/logout
   서버 → accessToken을 Redis 블랙리스트에 등록
          (이후 해당 토큰으로 요청 시 거부)
```

### 7.3 Spring Security 필터 체인

```
HTTP 요청
    │
    ▼
┌─────────────────────┐
│ CORS Filter          │  → CORS 헤더 처리
├─────────────────────┤
│ JWT Authentication   │  → Authorization 헤더에서 토큰 추출
│ Filter               │  → 토큰 검증 (서명, 만료, 블랙리스트)
│                      │  → 유효하면 SecurityContext에 사용자 정보 저장
├─────────────────────┤
│ Authorization Filter │  → 해당 API에 접근 권한이 있는지 확인
│                      │  → ADMIN만 접근 가능한 API에 DRIVER가 접근 → 403
└─────────────────────┘
    │
    ▼
  Controller → Service → Repository → DB
```

### 7.4 역할(Role) 기반 접근 제어

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            // 인증 없이 접근 가능
            .requestMatchers("/api/v1/auth/**").permitAll()

            // ADMIN만 접근 가능
            .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

            // ADMIN 또는 MANAGER만 접근 가능
            .requestMatchers("/api/v1/master/**").hasAnyRole("ADMIN", "MANAGER")

            // 나머지는 인증만 되면 OK
            .anyRequest().authenticated()
        );
    }
}
```

| 역할 | 접근 가능 기능 |
|------|-------------|
| `ADMIN` | 전체 관리 (사용자, 기준정보, 감사로그 등) |
| `MANAGER` | 배차, 계량, 출문, 기준정보 관리 |
| `DRIVER` | 배정된 배차 조회, 마이페이지 |

### 7.5 비밀번호 암호화

```java
// BCrypt: 단방향 해싱 (복호화 불가)
// 같은 비밀번호도 매번 다른 해시값 생성 (salt 포함)

passwordEncoder.encode("myPassword123");
// → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

passwordEncoder.matches("myPassword123", encodedPassword);  // true
passwordEncoder.matches("wrongPassword", encodedPassword);  // false
```

---

## 8. 실시간 통신 (WebSocket / STOMP)

### 8.1 WebSocket이란?

HTTP는 요청-응답 방식이지만, WebSocket은 **양방향 실시간 통신**이 가능하다.

```
HTTP (단방향):
클라이언트 → "새 데이터 있어?" → 서버
클라이언트 ← "없어"              ← 서버
클라이언트 → "지금은?"           → 서버
클라이언트 ← "없어"              ← 서버
클라이언트 → "지금은?"           → 서버
클라이언트 ← "있어! 여기"        ← 서버

WebSocket (양방향):
클라이언트 ←→ 연결 수립 ←→ 서버
(서버가 새 데이터 있으면 즉시 푸시)
서버 → "계량 완료! 2,450kg"    → 클라이언트
서버 → "새 배차 등록됨"        → 클라이언트
```

### 8.2 STOMP 프로토콜

STOMP(Simple Text Oriented Messaging Protocol)는 WebSocket 위에서 동작하는 메시징 프로토콜이다. 구독(subscribe)/발행(publish) 패턴을 제공한다.

```
서버 설정:
  /ws           → WebSocket 연결 엔드포인트
  /topic/*      → 서버 → 클라이언트 브로드캐스트 (구독)
  /app/*        → 클라이언트 → 서버 메시지 전송
```

### 8.3 프론트엔드 WebSocket 연결

```typescript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),  // SockJS 폴백
    reconnectDelay: 5000,                        // 연결 끊기면 5초 후 재접속

    onConnect: () => {
        console.log('WebSocket 연결됨');

        // 구독: 계량 상태 업데이트 수신
        client.subscribe('/topic/weighing-updates', (message) => {
            const data = JSON.parse(message.body);
            // data = { weighingId: 1, status: 'COMPLETED', weight: 2450.5 }
            updateWeighingStatus(data);
        });

        // 구독: 장비 상태 변경 수신
        client.subscribe('/topic/equipment-status', (message) => {
            const data = JSON.parse(message.body);
            updateEquipmentDisplay(data);
        });
    },

    onDisconnect: () => {
        console.log('WebSocket 연결 해제');
    },
});

client.activate();  // 연결 시작

// 메시지 전송 (클라이언트 → 서버)
client.publish({
    destination: '/app/weighing-command',
    body: JSON.stringify({ action: 'START', scaleId: 1 }),
});
```

### 8.4 백엔드 WebSocket 설정

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // SockJS 폴백 (WebSocket 미지원 브라우저 대응)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");    // 구독 경로 prefix
        registry.setApplicationDestinationPrefixes("/app");  // 전송 경로 prefix
    }
}

// 서비스에서 메시지 발행
@Service
@RequiredArgsConstructor
public class WeighingService {
    private final SimpMessagingTemplate messagingTemplate;

    public void completeWeighing(Long weighingId) {
        // ... 비즈니스 로직 ...

        // 모든 구독자에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/weighing-updates",
            new WeighingUpdateMessage(weighingId, "COMPLETED", weight));
    }
}
```

---

## 9. 빌드와 배포 (Vite, Gradle, Vercel, Railway)

### 9.1 프론트엔드 빌드 (Vite)

```bash
# 개발 서버 실행 (HMR: 코드 수정 시 즉시 반영)
npm run dev    # → http://localhost:3000

# 프로덕션 빌드
npm run build  # → tsc(타입체크) && vite build → dist/ 폴더 생성
```

**vite.config.ts 핵심 설정:**

```typescript
export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        proxy: {
            // 개발 시 /api 요청을 백엔드로 프록시
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
            // WebSocket 프록시
            '/ws': {
                target: 'http://localhost:8080',
                ws: true,
            },
        },
    },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),  // @ = src/
        },
    },
});
```

### 9.2 백엔드 빌드 (Gradle)

```bash
# 빌드 (JAR 생성)
./gradlew build           # → build/libs/weighing-0.0.1-SNAPSHOT.jar

# 테스트 실행
./gradlew test

# 실행
java -jar build/libs/weighing-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
# 또는
./gradlew bootRun         # 개발 시
```

### 9.3 배포 구조

```
GitHub (main 브랜치 push)
    │
    ├──→ Vercel (프론트엔드 자동 배포)
    │    ├── npm run build 실행
    │    ├── dist/ 정적 파일 호스팅
    │    └── API 프록시 설정 (vercel.json)
    │         /api/* → Railway 백엔드
    │         /ws/*  → Railway 백엔드
    │
    └──→ Railway (백엔드 자동 배포)
         ├── Gradle 빌드 실행
         ├── JAR 파일 실행
         ├── PostgreSQL (관리형 인스턴스)
         └── Redis (관리형 인스턴스)
```

### 9.4 환경 변수 관리

```yaml
# 백엔드: application-prod.yml (Railway 환경 변수로 주입)
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

jwt:
  secret: ${JWT_SECRET}       # 절대 하드코딩하지 않는다!
```

**환경 변수 관리 원칙:**
- 비밀번호, API 키, JWT Secret 등은 **절대** 코드에 넣지 않는다
- `.env` 파일은 `.gitignore`에 추가한다
- Railway, Vercel 등의 환경 변수 기능을 사용한다

---

## 10. 개발 환경 설정

### 10.1 필수 소프트웨어

| 소프트웨어 | 버전 | 용도 |
|-----------|------|------|
| JDK | 17+ | 백엔드 실행 |
| Node.js | 18+ | 프론트엔드 실행 |
| Git | Latest | 버전 관리 |
| IntelliJ IDEA | 최신 | 백엔드 IDE |
| VS Code | 최신 | 프론트엔드 IDE |
| Flutter SDK | 3.10+ | 모바일 개발 |
| Android Studio | 최신 | 안드로이드 에뮬레이터 |

### 10.2 백엔드 로컬 실행

```bash
cd backend

# 개발 프로파일로 실행 (H2 인메모리 DB + 내장 Redis)
./gradlew bootRun

# 또는 IntelliJ에서:
# 1. WeighingApplication.java 열기
# 2. main() 옆 ▶ 버튼 클릭
# 3. Environment: spring.profiles.active=dev 설정

# Swagger UI 확인: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### 10.3 프론트엔드 로컬 실행

```bash
cd frontend

# 의존성 설치 (최초 1회 또는 package.json 변경 시)
npm install

# 개발 서버 실행
npm run dev    # → http://localhost:3000

# 타입 체크 + 프로덕션 빌드
npm run build
```

### 10.4 모바일 앱 로컬 실행

```bash
cd mobile

# 의존성 설치
flutter pub get

# 에뮬레이터에서 실행
flutter run

# Mock 데이터 사용 설정: lib/config/api_config.dart
# static const bool useMockData = true;
```

### 10.5 추천 VS Code 확장 프로그램

| 확장 | 용도 |
|------|------|
| ESLint | JavaScript/TypeScript 코드 분석 |
| Prettier | 코드 포맷팅 |
| TypeScript Importer | import 자동 추가 |
| ES7+ React Snippets | React 코드 스니펫 |
| Dart | Dart 언어 지원 |
| Flutter | Flutter 개발 도구 |

### 10.6 추천 IntelliJ 플러그인

| 플러그인 | 용도 |
|---------|------|
| Lombok | Lombok 어노테이션 지원 |
| Spring Boot Assistant | 설정 자동완성 |
| Database Tools | DB 브라우저 |
| GitToolBox | Git 상태 표시 |

---

## 11. 코드 컨벤션과 패턴

### 11.1 네이밍 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| **Java 클래스** | PascalCase | `DispatchService`, `WeighingController` |
| **Java 메서드/변수** | camelCase | `findByPlateNumber`, `dispatchDate` |
| **Java 상수** | UPPER_SNAKE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| **Java 패키지** | lowercase | `com.dongkuk.weighing.dispatch` |
| **DB 테이블/컬럼** | snake_case | `weighing_records`, `plate_number` |
| **React 컴포넌트** | PascalCase | `DispatchPage`, `SortableTable` |
| **React 파일** | PascalCase.tsx | `DispatchPage.tsx`, `MyPage.tsx` |
| **TypeScript 변수/함수** | camelCase | `handleSubmit`, `fetchData` |
| **TypeScript 인터페이스** | PascalCase | `Dispatch`, `ApiResponse<T>` |
| **CSS 클래스** | kebab-case | `main-layout`, `search-bar` |
| **Flutter 클래스** | PascalCase | `DispatchListScreen` |
| **Flutter 파일** | snake_case.dart | `dispatch_list_screen.dart` |
| **API 엔드포인트** | kebab-case | `/gate-passes`, `/weighing-records` |
| **JSON 필드** | snake_case | `dispatch_date`, `plate_number` |

### 11.2 Git 커밋 메시지

```
<type>: <설명>

type:
  feat:     새 기능
  fix:      버그 수정
  refactor: 리팩토링 (기능 변경 없음)
  docs:     문서 변경
  style:    코드 스타일 (포맷팅 등)
  test:     테스트 추가/수정
  chore:    빌드 설정, 패키지 등

예시:
  feat: 배차 검색 필터 기능 추가
  fix: 토큰 갱신 시 무한 루프 수정
  refactor: 계량 서비스 교차 검증 로직 분리
```

### 11.3 API 설계 규칙

```
HTTP 메서드 선택 기준:
  GET    → 데이터 조회 (변경 없음)
  POST   → 데이터 생성 (새 리소스 추가)
  PUT    → 데이터 수정 (전체 업데이트)
  PATCH  → 데이터 수정 (일부 업데이트)
  DELETE → 데이터 삭제

URL 설계:
  GET    /api/v1/dispatches           → 배차 목록 조회
  GET    /api/v1/dispatches/123       → 배차 상세 조회
  POST   /api/v1/dispatches           → 배차 등록
  PUT    /api/v1/dispatches/123       → 배차 수정
  DELETE /api/v1/dispatches/123       → 배차 삭제
  PUT    /api/v1/dispatches/123/cancel → 배차 취소 (상태 변경)

주의:
  ✅ /api/v1/dispatches       (복수형)
  ❌ /api/v1/dispatch          (단수형)
  ✅ /api/v1/gate-passes      (kebab-case)
  ❌ /api/v1/gatePasses        (camelCase)
```

### 11.4 에러 처리 패턴

```
백엔드:
  - 비즈니스 에러 → throw new BusinessException(ErrorCode.XXXX)
  - 입력 검증 실패 → @Valid + MethodArgumentNotValidException (자동)
  - 전역 핸들러(GlobalExceptionHandler)가 일관된 JSON 응답으로 변환

프론트엔드:
  - API 호출은 try-catch로 감싸기
  - 성공: message.success('저장되었습니다')
  - 실패: message.error(에러 메시지) 또는 서버 에러 메시지 표시
  - 폼 검증 실패: Ant Design Form이 자동으로 에러 메시지 표시
```

---

## 12. 자주 하는 실수와 주의사항

### 12.1 백엔드

| 실수 | 설명 | 해결 |
|------|------|------|
| N+1 문제 | 연관 Entity를 반복 조회하여 쿼리가 대량 발생 | `@EntityGraph` 또는 `JOIN FETCH` 사용 |
| LazyInitializationException | 트랜잭션 밖에서 Lazy 로딩 객체 접근 | DTO로 필요한 데이터만 변환하여 반환 |
| `@Transactional` 누락 | 데이터 변경인데 readOnly 트랜잭션 | Service의 CUD 메서드에 `@Transactional` 추가 |
| Entity 직접 반환 | 순환 참조, 보안 정보 노출 | DTO로 변환하여 반환 |
| 비밀번호 평문 저장 | 보안 취약점 | BCrypt로 해싱 후 저장 |
| JWT Secret 하드코딩 | 보안 취약점 | 환경 변수로 외부 주입 |

**N+1 문제 예시:**

```java
// ❌ N+1 문제 발생 코드
List<Dispatch> dispatches = dispatchRepository.findAll();  // 1번 쿼리
for (Dispatch d : dispatches) {
    d.getVehicle().getPlateNumber();  // 배차마다 1번씩 추가 쿼리 발생!
}
// 결과: 배차 100건이면 101번 쿼리 실행 (1 + 100)

// ✅ JOIN FETCH로 해결
@Query("SELECT d FROM Dispatch d JOIN FETCH d.vehicle")
List<Dispatch> findAllWithVehicle();  // 1번의 JOIN 쿼리로 해결
```

### 12.2 프론트엔드

| 실수 | 설명 | 해결 |
|------|------|------|
| useEffect 무한 루프 | 의존성 배열에 매번 새로 생성되는 객체/함수 포함 | `useCallback`, `useMemo` 사용 |
| 직접 state 수정 | `state.push(item)` 처럼 직접 수정 | `setState([...state, item])` 새 배열 생성 |
| key prop 누락 | 리스트 렌더링 시 key 미지정 | `<Component key={item.id} />` |
| 토큰 만료 미처리 | 401 에러 시 사용자가 수동 재로그인 | Axios 인터셉터에서 자동 갱신 |
| 메모리 누수 | 언마운트 후 setState 호출 | useEffect cleanup 함수에서 정리 |
| any 타입 남용 | TypeScript의 장점을 잃음 | 명시적 타입 또는 interface 정의 |

**state 수정 실수:**

```tsx
// ❌ 절대 직접 수정하면 안 됨 (React가 변경을 감지 못 함)
const [items, setItems] = useState<string[]>([]);
items.push('새 아이템');         // 직접 수정 → UI 갱신 안 됨

// ✅ 새 배열을 만들어서 setState 호출
setItems([...items, '새 아이템']);    // 스프레드 연산자로 복사 + 추가
setItems(prev => [...prev, '새 아이템']);  // 이전 값 기반 (더 안전)

// 삭제
setItems(prev => prev.filter(item => item !== '삭제할 아이템'));

// 수정
setItems(prev => prev.map(item =>
    item.id === targetId ? { ...item, name: '새이름' } : item
));
```

### 12.3 Flutter

| 실수 | 설명 | 해결 |
|------|------|------|
| setState 과다 호출 | 불필요한 리빌드 발생 | Provider로 상태 분리 |
| BuildContext 비동기 사용 | async 함수에서 context 사용 시 오류 | `mounted` 체크 후 사용 |
| 시리얼라이제이션 누락 | JSON → 모델 변환 코드 빠뜨림 | `fromJson` 팩토리 메서드 구현 |

### 12.4 공통

| 실수 | 설명 | 해결 |
|------|------|------|
| `.env` 파일 커밋 | 비밀번호, API 키 등이 Git에 올라감 | `.gitignore`에 추가 |
| CORS 오류 | 프론트-백엔드 도메인 불일치 | 백엔드 CORS 설정 확인 |
| 시간대 불일치 | UTC vs KST 혼동 | 서버는 UTC 저장, 표시 시 KST 변환 |
| 대소문자 이슈 | Windows는 대소문자 구분 안 함, Linux는 구분 | 일관된 네이밍 사용 |

---

## 부록: 핵심 용어 사전

| 용어 | 설명 |
|------|------|
| **REST API** | HTTP 메서드(GET/POST/PUT/DELETE)로 리소스를 조작하는 API 설계 방식 |
| **SPA** | Single Page Application. 페이지 전환 없이 컴포넌트만 교체하는 웹 앱 |
| **ORM** | Object-Relational Mapping. 객체와 DB 테이블을 매핑하는 기술 (JPA) |
| **DTO** | Data Transfer Object. API 요청/응답에 사용되는 데이터 구조체 |
| **DI** | Dependency Injection. 객체를 직접 생성하지 않고 외부에서 주입받는 패턴 |
| **JWT** | JSON Web Token. 서버가 발급하는 인증 토큰 |
| **STOMP** | WebSocket 위의 메시징 프로토콜 (Pub/Sub 패턴) |
| **HMR** | Hot Module Replacement. 코드 수정 시 브라우저 새로고침 없이 반영 |
| **CORS** | Cross-Origin Resource Sharing. 다른 도메인에서 API 호출 허용 설정 |
| **HikariCP** | JDBC 커넥션 풀 라이브러리 (DB 연결 재사용으로 성능 향상) |
| **FCM** | Firebase Cloud Messaging. Google의 푸시 알림 서비스 |
| **Dirty Checking** | JPA가 Entity 변경을 자동 감지하여 UPDATE 쿼리를 생성하는 기능 |
| **Bean** | Spring이 관리하는 객체 (Controller, Service, Repository 등) |
| **Profile** | Spring의 환경별 설정 분리 기능 (dev, prod, test) |
| **Interceptor** | 요청/응답을 가로채서 공통 처리하는 미들웨어 (Axios, Spring 모두 사용) |

---

## 13. 최근 추가된 프론트엔드 패턴

### 13.1 페이지 레지스트리 (pageRegistry.ts)

모든 페이지를 `config/pageRegistry.ts`에서 중앙 관리한다. 새 페이지 추가 시 이 파일만 수정하면 사이드바 메뉴, 탭 네비게이션, 권한 제어가 자동 적용된다.

```typescript
// config/pageRegistry.ts
export interface PageConfig {
  component: React.LazyExoticComponent<React.FC>; // React.lazy 코드분할
  title: string;        // 메뉴/탭 표시 제목
  icon: React.ReactNode; // 메뉴 아이콘 (Ant Design Icons)
  closable: boolean;     // 탭 닫기 가능 여부
  roles?: ('ADMIN' | 'MANAGER' | 'DRIVER')[]; // 접근 가능 역할
}

// 새 페이지 추가 예시
const NewPage = React.lazy(() => import('../pages/NewPage'));

export const PAGE_REGISTRY: Record<string, PageConfig> = {
  '/new-page': {
    component: NewPage,
    title: '새 페이지',
    icon: React.createElement(SomeIcon),
    closable: true,
    roles: ['ADMIN', 'MANAGER'], // 생략 시 전체 접근 가능
  },
  // ...기존 페이지들
};
```

**핵심 포인트:**
- `React.lazy`로 코드분할 → 해당 페이지 방문 시에만 JS 번들 로드
- `closable: false`는 계량소 관제 같은 고정 탭에 사용
- `PINNED_TABS` 배열에 추가하면 앱 시작 시 자동 열림
- `MAX_TABS = 10`으로 최대 탭 수 제한

### 13.2 인증 컨텍스트 (AuthContext.tsx)

`AuthContext`는 전역 인증 상태를 관리한다. `localStorage`의 토큰을 기반으로 로그인 상태를 유지하고, 토큰 갱신을 자동 처리한다.

```tsx
// 사용 방법
import { useAuth } from '../context/AuthContext';

const MyComponent: React.FC = () => {
    const { user, isAuthenticated, logout } = useAuth();

    if (!isAuthenticated) return <Navigate to="/login" />;

    return <div>안녕하세요, {user?.userName}님</div>;
};
```

### 13.3 CRUD 상태 관리 훅 (useCrudState.ts)

기준정보 페이지(운송사, 차량, 계량대, 공통코드)는 모두 동일한 CRUD 패턴을 따른다. `useCrudState`로 이 패턴을 재사용한다.

```tsx
const {
    data, loading, searched,
    totalElements, currentPage,
    createModalOpen, editModalOpen, editingRecord,
    setCreateModalOpen, setEditModalOpen,
    handleSearch, handlePageChange,
    handleCreate, handleEdit, handleDelete,
} = useCrudState<Vehicle>({
    fetchUrl: '/master/vehicles',
    createUrl: '/master/vehicles',
    updateUrl: (id) => `/master/vehicles/${id}`,
    deleteUrl: (id) => `/master/vehicles/${id}`,
});
```

### 13.4 MasterCrudPage 공통 컴포넌트

기준정보 CRUD 페이지의 레이아웃과 동작을 표준화하는 공통 컴포넌트다. 검색 폼, 데이터 테이블, 생성/수정 모달을 자동 구성한다.

```tsx
<MasterCrudPage
    title="차량 관리"
    columns={vehicleColumns}
    searchFields={searchFields}
    createFields={createFields}
    editFields={editFields}
    fetchUrl="/master/vehicles"
    createUrl="/master/vehicles"
    updateUrl={(id) => `/master/vehicles/${id}`}
    deleteUrl={(id) => `/master/vehicles/${id}`}
/>
```

### 13.5 API 호출 훅 (useApiCall.ts)

API 호출의 로딩/성공/에러 상태를 자동 관리하는 훅이다.

```tsx
const { execute, loading, error } = useApiCall();

const handleSave = async () => {
    const result = await execute(
        () => apiClient.post('/dispatches', formData),
        { successMessage: '배차가 등록되었습니다.' }
    );
    if (result) refreshData();
};
```

### 13.6 키보드 단축키 (useKeyboardShortcuts.ts)

페이지별 키보드 단축키를 등록하는 훅이다. 컴포넌트 언마운트 시 자동 해제된다.

```tsx
useKeyboardShortcuts([
    { key: 'n', ctrl: true, handler: () => setCreateModalOpen(true), description: '신규 등록' },
    { key: 'f', ctrl: true, handler: () => searchInputRef.current?.focus(), description: '검색' },
    { key: 'Escape', handler: () => setModalOpen(false), description: '모달 닫기' },
]);
```

### 13.7 WebSocket 훅 (useWebSocket.ts)

STOMP 프로토콜 기반 WebSocket 연결을 관리하는 훅이다. 연결/재연결/구독을 자동 처리한다.

```tsx
const { connected, subscribe, publish } = useWebSocket({
    url: '/ws',
    onConnect: () => console.log('WebSocket 연결됨'),
});

// 구독
useEffect(() => {
    if (connected) {
        subscribe('/topic/weighing-updates', (message) => {
            const data = JSON.parse(message.body);
            updateWeighingStatus(data);
        });
    }
}, [connected]);
```

### 13.8 계량소 관제 아키텍처

`WeighingStationPage`는 현장 계량소를 실시간 관제하는 전용 페이지다. 여러 하위 컴포넌트로 구성된다:

```
WeighingStationPage
├── ConnectionStatusBar    → 장비 연결 상태 (인디게이터, LPR, 전광판, 차단기)
├── WeightDisplay          → 실시간 중량 표시 (큰 숫자, 안정/불안정 상태)
├── VehicleInfoPanel       → 현재 계량 중인 차량/배차 정보
├── ProcessStateBar        → 계량 진행 단계 표시 (진입→감지→촬영→인식→계량→완료)
├── ActionButtons          → 계량 시작/완료/취소 등 액션 버튼
├── ModeToggle             → 자동/수동 모드 전환
├── ManualControls         → 수동 모드 시 직접 제어 패널
├── WeighingHistoryTable   → 최근 계량 이력 테이블
├── StatusLog              → 장비/시스템 이벤트 로그
└── SimulatorPanel         → 개발용 하드웨어 시뮬레이터
```

**데이터 흐름:**
- `useWeighingStation` 훅: 계량 비즈니스 로직 (상태 관리, API 호출)
- `useWeighingStationSocket` 훅: WebSocket 실시간 데이터 수신
- `weighingStationApi.ts`: 계량소 전용 REST API 호출

### 13.9 대시보드 탭 구조

`DashboardPage`는 3개 탭으로 구성된다:

| 탭 | 컴포넌트 | 내용 |
|---|---------|------|
| 개요 | `OverviewTab` | KPI 카드 (AnimatedNumber), 일별 추이 차트, 품목별 비율 |
| 실시간 | `RealtimeTab` | WebSocket 기반 실시간 계량 현황, 계량대 상태 |
| 분석 | `AnalysisTab` | 상세 통계 차트, 기간별/조건별 분석 |

### 13.10 ECharts 설정

ECharts 6.0은 tree-shaking을 위해 필요한 컴포넌트만 수동 등록해야 한다:

```typescript
// utils/echartsSetup.ts - 앱 시작 시 1회 호출
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);
```

```typescript
// utils/chartOptions.ts - 공통 차트 옵션
export const createLineChartOption = (data: DailyStatistics[]) => ({
    // ... 표준화된 차트 옵션
});
```

---

## 14. 최근 추가된 모바일 패턴

### 14.1 오프라인 캐시 서비스

`offline_cache_service.dart`는 SharedPreferences 기반으로 핵심 데이터를 로컬에 캐시한다. 네트워크 불안정 시에도 기본 정보를 표시할 수 있다.

```dart
class OfflineCacheService {
    // 배차 목록 캐시
    Future<void> cacheDispatches(List<Dispatch> dispatches);
    Future<List<Dispatch>?> getCachedDispatches();

    // 캐시 만료 확인
    bool isCacheExpired(String key, {Duration maxAge = const Duration(hours: 1)});
}
```

**주의**: SharedPreferences는 소량 데이터에 적합하다. 대량 데이터는 SQLite 사용을 고려해야 한다.

### 14.2 토스트 유틸

`utils/toast_utils.dart`는 SnackBar 기반 알림을 표준화한다:

```dart
ToastUtils.showSuccess(context, '배차가 등록되었습니다.');
ToastUtils.showError(context, '네트워크 오류가 발생했습니다.');
ToastUtils.showWarning(context, 'OTP가 곧 만료됩니다.');
```

### 14.3 모바일 화면 구조

```
홈 (HomeScreen)
├── 배차 목록 (DispatchListScreen)
│   └── 배차 상세 (DispatchDetailScreen)
├── 계량
│   ├── OTP 입력 (OtpInputScreen)
│   └── 계량 진행 (WeighingProgressScreen)
├── 전자 계량표
│   ├── 목록 (SlipListScreen)
│   └── 상세 (SlipDetailScreen)
├── 이력 조회 (HistoryScreen)
├── 공지사항 (NoticeScreen)
├── 알림 (NotificationListScreen)
└── OTP 로그인 (OtpLoginScreen)
```

---

## 15. 최근 추가된 데스크톱 패턴

### 15.1 스플래시 폼

`SplashForm.cs`는 앱 시작 시 초기화 상태를 표시한다:
- 설정 파일 로드
- 백엔드 API 연결 확인
- 하드웨어 장비 연결 확인 (인디게이터, LPR, 전광판, 차단기)
- 초기화 완료 후 MainForm으로 전환

### 15.2 하드웨어 인터페이스 패턴

모든 하드웨어 장비는 인터페이스로 추상화되어 있다. 실제 장비와 시뮬레이터가 동일한 인터페이스를 구현한다:

```csharp
// 인터페이스 정의
public interface ILprCamera {
    Task<LprCaptureResult> CaptureAsync();
    bool IsConnected { get; }
}

// 실제 구현 (운영)
public class LprCamera : ILprCamera { ... }

// 시뮬레이터 (개발)
public class LprCameraSimulator : ILprCamera { ... }
```

### 15.3 계량 프로세스 오케스트레이터

`WeighingProcessService`가 전체 계량 프로세스를 관리한다:

```
차량 감지 → LPR 촬영 → AI 검증 → 배차 매칭 → 계량 시작
→ 중량 안정화 대기 → 중량 기록 → 전광판 표시 → 차단기 개방
→ API 서버 전송 → 완료
```

### 15.4 xUnit 테스트

데스크톱 프로그램의 핵심 서비스에 대한 단위 테스트가 작성되어 있다:
- `ApiServiceTests.cs`: REST API 호출 테스트
- `IndicatorServiceTests.cs`: 인디게이터 데이터 파싱 테스트
- `LocalCacheServiceTests.cs`: SQLite 캐시 CRUD 테스트

```bash
cd weighing-cs
dotnet test      # xUnit 테스트 실행
```

---

## 부록: 핵심 용어 사전

| 용어 | 설명 |
|------|------|
| **REST API** | HTTP 메서드(GET/POST/PUT/DELETE)로 리소스를 조작하는 API 설계 방식 |
| **SPA** | Single Page Application. 페이지 전환 없이 컴포넌트만 교체하는 웹 앱 |
| **ORM** | Object-Relational Mapping. 객체와 DB 테이블을 매핑하는 기술 (JPA) |
| **DTO** | Data Transfer Object. API 요청/응답에 사용되는 데이터 구조체 |
| **DI** | Dependency Injection. 객체를 직접 생성하지 않고 외부에서 주입받는 패턴 |
| **JWT** | JSON Web Token. 서버가 발급하는 인증 토큰 |
| **STOMP** | WebSocket 위의 메시징 프로토콜 (Pub/Sub 패턴) |
| **HMR** | Hot Module Replacement. 코드 수정 시 브라우저 새로고침 없이 반영 |
| **CORS** | Cross-Origin Resource Sharing. 다른 도메인에서 API 호출 허용 설정 |
| **HikariCP** | JDBC 커넥션 풀 라이브러리 (DB 연결 재사용으로 성능 향상) |
| **FCM** | Firebase Cloud Messaging. Google의 푸시 알림 서비스 |
| **Dirty Checking** | JPA가 Entity 변경을 자동 감지하여 UPDATE 쿼리를 생성하는 기능 |
| **Bean** | Spring이 관리하는 객체 (Controller, Service, Repository 등) |
| **Profile** | Spring의 환경별 설정 분리 기능 (dev, prod, test) |
| **Interceptor** | 요청/응답을 가로채서 공통 처리하는 미들웨어 (Axios, Spring 모두 사용) |
| **LPR** | License Plate Recognition. 차량번호판 자동인식 기술 |
| **OTP** | One-Time Password. 일회용 보안 비밀번호 (Redis 기반, TTL 5분) |
| **인디게이터** | 계량대에서 중량값을 표시/전송하는 장치 (시리얼 통신) |
| **전광판** | OTP, 계량 안내 등을 표시하는 LED 디스플레이 (TCP 통신) |
| **차단기** | 계량대 진입/출구 자동 차단기 (TCP 통신) |
| **Tree-shaking** | 사용하지 않는 코드를 빌드 시 자동 제거하는 최적화 기법 |
| **Code Splitting** | React.lazy를 이용해 페이지별 JS 번들을 분리하는 기법 |
| **@dnd-kit** | React 드래그 앤 드롭 라이브러리 (테이블 행 정렬에 사용) |

---

> **마지막 조언**: 이 문서를 한 번에 외우려 하지 말고, 실제 코드를 읽으면서 필요할 때 참고하세요. 가장 좋은 학습은 프로젝트 코드를 직접 수정하고 동작을 확인하는 것입니다.

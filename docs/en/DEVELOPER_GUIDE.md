# Busan Smart Weighing System - New Developer Technical Guide

> **Audience**: New/junior developers joining the project
> **Purpose**: Understanding core concepts and practical patterns of all technology stacks used in the project

---

## Table of Contents

1. [Overall Project Structure](#1-overall-project-structure)
2. [Database (PostgreSQL + Redis)](#2-database-postgresql--redis)
3. [Backend WAS (Spring Boot)](#3-backend-was-spring-boot)
4. [Frontend (React + TypeScript)](#4-frontend-react--typescript)
5. [Mobile App (Flutter)](#5-mobile-app-flutter)
6. [Desktop Program (C# .NET WinForms)](#6-desktop-program-c-net-winforms)
7. [Authentication and Security (JWT + Spring Security)](#7-authentication-and-security-jwt--spring-security)
8. [Real-Time Communication (WebSocket / STOMP)](#8-real-time-communication-websocket--stomp)
9. [Build and Deployment (Vite, Gradle, Vercel, Railway)](#9-build-and-deployment-vite-gradle-vercel-railway)
10. [Development Environment Setup](#10-development-environment-setup)
11. [Code Conventions and Patterns](#11-code-conventions-and-patterns)
12. [Common Mistakes and Precautions](#12-common-mistakes-and-precautions)

---

## 1. Overall Project Structure

### 1.1 System Architecture Overview

```
┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
│  React Web   │   │ Flutter App  │   │  C# WinForms     │
│  (Vercel)    │   │ (iOS/Android)│   │  (On-site PC)    │
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

### 1.2 Monorepo Directory Structure

```
busan-smart-weighing/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/com/dongkuk/weighing/
│   │   ├── auth/               # Authentication (JWT, Login)
│   │   ├── user/               # User Management
│   │   ├── master/             # Master Data (Companies, Vehicles, Scales, Codes)
│   │   ├── dispatch/           # Dispatch Management
│   │   ├── weighing/           # Core Weighing Logic
│   │   ├── gatepass/           # Gate Pass Management
│   │   ├── slip/               # Slip Management
│   │   ├── notification/       # Push Notifications (FCM)
│   │   ├── dashboard/          # Dashboard Statistics
│   │   ├── audit/              # Audit Logs
│   │   └── global/             # Common Configs, Exception Handling, Utilities
│   └── src/main/resources/
│       ├── application.yml     # Common Configuration
│       ├── application-dev.yml # Development Environment
│       └── application-prod.yml# Production Environment
│
├── frontend/                   # React Web Frontend
│   ├── src/
│   │   ├── api/                # Axios Client
│   │   ├── components/         # Reusable Components
│   │   ├── context/            # React Context (Theme, Tabs)
│   │   ├── hooks/              # Custom Hooks
│   │   ├── layouts/            # Layouts
│   │   ├── pages/              # Page Components
│   │   ├── theme/              # Theme Configuration
│   │   ├── types/              # TypeScript Type Definitions
│   │   └── utils/              # Utility Functions
│   ├── package.json
│   └── vite.config.ts
│
├── mobile/                     # Flutter Mobile App
│   ├── lib/
│   │   ├── config/             # API Configuration
│   │   ├── models/             # Data Models
│   │   ├── providers/          # State Management
│   │   ├── screens/            # Screens
│   │   ├── services/           # API/Notification Services
│   │   └── widgets/            # Widgets
│   └── pubspec.yaml
│
└── WeighingCS/                 # C# Desktop Program
    ├── Models/
    ├── Services/
    ├── Interfaces/
    └── MainForm.cs
```

### 1.3 Key Technology Versions

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Backend Runtime |
| Spring Boot | 3.2.5 | Backend Framework |
| PostgreSQL | Latest | Relational Database |
| Redis | Latest | Cache, Token Management |
| React | 18.3.1 | Web Frontend |
| TypeScript | 5.9.3 | Frontend Type System |
| Vite | 7.3.1 | Frontend Build Tool |
| Ant Design | 5.29.3 | UI Component Library |
| Flutter | 3.10.4+ | Mobile App |
| .NET | 8.0 | Desktop Program |

---

## 2. Database (PostgreSQL + Redis)

### 2.1 What is PostgreSQL?

PostgreSQL (Postgres) is an open-source relational database management system (RDBMS). It is similar to MySQL but offers richer advanced features such as JSONB, Window Functions, CTEs, etc.

### 2.2 Essential SQL Concepts

#### Basic CRUD

```sql
-- Create: Insert data
INSERT INTO users (login_id, user_name, phone_number, user_role)
VALUES ('hong123', '홍길동', '010-1234-5678', 'MANAGER');

-- Read: Query data
SELECT * FROM users WHERE user_role = 'MANAGER';

-- Pagination (query auto-generated by Spring Data JPA)
SELECT * FROM users
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;  -- First page, 20 records per page

-- Update: Modify data
UPDATE users SET phone_number = '010-9999-8888' WHERE user_id = 1;

-- Delete: Remove data
DELETE FROM users WHERE user_id = 1;
```

#### JOIN - Connecting Relationships Between Tables

```sql
-- Query dispatch with vehicle information together
-- INNER JOIN: Returns only rows with matching data in both tables
SELECT d.dispatch_id, d.item_name, v.plate_number, v.driver_name
FROM dispatches d
INNER JOIN vehicles v ON d.vehicle_id = v.vehicle_id;

-- LEFT JOIN: Based on the left table; NULL if no data in the right table
SELECT d.dispatch_id, d.item_name, g.pass_status
FROM dispatches d
LEFT JOIN gate_passes g ON d.dispatch_id = g.dispatch_id;
-- Dispatches without gate pass records are also included (g.pass_status = NULL)
```

#### Index - Improving Search Speed

```sql
-- An index is like "a table of contents in a book"
-- Without an index, a WHERE clause checks every row one by one (Full Scan)
-- With an index, the desired value can be found quickly

CREATE INDEX idx_dispatch_date ON dispatches(dispatch_date);
CREATE INDEX idx_vehicle_plate ON vehicles(plate_number);

-- Composite index: Combination of multiple columns
CREATE INDEX idx_dispatch_search ON dispatches(dispatch_status, dispatch_date);
```

**Index Considerations:**
- Indexes are also updated during INSERT/UPDATE/DELETE, so write performance decreases
- Create indexes on columns frequently used in WHERE, ORDER BY, and JOIN
- Indexes are unnecessary for tables with small amounts of data

#### Transaction

```sql
-- Transaction: "Either all succeed or all fail"
-- Example: Saving the record + changing status as a single unit when weighing is completed

BEGIN;  -- Start transaction

UPDATE weighing_records SET weighing_status = 'COMPLETED'
WHERE weighing_id = 100;

INSERT INTO weighing_slips (weighing_id, slip_number, issued_at)
VALUES (100, 'SLP-2026-001', NOW());

COMMIT;  -- Both queries succeeded → committed

-- If an error occurs in between
ROLLBACK;  -- Both queries cancelled, restored to previous state
```

**Using Transactions in Spring (you don't write SQL directly in code):**

```java
@Service
public class WeighingService {

    @Transactional  // The entire method is a single transaction
    public void completeWeighing(Long weighingId) {
        WeighingRecord record = weighingRepository.findById(weighingId)
            .orElseThrow();
        record.complete();                    // Change status
        weighingRepository.save(record);      // Persist to DB

        WeighingSlip slip = WeighingSlip.create(record);
        slipRepository.save(slip);            // Create slip

        // If the method completes normally → COMMIT
        // If an exception is thrown → automatic ROLLBACK
    }
}
```

### 2.3 JPA and Entity

In this project, we don't write SQL directly. Instead, we use JPA (Java Persistence API), an ORM (Object-Relational Mapping) technology that maps Java objects to database tables.

```java
// Entity: A Java class that maps 1:1 to a database table

@Entity                      // This class is a DB table
@Table(name = "vehicles")    // Table name: vehicles
public class Vehicle extends BaseEntity {  // BaseEntity: Auto-manages createdAt, updatedAt

    @Id                      // This field is the Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment (1, 2, 3...)
    private Long vehicleId;

    @Column(nullable = false, length = 20)  // NOT NULL, max 20 characters
    private String plateNumber;

    @Column(length = 20)     // NULL allowed, max 20 characters
    private String vehicleType;

    @ManyToOne(fetch = FetchType.LAZY)      // N:1 relationship, lazy loading
    @JoinColumn(name = "company_id")        // FK column name
    private Company company;

    private BigDecimal defaultTareWeight;
    private BigDecimal maxLoadWeight;
}
```

**Key Concepts:**
- `@Entity` → This class = DB table
- `@Id` → Primary Key (unique identifier)
- `@Column` → Table column settings (length, NULL allowance, etc.)
- `@ManyToOne` → Many-to-one relationship (Vehicle N : Company 1)
- `FetchType.LAZY` → Only queries the DB when actually accessed (performance optimization)

### 2.4 Repository - Data Access Layer

```java
// Spring Data JPA: Just declare an interface and the implementation is auto-generated
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Query auto-generated from method name alone!
    // SELECT * FROM vehicles WHERE plate_number = ?
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    // SELECT * FROM vehicles WHERE company_id = ? ORDER BY plate_number ASC
    List<Vehicle> findByCompanyIdOrderByPlateNumberAsc(Long companyId);

    // Write complex queries directly with @Query
    @Query("SELECT v FROM Vehicle v WHERE v.plateNumber LIKE %:keyword%")
    Page<Vehicle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
```

**Method Name Conventions:**
| Keyword | Meaning | Example |
|---------|---------|---------|
| `findBy` | SELECT WHERE | `findByUserName(name)` |
| `And` | AND condition | `findByRoleAndActive(role, active)` |
| `Or` | OR condition | `findByNameOrEmail(name, email)` |
| `OrderBy` | Sorting | `findByRoleOrderByNameAsc(role)` |
| `Between` | Range | `findByDateBetween(start, end)` |
| `Like` | Partial match | `findByNameLike(pattern)` |
| `IsNull` | NULL check | `findByDeletedAtIsNull()` |

### 2.5 Redis - In-Memory Cache

Redis is a memory-based Key-Value store. It stores data in memory rather than disk, making it extremely fast.

**Redis Usage in This Project:**

```
1. Token Blacklist: Stores logged-out JWT tokens
   KEY: "blacklist:eyJhbGciOi..."  VALUE: "true"  TTL: 30 minutes

2. OTP Code Storage: One-time authentication codes
   KEY: "otp:hong123"  VALUE: "482917"  TTL: 5 minutes

3. Rate Limiting: API call count limits
   KEY: "rate:192.168.1.1"  VALUE: "45"  TTL: 1 minute
```

**Essential Redis Commands (needed for debugging):**

```bash
# Connect to Redis CLI
redis-cli -h localhost -p 6370

# Look up a key
GET "blacklist:token-value-here"

# Check key list (Caution: Do NOT use KEYS * in production)
KEYS "otp:*"

# Check TTL (remaining expiration time, in seconds)
TTL "otp:hong123"

# Delete a key
DEL "blacklist:token-value-here"
```

### 2.6 Database Configuration by Environment

| Environment | DB | DDL Strategy | Purpose |
|------------|-----|-------------|---------|
| **dev** | H2 (in-memory) | `create` | Local development (tables auto-created at app startup) |
| **test** | H2 (in-memory) | `create-drop` | Testing (dropped after tests complete) |
| **prod** | PostgreSQL | `validate` | Production (only validates table structure, no modifications) |

**DDL Strategy Descriptions:**
- `create`: Drops existing tables and creates new ones at app startup (data loss)
- `create-drop`: create + drops tables at app shutdown
- `update`: ALTER TABLE only for changed entities (can be risky)
- `validate`: Only validates that DB schema matches entities (suitable for production)
- `none`: Does nothing

---

## 3. Backend WAS (Spring Boot)

### 3.1 What is Spring Boot?

Spring Boot is a Java-based web application framework. As a WAS (Web Application Server), it has Tomcat embedded, so you can run a server with just a single JAR file.

### 3.2 Core Architecture: Layered Structure

```
HTTP Request → Controller → Service → Repository → DB
HTTP Response ← Controller ← Service ← Repository ← DB
```

```
┌─────────────────────────────────────────────────┐
│  Controller (Presentation Layer)                 │
│  - Handles HTTP requests/responses               │
│  - Request data validation                       │
│  - Response format conversion                    │
├─────────────────────────────────────────────────┤
│  Service (Business Layer)                        │
│  - Core business logic                           │
│  - Transaction management (@Transactional)       │
│  - Combines multiple repositories                │
├─────────────────────────────────────────────────┤
│  Repository (Data Access Layer)                  │
│  - Executes DB CRUD                              │
│  - Spring Data JPA interfaces                    │
├─────────────────────────────────────────────────┤
│  Domain/Entity (Domain Layer)                    │
│  - Objects mapped to DB tables                   │
│  - Contains domain rules                         │
└─────────────────────────────────────────────────┘
```

### 3.3 Controller - API Endpoint Definition

```java
@RestController                    // Controller that returns JSON responses
@RequestMapping("/api/v1/dispatches")  // Base URL path
@RequiredArgsConstructor           // Auto-generates constructor for final fields (Lombok)
public class DispatchController {

    private final DispatchService dispatchService;  // DI (Dependency Injection)

    // GET /api/v1/dispatches?page=0&size=20&startDate=2026-01-01
    @GetMapping
    public ApiResponse<Page<DispatchResponse>> getDispatches(
            DispatchSearchCondition condition,  // Auto-maps query parameters
            Pageable pageable                   // Auto-maps pagination info
    ) {
        return ApiResponse.success(dispatchService.search(condition, pageable));
    }

    // POST /api/v1/dispatches (body: JSON)
    @PostMapping
    public ApiResponse<DispatchResponse> create(
            @Valid @RequestBody DispatchCreateRequest request  // JSON → object conversion + validation
    ) {
        return ApiResponse.success(dispatchService.create(request));
    }

    // PUT /api/v1/dispatches/123
    @PutMapping("/{id}")
    public ApiResponse<DispatchResponse> update(
            @PathVariable Long id,                              // Extract ID from URL
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

**Key Annotations Summary:**
| Annotation | Meaning |
|-----------|---------|
| `@RestController` | REST API controller (JSON responses) |
| `@RequestMapping` | URL path mapping |
| `@GetMapping` | Handles HTTP GET requests |
| `@PostMapping` | Handles HTTP POST requests |
| `@PutMapping` | Handles HTTP PUT requests |
| `@DeleteMapping` | Handles HTTP DELETE requests |
| `@PathVariable` | Extracts variable from URL path (`/users/{id}`) |
| `@RequestBody` | Converts HTTP body JSON to an object |
| `@Valid` | Executes Bean Validation |
| `@RequestParam` | Extracts query parameter (`?name=홍길동`) |

### 3.4 DTO - Data Transfer Object

DTO (Data Transfer Object) is a data structure used for API requests/responses. Instead of exposing the Entity directly, it transfers only the necessary data.

```java
// Request DTO: Client → Server
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

// Response DTO: Server → Client
public record DispatchResponse(
    Long dispatchId,
    Long vehicleId,
    String plateNumber,     // Related data fetched from Entity
    String itemType,
    String itemName,
    LocalDate dispatchDate,
    String dispatchStatus,
    LocalDateTime createdAt
) {
    // Entity → DTO conversion factory method
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

**Why Not Return the Entity Directly?**
1. **Security**: Sensitive information like passwords and internal IDs could be exposed
2. **Flexibility**: API specs and DB schema can be changed independently
3. **Circular Reference**: Bidirectional relationships between entities can cause infinite loops during JSON serialization

### 3.5 Bean Validation - Request Data Validation

```java
// Commonly used validation annotations
@NotNull                          // Cannot be null
@NotBlank                         // Cannot be null, empty string, or whitespace only
@NotEmpty                         // Cannot be null or empty string (whitespace allowed)
@Size(min = 3, max = 50)         // String length constraint
@Min(0)                          // Minimum value
@Max(100)                        // Maximum value
@Email                           // Email format
@Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$")  // Regex matching
@Past                            // Past dates only
@Future                          // Future dates only
```

### 3.6 Service - Business Logic

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // Default: read-only (optimized for SELECT)
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final VehicleRepository vehicleRepository;

    // Query: readOnly = true (default)
    public Page<DispatchResponse> search(
            DispatchSearchCondition condition, Pageable pageable) {
        return dispatchRepository.searchByCondition(condition, pageable)
                .map(DispatchResponse::from);
    }

    // Create: @Transactional disables read-only
    @Transactional   // readOnly = false → INSERT/UPDATE/DELETE enabled
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
- `readOnly = true`: Executes only SELECT, disables Hibernate Dirty Checking → improved performance
- `readOnly = false` (default): INSERT/UPDATE/DELETE enabled, Dirty Checking active

### 3.7 Exception Handling Pattern

```java
// ErrorCode enum: Manage all error codes in one place
public enum ErrorCode {
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "차량을 찾을 수 없습니다"),
    DUPLICATE_PLATE_NUMBER(HttpStatus.CONFLICT, "이미 등록된 차량번호입니다"),
    INVALID_WEIGHING_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 계량 상태입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다");

    private final HttpStatus status;
    private final String message;
}

// BusinessException: Exception thrown from business logic
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// GlobalExceptionHandler: Catches all exceptions and converts them to consistent responses
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

### 3.8 Standard API Response Format

```java
// All API responses follow this unified format
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
// Success response example
{
  "success": true,
  "data": {
    "dispatch_id": 1,
    "item_name": "철근",
    "dispatch_status": "REGISTERED"
  },
  "error": null
}

// Error response example
{
  "success": false,
  "data": null,
  "error": {
    "code": "VEHICLE_NOT_FOUND",
    "message": "차량을 찾을 수 없습니다"
  }
}
```

### 3.9 Lombok - Eliminating Boilerplate Code

```java
// Without Lombok (verbose code)
public class Vehicle {
    private Long vehicleId;
    private String plateNumber;

    public Vehicle() {}

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long id) { this.vehicleId = id; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String pn) { this.plateNumber = pn; }
}

// With Lombok (clean code)
@Getter                        // Generates getters for all fields
@NoArgsConstructor             // Default constructor
@AllArgsConstructor            // Constructor with all fields
@Builder                       // Builder pattern available
public class Vehicle {
    private Long vehicleId;
    private String plateNumber;
}

// Using the Builder pattern
Vehicle vehicle = Vehicle.builder()
    .vehicleId(1L)
    .plateNumber("12가3456")
    .build();
```

| Annotation | Generated Code |
|-----------|---------------|
| `@Getter` | Getters for all fields |
| `@Setter` | Setters for all fields |
| `@NoArgsConstructor` | No-argument constructor |
| `@AllArgsConstructor` | Constructor with all fields as parameters |
| `@RequiredArgsConstructor` | Constructor with only final fields as parameters |
| `@Builder` | Builder pattern |
| `@ToString` | toString() |
| `@EqualsAndHashCode` | equals(), hashCode() |
| `@Data` | Getter + Setter + ToString + EqualsAndHashCode + RequiredArgs |

### 3.10 Dependency Injection (DI)

This is the most core concept of Spring. Instead of creating objects directly, Spring creates and injects them for you.

```java
// ❌ Bad example: Direct creation
public class DispatchService {
    private DispatchRepository repo = new DispatchRepository();  // Direct creation
}

// ✅ Good example: Spring injection
@Service
@RequiredArgsConstructor
public class DispatchService {
    private final DispatchRepository repo;  // Spring automatically injects
}
```

**Why Use DI?**
- Easy to replace with Mock objects during testing
- No need to modify code when changing implementations
- Spring manages the lifecycle of objects

---

## 4. Frontend (React + TypeScript)

### 4.1 What is React?

React is a JavaScript library for building user interfaces (UI). It is **component-based**, dividing the UI into small pieces that are assembled together.

### 4.2 TypeScript Basics

TypeScript is a language that adds a **type system** to JavaScript. It enables catching errors at the time of code writing.

```typescript
// JavaScript (no types, errors found at runtime)
function addUser(user) {
    console.log(user.name);  // No way to know if user has a name property
}

// TypeScript (with types, errors found at code writing time)
interface User {
    userId: number;
    loginId: string;
    userName: string;
    phoneNumber: string;
    userRole: 'ADMIN' | 'MANAGER' | 'DRIVER';  // Union type: only these 3 values allowed
    isActive: boolean;
}

function addUser(user: User): void {
    console.log(user.userName);  // IDE provides auto-completion
    console.log(user.address);   // ❌ Compile error! address doesn't exist in User
}
```

**Commonly Used Types:**

```typescript
// Basic types
let name: string = '홍길동';
let age: number = 25;
let isActive: boolean = true;
let data: null = null;
let value: undefined = undefined;

// Arrays
let ids: number[] = [1, 2, 3];
let names: string[] = ['홍길동', '김철수'];

// Objects (interface)
interface Vehicle {
    vehicleId: number;
    plateNumber: string;
    maxLoadWeight?: number;  // ? = optional field (may not exist)
}

// Generics: Use types like variables
interface ApiResponse<T> {
    success: boolean;
    data: T;
    error: { code: string; message: string } | null;
}

// Usage
const response: ApiResponse<User[]> = await api.get('/users');
//   The compiler knows response.data is of type User[]
```

### 4.3 React Functional Components

```tsx
// React.FC: Functional component type
const UserCard: React.FC<{ user: User }> = ({ user }) => {
    return (
        <div>
            <h3>{user.userName}</h3>
            <p>{user.phoneNumber}</p>
        </div>
    );
};

// Usage
<UserCard user={userData} />
```

### 4.4 React Hooks - Core Concepts

#### useState - State Management

```tsx
const [count, setCount] = useState(0);
//     value  setter function  initial value

// Changing value → component re-renders
setCount(5);           // count changes to 5, UI updates
setCount(prev => prev + 1);  // Change based on previous value (safe approach)
```

**Actual Project Example (DispatchPage.tsx):**

```tsx
const DispatchPage: React.FC = () => {
    // Data state
    const [data, setData] = useState<Dispatch[]>([]);       // Dispatch list
    const [loading, setLoading] = useState(false);           // Loading state
    const [searched, setSearched] = useState(false);         // Search performed
    const [totalElements, setTotalElements] = useState(0);   // Total count
    const [currentPage, setCurrentPage] = useState(1);       // Current page

    // Modal state
    const [createModalOpen, setCreateModalOpen] = useState(false);
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [editingRecord, setEditingRecord] = useState<Dispatch | null>(null);

    // ...
};
```

#### useEffect - Side Effect Handling

```tsx
// Execute when the component appears on screen (mount)
useEffect(() => {
    fetchData();           // API call
}, []);                    // Empty array: execute only once initially

// Execute when a specific value changes
useEffect(() => {
    fetchData(searchKeyword);  // Execute every time searchKeyword changes
}, [searchKeyword]);           // Dependency array

// Cleanup function: Execute when the component is removed
useEffect(() => {
    const ws = new WebSocket('ws://...');
    ws.onmessage = handleMessage;

    return () => {
        ws.close();  // Close WebSocket connection when component unmounts
    };
}, []);
```

#### useCallback - Function Memoization

```tsx
// Problem: fetchData function is recreated every time the component re-renders
// → Adding it to useEffect's dependency array can cause infinite loops

// Solution: Memoize the function with useCallback
const fetchData = useCallback(async (keyword?: string) => {
    setLoading(true);
    try {
        const res = await apiClient.get('/master/companies', { params });
        setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
}, []);  // Empty dependency array, so the function is created only once

// Now safe to use in useEffect
useEffect(() => { fetchData(); }, [fetchData]);
```

#### Form.useForm - Ant Design Form Management

```tsx
const [form] = Form.useForm();   // Create form instance

// Set form values (when opening edit modal)
form.setFieldsValue({
    plateNumber: record.plateNumber,
    vehicleType: record.vehicleType,
});

// Get values after form validation
const values = await form.validateFields();  // Throws exception on validation failure

// Reset form
form.resetFields();
```

### 4.5 Ant Design - UI Components

Main Ant Design components used in this project:

```tsx
import {
    Button,       // Button
    Input,        // Text input
    Select,       // Dropdown select
    DatePicker,   // Date picker
    Form,         // Form (with validation)
    Table,        // Data table
    Modal,        // Modal dialog
    message,      // Notification message (toast)
    Tag,          // Tag/label
    Space,        // Spacing component
    Popconfirm,   // Confirmation popup
    Card,         // Card layout
    Typography,   // Text styling
    Pagination,   // Pagination
    Switch,       // Toggle switch
    Tabs,         // Tab menu
} from 'antd';
```

**Form + Validation Example:**

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

    {/* Cross-validation: maxLoadWeight > defaultTareWeight */}
    <Form.Item
        name="maxLoadWeight"
        label="최대 적재중량"
        dependencies={['defaultTareWeight']}  // Re-validate when this field changes
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

### 4.6 Axios - HTTP Client

```typescript
// api/client.ts - Axios instance configuration
import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api/v1',
    timeout: 10000,
});

// Request interceptor: Automatically attach JWT token to all requests
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Response interceptor: Refresh token on 401 error
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            // accessToken expired → attempt renewal with refreshToken
            const refreshToken = localStorage.getItem('refreshToken');
            const res = await axios.post('/api/v1/auth/refresh', { refreshToken });
            localStorage.setItem('accessToken', res.data.data.accessToken);

            // Retry the original request
            error.config.headers.Authorization = `Bearer ${res.data.data.accessToken}`;
            return apiClient(error.config);
        }
        return Promise.reject(error);
    }
);
```

**API Call Patterns:**

```typescript
// GET request (query)
const res = await apiClient.get('/dispatches', {
    params: { page: 0, size: 20, startDate: '2026-01-01' }
});
const dispatches: Dispatch[] = res.data.data.content;

// POST request (create)
await apiClient.post('/dispatches', {
    vehicleId: 1,
    itemName: '철근',
    dispatchDate: '2026-01-29'
});

// PUT request (update)
await apiClient.put(`/dispatches/${id}`, updatedData);

// DELETE request (delete)
await apiClient.delete(`/dispatches/${id}`);
```

### 4.7 React Router - Client-Side Routing

```tsx
// Handles page transitions in SPA (Single Page Application)
// Does not actually load a new page; only swaps components

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

<BrowserRouter>
    <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<MainLayout />}>  {/* Shared layout */}
            <Route index element={<DashboardPage />} />
            <Route path="dispatch" element={<DispatchPage />} />
            <Route path="weighing" element={<WeighingPage />} />
            <Route path="master/vehicles" element={<MasterVehiclePage />} />
            <Route path="admin/users" element={<AdminUserPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" />} />  {/* 404 → home */}
    </Routes>
</BrowserRouter>
```

### 4.8 Context API - Global State Management

```tsx
// Theme Context example
interface ThemeContextType {
    isDark: boolean;
    toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType>({
    isDark: false,
    toggleTheme: () => {},
});

// Provider: Provides state at the top level
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

// In the consuming component:
const { isDark, toggleTheme } = useContext(ThemeContext);
```

### 4.9 JSON Naming Conversion (camelCase ↔ snake_case)

```
Frontend (JavaScript):  camelCase   → plateNumber, dispatchDate
Backend (Java/JSON):    snake_case  → plate_number, dispatch_date
```

Axios interceptors handle the automatic conversion:
- **On request**: camelCase → snake_case
- **On response**: snake_case → camelCase

This allows the frontend and backend to maintain their respective naming conventions while communicating.

---

## 5. Mobile App (Flutter)

### 5.1 What is Flutter?

Flutter is a cross-platform UI framework created by Google. You can build iOS, Android, and Web apps with **a single codebase**. It uses the Dart programming language.

### 5.2 Dart Basic Syntax

```dart
// Variable declaration
String name = '홍길동';           // Explicit type
var age = 25;                    // Type inference
final loginId = 'hong123';       // Runtime constant (cannot change after assignment)
const pi = 3.14;                 // Compile-time constant

// Null Safety (Dart 3.x)
String? nullableName;            // ? = can be null
String nonNullName = '홍길동';    // Cannot be null

nullableName?.length;            // ?. = don't call if null
nullableName ?? '이름없음';       // ?? = use default value if null
nullableName!;                   // ! = assert non-null (dangerous)

// Asynchronous programming
Future<List<Dispatch>> fetchDispatches() async {
    final response = await dio.get('/dispatches');
    return response.data.map((json) => Dispatch.fromJson(json)).toList();
}

// Classes
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

### 5.3 Widget - Flutter's Basic Unit

In Flutter, every element on the screen is a Widget. It is similar to React's Component.

```dart
// StatelessWidget: Widget without state (static UI)
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

// StatefulWidget: Widget with state (dynamic UI)
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
        _fetchData();        // Load data on screen initialization
    }

    Future<void> _fetchData() async {
        setState(() => _isLoading = true);   // Same as React's setLoading(true)
        // ... API call ...
        setState(() => _isLoading = false);  // Update screen
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

### 5.4 Provider - State Management

Provider is a lightweight state management pattern for Flutter. It is similar to React Context.

```dart
// State class (extends ChangeNotifier)
class AuthProvider extends ChangeNotifier {
    User? _currentUser;
    bool _isAuthenticated = false;

    User? get currentUser => _currentUser;
    bool get isAuthenticated => _isAuthenticated;

    Future<void> login(String loginId, String password) async {
        final response = await apiService.login(loginId, password);
        _currentUser = response.user;
        _isAuthenticated = true;
        notifyListeners();  // Same as React's setState() → UI update
    }

    void logout() {
        _currentUser = null;
        _isAuthenticated = false;
        notifyListeners();
    }
}

// Register Provider at the app's top level
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

// Use state in widgets
class ProfileScreen extends StatelessWidget {
    @override
    Widget build(BuildContext context) {
        // Read state from Provider
        final auth = Provider.of<AuthProvider>(context);
        // or
        final auth = context.watch<AuthProvider>();  // Rebuilds on change
        final auth = context.read<AuthProvider>();   // Does not rebuild on change

        return Text('안녕하세요, ${auth.currentUser?.userName}');
    }
}
```

### 5.5 Go Router - Navigation

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
            return '/login';  // Not authenticated → redirect to login page
        }
        return null;  // Proceed normally
    },
);
```

### 5.6 Dio - HTTP Client

```dart
// HTTP client similar to Axios
class ApiService {
    late final Dio _dio;

    ApiService() {
        _dio = Dio(BaseOptions(
            baseUrl: ApiConfig.baseUrl,     // 'http://..../api/v1'
            connectTimeout: const Duration(seconds: 10),
        ));

        // Interceptor: Automatically attach token to all requests
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

### 5.7 React vs Flutter Comparison

| Concept | React | Flutter |
|---------|-------|---------|
| Basic unit | Component | Widget |
| State management | useState | setState / Provider |
| Lifecycle | useEffect | initState / dispose |
| Global state | Context API | Provider / Riverpod |
| Routing | React Router | Go Router |
| HTTP | Axios | Dio |
| Styling | CSS / Styled | Widget properties |
| List rendering | `.map()` | `ListView.builder()` |
| Conditional rendering | `{condition && <Widget>}` | `if (condition) Widget()` |

---

## 6. Desktop Program (C# .NET WinForms)

### 6.1 Role

This is a program that runs on a Windows PC installed at the on-site weighing station.

- Receives real-time weight data from the scale via **serial port (COM)** communication
- Displays weighing results on the display board
- Controls barriers (open/close)
- Caches data in SQLite when offline

### 6.2 Core Components

```csharp
// Serial port communication (scale connection)
using System.IO.Ports;

var port = new SerialPort("COM1", 9600, Parity.None, 8, StopBits.One);
port.DataReceived += (sender, e) => {
    string data = port.ReadLine();
    decimal weight = ParseWeight(data);  // "  1250.5kg" → 1250.5
    UpdateDisplay(weight);
};
port.Open();

// SQLite local cache (offline fallback)
using System.Data.SQLite;

var conn = new SQLiteConnection("Data Source=weighing_cache.db");
conn.Open();
var cmd = new SQLiteCommand("INSERT INTO cache (data, synced) VALUES (@d, 0)", conn);
cmd.Parameters.AddWithValue("@d", jsonData);
cmd.ExecuteNonQuery();

// REST API call (backend integration)
using var httpClient = new HttpClient();
httpClient.DefaultRequestHeaders.Authorization =
    new AuthenticationHeaderValue("Bearer", token);

var response = await httpClient.PostAsync(
    "http://server/api/v1/weighing",
    new StringContent(json, Encoding.UTF8, "application/json")
);
```

### 6.3 Configuration File (appsettings.json)

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

---

## 7. Authentication and Security (JWT + Spring Security)

### 7.1 What is JWT (JSON Web Token)?

JWT is a **digital ID card** issued to the client. It allows the server to identify users without maintaining sessions.

```
JWT Structure:
xxxxx.yyyyy.zzzzz
  │      │      │
  │      │      └─ Signature: Prevents tampering
  │      └─ Payload: User info, expiration time, etc.
  └─ Header: Algorithm info
```

```json
// Payload example (when Base64 decoded)
{
  "sub": "hong123",         // User ID
  "role": "MANAGER",        // User role
  "iat": 1737100000,        // Issued at
  "exp": 1737101800         // Expiration time (30 minutes later)
}
```

### 7.2 Authentication Flow

```
1. Login
   Client → POST /auth/login { loginId, password }
   Server → { accessToken: "eyJ...", refreshToken: "eyJ..." }

2. API Request (authentication required)
   Client → GET /dispatches
                Headers: { Authorization: "Bearer eyJ..." }
   Server → JWT verification → valid → return data

3. Token Expiration (after 30 minutes)
   Client → GET /dispatches → 401 Unauthorized
   Client → POST /auth/refresh { refreshToken: "eyJ..." }
   Server → new { accessToken: "eyJ..." }
   Client → GET /dispatches (retry with new token)

4. Logout
   Client → POST /auth/logout
   Server → Register accessToken in Redis blacklist
            (subsequent requests with that token are rejected)
```

### 7.3 Spring Security Filter Chain

```
HTTP Request
    │
    ▼
┌─────────────────────┐
│ CORS Filter          │  → Handles CORS headers
├─────────────────────┤
│ JWT Authentication   │  → Extracts token from Authorization header
│ Filter               │  → Validates token (signature, expiration, blacklist)
│                      │  → If valid, stores user info in SecurityContext
├─────────────────────┤
│ Authorization Filter │  → Checks if user has access to the API
│                      │  → DRIVER accessing ADMIN-only API → 403
└─────────────────────┘
    │
    ▼
  Controller → Service → Repository → DB
```

### 7.4 Role-Based Access Control

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            // Accessible without authentication
            .requestMatchers("/api/v1/auth/**").permitAll()

            // ADMIN only
            .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

            // ADMIN or MANAGER only
            .requestMatchers("/api/v1/master/**").hasAnyRole("ADMIN", "MANAGER")

            // Everything else requires authentication
            .anyRequest().authenticated()
        );
    }
}
```

| Role | Accessible Features |
|------|-------------------|
| `ADMIN` | Full management (users, master data, audit logs, etc.) |
| `MANAGER` | Dispatch, weighing, gate pass, master data management |
| `DRIVER` | View assigned dispatches, my page |

### 7.5 Password Encryption

```java
// BCrypt: One-way hashing (cannot be decrypted)
// Same password generates different hash values each time (includes salt)

passwordEncoder.encode("myPassword123");
// → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

passwordEncoder.matches("myPassword123", encodedPassword);  // true
passwordEncoder.matches("wrongPassword", encodedPassword);  // false
```

---

## 8. Real-Time Communication (WebSocket / STOMP)

### 8.1 What is WebSocket?

HTTP follows a request-response model, but WebSocket enables **bidirectional real-time communication**.

```
HTTP (unidirectional):
Client → "Any new data?" → Server
Client ← "No"             ← Server
Client → "How about now?"  → Server
Client ← "No"             ← Server
Client → "How about now?"  → Server
Client ← "Yes! Here it is" ← Server

WebSocket (bidirectional):
Client ←→ Connection established ←→ Server
(Server pushes immediately when new data is available)
Server → "Weighing complete! 2,450kg"  → Client
Server → "New dispatch registered"      → Client
```

### 8.2 STOMP Protocol

STOMP (Simple Text Oriented Messaging Protocol) is a messaging protocol that operates on top of WebSocket. It provides a subscribe/publish pattern.

```
Server Configuration:
  /ws           → WebSocket connection endpoint
  /topic/*      → Server → Client broadcast (subscription)
  /app/*        → Client → Server message sending
```

### 8.3 Frontend WebSocket Connection

```typescript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),  // SockJS fallback
    reconnectDelay: 5000,                        // Reconnect after 5 seconds if disconnected

    onConnect: () => {
        console.log('WebSocket connected');

        // Subscribe: Receive weighing status updates
        client.subscribe('/topic/weighing-updates', (message) => {
            const data = JSON.parse(message.body);
            // data = { weighingId: 1, status: 'COMPLETED', weight: 2450.5 }
            updateWeighingStatus(data);
        });

        // Subscribe: Receive equipment status changes
        client.subscribe('/topic/equipment-status', (message) => {
            const data = JSON.parse(message.body);
            updateEquipmentDisplay(data);
        });
    },

    onDisconnect: () => {
        console.log('WebSocket disconnected');
    },
});

client.activate();  // Start connection

// Send message (Client → Server)
client.publish({
    destination: '/app/weighing-command',
    body: JSON.stringify({ action: 'START', scaleId: 1 }),
});
```

### 8.4 Backend WebSocket Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // SockJS fallback (for browsers that don't support WebSocket)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");    // Subscription path prefix
        registry.setApplicationDestinationPrefixes("/app");  // Send path prefix
    }
}

// Publish messages from service
@Service
@RequiredArgsConstructor
public class WeighingService {
    private final SimpMessagingTemplate messagingTemplate;

    public void completeWeighing(Long weighingId) {
        // ... business logic ...

        // Broadcast to all subscribers
        messagingTemplate.convertAndSend("/topic/weighing-updates",
            new WeighingUpdateMessage(weighingId, "COMPLETED", weight));
    }
}
```

---

## 9. Build and Deployment (Vite, Gradle, Vercel, Railway)

### 9.1 Frontend Build (Vite)

```bash
# Start development server (HMR: instantly reflects code changes)
npm run dev    # → http://localhost:3000

# Production build
npm run build  # → tsc (type check) && vite build → generates dist/ folder
```

**vite.config.ts Key Configuration:**

```typescript
export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        proxy: {
            // Proxy /api requests to backend during development
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
            // WebSocket proxy
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

### 9.2 Backend Build (Gradle)

```bash
# Build (generate JAR)
./gradlew build           # → build/libs/weighing-0.0.1-SNAPSHOT.jar

# Run tests
./gradlew test

# Execute
java -jar build/libs/weighing-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
# or
./gradlew bootRun         # During development
```

### 9.3 Deployment Architecture

```
GitHub (push to main branch)
    │
    ├──→ Vercel (Frontend auto-deployment)
    │    ├── Runs npm run build
    │    ├── Hosts dist/ static files
    │    └── API proxy configuration (vercel.json)
    │         /api/* → Railway backend
    │         /ws/*  → Railway backend
    │
    └──→ Railway (Backend auto-deployment)
         ├── Runs Gradle build
         ├── Executes JAR file
         ├── PostgreSQL (managed instance)
         └── Redis (managed instance)
```

### 9.4 Environment Variable Management

```yaml
# Backend: application-prod.yml (injected via Railway environment variables)
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
  secret: ${JWT_SECRET}       # NEVER hardcode this!
```

**Environment Variable Management Principles:**
- Passwords, API keys, JWT secrets, etc. must **NEVER** be put in code
- `.env` files should be added to `.gitignore`
- Use environment variable features of Railway, Vercel, etc.

---

## 10. Development Environment Setup

### 10.1 Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| JDK | 17+ | Backend execution |
| Node.js | 18+ | Frontend execution |
| Git | Latest | Version control |
| IntelliJ IDEA | Latest | Backend IDE |
| VS Code | Latest | Frontend IDE |
| Flutter SDK | 3.10+ | Mobile development |
| Android Studio | Latest | Android emulator |

### 10.2 Running Backend Locally

```bash
cd backend

# Run with dev profile (H2 in-memory DB + embedded Redis)
./gradlew bootRun

# Or in IntelliJ:
# 1. Open WeighingApplication.java
# 2. Click ▶ button next to main()
# 3. Set Environment: spring.profiles.active=dev

# Check Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### 10.3 Running Frontend Locally

```bash
cd frontend

# Install dependencies (first time or when package.json changes)
npm install

# Start development server
npm run dev    # → http://localhost:3000

# Type check + production build
npm run build
```

### 10.4 Running Mobile App Locally

```bash
cd mobile

# Install dependencies
flutter pub get

# Run on emulator
flutter run

# Enable mock data: lib/config/api_config.dart
# static const bool useMockData = true;
```

### 10.5 Recommended VS Code Extensions

| Extension | Purpose |
|-----------|---------|
| ESLint | JavaScript/TypeScript code analysis |
| Prettier | Code formatting |
| TypeScript Importer | Auto-add imports |
| ES7+ React Snippets | React code snippets |
| Dart | Dart language support |
| Flutter | Flutter development tools |

### 10.6 Recommended IntelliJ Plugins

| Plugin | Purpose |
|--------|---------|
| Lombok | Lombok annotation support |
| Spring Boot Assistant | Configuration auto-completion |
| Database Tools | DB browser |
| GitToolBox | Git status display |

---

## 11. Code Conventions and Patterns

### 11.1 Naming Rules

| Target | Convention | Example |
|--------|-----------|---------|
| **Java class** | PascalCase | `DispatchService`, `WeighingController` |
| **Java method/variable** | camelCase | `findByPlateNumber`, `dispatchDate` |
| **Java constant** | UPPER_SNAKE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| **Java package** | lowercase | `com.dongkuk.weighing.dispatch` |
| **DB table/column** | snake_case | `weighing_records`, `plate_number` |
| **React component** | PascalCase | `DispatchPage`, `SortableTable` |
| **React file** | PascalCase.tsx | `DispatchPage.tsx`, `MyPage.tsx` |
| **TypeScript variable/function** | camelCase | `handleSubmit`, `fetchData` |
| **TypeScript interface** | PascalCase | `Dispatch`, `ApiResponse<T>` |
| **CSS class** | kebab-case | `main-layout`, `search-bar` |
| **Flutter class** | PascalCase | `DispatchListScreen` |
| **Flutter file** | snake_case.dart | `dispatch_list_screen.dart` |
| **API endpoint** | kebab-case | `/gate-passes`, `/weighing-records` |
| **JSON field** | snake_case | `dispatch_date`, `plate_number` |

### 11.2 Git Commit Messages

```
<type>: <description>

type:
  feat:     New feature
  fix:      Bug fix
  refactor: Refactoring (no functionality change)
  docs:     Documentation changes
  style:    Code style (formatting, etc.)
  test:     Add/modify tests
  chore:    Build configuration, packages, etc.

Examples:
  feat: 배차 검색 필터 기능 추가
  fix: 토큰 갱신 시 무한 루프 수정
  refactor: 계량 서비스 교차 검증 로직 분리
```

### 11.3 API Design Rules

```
HTTP Method Selection Criteria:
  GET    → Query data (no changes)
  POST   → Create data (add new resource)
  PUT    → Modify data (full update)
  PATCH  → Modify data (partial update)
  DELETE → Delete data

URL Design:
  GET    /api/v1/dispatches           → Query dispatch list
  GET    /api/v1/dispatches/123       → Query dispatch detail
  POST   /api/v1/dispatches           → Create dispatch
  PUT    /api/v1/dispatches/123       → Update dispatch
  DELETE /api/v1/dispatches/123       → Delete dispatch
  PUT    /api/v1/dispatches/123/cancel → Cancel dispatch (status change)

Notes:
  ✅ /api/v1/dispatches       (plural)
  ❌ /api/v1/dispatch          (singular)
  ✅ /api/v1/gate-passes      (kebab-case)
  ❌ /api/v1/gatePasses        (camelCase)
```

### 11.4 Error Handling Patterns

```
Backend:
  - Business error → throw new BusinessException(ErrorCode.XXXX)
  - Input validation failure → @Valid + MethodArgumentNotValidException (automatic)
  - GlobalExceptionHandler converts to consistent JSON response

Frontend:
  - Wrap API calls in try-catch
  - Success: message.success('저장되었습니다')
  - Failure: message.error(error message) or display server error message
  - Form validation failure: Ant Design Form automatically displays error messages
```

---

## 12. Common Mistakes and Precautions

### 12.1 Backend

| Mistake | Description | Solution |
|---------|-------------|----------|
| N+1 Problem | Repeatedly querying related entities causes massive queries | Use `@EntityGraph` or `JOIN FETCH` |
| LazyInitializationException | Accessing lazy-loaded object outside transaction | Convert to DTO with only needed data |
| Missing `@Transactional` | Data change with read-only transaction | Add `@Transactional` to CUD methods in Service |
| Returning Entity directly | Circular reference, security info exposure | Convert to DTO before returning |
| Storing passwords in plain text | Security vulnerability | Hash with BCrypt before storing |
| Hardcoding JWT Secret | Security vulnerability | Inject externally via environment variables |

**N+1 Problem Example:**

```java
// ❌ Code that causes N+1 problem
List<Dispatch> dispatches = dispatchRepository.findAll();  // 1 query
for (Dispatch d : dispatches) {
    d.getVehicle().getPlateNumber();  // Additional query for each dispatch!
}
// Result: 101 queries for 100 dispatches (1 + 100)

// ✅ Solved with JOIN FETCH
@Query("SELECT d FROM Dispatch d JOIN FETCH d.vehicle")
List<Dispatch> findAllWithVehicle();  // Solved with a single JOIN query
```

### 12.2 Frontend

| Mistake | Description | Solution |
|---------|-------------|----------|
| useEffect infinite loop | Objects/functions recreated each render in dependency array | Use `useCallback`, `useMemo` |
| Direct state mutation | Modifying state directly like `state.push(item)` | `setState([...state, item])` create new array |
| Missing key prop | No key specified in list rendering | `<Component key={item.id} />` |
| Unhandled token expiration | User must manually re-login on 401 error | Auto-refresh in Axios interceptor |
| Memory leak | setState called after unmount | Clean up in useEffect cleanup function |
| Overuse of `any` type | Loses benefits of TypeScript | Define explicit types or interfaces |

**State Mutation Mistake:**

```tsx
// ❌ Never modify directly (React cannot detect the change)
const [items, setItems] = useState<string[]>([]);
items.push('새 아이템');         // Direct modification → UI doesn't update

// ✅ Create a new array and call setState
setItems([...items, '새 아이템']);    // Copy with spread operator + add
setItems(prev => [...prev, '새 아이템']);  // Based on previous value (safer)

// Delete
setItems(prev => prev.filter(item => item !== '삭제할 아이템'));

// Update
setItems(prev => prev.map(item =>
    item.id === targetId ? { ...item, name: '새이름' } : item
));
```

### 12.3 Flutter

| Mistake | Description | Solution |
|---------|-------------|----------|
| Excessive setState calls | Unnecessary rebuilds | Separate state with Provider |
| Using BuildContext in async | Error when using context in async function | Check `mounted` before use |
| Missing serialization | Forgot JSON → model conversion code | Implement `fromJson` factory method |

### 12.4 Common

| Mistake | Description | Solution |
|---------|-------------|----------|
| Committing `.env` file | Passwords, API keys pushed to Git | Add to `.gitignore` |
| CORS error | Frontend-backend domain mismatch | Check backend CORS configuration |
| Timezone mismatch | UTC vs KST confusion | Store UTC on server, convert to KST for display |
| Case sensitivity issue | Windows is case-insensitive, Linux is case-sensitive | Use consistent naming |

---

## Appendix: Key Terms Glossary

| Term | Description |
|------|-------------|
| **REST API** | API design approach that operates on resources using HTTP methods (GET/POST/PUT/DELETE) |
| **SPA** | Single Page Application. A web app that swaps components without page transitions |
| **ORM** | Object-Relational Mapping. Technology that maps objects to DB tables (JPA) |
| **DTO** | Data Transfer Object. Data structure used for API requests/responses |
| **DI** | Dependency Injection. Pattern of receiving objects from external sources instead of creating them directly |
| **JWT** | JSON Web Token. Authentication token issued by the server |
| **STOMP** | Messaging protocol on top of WebSocket (Pub/Sub pattern) |
| **HMR** | Hot Module Replacement. Reflects code changes without browser refresh |
| **CORS** | Cross-Origin Resource Sharing. Configuration to allow API calls from different domains |
| **HikariCP** | JDBC connection pool library (improves performance by reusing DB connections) |
| **FCM** | Firebase Cloud Messaging. Google's push notification service |
| **Dirty Checking** | JPA feature that auto-detects Entity changes and generates UPDATE queries |
| **Bean** | Object managed by Spring (Controller, Service, Repository, etc.) |
| **Profile** | Spring's environment-specific configuration separation feature (dev, prod, test) |
| **Interceptor** | Middleware that intercepts requests/responses for common processing (used in both Axios and Spring) |

---

## 13. Recently Added Frontend Patterns

### 13.1 Page Registry (pageRegistry.ts)

All pages are centrally managed in `config/pageRegistry.ts`. When adding a new page, just modify this file and the sidebar menu, tab navigation, and permission control will be automatically applied.

```typescript
// config/pageRegistry.ts
export interface PageConfig {
  component: React.LazyExoticComponent<React.FC>; // React.lazy code splitting
  title: string;        // Menu/tab display title
  icon: React.ReactNode; // Menu icon (Ant Design Icons)
  closable: boolean;     // Whether the tab can be closed
  roles?: ('ADMIN' | 'MANAGER' | 'DRIVER')[]; // Roles that can access
}

// Example of adding a new page
const NewPage = React.lazy(() => import('../pages/NewPage'));

export const PAGE_REGISTRY: Record<string, PageConfig> = {
  '/new-page': {
    component: NewPage,
    title: '새 페이지',
    icon: React.createElement(SomeIcon),
    closable: true,
    roles: ['ADMIN', 'MANAGER'], // Omit for unrestricted access
  },
  // ...existing pages
};
```

**Key Points:**
- `React.lazy` for code splitting → JS bundle loaded only when visiting the page
- `closable: false` is used for pinned tabs like the weighing station control
- Adding to the `PINNED_TABS` array opens tabs automatically at app startup
- `MAX_TABS = 10` limits the maximum number of tabs

### 13.2 Authentication Context (AuthContext.tsx)

`AuthContext` manages the global authentication state. It maintains login state based on tokens in `localStorage` and handles token refresh automatically.

```tsx
// Usage
import { useAuth } from '../context/AuthContext';

const MyComponent: React.FC = () => {
    const { user, isAuthenticated, logout } = useAuth();

    if (!isAuthenticated) return <Navigate to="/login" />;

    return <div>안녕하세요, {user?.userName}님</div>;
};
```

### 13.3 CRUD State Management Hook (useCrudState.ts)

Master data pages (companies, vehicles, scales, common codes) all follow the same CRUD pattern. `useCrudState` reuses this pattern.

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

### 13.4 MasterCrudPage Common Component

A common component that standardizes the layout and behavior of master data CRUD pages. It automatically configures search forms, data tables, and create/edit modals.

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

### 13.5 API Call Hook (useApiCall.ts)

A hook that automatically manages loading/success/error states for API calls.

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

### 13.6 Keyboard Shortcuts (useKeyboardShortcuts.ts)

A hook that registers page-specific keyboard shortcuts. Automatically deregistered when the component unmounts.

```tsx
useKeyboardShortcuts([
    { key: 'n', ctrl: true, handler: () => setCreateModalOpen(true), description: '신규 등록' },
    { key: 'f', ctrl: true, handler: () => searchInputRef.current?.focus(), description: '검색' },
    { key: 'Escape', handler: () => setModalOpen(false), description: '모달 닫기' },
]);
```

### 13.7 WebSocket Hook (useWebSocket.ts)

A hook that manages STOMP protocol-based WebSocket connections. Handles connection/reconnection/subscription automatically.

```tsx
const { connected, subscribe, publish } = useWebSocket({
    url: '/ws',
    onConnect: () => console.log('WebSocket 연결됨'),
});

// Subscribe
useEffect(() => {
    if (connected) {
        subscribe('/topic/weighing-updates', (message) => {
            const data = JSON.parse(message.body);
            updateWeighingStatus(data);
        });
    }
}, [connected]);
```

### 13.8 Weighing Station Control Architecture

`WeighingStationPage` is a dedicated page for real-time monitoring of the on-site weighing station. It consists of multiple sub-components:

```
WeighingStationPage
├── ConnectionStatusBar    → Equipment connection status (indicator, LPR, display board, barrier)
├── WeightDisplay          → Real-time weight display (large numbers, stable/unstable status)
├── VehicleInfoPanel       → Currently weighing vehicle/dispatch information
├── ProcessStateBar        → Weighing progress stage display (entry→detection→capture→recognition→weighing→complete)
├── ActionButtons          → Action buttons for start/complete/cancel weighing
├── ModeToggle             → Auto/manual mode toggle
├── ManualControls         → Manual control panel for manual mode
├── WeighingHistoryTable   → Recent weighing history table
├── StatusLog              → Equipment/system event log
└── SimulatorPanel         → Development hardware simulator
```

**Data Flow:**
- `useWeighingStation` hook: Weighing business logic (state management, API calls)
- `useWeighingStationSocket` hook: WebSocket real-time data reception
- `weighingStationApi.ts`: Weighing station-specific REST API calls

### 13.9 Dashboard Tab Structure

`DashboardPage` consists of 3 tabs:

| Tab | Component | Content |
|-----|-----------|---------|
| Overview | `OverviewTab` | KPI cards (AnimatedNumber), daily trend charts, item ratio |
| Real-time | `RealtimeTab` | WebSocket-based real-time weighing status, scale status |
| Analysis | `AnalysisTab` | Detailed statistical charts, period/condition analysis |

### 13.10 ECharts Configuration

ECharts 6.0 requires manual registration of only needed components for tree-shaking:

```typescript
// utils/echartsSetup.ts - Called once at app startup
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);
```

```typescript
// utils/chartOptions.ts - Common chart options
export const createLineChartOption = (data: DailyStatistics[]) => ({
    // ... standardized chart options
});
```

---

## 14. Recently Added Mobile Patterns

### 14.1 Offline Cache Service

`offline_cache_service.dart` caches essential data locally using SharedPreferences. Basic information can be displayed even when the network is unstable.

```dart
class OfflineCacheService {
    // Cache dispatch list
    Future<void> cacheDispatches(List<Dispatch> dispatches);
    Future<List<Dispatch>?> getCachedDispatches();

    // Check cache expiration
    bool isCacheExpired(String key, {Duration maxAge = const Duration(hours: 1)});
}
```

**Note**: SharedPreferences is suitable for small amounts of data. For large data volumes, consider using SQLite.

### 14.2 Toast Utility

`utils/toast_utils.dart` standardizes SnackBar-based notifications:

```dart
ToastUtils.showSuccess(context, '배차가 등록되었습니다.');
ToastUtils.showError(context, '네트워크 오류가 발생했습니다.');
ToastUtils.showWarning(context, 'OTP가 곧 만료됩니다.');
```

### 14.3 Mobile Screen Structure

```
Home (HomeScreen)
├── Dispatch List (DispatchListScreen)
│   └── Dispatch Detail (DispatchDetailScreen)
├── Weighing
│   ├── OTP Input (OtpInputScreen)
│   └── Weighing Progress (WeighingProgressScreen)
├── Electronic Weighing Slip
│   ├── List (SlipListScreen)
│   └── Detail (SlipDetailScreen)
├── History (HistoryScreen)
├── Notices (NoticeScreen)
├── Notifications (NotificationListScreen)
└── OTP Login (OtpLoginScreen)
```

---

## 15. Recently Added Desktop Patterns

### 15.1 Splash Form

`SplashForm.cs` displays initialization status at app startup:
- Load configuration file
- Verify backend API connection
- Verify hardware device connections (indicator, LPR, display board, barrier)
- Transition to MainForm after initialization is complete

### 15.2 Hardware Interface Pattern

All hardware devices are abstracted through interfaces. Both real devices and simulators implement the same interface:

```csharp
// Interface definition
public interface ILprCamera {
    Task<LprCaptureResult> CaptureAsync();
    bool IsConnected { get; }
}

// Real implementation (production)
public class LprCamera : ILprCamera { ... }

// Simulator (development)
public class LprCameraSimulator : ILprCamera { ... }
```

### 15.3 Weighing Process Orchestrator

`WeighingProcessService` manages the entire weighing process:

```
Vehicle detection → LPR capture → AI verification → Dispatch matching → Start weighing
→ Wait for weight stabilization → Record weight → Display board update → Open barrier
→ Send to API server → Complete
```

### 15.4 xUnit Tests

Unit tests are written for core services of the desktop program:
- `ApiServiceTests.cs`: REST API call tests
- `IndicatorServiceTests.cs`: Indicator data parsing tests
- `LocalCacheServiceTests.cs`: SQLite cache CRUD tests

```bash
cd weighing-cs
dotnet test      # Run xUnit tests
```

---

## Appendix: Key Terms Glossary

| Term | Description |
|------|-------------|
| **REST API** | API design approach that operates on resources using HTTP methods (GET/POST/PUT/DELETE) |
| **SPA** | Single Page Application. A web app that swaps components without page transitions |
| **ORM** | Object-Relational Mapping. Technology that maps objects to DB tables (JPA) |
| **DTO** | Data Transfer Object. Data structure used for API requests/responses |
| **DI** | Dependency Injection. Pattern of receiving objects from external sources instead of creating them directly |
| **JWT** | JSON Web Token. Authentication token issued by the server |
| **STOMP** | Messaging protocol on top of WebSocket (Pub/Sub pattern) |
| **HMR** | Hot Module Replacement. Reflects code changes without browser refresh |
| **CORS** | Cross-Origin Resource Sharing. Configuration to allow API calls from different domains |
| **HikariCP** | JDBC connection pool library (improves performance by reusing DB connections) |
| **FCM** | Firebase Cloud Messaging. Google's push notification service |
| **Dirty Checking** | JPA feature that auto-detects Entity changes and generates UPDATE queries |
| **Bean** | Object managed by Spring (Controller, Service, Repository, etc.) |
| **Profile** | Spring's environment-specific configuration separation feature (dev, prod, test) |
| **Interceptor** | Middleware that intercepts requests/responses for common processing (used in both Axios and Spring) |
| **LPR** | License Plate Recognition. Automatic vehicle license plate recognition technology |
| **OTP** | One-Time Password. Single-use security password (Redis-based, TTL 5 minutes) |
| **Indicator** | Device that displays/transmits weight values from the scale (serial communication) |
| **Display Board** | LED display that shows OTP, weighing guidance, etc. (TCP communication) |
| **Barrier** | Automatic barrier at weighing station entry/exit (TCP communication) |
| **Tree-shaking** | Optimization technique that automatically removes unused code during build |
| **Code Splitting** | Technique of separating JS bundles per page using React.lazy |
| **@dnd-kit** | React drag and drop library (used for table row sorting) |

---

> **Final Advice**: Don't try to memorize this document all at once. Refer to it as needed while reading the actual code. The best way to learn is to directly modify the project code and verify how it works.

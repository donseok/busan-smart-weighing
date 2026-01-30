# Hệ thống Cân thông minh Busan - Hướng dẫn Kỹ thuật cho Lập trình viên mới

> **Đối tượng**: Lập trình viên mới/junior tham gia dự án
> **Mục đích**: Hiểu các khái niệm cốt lõi và mẫu thực hành của toàn bộ công nghệ được sử dụng trong dự án

---

## Mục lục

1. [Cấu trúc tổng thể dự án](#1-cấu-trúc-tổng-thể-dự-án)
2. [Cơ sở dữ liệu (PostgreSQL + Redis)](#2-cơ-sở-dữ-liệu-postgresql--redis)
3. [Backend WAS (Spring Boot)](#3-backend-was-spring-boot)
4. [Frontend (React + TypeScript)](#4-frontend-react--typescript)
5. [Ứng dụng Di động (Flutter)](#5-ứng-dụng-di-động-flutter)
6. [Chương trình Desktop (C# .NET WinForms)](#6-chương-trình-desktop-c-net-winforms)
7. [Xác thực và Bảo mật (JWT + Spring Security)](#7-xác-thực-và-bảo-mật-jwt--spring-security)
8. [Giao tiếp Thời gian thực (WebSocket / STOMP)](#8-giao-tiếp-thời-gian-thực-websocket--stomp)
9. [Build và Triển khai (Vite, Gradle, Vercel, Railway)](#9-build-và-triển-khai-vite-gradle-vercel-railway)
10. [Thiết lập Môi trường Phát triển](#10-thiết-lập-môi-trường-phát-triển)
11. [Quy ước Code và Mẫu thiết kế](#11-quy-ước-code-và-mẫu-thiết-kế)
12. [Các lỗi thường gặp và Lưu ý](#12-các-lỗi-thường-gặp-và-lưu-ý)
13. [Các Mẫu Frontend Mới Thêm Gần đây](#13-các-mẫu-frontend-mới-thêm-gần-đây)
14. [Các Mẫu Mobile Mới Thêm Gần đây](#14-các-mẫu-mobile-mới-thêm-gần-đây)
15. [Các Mẫu Desktop Mới Thêm Gần đây](#15-các-mẫu-desktop-mới-thêm-gần-đây)
16. [Các Module Backend Mới Thêm Gần đây](#16-các-module-backend-mới-thêm-gần-đây)
17. [Các Component Frontend Mới Thêm Gần đây](#17-các-component-frontend-mới-thêm-gần-đây)

---

## 1. Cấu trúc tổng thể dự án

### 1.1 Tổng quan Kiến trúc Hệ thống

```
┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
│  React Web   │   │ Flutter App  │   │  C# WinForms     │
│  (Vercel)    │   │ (iOS/Android)│   │  (PC tại hiện trường) │
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

### 1.2 Cấu trúc Thư mục Monorepo

```
busan-smart-weighing/
├── backend/                    # Backend Spring Boot
│   ├── src/main/java/com/dongkuk/weighing/
│   │   ├── auth/               # Xác thực (JWT, Đăng nhập)
│   │   ├── user/               # Quản lý Người dùng
│   │   ├── master/             # Dữ liệu Chủ (Công ty vận tải, Xe, Trạm cân, Mã)
│   │   ├── dispatch/           # Quản lý Điều phối
│   │   ├── weighing/           # Logic Cân cốt lõi
│   │   ├── gatepass/           # Quản lý Giấy xuất cổng
│   │   ├── slip/               # Quản lý Phiếu cân
│   │   ├── favorite/           # Quản lý Yêu thích
│   │   ├── help/               # Hướng dẫn Sử dụng (FAQ)
│   │   ├── monitoring/         # Giám sát Thiết bị
│   │   ├── mypage/             # Trang Cá nhân
│   │   ├── notice/             # Thông báo / Tin tức
│   │   ├── setting/            # Cài đặt Hệ thống
│   │   ├── inquiry/            # Quản lý Yêu cầu Hỗ trợ
│   │   ├── statistics/         # Thống kê / Báo cáo
│   │   ├── notification/       # Thông báo đẩy (FCM)
│   │   ├── dashboard/          # Thống kê Dashboard
│   │   ├── audit/              # Nhật ký Kiểm toán
│   │   └── global/             # Cấu hình chung, Xử lý ngoại lệ, Tiện ích
│   └── src/main/resources/
│       ├── application.yml     # Cấu hình chung
│       ├── application-dev.yml # Môi trường Phát triển
│       └── application-prod.yml# Môi trường Sản xuất
│
├── frontend/                   # Frontend Web React
│   ├── src/
│   │   ├── api/                # Axios Client
│   │   ├── components/         # Component tái sử dụng
│   │   ├── context/            # React Context (Theme, Tab)
│   │   ├── hooks/              # Custom Hook
│   │   ├── layouts/            # Layout
│   │   ├── pages/              # Component Trang
│   │   ├── theme/              # Cấu hình Theme
│   │   ├── types/              # Định nghĩa Kiểu TypeScript
│   │   └── utils/              # Hàm Tiện ích
│   ├── package.json
│   └── vite.config.ts
│
├── mobile/                     # Ứng dụng Di động Flutter
│   ├── lib/
│   │   ├── config/             # Cấu hình API
│   │   ├── models/             # Model Dữ liệu
│   │   ├── providers/          # Quản lý Trạng thái
│   │   ├── screens/            # Màn hình
│   │   ├── services/           # Dịch vụ API/Thông báo
│   │   └── widgets/            # Widget
│   └── pubspec.yaml
│
└── WeighingCS/                 # Chương trình Desktop C#
    ├── Models/
    ├── Services/
    ├── Interfaces/
    └── MainForm.cs
```

### 1.3 Tóm tắt Phiên bản Công nghệ Chính

| Công nghệ | Phiên bản | Mục đích |
|-----------|---------|---------|
| Java | 17 | Runtime Backend |
| Spring Boot | 3.2.5 | Framework Backend |
| PostgreSQL | Latest | Cơ sở dữ liệu quan hệ |
| Redis | Latest | Cache, Quản lý Token |
| React | 18.3.1 | Frontend Web |
| TypeScript | 5.9.3 | Hệ thống Kiểu Frontend |
| Vite | 7.3.1 | Công cụ Build Frontend |
| Ant Design | 5.29.3 | Thư viện Component UI |
| Flutter | 3.10.4+ | Ứng dụng Di động |
| .NET | 8.0 | Chương trình Desktop |

---

## 2. Cơ sở dữ liệu (PostgreSQL + Redis)

### 2.1 PostgreSQL là gì?

PostgreSQL (Postgres) là hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) mã nguồn mở. Tương tự MySQL nhưng có nhiều tính năng nâng cao hơn (JSONB, Window Functions, CTE, v.v.).

### 2.2 Các khái niệm SQL cần biết

#### CRUD cơ bản

```sql
-- Create: Chèn dữ liệu
INSERT INTO users (login_id, user_name, phone_number, user_role)
VALUES ('hong123', '홍길동', '010-1234-5678', 'MANAGER');

-- Read: Truy vấn dữ liệu
SELECT * FROM users WHERE user_role = 'MANAGER';

-- Phân trang (truy vấn được Spring Data JPA tự động tạo)
SELECT * FROM users
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;  -- Trang đầu tiên, 20 bản ghi mỗi trang

-- Update: Sửa dữ liệu
UPDATE users SET phone_number = '010-9999-8888' WHERE user_id = 1;

-- Delete: Xóa dữ liệu
DELETE FROM users WHERE user_id = 1;
```

#### JOIN - Liên kết Quan hệ giữa các Bảng

```sql
-- Truy vấn thông tin điều phối cùng với thông tin xe
-- INNER JOIN: Chỉ trả về các hàng có dữ liệu khớp ở cả hai bảng
SELECT d.dispatch_id, d.item_name, v.plate_number, v.driver_name
FROM dispatches d
INNER JOIN vehicles v ON d.vehicle_id = v.vehicle_id;

-- LEFT JOIN: Dựa trên bảng bên trái; NULL nếu không có dữ liệu ở bảng bên phải
SELECT d.dispatch_id, d.item_name, g.pass_status
FROM dispatches d
LEFT JOIN gate_passes g ON d.dispatch_id = g.dispatch_id;
-- Bao gồm cả các điều phối không có bản ghi giấy xuất cổng (g.pass_status = NULL)
```

#### Index - Cải thiện Tốc độ Tìm kiếm

```sql
-- Index giống như "mục lục của cuốn sách"
-- Không có index, mệnh đề WHERE phải kiểm tra từng hàng một (Full Scan)
-- Có index, giá trị mong muốn có thể được tìm thấy nhanh chóng

CREATE INDEX idx_dispatch_date ON dispatches(dispatch_date);
CREATE INDEX idx_vehicle_plate ON vehicles(plate_number);

-- Index phức hợp: Kết hợp nhiều cột
CREATE INDEX idx_dispatch_search ON dispatches(dispatch_status, dispatch_date);
```

**Lưu ý về Index:**
- Index cũng được cập nhật khi INSERT/UPDATE/DELETE, nên hiệu suất ghi sẽ giảm
- Tạo index trên các cột thường xuyên được sử dụng trong WHERE, ORDER BY và JOIN
- Index không cần thiết cho các bảng có ít dữ liệu

#### Transaction (Giao dịch)

```sql
-- Transaction: "Hoặc tất cả thành công hoặc tất cả thất bại"
-- Ví dụ: Lưu bản ghi + thay đổi trạng thái như một đơn vị khi hoàn thành cân

BEGIN;  -- Bắt đầu transaction

UPDATE weighing_records SET weighing_status = 'COMPLETED'
WHERE weighing_id = 100;

INSERT INTO weighing_slips (weighing_id, slip_number, issued_at)
VALUES (100, 'SLP-2026-001', NOW());

COMMIT;  -- Cả hai truy vấn thành công → xác nhận

-- Nếu có lỗi xảy ra giữa chừng
ROLLBACK;  -- Cả hai truy vấn bị hủy, khôi phục trạng thái trước đó
```

**Sử dụng Transaction trong Spring (bạn không viết SQL trực tiếp trong code):**

```java
@Service
public class WeighingService {

    @Transactional  // Toàn bộ phương thức là một transaction duy nhất
    public void completeWeighing(Long weighingId) {
        WeighingRecord record = weighingRepository.findById(weighingId)
            .orElseThrow();
        record.complete();                    // Thay đổi trạng thái
        weighingRepository.save(record);      // Lưu vào DB

        WeighingSlip slip = WeighingSlip.create(record);
        slipRepository.save(slip);            // Tạo phiếu cân

        // Nếu phương thức hoàn thành bình thường → COMMIT
        // Nếu ngoại lệ xảy ra → tự động ROLLBACK
    }
}
```

### 2.3 JPA và Entity

Trong dự án này, chúng ta không viết SQL trực tiếp mà sử dụng JPA (Java Persistence API), một công nghệ ORM (Object-Relational Mapping) ánh xạ đối tượng Java sang bảng cơ sở dữ liệu.

```java
// Entity: Lớp Java ánh xạ 1:1 với bảng cơ sở dữ liệu

@Entity                      // Lớp này là một bảng DB
@Table(name = "vehicles")    // Tên bảng: vehicles
public class Vehicle extends BaseEntity {  // BaseEntity: Tự động quản lý createdAt, updatedAt

    @Id                      // Trường này là Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Tự động tăng (1, 2, 3...)
    private Long vehicleId;

    @Column(nullable = false, length = 20)  // NOT NULL, tối đa 20 ký tự
    private String plateNumber;

    @Column(length = 20)     // Cho phép NULL, tối đa 20 ký tự
    private String vehicleType;

    @ManyToOne(fetch = FetchType.LAZY)      // Quan hệ N:1, lazy loading
    @JoinColumn(name = "company_id")        // Tên cột FK
    private Company company;

    private BigDecimal defaultTareWeight;
    private BigDecimal maxLoadWeight;
}
```

**Khái niệm Chính:**
- `@Entity` → Lớp này = bảng DB
- `@Id` → Primary Key (định danh duy nhất)
- `@Column` → Cấu hình cột bảng (độ dài, cho phép NULL, v.v.)
- `@ManyToOne` → Quan hệ nhiều-một (Xe N : Công ty vận tải 1)
- `FetchType.LAZY` → Chỉ truy vấn DB khi thực sự truy cập (tối ưu hóa hiệu suất)

### 2.4 Repository - Tầng Truy cập Dữ liệu

```java
// Spring Data JPA: Chỉ cần khai báo interface và implementation sẽ được tự động tạo
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Truy vấn được tự động tạo chỉ từ tên phương thức!
    // SELECT * FROM vehicles WHERE plate_number = ?
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    // SELECT * FROM vehicles WHERE company_id = ? ORDER BY plate_number ASC
    List<Vehicle> findByCompanyIdOrderByPlateNumberAsc(Long companyId);

    // Viết truy vấn phức tạp trực tiếp với @Query
    @Query("SELECT v FROM Vehicle v WHERE v.plateNumber LIKE %:keyword%")
    Page<Vehicle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
```

**Quy ước Đặt tên Phương thức:**
| Từ khóa | Ý nghĩa | Ví dụ |
|---------|---------|-------|
| `findBy` | SELECT WHERE | `findByUserName(name)` |
| `And` | Điều kiện AND | `findByRoleAndActive(role, active)` |
| `Or` | Điều kiện OR | `findByNameOrEmail(name, email)` |
| `OrderBy` | Sắp xếp | `findByRoleOrderByNameAsc(role)` |
| `Between` | Phạm vi | `findByDateBetween(start, end)` |
| `Like` | Khớp một phần | `findByNameLike(pattern)` |
| `IsNull` | Kiểm tra NULL | `findByDeletedAtIsNull()` |

### 2.5 Redis - Cache trong Bộ nhớ

Redis là kho lưu trữ Key-Value dựa trên bộ nhớ. Lưu dữ liệu trong bộ nhớ thay vì đĩa, nên cực kỳ nhanh.

**Mục đích sử dụng Redis trong Dự án:**

```
1. Danh sách đen Token: Lưu các JWT token đã đăng xuất
   KEY: "blacklist:eyJhbGciOi..."  VALUE: "true"  TTL: 30 phút

2. Lưu mã OTP: Mã xác thực dùng một lần
   KEY: "otp:hong123"  VALUE: "482917"  TTL: 5 phút

3. Giới hạn Tốc độ: Giới hạn số lần gọi API
   KEY: "rate:192.168.1.1"  VALUE: "45"  TTL: 1 phút
```

**Các lệnh Redis cần thiết (khi debug):**

```bash
# Kết nối Redis CLI
redis-cli -h localhost -p 6370

# Tra cứu key
GET "blacklist:token-value-here"

# Kiểm tra danh sách key (Chú ý: KHÔNG dùng KEYS * trên production)
KEYS "otp:*"

# Kiểm tra TTL (thời gian hết hạn còn lại, tính bằng giây)
TTL "otp:hong123"

# Xóa key
DEL "blacklist:token-value-here"
```

### 2.6 Cấu hình Cơ sở dữ liệu theo Môi trường

| Môi trường | DB | Chiến lược DDL | Mục đích |
|-----------|-----|-------------|---------|
| **dev** | H2 (in-memory) | `create` | Phát triển local (bảng tự động tạo khi khởi động ứng dụng) |
| **test** | H2 (in-memory) | `create-drop` | Test (xóa sau khi test hoàn thành) |
| **prod** | PostgreSQL | `validate` | Sản xuất (chỉ xác minh cấu trúc bảng, không sửa đổi) |

**Giải thích Chiến lược DDL:**
- `create`: Drop bảng hiện có và tạo mới khi khởi động ứng dụng (mất dữ liệu)
- `create-drop`: create + drop bảng khi tắt ứng dụng
- `update`: ALTER TABLE chỉ cho entity đã thay đổi (có thể rủi ro)
- `validate`: Chỉ xác minh schema DB khớp với entity (phù hợp cho production)
- `none`: Không làm gì

---

## 3. Backend WAS (Spring Boot)

### 3.1 Spring Boot là gì?

Spring Boot là framework ứng dụng web dựa trên Java. Với vai trò WAS (Web Application Server), nó có Tomcat tích hợp sẵn, cho phép bạn chạy server chỉ với một file JAR duy nhất.

### 3.2 Kiến trúc Cốt lõi: Cấu trúc Phân tầng

```
HTTP Request → Controller → Service → Repository → DB
HTTP Response ← Controller ← Service ← Repository ← DB
```

```
┌─────────────────────────────────────────────────┐
│  Controller (Tầng Trình bày)                     │
│  - Xử lý HTTP request/response                   │
│  - Xác thực dữ liệu request (Validation)         │
│  - Chuyển đổi định dạng response                  │
├─────────────────────────────────────────────────┤
│  Service (Tầng Nghiệp vụ)                        │
│  - Logic nghiệp vụ cốt lõi                       │
│  - Quản lý transaction (@Transactional)           │
│  - Kết hợp nhiều Repository                       │
├─────────────────────────────────────────────────┤
│  Repository (Tầng Truy cập Dữ liệu)              │
│  - Thực thi CRUD DB                               │
│  - Interface Spring Data JPA                       │
├─────────────────────────────────────────────────┤
│  Domain/Entity (Tầng Domain)                      │
│  - Đối tượng ánh xạ với bảng DB                   │
│  - Chứa quy tắc domain                            │
└─────────────────────────────────────────────────┘
```

### 3.3 Controller - Định nghĩa API Endpoint

```java
@RestController                    // Controller trả về JSON response
@RequestMapping("/api/v1/dispatches")  // Đường dẫn URL cơ sở
@RequiredArgsConstructor           // Tự động tạo constructor cho trường final (Lombok)
public class DispatchController {

    private final DispatchService dispatchService;  // DI (Dependency Injection)

    // GET /api/v1/dispatches?page=0&size=20&startDate=2026-01-01
    @GetMapping
    public ApiResponse<Page<DispatchResponse>> getDispatches(
            DispatchSearchCondition condition,  // Tự động ánh xạ query parameter
            Pageable pageable                   // Tự động ánh xạ thông tin phân trang
    ) {
        return ApiResponse.success(dispatchService.search(condition, pageable));
    }

    // POST /api/v1/dispatches (body: JSON)
    @PostMapping
    public ApiResponse<DispatchResponse> create(
            @Valid @RequestBody DispatchCreateRequest request  // JSON → chuyển đổi object + xác thực
    ) {
        return ApiResponse.success(dispatchService.create(request));
    }

    // PUT /api/v1/dispatches/123
    @PutMapping("/{id}")
    public ApiResponse<DispatchResponse> update(
            @PathVariable Long id,                              // Trích xuất ID từ URL
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

**Tổng hợp các Annotation Chính:**
| Annotation | Ý nghĩa |
|-----------|---------|
| `@RestController` | Controller REST API (JSON response) |
| `@RequestMapping` | Ánh xạ đường dẫn URL |
| `@GetMapping` | Xử lý HTTP GET request |
| `@PostMapping` | Xử lý HTTP POST request |
| `@PutMapping` | Xử lý HTTP PUT request |
| `@DeleteMapping` | Xử lý HTTP DELETE request |
| `@PathVariable` | Trích xuất biến từ đường dẫn URL (`/users/{id}`) |
| `@RequestBody` | Chuyển đổi JSON trong HTTP body thành object |
| `@Valid` | Thực thi Bean Validation |
| `@RequestParam` | Trích xuất query parameter (`?name=홍길동`) |

### 3.4 DTO - Data Transfer Object

DTO (Data Transfer Object) là cấu trúc dữ liệu dùng cho API request/response. Thay vì trực tiếp trả về Entity, chỉ truyền dữ liệu cần thiết.

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
    String plateNumber,     // Dữ liệu liên quan lấy từ Entity
    String itemType,
    String itemName,
    LocalDate dispatchDate,
    String dispatchStatus,
    LocalDateTime createdAt
) {
    // Phương thức factory chuyển đổi Entity → DTO
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

**Tại sao không trả về Entity trực tiếp?**
1. **Bảo mật**: Thông tin nhạy cảm như mật khẩu, ID nội bộ có thể bị lộ
2. **Linh hoạt**: Có thể thay đổi API spec và DB schema độc lập
3. **Tham chiếu vòng**: Quan hệ hai chiều giữa các entity có thể gây vòng lặp vô hạn khi serialize JSON

### 3.5 Bean Validation - Xác thực Dữ liệu Request

```java
// Các annotation xác thực thường dùng
@NotNull                          // Không được null
@NotBlank                         // Không được null, chuỗi rỗng, hoặc chỉ có khoảng trắng
@NotEmpty                         // Không được null hoặc chuỗi rỗng (cho phép khoảng trắng)
@Size(min = 3, max = 50)         // Giới hạn độ dài chuỗi
@Min(0)                          // Giá trị tối thiểu
@Max(100)                        // Giá trị tối đa
@Email                           // Định dạng email
@Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$")  // Khớp regex
@Past                            // Chỉ ngày trong quá khứ
@Future                          // Chỉ ngày trong tương lai
```

### 3.6 Service - Logic Nghiệp vụ

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // Mặc định: chỉ đọc (tối ưu cho SELECT)
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final VehicleRepository vehicleRepository;

    // Truy vấn: readOnly = true (mặc định)
    public Page<DispatchResponse> search(
            DispatchSearchCondition condition, Pageable pageable) {
        return dispatchRepository.searchByCondition(condition, pageable)
                .map(DispatchResponse::from);
    }

    // Tạo mới: @Transactional tắt chế độ chỉ đọc
    @Transactional   // readOnly = false → cho phép INSERT/UPDATE/DELETE
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
- `readOnly = true`: Chỉ thực thi SELECT, tắt Hibernate Dirty Checking → cải thiện hiệu suất
- `readOnly = false` (mặc định): Cho phép INSERT/UPDATE/DELETE, kích hoạt Dirty Checking

### 3.7 Mẫu Xử lý Ngoại lệ

```java
// ErrorCode enum: Quản lý tất cả mã lỗi ở một nơi
public enum ErrorCode {
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "차량을 찾을 수 없습니다"),
    DUPLICATE_PLATE_NUMBER(HttpStatus.CONFLICT, "이미 등록된 차량번호입니다"),
    INVALID_WEIGHING_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 계량 상태입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다");

    private final HttpStatus status;
    private final String message;
}

// BusinessException: Ngoại lệ phát sinh từ logic nghiệp vụ
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// GlobalExceptionHandler: Bắt tất cả ngoại lệ và chuyển thành response nhất quán
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

### 3.8 Định dạng Response API Chuẩn

```java
// Tất cả API response theo định dạng thống nhất này
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
// Ví dụ response thành công
{
  "success": true,
  "data": {
    "dispatch_id": 1,
    "item_name": "철근",
    "dispatch_status": "REGISTERED"
  },
  "error": null
}

// Ví dụ response lỗi
{
  "success": false,
  "data": null,
  "error": {
    "code": "VEHICLE_NOT_FOUND",
    "message": "차량을 찾을 수 없습니다"
  }
}
```

### 3.9 Lombok - Loại bỏ Code Boilerplate

```java
// Không có Lombok (code dài dòng)
public class Vehicle {
    private Long vehicleId;
    private String plateNumber;

    public Vehicle() {}

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long id) { this.vehicleId = id; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String pn) { this.plateNumber = pn; }
}

// Với Lombok (code gọn gàng)
@Getter                        // Tạo getter cho tất cả trường
@NoArgsConstructor             // Constructor mặc định
@AllArgsConstructor            // Constructor với tất cả trường
@Builder                       // Builder pattern sẵn sàng sử dụng
public class Vehicle {
    private Long vehicleId;
    private String plateNumber;
}

// Sử dụng Builder pattern
Vehicle vehicle = Vehicle.builder()
    .vehicleId(1L)
    .plateNumber("12가3456")
    .build();
```

| Annotation | Code được Tạo |
|-----------|--------------|
| `@Getter` | Getter cho tất cả trường |
| `@Setter` | Setter cho tất cả trường |
| `@NoArgsConstructor` | Constructor không tham số |
| `@AllArgsConstructor` | Constructor với tất cả trường là tham số |
| `@RequiredArgsConstructor` | Constructor chỉ với trường final là tham số |
| `@Builder` | Builder pattern |
| `@ToString` | toString() |
| `@EqualsAndHashCode` | equals(), hashCode() |
| `@Data` | Getter + Setter + ToString + EqualsAndHashCode + RequiredArgs |

### 3.10 Dependency Injection (Tiêm phụ thuộc)

Đây là khái niệm cốt lõi nhất của Spring. Thay vì tạo object trực tiếp, Spring tạo và tiêm chúng cho bạn.

```java
// ❌ Ví dụ xấu: Tạo trực tiếp
public class DispatchService {
    private DispatchRepository repo = new DispatchRepository();  // Tạo trực tiếp
}

// ✅ Ví dụ tốt: Spring tiêm
@Service
@RequiredArgsConstructor
public class DispatchService {
    private final DispatchRepository repo;  // Spring tự động tiêm
}
```

**Tại sao sử dụng DI?**
- Dễ dàng thay thế bằng Mock object khi test
- Không cần sửa code khi thay đổi implementation
- Spring quản lý vòng đời của object

---

## 4. Frontend (React + TypeScript)

### 4.1 React là gì?

React là thư viện JavaScript để xây dựng giao diện người dùng (UI). Dựa trên **component**, chia UI thành các phần nhỏ và kết hợp lại.

### 4.2 Cơ bản TypeScript

TypeScript là ngôn ngữ thêm **hệ thống kiểu** vào JavaScript. Cho phép phát hiện lỗi ngay khi viết code.

```typescript
// JavaScript (không có kiểu, phát hiện lỗi lúc runtime)
function addUser(user) {
    console.log(user.name);  // Không thể biết user có thuộc tính name hay không
}

// TypeScript (có kiểu, phát hiện lỗi lúc viết code)
interface User {
    userId: number;
    loginId: string;
    userName: string;
    phoneNumber: string;
    userRole: 'ADMIN' | 'MANAGER' | 'DRIVER';  // Union type: chỉ cho phép 3 giá trị này
    isActive: boolean;
}

function addUser(user: User): void {
    console.log(user.userName);  // IDE hỗ trợ auto-completion
    console.log(user.address);   // ❌ Lỗi biên dịch! address không tồn tại trong User
}
```

**Các kiểu thường dùng:**

```typescript
// Kiểu cơ bản
let name: string = '홍길동';
let age: number = 25;
let isActive: boolean = true;
let data: null = null;
let value: undefined = undefined;

// Mảng
let ids: number[] = [1, 2, 3];
let names: string[] = ['홍길동', '김철수'];

// Object (interface)
interface Vehicle {
    vehicleId: number;
    plateNumber: string;
    maxLoadWeight?: number;  // ? = trường tùy chọn (có thể không tồn tại)
}

// Generic: Sử dụng kiểu như biến
interface ApiResponse<T> {
    success: boolean;
    data: T;
    error: { code: string; message: string } | null;
}

// Sử dụng
const response: ApiResponse<User[]> = await api.get('/users');
//   Compiler biết response.data có kiểu User[]
```

### 4.3 React Functional Component

```tsx
// React.FC: Kiểu functional component
const UserCard: React.FC<{ user: User }> = ({ user }) => {
    return (
        <div>
            <h3>{user.userName}</h3>
            <p>{user.phoneNumber}</p>
        </div>
    );
};

// Sử dụng
<UserCard user={userData} />
```

### 4.4 React Hooks - Khái niệm Cốt lõi

#### useState - Quản lý Trạng thái

```tsx
const [count, setCount] = useState(0);
//     giá trị  hàm thay đổi    giá trị khởi tạo

// Thay đổi giá trị → component re-render
setCount(5);           // count thay đổi thành 5, UI cập nhật
setCount(prev => prev + 1);  // Thay đổi dựa trên giá trị trước (cách an toàn)
```

**Ví dụ thực tế trong Dự án (DispatchPage.tsx):**

```tsx
const DispatchPage: React.FC = () => {
    // Trạng thái dữ liệu
    const [data, setData] = useState<Dispatch[]>([]);       // Danh sách điều phối
    const [loading, setLoading] = useState(false);           // Trạng thái loading
    const [searched, setSearched] = useState(false);         // Đã tìm kiếm chưa
    const [totalElements, setTotalElements] = useState(0);   // Tổng số bản ghi
    const [currentPage, setCurrentPage] = useState(1);       // Trang hiện tại

    // Trạng thái modal
    const [createModalOpen, setCreateModalOpen] = useState(false);
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [editingRecord, setEditingRecord] = useState<Dispatch | null>(null);

    // ...
};
```

#### useEffect - Xử lý Side Effect

```tsx
// Thực thi khi component xuất hiện trên màn hình (mount)
useEffect(() => {
    fetchData();           // Gọi API
}, []);                    // Mảng rỗng: chỉ thực thi 1 lần đầu

// Thực thi khi giá trị cụ thể thay đổi
useEffect(() => {
    fetchData(searchKeyword);  // Thực thi mỗi khi searchKeyword thay đổi
}, [searchKeyword]);           // Mảng dependency

// Hàm cleanup: Thực thi khi component bị xóa
useEffect(() => {
    const ws = new WebSocket('ws://...');
    ws.onmessage = handleMessage;

    return () => {
        ws.close();  // Đóng kết nối WebSocket khi component unmount
    };
}, []);
```

#### useCallback - Ghi nhớ Hàm

```tsx
// Vấn đề: Hàm fetchData được tạo lại mỗi khi component re-render
// → Thêm vào mảng dependency của useEffect có thể gây vòng lặp vô hạn

// Giải pháp: Ghi nhớ hàm với useCallback
const fetchData = useCallback(async (keyword?: string) => {
    setLoading(true);
    try {
        const res = await apiClient.get('/master/companies', { params });
        setData(res.data.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
}, []);  // Mảng dependency rỗng, hàm chỉ được tạo một lần

// Bây giờ có thể sử dụng an toàn trong useEffect
useEffect(() => { fetchData(); }, [fetchData]);
```

#### Form.useForm - Quản lý Form Ant Design

```tsx
const [form] = Form.useForm();   // Tạo instance form

// Đặt giá trị form (khi mở modal chỉnh sửa)
form.setFieldsValue({
    plateNumber: record.plateNumber,
    vehicleType: record.vehicleType,
});

// Lấy giá trị sau khi xác thực form
const values = await form.validateFields();  // Ném ngoại lệ nếu xác thực thất bại

// Đặt lại form
form.resetFields();
```

### 4.5 Ant Design - Component UI

Các component Ant Design chính được sử dụng trong dự án:

```tsx
import {
    Button,       // Nút
    Input,        // Nhập liệu văn bản
    Select,       // Dropdown chọn
    DatePicker,   // Bộ chọn ngày
    Form,         // Form (có xác thực)
    Table,        // Bảng dữ liệu
    Modal,        // Hộp thoại modal
    message,      // Thông báo (toast)
    Tag,          // Tag/nhãn
    Space,        // Component khoảng cách
    Popconfirm,   // Popup xác nhận
    Card,         // Layout card
    Typography,   // Kiểu chữ
    Pagination,   // Phân trang
    Switch,       // Công tắc bật/tắt
    Tabs,         // Menu tab
} from 'antd';
```

**Ví dụ Form + Validation:**

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

    {/* Xác thực chéo: maxLoadWeight > defaultTareWeight */}
    <Form.Item
        name="maxLoadWeight"
        label="최대 적재중량"
        dependencies={['defaultTareWeight']}  // Xác thực lại khi trường này thay đổi
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
// api/client.ts - Cấu hình Axios instance
import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api/v1',
    timeout: 10000,
});

// Request interceptor: Tự động đính kèm JWT token vào tất cả request
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Response interceptor: Làm mới token khi lỗi 401
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            // accessToken hết hạn → thử gia hạn với refreshToken
            const refreshToken = localStorage.getItem('refreshToken');
            const res = await axios.post('/api/v1/auth/refresh', { refreshToken });
            localStorage.setItem('accessToken', res.data.data.accessToken);

            // Thử lại request ban đầu
            error.config.headers.Authorization = `Bearer ${res.data.data.accessToken}`;
            return apiClient(error.config);
        }
        return Promise.reject(error);
    }
);
```

**Mẫu Gọi API:**

```typescript
// GET request (truy vấn)
const res = await apiClient.get('/dispatches', {
    params: { page: 0, size: 20, startDate: '2026-01-01' }
});
const dispatches: Dispatch[] = res.data.data.content;

// POST request (tạo mới)
await apiClient.post('/dispatches', {
    vehicleId: 1,
    itemName: '철근',
    dispatchDate: '2026-01-29'
});

// PUT request (cập nhật)
await apiClient.put(`/dispatches/${id}`, updatedData);

// DELETE request (xóa)
await apiClient.delete(`/dispatches/${id}`);
```

### 4.7 React Router - Routing phía Client

```tsx
// Xử lý chuyển trang trong SPA (Single Page Application)
// Thực tế không load trang mới; chỉ thay đổi component

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

<BrowserRouter>
    <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<MainLayout />}>  {/* Layout chia sẻ */}
            <Route index element={<DashboardPage />} />
            <Route path="dispatch" element={<DispatchPage />} />
            <Route path="weighing" element={<WeighingPage />} />
            <Route path="master/vehicles" element={<MasterVehiclePage />} />
            <Route path="admin/users" element={<AdminUserPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" />} />  {/* 404 → trang chủ */}
    </Routes>
</BrowserRouter>
```

### 4.8 Context API - Quản lý Trạng thái Toàn cục

```tsx
// Ví dụ Theme Context
interface ThemeContextType {
    isDark: boolean;
    toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType>({
    isDark: false,
    toggleTheme: () => {},
});

// Provider: Cung cấp trạng thái ở cấp cao nhất
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

// Trong component sử dụng:
const { isDark, toggleTheme } = useContext(ThemeContext);
```

### 4.9 Chuyển đổi Đặt tên JSON (camelCase ↔ snake_case)

```
Frontend (JavaScript):  camelCase   → plateNumber, dispatchDate
Backend (Java/JSON):    snake_case  → plate_number, dispatch_date
```

Axios interceptor xử lý chuyển đổi tự động:
- **Khi gửi request**: camelCase → snake_case
- **Khi nhận response**: snake_case → camelCase

Điều này cho phép frontend và backend duy trì quy ước đặt tên riêng trong khi giao tiếp.

---

## 5. Ứng dụng Di động (Flutter)

### 5.1 Flutter là gì?

Flutter là framework UI đa nền tảng do Google tạo ra. Bạn có thể xây dựng ứng dụng iOS, Android và Web với **một codebase duy nhất**. Sử dụng ngôn ngữ lập trình Dart.

### 5.2 Cú pháp Cơ bản Dart

```dart
// Khai báo biến
String name = '홍길동';           // Kiểu tường minh
var age = 25;                    // Suy luận kiểu
final loginId = 'hong123';       // Hằng runtime (không thể thay đổi sau khi gán)
const pi = 3.14;                 // Hằng compile-time

// Null Safety (Dart 3.x)
String? nullableName;            // ? = có thể null
String nonNullName = '홍길동';    // Không thể null

nullableName?.length;            // ?. = không gọi nếu null
nullableName ?? '이름없음';       // ?? = dùng giá trị mặc định nếu null
nullableName!;                   // ! = khẳng định không null (nguy hiểm)

// Lập trình bất đồng bộ
Future<List<Dispatch>> fetchDispatches() async {
    final response = await dio.get('/dispatches');
    return response.data.map((json) => Dispatch.fromJson(json)).toList();
}

// Lớp
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

### 5.3 Widget - Đơn vị Cơ bản của Flutter

Trong Flutter, mọi phần tử trên màn hình đều là Widget. Tương tự Component trong React.

```dart
// StatelessWidget: Widget không có trạng thái (UI tĩnh)
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

// StatefulWidget: Widget có trạng thái (UI động)
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
        _fetchData();        // Load dữ liệu khi khởi tạo màn hình
    }

    Future<void> _fetchData() async {
        setState(() => _isLoading = true);   // Tương tự setLoading(true) trong React
        // ... Gọi API ...
        setState(() => _isLoading = false);  // Cập nhật màn hình
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

### 5.4 Provider - Quản lý Trạng thái

Provider là mẫu quản lý trạng thái nhẹ cho Flutter. Tương tự React Context.

```dart
// Lớp trạng thái (kế thừa ChangeNotifier)
class AuthProvider extends ChangeNotifier {
    User? _currentUser;
    bool _isAuthenticated = false;

    User? get currentUser => _currentUser;
    bool get isAuthenticated => _isAuthenticated;

    Future<void> login(String loginId, String password) async {
        final response = await apiService.login(loginId, password);
        _currentUser = response.user;
        _isAuthenticated = true;
        notifyListeners();  // Tương tự setState() trong React → cập nhật UI
    }

    void logout() {
        _currentUser = null;
        _isAuthenticated = false;
        notifyListeners();
    }
}

// Đăng ký Provider ở cấp cao nhất ứng dụng
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

// Sử dụng trạng thái trong widget
class ProfileScreen extends StatelessWidget {
    @override
    Widget build(BuildContext context) {
        // Đọc trạng thái từ Provider
        final auth = Provider.of<AuthProvider>(context);
        // hoặc
        final auth = context.watch<AuthProvider>();  // Rebuild khi thay đổi
        final auth = context.read<AuthProvider>();   // Không rebuild khi thay đổi

        return Text('안녕하세요, ${auth.currentUser?.userName}');
    }
}
```

### 5.5 Go Router - Điều hướng

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
            return '/login';  // Chưa xác thực → chuyển hướng đến trang đăng nhập
        }
        return null;  // Tiếp tục bình thường
    },
);
```

### 5.6 Dio - HTTP Client

```dart
// HTTP client tương tự Axios
class ApiService {
    late final Dio _dio;

    ApiService() {
        _dio = Dio(BaseOptions(
            baseUrl: ApiConfig.baseUrl,     // 'http://..../api/v1'
            connectTimeout: const Duration(seconds: 10),
        ));

        // Interceptor: Tự động đính kèm token vào tất cả request
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

### 5.7 So sánh React vs Flutter

| Khái niệm | React | Flutter |
|-----------|-------|---------|
| Đơn vị cơ bản | Component | Widget |
| Quản lý trạng thái | useState | setState / Provider |
| Vòng đời | useEffect | initState / dispose |
| Trạng thái toàn cục | Context API | Provider / Riverpod |
| Routing | React Router | Go Router |
| HTTP | Axios | Dio |
| Styling | CSS / Styled | Thuộc tính Widget |
| Render danh sách | `.map()` | `ListView.builder()` |
| Render có điều kiện | `{condition && <Widget>}` | `if (condition) Widget()` |

---

## 6. Chương trình Desktop (C# .NET WinForms)

### 6.1 Vai trò

Đây là chương trình chạy trên PC Windows tại hiện trường (trạm cân).

- Nhận dữ liệu trọng lượng thời gian thực từ cân qua giao tiếp **cổng nối tiếp (COM)**
- Hiển thị kết quả cân trên bảng điện tử
- Điều khiển thanh chắn (mở/đóng)
- Cache dữ liệu vào SQLite khi offline

### 6.2 Các Thành phần Cốt lõi

```csharp
// Giao tiếp cổng nối tiếp (kết nối cân)
using System.IO.Ports;

var port = new SerialPort("COM1", 9600, Parity.None, 8, StopBits.One);
port.DataReceived += (sender, e) => {
    string data = port.ReadLine();
    decimal weight = ParseWeight(data);  // "  1250.5kg" → 1250.5
    UpdateDisplay(weight);
};
port.Open();

// Cache local SQLite (dự phòng offline)
using System.Data.SQLite;

var conn = new SQLiteConnection("Data Source=weighing_cache.db");
conn.Open();
var cmd = new SQLiteCommand("INSERT INTO cache (data, synced) VALUES (@d, 0)", conn);
cmd.Parameters.AddWithValue("@d", jsonData);
cmd.ExecuteNonQuery();

// Gọi REST API (tích hợp backend)
using var httpClient = new HttpClient();
httpClient.DefaultRequestHeaders.Authorization =
    new AuthenticationHeaderValue("Bearer", token);

var response = await httpClient.PostAsync(
    "http://server/api/v1/weighing",
    new StringContent(json, Encoding.UTF8, "application/json")
);
```

### 6.3 File Cấu hình (appsettings.json)

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

### 6.4 Hệ thống UI Hiện đại (GDI+ Custom Control)

Chương trình desktop áp dụng toàn diện **custom control dựa trên GDI+** để đạt chất lượng hình ảnh ngang tầm ứng dụng web. Thay vì dùng control WinForms gốc, chương trình render trực tiếp trong `OnPaint` để triển khai dark theme, bo tròn góc, hiệu ứng glow, v.v.

#### 6.4.1 Hệ thống Design Token Theme

`Controls/Theme.cs` quản lý tập trung tất cả thuộc tính trực quan. Dựa trên bảng màu Tailwind CSS Slate, định nghĩa 5 tầng nền, màu ngữ nghĩa, typography và hằng số khoảng cách.

```csharp
// Tầng nền (từ tối đến sáng)
BgDarkest  #060D1B  → Header/Footer
BgBase     #0B1120  → Nền chính
BgElevated #0F172A  → Trường nhập liệu
BgSurface  #1E293B  → Thẻ (Card)
BgHover    #283548  → Trạng thái hover

// Hệ số tỉ lệ
FontScale  = 1.5f   // Tỉ lệ kích thước font
LayoutScale = 1.25f // Tỉ lệ layout/khoảng cách

// Font (cache tĩnh, KHÔNG Dispose!)
Theme.FontBody      → 9.5pt x FontScale = 14.25pt Segoe UI
Theme.FontBodyBold  → 9.5pt x FontScale = 14.25pt Segoe UI Bold
Theme.FontMono      → 10pt x FontScale = 15pt Consolas

// Tiện ích màu sắc
Theme.WithAlpha(color, alpha)  → Độ trong suốt alpha
Theme.Lighten(color, factor)   → Làm sáng
Theme.Darken(color, factor)    → Làm tối
```

> **Luu y**: Thuoc tinh `Theme.FontXxx` la instance chia se duoc cache tinh. Neu su dung nhu `using var font = Theme.FontBody`, sau khi Dispose font se **bi vo hieu hoa toan cuc** va tat ca control se gap ngoai le "Parameter is not valid". Phai su dung `var font = Theme.FontBody` chi de tham chieu.
>
> **An toan chuyen doi theme**: `InvalidateFontCache()` khong `Dispose()` instance font cu ma chi dat tham chieu ve `null`. Ly do la `OnPaint` cua control co the duoc goi truoc khi handler `ThemeChanged` gan lai font moi, gay ra crash do race condition neu Dispose. Font cu se duoc GC thu hoi.

#### 6.4.2 Cấu hình Custom Control

| Control | Mô tả | Phương thức Triển khai |
|---------|-------|----------------------|
| `HeaderBar` | Header trên cùng (logo, tiêu đề, LED kết nối, toggle theme, đồng hồ) | Kế thừa Control, Timer |
| `StatusFooter` | Thanh trạng thái dưới cùng (trạm cân, chế độ, đồng bộ, thời gian) | Kế thừa Control, Timer |
| `WeightDisplayPanel` | Hiển thị trọng lượng lớn (glow, badge ổn định) | Kế thừa Control |
| `CardPanel` | Container thẻ (hiệu ứng kính, bóng đổ, thanh accent) | Kế thừa Panel |
| `ModernButton` | Nút (3 loại Primary/Secondary/Danger) | Kế thừa Control |
| `ModernToggle` | Toggle trượt (chuyển đổi tự động/thủ công, hoạt ảnh) | Kế thừa Control, Timer |
| `ModernTextBox` | Nhập text (viền glow, placeholder) | Kế thừa Control + ủy quyền TextBox |
| `ModernComboBox` | Dropdown (render item tùy chỉnh) | Kế thừa Control + ủy quyền ComboBox |
| `ModernCheckBox` | Checkbox (render tùy chỉnh, dấu check) | Kế thừa Control |
| `ModernListView` | ListView (hàng xen kẽ màu, màu trạng thái, cột cuối tự động fill) | Kế thừa ListView (OwnerDraw) |
| `ProcessStepBar` | Hiển thị 4 bước quy trình (indicator hình tròn) | Kế thừa Control |
| `TerminalLogPanel` | Bảng log kiểu terminal | Kế thừa Control |
| `ModernProgressBar` | Thanh tiến trình (cho splash screen) | Kế thừa Control |

#### 6.4.3 Cấu trúc Layout

Main form sử dụng layout 3 phần:

```
┌─────────────────────────────────────────────────┐
│  HeaderBar (Dock.Top, 56px)                     │
│  [DK Logo] Busan Smart Weighing System ● Scale ... HH:mm│
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
│  Scale#1 · COM1  ● Auto Mode          v1.0.0 HH:mm:ss│
└─────────────────────────────────────────────────┘
```

#### 6.4.4 Mẫu Rendering

Tất cả custom control tuân theo mẫu sau:

```csharp
public class CustomControl : Control
{
    public CustomControl()
    {
        // Double buffering bắt buộc
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw, true);
    }

    // Ngăn nhấp nháy nền
    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        if (Width < 10 || Height < 10) return; // Bảo vệ kích thước zero

        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // Tạo hình chữ nhật bo tròn bằng RoundedRectHelper
        using var path = RoundedRectHelper.Create(bounds, Theme.RadiusMedium);
        // ... GDI+ rendering
    }
}
```

**Mẫu Wrapper** (ModernTextBox, ModernComboBox): Bao bọc control gốc bên trong nhưng chỉ tùy chỉnh render viền ngoài và hiệu ứng focus.

```csharp
public class ModernTextBox : Control
{
    private readonly TextBox _inner;  // Control gốc bên trong

    // Ủy quyền thuộc tính Text, Font, v.v. cho _inner
    public override string Text
    {
        get => _inner.Text;
        set => _inner.Text = value ?? "";
    }
}
```

---

## 7. Xác thực và Bảo mật (JWT + Spring Security)

### 7.1 JWT (JSON Web Token) là gì?

JWT là **thẻ căn cước kỹ thuật số** được cấp cho client. Cho phép server xác định người dùng mà không cần duy trì session.

```
Cấu trúc JWT:
xxxxx.yyyyy.zzzzz
  │      │      │
  │      │      └─ Signature (Chữ ký): Ngăn chặn giả mạo
  │      └─ Payload (Nội dung): Thông tin người dùng, thời gian hết hạn, v.v.
  └─ Header (Tiêu đề): Thông tin thuật toán
```

```json
// Ví dụ Payload (khi giải mã Base64)
{
  "sub": "hong123",         // ID người dùng
  "role": "MANAGER",        // Vai trò người dùng
  "iat": 1737100000,        // Thời gian cấp
  "exp": 1737101800         // Thời gian hết hạn (30 phút sau)
}
```

### 7.2 Luồng Xác thực

```
1. Đăng nhập
   Client → POST /auth/login { loginId, password }
   Server → { accessToken: "eyJ...", refreshToken: "eyJ..." }

2. API Request (yêu cầu xác thực)
   Client → GET /dispatches
                Headers: { Authorization: "Bearer eyJ..." }
   Server → Xác minh JWT → hợp lệ → trả về dữ liệu

3. Token hết hạn (sau 30 phút)
   Client → GET /dispatches → 401 Unauthorized
   Client → POST /auth/refresh { refreshToken: "eyJ..." }
   Server → { accessToken: "eyJ..." } mới
   Client → GET /dispatches (thử lại với token mới)

4. Đăng xuất
   Client → POST /auth/logout
   Server → Đăng ký accessToken vào danh sách đen Redis
            (các request tiếp theo với token đó sẽ bị từ chối)
```

### 7.3 Chuỗi Filter Spring Security

```
HTTP Request
    │
    ▼
┌─────────────────────┐
│ CORS Filter          │  → Xử lý header CORS
├─────────────────────┤
│ JWT Authentication   │  → Trích xuất token từ header Authorization
│ Filter               │  → Xác minh token (chữ ký, hết hạn, danh sách đen)
│                      │  → Nếu hợp lệ, lưu thông tin người dùng vào SecurityContext
├─────────────────────┤
│ Authorization Filter │  → Kiểm tra quyền truy cập API
│                      │  → DRIVER truy cập API chỉ dành cho ADMIN → 403
└─────────────────────┘
    │
    ▼
  Controller → Service → Repository → DB
```

### 7.4 Kiểm soát Truy cập dựa trên Vai trò

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            // Truy cập không cần xác thực
            .requestMatchers("/api/v1/auth/**").permitAll()

            // Chỉ ADMIN
            .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

            // Chỉ ADMIN hoặc MANAGER
            .requestMatchers("/api/v1/master/**").hasAnyRole("ADMIN", "MANAGER")

            // Còn lại chỉ cần xác thực
            .anyRequest().authenticated()
        );
    }
}
```

| Vai trò | Chức năng Có thể Truy cập |
|---------|--------------------------|
| `ADMIN` | Quản lý toàn bộ (người dùng, dữ liệu chủ, nhật ký kiểm toán, v.v.) |
| `MANAGER` | Quản lý điều phối, cân, giấy xuất cổng, dữ liệu chủ |
| `DRIVER` | Xem điều phối được phân công, trang cá nhân |

### 7.5 Mã hóa Mật khẩu

```java
// BCrypt: Hash một chiều (không thể giải mã)
// Cùng mật khẩu tạo ra hash khác nhau mỗi lần (bao gồm salt)

passwordEncoder.encode("myPassword123");
// → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

passwordEncoder.matches("myPassword123", encodedPassword);  // true
passwordEncoder.matches("wrongPassword", encodedPassword);  // false
```

---

## 8. Giao tiếp Thời gian thực (WebSocket / STOMP)

### 8.1 WebSocket là gì?

HTTP theo mô hình request-response, nhưng WebSocket cho phép **giao tiếp hai chiều thời gian thực**.

```
HTTP (một chiều):
Client → "Có dữ liệu mới không?" → Server
Client ← "Không"                   ← Server
Client → "Giờ thì sao?"           → Server
Client ← "Không"                   ← Server
Client → "Giờ thì sao?"           → Server
Client ← "Có! Đây nè"            ← Server

WebSocket (hai chiều):
Client ←→ Thiết lập kết nối ←→ Server
(Server đẩy ngay khi có dữ liệu mới)
Server → "Cân hoàn thành! 2.450kg"  → Client
Server → "Điều phối mới đã đăng ký"  → Client
```

### 8.2 Giao thức STOMP

STOMP (Simple Text Oriented Messaging Protocol) là giao thức nhắn tin hoạt động trên WebSocket. Cung cấp mẫu đăng ký (subscribe)/phát hành (publish).

```
Cấu hình Server:
  /ws           → Endpoint kết nối WebSocket
  /topic/*      → Server → Client broadcast (đăng ký)
  /app/*        → Client → Server gửi tin nhắn
```

### 8.3 Kết nối WebSocket phía Frontend

```typescript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),  // SockJS fallback
    reconnectDelay: 5000,                        // Kết nối lại sau 5 giây nếu bị ngắt

    onConnect: () => {
        console.log('WebSocket đã kết nối');

        // Đăng ký: Nhận cập nhật trạng thái cân
        client.subscribe('/topic/weighing-updates', (message) => {
            const data = JSON.parse(message.body);
            // data = { weighingId: 1, status: 'COMPLETED', weight: 2450.5 }
            updateWeighingStatus(data);
        });

        // Đăng ký: Nhận thay đổi trạng thái thiết bị
        client.subscribe('/topic/equipment-status', (message) => {
            const data = JSON.parse(message.body);
            updateEquipmentDisplay(data);
        });
    },

    onDisconnect: () => {
        console.log('WebSocket đã ngắt kết nối');
    },
});

client.activate();  // Bắt đầu kết nối

// Gửi tin nhắn (Client → Server)
client.publish({
    destination: '/app/weighing-command',
    body: JSON.stringify({ action: 'START', scaleId: 1 }),
});
```

### 8.4 Cấu hình WebSocket phía Backend

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // SockJS fallback (cho trình duyệt không hỗ trợ WebSocket)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");    // Prefix đường dẫn đăng ký
        registry.setApplicationDestinationPrefixes("/app");  // Prefix đường dẫn gửi
    }
}

// Phát hành tin nhắn từ service
@Service
@RequiredArgsConstructor
public class WeighingService {
    private final SimpMessagingTemplate messagingTemplate;

    public void completeWeighing(Long weighingId) {
        // ... logic nghiệp vụ ...

        // Broadcast đến tất cả subscriber
        messagingTemplate.convertAndSend("/topic/weighing-updates",
            new WeighingUpdateMessage(weighingId, "COMPLETED", weight));
    }
}
```

---

## 9. Build và Triển khai (Vite, Gradle, Vercel, Railway)

### 9.1 Build Frontend (Vite)

```bash
# Khởi chạy server phát triển (HMR: phản ánh thay đổi code ngay lập tức)
npm run dev    # → http://localhost:3000

# Build production
npm run build  # → tsc (kiểm tra kiểu) && vite build → tạo thư mục dist/
```

**Cấu hình Chính vite.config.ts:**

```typescript
export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        proxy: {
            // Proxy request /api đến backend khi phát triển
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
            // Proxy WebSocket
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

### 9.2 Build Backend (Gradle)

```bash
# Build (tạo JAR)
./gradlew build           # → build/libs/weighing-0.0.1-SNAPSHOT.jar

# Chạy test
./gradlew test

# Thực thi
java -jar build/libs/weighing-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
# hoặc
./gradlew bootRun         # Khi phát triển
```

### 9.3 Kiến trúc Triển khai

```
GitHub (push lên nhánh main)
    │
    ├──→ Vercel (Triển khai frontend tự động)
    │    ├── Chạy npm run build
    │    ├── Host file tĩnh dist/
    │    └── Cấu hình proxy API (vercel.json)
    │         /api/* → Railway backend
    │         /ws/*  → Railway backend
    │
    └──→ Railway (Triển khai backend tự động)
         ├── Chạy Gradle build
         ├── Thực thi file JAR
         ├── PostgreSQL (instance quản lý)
         └── Redis (instance quản lý)
```

### 9.4 Quản lý Biến Môi trường

```yaml
# Backend: application-prod.yml (tiêm qua biến môi trường Railway)
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
  secret: ${JWT_SECRET}       # KHÔNG BAO GIỜ hardcode!
```

**Nguyên tắc Quản lý Biến Môi trường:**
- Mật khẩu, API key, JWT secret, v.v. **KHÔNG BAO GIỜ** được đặt trong code
- File `.env` phải được thêm vào `.gitignore`
- Sử dụng tính năng biến môi trường của Railway, Vercel, v.v.

---

## 10. Thiết lập Môi trường Phát triển

### 10.1 Phần mềm Bắt buộc

| Phần mềm | Phiên bản | Mục đích |
|----------|---------|---------|
| JDK | 17+ | Chạy Backend |
| Node.js | 18+ | Chạy Frontend |
| Git | Latest | Quản lý phiên bản |
| IntelliJ IDEA | Latest | IDE Backend |
| VS Code | Latest | IDE Frontend |
| Flutter SDK | 3.10+ | Phát triển mobile |
| Android Studio | Latest | Trình giả lập Android |

### 10.2 Chạy Backend Cục bộ

```bash
cd backend

# Chạy với profile dev (H2 in-memory DB + Redis nhúng)
./gradlew bootRun

# Hoặc trong IntelliJ:
# 1. Mở WeighingApplication.java
# 2. Click nút ▶ bên cạnh main()
# 3. Đặt Environment: spring.profiles.active=dev

# Kiểm tra Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### 10.3 Chạy Frontend Cục bộ

```bash
cd frontend

# Cài đặt dependency (lần đầu hoặc khi package.json thay đổi)
npm install

# Khởi chạy server phát triển
npm run dev    # → http://localhost:3000

# Kiểm tra kiểu + build production
npm run build
```

### 10.4 Chạy Ứng dụng Di động Cục bộ

```bash
cd mobile

# Cài đặt dependency
flutter pub get

# Chạy trên trình giả lập
flutter run

# Bật dữ liệu mock: lib/config/api_config.dart
# static const bool useMockData = true;
```

### 10.5 Extension VS Code Được Khuyến nghị

| Extension | Mục đích |
|-----------|---------|
| ESLint | Phân tích code JavaScript/TypeScript |
| Prettier | Định dạng code |
| TypeScript Importer | Tự động thêm import |
| ES7+ React Snippets | Đoạn code React |
| Dart | Hỗ trợ ngôn ngữ Dart |
| Flutter | Công cụ phát triển Flutter |

### 10.6 Plugin IntelliJ Được Khuyến nghị

| Plugin | Mục đích |
|--------|---------|
| Lombok | Hỗ trợ annotation Lombok |
| Spring Boot Assistant | Auto-completion cấu hình |
| Database Tools | Trình duyệt DB |
| GitToolBox | Hiển thị trạng thái Git |

---

## 11. Quy ước Code và Mẫu thiết kế

### 11.1 Quy tắc Đặt tên

| Đối tượng | Quy ước | Ví dụ |
|----------|---------|-------|
| **Lớp Java** | PascalCase | `DispatchService`, `WeighingController` |
| **Phương thức/biến Java** | camelCase | `findByPlateNumber`, `dispatchDate` |
| **Hằng số Java** | UPPER_SNAKE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| **Package Java** | lowercase | `com.dongkuk.weighing.dispatch` |
| **Bảng/cột DB** | snake_case | `weighing_records`, `plate_number` |
| **Component React** | PascalCase | `DispatchPage`, `SortableTable` |
| **File React** | PascalCase.tsx | `DispatchPage.tsx`, `MyPage.tsx` |
| **Biến/hàm TypeScript** | camelCase | `handleSubmit`, `fetchData` |
| **Interface TypeScript** | PascalCase | `Dispatch`, `ApiResponse<T>` |
| **Lớp CSS** | kebab-case | `main-layout`, `search-bar` |
| **Lớp Flutter** | PascalCase | `DispatchListScreen` |
| **File Flutter** | snake_case.dart | `dispatch_list_screen.dart` |
| **API endpoint** | kebab-case | `/gate-passes`, `/weighing-records` |
| **Trường JSON** | snake_case | `dispatch_date`, `plate_number` |

### 11.2 Commit Message Git

```
<type>: <mô tả>

type:
  feat:     Tính năng mới
  fix:      Sửa lỗi
  refactor: Tái cấu trúc (không thay đổi chức năng)
  docs:     Thay đổi tài liệu
  style:    Kiểu code (định dạng, v.v.)
  test:     Thêm/sửa test
  chore:    Cấu hình build, package, v.v.

Ví dụ:
  feat: 배차 검색 필터 기능 추가
  fix: 토큰 갱신 시 무한 루프 수정
  refactor: 계량 서비스 교차 검증 로직 분리
```

### 11.3 Quy tắc Thiết kế API

```
Tiêu chí Chọn HTTP Method:
  GET    → Truy vấn dữ liệu (không thay đổi)
  POST   → Tạo dữ liệu (thêm tài nguyên mới)
  PUT    → Sửa dữ liệu (cập nhật toàn bộ)
  PATCH  → Sửa dữ liệu (cập nhật một phần)
  DELETE → Xóa dữ liệu

Thiết kế URL:
  GET    /api/v1/dispatches           → Truy vấn danh sách điều phối
  GET    /api/v1/dispatches/123       → Truy vấn chi tiết điều phối
  POST   /api/v1/dispatches           → Tạo điều phối
  PUT    /api/v1/dispatches/123       → Cập nhật điều phối
  DELETE /api/v1/dispatches/123       → Xóa điều phối
  PUT    /api/v1/dispatches/123/cancel → Hủy điều phối (thay đổi trạng thái)

Lưu ý:
  ✅ /api/v1/dispatches       (số nhiều)
  ❌ /api/v1/dispatch          (số ít)
  ✅ /api/v1/gate-passes      (kebab-case)
  ❌ /api/v1/gatePasses        (camelCase)
```

### 11.4 Mẫu Xử lý Lỗi

```
Backend:
  - Lỗi nghiệp vụ → throw new BusinessException(ErrorCode.XXXX)
  - Xác thực đầu vào thất bại → @Valid + MethodArgumentNotValidException (tự động)
  - GlobalExceptionHandler chuyển thành JSON response nhất quán

Frontend:
  - Bọc API call trong try-catch
  - Thành công: message.success('저장되었습니다')
  - Thất bại: message.error(thông báo lỗi) hoặc hiển thị lỗi từ server
  - Xác thực form thất bại: Ant Design Form tự động hiển thị thông báo lỗi
```

---

## 12. Các lỗi thường gặp và Lưu ý

### 12.1 Backend

| Lỗi | Mô tả | Giải pháp |
|-----|-------|----------|
| Vấn đề N+1 | Truy vấn entity liên quan lặp đi lặp lại gây ra hàng loạt query | Sử dụng `@EntityGraph` hoặc `JOIN FETCH` |
| LazyInitializationException | Truy cập object lazy-load ngoài transaction | Chuyển đổi sang DTO chỉ với dữ liệu cần thiết |
| Thiếu `@Transactional` | Thay đổi dữ liệu với transaction chỉ đọc | Thêm `@Transactional` vào phương thức CUD trong Service |
| Trả về Entity trực tiếp | Tham chiếu vòng, lộ thông tin bảo mật | Chuyển đổi sang DTO trước khi trả về |
| Lưu mật khẩu dạng text | Lỗ hổng bảo mật | Hash bằng BCrypt trước khi lưu |
| Hardcode JWT Secret | Lỗ hổng bảo mật | Tiêm từ bên ngoài qua biến môi trường |

**Ví dụ Vấn đề N+1:**

```java
// ❌ Code gây ra vấn đề N+1
List<Dispatch> dispatches = dispatchRepository.findAll();  // 1 query
for (Dispatch d : dispatches) {
    d.getVehicle().getPlateNumber();  // Query bổ sung cho mỗi điều phối!
}
// Kết quả: 101 query cho 100 điều phối (1 + 100)

// ✅ Giải quyết với JOIN FETCH
@Query("SELECT d FROM Dispatch d JOIN FETCH d.vehicle")
List<Dispatch> findAllWithVehicle();  // Giải quyết với một JOIN query duy nhất
```

### 12.2 Frontend

| Lỗi | Mô tả | Giải pháp |
|-----|-------|----------|
| Vòng lặp vô hạn useEffect | Object/hàm tạo lại mỗi lần render trong mảng dependency | Sử dụng `useCallback`, `useMemo` |
| Thay đổi state trực tiếp | Sửa trực tiếp như `state.push(item)` | `setState([...state, item])` tạo mảng mới |
| Thiếu key prop | Không chỉ định key khi render danh sách | `<Component key={item.id} />` |
| Không xử lý token hết hạn | Người dùng phải đăng nhập lại thủ công khi lỗi 401 | Tự động làm mới trong Axios interceptor |
| Rò rỉ bộ nhớ | Gọi setState sau khi unmount | Dọn dẹp trong cleanup function của useEffect |
| Lạm dụng kiểu `any` | Mất lợi ích của TypeScript | Định nghĩa kiểu hoặc interface rõ ràng |

**Lỗi Thay đổi State:**

```tsx
// ❌ Không bao giờ sửa trực tiếp (React không phát hiện được thay đổi)
const [items, setItems] = useState<string[]>([]);
items.push('새 아이템');         // Sửa trực tiếp → UI không cập nhật

// ✅ Tạo mảng mới và gọi setState
setItems([...items, '새 아이템']);    // Sao chép với spread operator + thêm
setItems(prev => [...prev, '새 아이템']);  // Dựa trên giá trị trước (an toàn hơn)

// Xóa
setItems(prev => prev.filter(item => item !== '삭제할 아이템'));

// Cập nhật
setItems(prev => prev.map(item =>
    item.id === targetId ? { ...item, name: '새이름' } : item
));
```

### 12.3 Flutter

| Lỗi | Mô tả | Giải pháp |
|-----|-------|----------|
| Gọi setState quá nhiều | Rebuild không cần thiết | Tách state với Provider |
| Sử dụng BuildContext trong async | Lỗi khi sử dụng context trong hàm async | Kiểm tra `mounted` trước khi sử dụng |
| Thiếu serialization | Quên code chuyển đổi JSON → model | Triển khai factory method `fromJson` |

### 12.4 Chung

| Lỗi | Mô tả | Giải pháp |
|-----|-------|----------|
| Commit file `.env` | Mật khẩu, API key bị push lên Git | Thêm vào `.gitignore` |
| Lỗi CORS | Không khớp domain frontend-backend | Kiểm tra cấu hình CORS backend |
| Không khớp múi giờ | Nhầm lẫn UTC vs KST | Server lưu UTC, chuyển KST khi hiển thị |
| Vấn đề phân biệt hoa/thường | Windows không phân biệt, Linux phân biệt | Sử dụng đặt tên nhất quán |

---

## 13. Các Mẫu Frontend Mới Thêm Gần đây

### 13.1 Page Registry (pageRegistry.ts)

Tất cả trang được quản lý tập trung trong `config/pageRegistry.ts`. Khi thêm trang mới, chỉ cần sửa file này và menu sidebar, tab navigation, kiểm soát quyền sẽ tự động được áp dụng.

```typescript
// config/pageRegistry.ts
export interface PageConfig {
  component: React.LazyExoticComponent<React.FC>; // React.lazy code splitting
  title: string;        // Tiêu đề hiển thị menu/tab
  icon: React.ReactNode; // Icon menu (Ant Design Icons)
  closable: boolean;     // Có thể đóng tab hay không
  roles?: ('ADMIN' | 'MANAGER' | 'DRIVER')[]; // Vai trò có quyền truy cập
}

// Ví dụ thêm trang mới
const NewPage = React.lazy(() => import('../pages/NewPage'));

export const PAGE_REGISTRY: Record<string, PageConfig> = {
  '/new-page': {
    component: NewPage,
    title: '새 페이지',
    icon: React.createElement(SomeIcon),
    closable: true,
    roles: ['ADMIN', 'MANAGER'], // Bỏ qua để cho phép tất cả truy cập
  },
  // ...các trang hiện có
};
```

**Điểm Chính:**
- `React.lazy` cho code splitting → JS bundle chỉ load khi truy cập trang
- `closable: false` dùng cho tab cố định như điều khiển trạm cân
- Thêm vào mảng `PINNED_TABS` để tự động mở tab khi khởi động ứng dụng
- `MAX_TABS = 10` giới hạn số tab tối đa

### 13.2 Authentication Context (AuthContext.tsx)

`AuthContext` quản lý trạng thái xác thực toàn cục. Duy trì trạng thái đăng nhập dựa trên token trong `localStorage` và tự động xử lý làm mới token.

```tsx
// Cách sử dụng
import { useAuth } from '../context/AuthContext';

const MyComponent: React.FC = () => {
    const { user, isAuthenticated, logout } = useAuth();

    if (!isAuthenticated) return <Navigate to="/login" />;

    return <div>안녕하세요, {user?.userName}님</div>;
};
```

### 13.3 Hook Quản lý Trạng thái CRUD (useCrudState.ts)

Các trang dữ liệu chủ (công ty vận tải, xe, trạm cân, mã chung) đều theo cùng mẫu CRUD. `useCrudState` tái sử dụng mẫu này.

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

### 13.4 Component Chung MasterCrudPage

Component chung chuẩn hóa layout và hành vi của trang CRUD dữ liệu chủ. Tự động cấu hình form tìm kiếm, bảng dữ liệu và modal tạo/sửa.

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

### 13.5 Hook Gọi API (useApiCall.ts)

Hook tự động quản lý trạng thái loading/thành công/lỗi cho API call.

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

### 13.6 Phím tắt Bàn phím (useKeyboardShortcuts.ts)

Hook đăng ký phím tắt cho từng trang. Tự động hủy đăng ký khi component unmount.

```tsx
useKeyboardShortcuts([
    { key: 'n', ctrl: true, handler: () => setCreateModalOpen(true), description: '신규 등록' },
    { key: 'f', ctrl: true, handler: () => searchInputRef.current?.focus(), description: '검색' },
    { key: 'Escape', handler: () => setModalOpen(false), description: '모달 닫기' },
]);
```

### 13.7 Hook WebSocket (useWebSocket.ts)

Hook quản lý kết nối WebSocket dựa trên giao thức STOMP. Tự động xử lý kết nối/kết nối lại/đăng ký.

```tsx
const { connected, subscribe, publish } = useWebSocket({
    url: '/ws',
    onConnect: () => console.log('WebSocket 연결됨'),
});

// Đăng ký
useEffect(() => {
    if (connected) {
        subscribe('/topic/weighing-updates', (message) => {
            const data = JSON.parse(message.body);
            updateWeighingStatus(data);
        });
    }
}, [connected]);
```

### 13.8 Kiến trúc Điều khiển Trạm Cân

`WeighingStationPage` là trang chuyên dụng giám sát thời gian thực trạm cân tại hiện trường. Bao gồm nhiều component con:

```
WeighingStationPage
├── ConnectionStatusBar    → Trạng thái kết nối thiết bị (indicator, LPR, bảng điện tử, thanh chắn)
├── WeightDisplay          → Hiển thị trọng lượng thời gian thực (số lớn, trạng thái ổn định/không ổn định)
├── VehicleInfoPanel       → Thông tin xe/điều phối đang cân
├── ProcessStateBar        → Hiển thị giai đoạn tiến trình cân (vào→phát hiện→chụp→nhận dạng→cân→hoàn thành)
├── ActionButtons          → Nút hành động bắt đầu/hoàn thành/hủy cân
├── ModeToggle             → Chuyển đổi chế độ tự động/thủ công
├── ManualControls         → Bảng điều khiển thủ công cho chế độ thủ công
├── WeighingHistoryTable   → Bảng lịch sử cân gần đây
├── StatusLog              → Log sự kiện thiết bị/hệ thống
└── SimulatorPanel         → Trình giả lập phần cứng phát triển
```

**Luồng Dữ liệu:**
- Hook `useWeighingStation`: Logic nghiệp vụ cân (quản lý trạng thái, gọi API)
- Hook `useWeighingStationSocket`: Nhận dữ liệu thời gian thực qua WebSocket
- `weighingStationApi.ts`: Gọi REST API chuyên dụng cho trạm cân

### 13.9 Cấu trúc Tab Dashboard

`DashboardPage` gồm 3 tab:

| Tab | Component | Nội dung |
|-----|-----------|---------|
| Tổng quan | `OverviewTab` | Thẻ KPI (AnimatedNumber), biểu đồ xu hướng hàng ngày, tỷ lệ hàng hóa |
| Thời gian thực | `RealtimeTab` | Trạng thái cân thời gian thực dựa trên WebSocket, trạng thái trạm cân |
| Phân tích | `AnalysisTab` | Biểu đồ thống kê chi tiết, phân tích theo kỳ/điều kiện |

### 13.10 Cấu hình ECharts

ECharts 6.0 yêu cầu đăng ký thủ công chỉ những component cần thiết cho tree-shaking:

```typescript
// utils/echartsSetup.ts - Gọi một lần khi khởi động ứng dụng
import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);
```

```typescript
// utils/chartOptions.ts - Tùy chọn biểu đồ chung
export const createLineChartOption = (data: DailyStatistics[]) => ({
    // ... tùy chọn biểu đồ chuẩn hóa
});
```

---

## 14. Các Mẫu Mobile Mới Thêm Gần đây

### 14.1 Dịch vụ Cache Offline

`offline_cache_service.dart` cache dữ liệu thiết yếu vào local bằng SharedPreferences. Thông tin cơ bản có thể hiển thị ngay cả khi mạng không ổn định.

```dart
class OfflineCacheService {
    // Cache danh sách điều phối
    Future<void> cacheDispatches(List<Dispatch> dispatches);
    Future<List<Dispatch>?> getCachedDispatches();

    // Kiểm tra cache hết hạn
    bool isCacheExpired(String key, {Duration maxAge = const Duration(hours: 1)});
}
```

**Lưu ý**: SharedPreferences phù hợp cho lượng dữ liệu nhỏ. Đối với dữ liệu lớn, hãy cân nhắc sử dụng SQLite.

### 14.2 Tiện ích Toast

`utils/toast_utils.dart` chuẩn hóa thông báo dựa trên SnackBar:

```dart
ToastUtils.showSuccess(context, '배차가 등록되었습니다.');
ToastUtils.showError(context, '네트워크 오류가 발생했습니다.');
ToastUtils.showWarning(context, 'OTP가 곧 만료됩니다.');
```

### 14.3 Cấu trúc Màn hình Di động

```
Trang chủ (HomeScreen)
├── Danh sách Điều phối (DispatchListScreen)
│   └── Chi tiết Điều phối (DispatchDetailScreen)
├── Cân
│   ├── Nhập OTP (OtpInputScreen)
│   └── Tiến trình Cân (WeighingProgressScreen)
├── Phiếu Cân Điện tử
│   ├── Danh sách (SlipListScreen)
│   └── Chi tiết (SlipDetailScreen)
├── Lịch sử (HistoryScreen)
├── Thông báo (NoticeScreen)
├── Danh sách Thông báo (NotificationListScreen)
└── Đăng nhập OTP (OtpLoginScreen)
```

---

## 15. Các Mẫu Desktop Mới Thêm Gần đây

### 15.1 Splash Form

`SplashForm.cs` hiển thị trạng thái khởi tạo khi khởi động ứng dụng:
- Load file cấu hình
- Xác minh kết nối API backend
- Xác minh kết nối thiết bị phần cứng (indicator, LPR, bảng điện tử, thanh chắn)
- Chuyển sang MainForm sau khi khởi tạo hoàn tất

### 15.2 Mẫu Interface Phần cứng

Tất cả thiết bị phần cứng được trừu tượng hóa qua interface. Cả thiết bị thật và simulator đều implement cùng interface:

```csharp
// Định nghĩa interface
public interface ILprCamera {
    Task<LprCaptureResult> CaptureAsync();
    bool IsConnected { get; }
}

// Implementation thực (production)
public class LprCamera : ILprCamera { ... }

// Simulator (phát triển)
public class LprCameraSimulator : ILprCamera { ... }
```

### 15.3 Bộ Điều phối Quy trình Cân

`WeighingProcessService` quản lý toàn bộ quy trình cân:

```
Phát hiện xe → Chụp LPR → Xác minh AI → Khớp điều phối → Bắt đầu cân
→ Chờ ổn định trọng lượng → Ghi trọng lượng → Cập nhật bảng điện tử → Mở thanh chắn
→ Gửi đến API server → Hoàn thành
```

### 15.4 Test xUnit

Các test đơn vị được viết cho dịch vụ cốt lõi của chương trình desktop:
- `ApiServiceTests.cs`: Test gọi REST API
- `IndicatorServiceTests.cs`: Test phân tích dữ liệu indicator
- `LocalCacheServiceTests.cs`: Test CRUD cache SQLite

```bash
cd weighing-cs
dotnet test      # Chạy test xUnit
```

---

## 16. Các Module Backend Mới Thêm Gần đây

Khi dự án mở rộng, các module backend sau đã được bổ sung. Mỗi module tuân theo cùng cấu trúc phân tầng (Controller → Service → Repository → Entity) như các module hiện có.

### 16.1 Yêu thích (favorite)

Module quản lý việc đăng ký và quản lý yêu thích cho menu, điều phối, xe, công ty vận tải và trạm cân theo từng người dùng. Tích hợp với component `FavoriteButton`/`FavoritesList` ở frontend.

**Cấu trúc Package:**

```
favorite/
├── controller/   FavoriteController
├── domain/       Favorite (Entity), FavoriteType (Enum), FavoriteRepository
├── dto/          FavoriteCreateRequest, FavoriteCheckRequest, FavoriteReorderRequest, FavoriteResponse
└── service/      FavoriteService
```

**Các trường chính Entity:**

```java
@Entity
@Table(name = "favorites")
public class Favorite extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favoriteId;

    private Long userId;
    @Enumerated(EnumType.STRING)
    private FavoriteType favoriteType;  // MENU, DISPATCH, VEHICLE, COMPANY, SCALE
    private String targetId;
    private String targetPath;
    private String displayName;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `GET` | `/api/v1/favorites` | Danh sách toàn bộ yêu thích | Tất cả |
| `GET` | `/api/v1/favorites/type/{type}` | Danh sách yêu thích theo loại | Tất cả |
| `POST` | `/api/v1/favorites` | Đăng ký yêu thích | Tất cả |
| `DELETE` | `/api/v1/favorites/{favoriteId}` | Xóa yêu thích | Tất cả |
| `POST` | `/api/v1/favorites/toggle` | Toggle đăng ký/hủy yêu thích | Tất cả |
| `POST` | `/api/v1/favorites/check` | Kiểm tra đã đăng ký yêu thích chưa | Tất cả |
| `PUT` | `/api/v1/favorites/reorder` | Thay đổi thứ tự yêu thích (kéo thả) | Tất cả |

**Quy tắc Nghiệp vụ:**
- Mỗi người dùng có thể đăng ký tối đa 20 mục yêu thích
- Ngăn đăng ký trùng lặp cùng đối tượng
- Khi thay đổi thứ tự bằng kéo thả, `sortOrder` được cập nhật hàng loạt

### 16.2 Hướng dẫn Sử dụng / FAQ (help)

Module quản lý FAQ. Người dùng có thể xem FAQ theo danh mục, quản trị viên có thể tạo/sửa/xóa FAQ.

**Cấu trúc Package:**

```
help/
├── controller/   HelpController
├── domain/       Faq (Entity), FaqCategory (Enum), FaqRepository
├── dto/          FaqCreateRequest, FaqUpdateRequest, FaqResponse
└── service/      HelpService
```

**Các trường chính Entity:**

```java
@Entity
@Table(name = "faqs")
public class Faq extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long faqId;

    private String question;
    @Column(columnDefinition = "TEXT")
    private String answer;
    @Enumerated(EnumType.STRING)
    private FaqCategory category;  // WEIGHING, DISPATCH, ACCOUNT, SYSTEM, OTHER
    private Integer sortOrder;
    private Boolean isPublished;
    private Long viewCount;
}
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `GET` | `/api/v1/help/faqs` | Toàn bộ danh sách FAQ công khai | Tất cả |
| `GET` | `/api/v1/help/faqs/category/{category}` | FAQ theo danh mục | Tất cả |
| `GET` | `/api/v1/help/faqs/{faqId}` | Chi tiết FAQ (tăng lượt xem) | Tất cả |
| `GET` | `/api/v1/help/faqs/admin` | Danh sách FAQ cho quản trị viên (bao gồm chưa công khai) | ADMIN |
| `POST` | `/api/v1/help/faqs` | Tạo FAQ | ADMIN |
| `PUT` | `/api/v1/help/faqs/{faqId}` | Sửa FAQ | ADMIN |
| `DELETE` | `/api/v1/help/faqs/{faqId}` | Xóa FAQ | ADMIN |

### 16.3 Giám sát Thiết bị (monitoring)

Module giám sát trạng thái thời gian thực của thiết bị phần cứng tại trạm cân (trạm cân, camera LPR, indicator, thanh chắn). Gửi thông báo thời gian thực đến frontend qua WebSocket khi trạng thái thiết bị thay đổi.

**Cấu trúc Package:**

```
monitoring/
├── controller/   DeviceMonitoringController
├── domain/       DeviceStatus (Entity), DeviceType (Enum), ConnectionStatus (Enum), DeviceStatusRepository
├── dto/          DeviceStatusResponse, DeviceStatusUpdateRequest, DeviceSummaryResponse
└── service/      DeviceMonitoringService
```

**Các trường chính Entity:**

```java
@Entity
@Table(name = "device_statuses")
public class DeviceStatus extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceId;

    private String deviceCode;
    private String deviceName;
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;        // SCALE, LPR_CAMERA, INDICATOR, BARRIER_GATE
    private String location;
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus;  // ONLINE, OFFLINE, ERROR
    private LocalDateTime lastConnectedAt;
    private String ipAddress;
    private String errorMessage;
    private Boolean isActive;
}
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `GET` | `/api/v1/monitoring/devices` | Danh sách toàn bộ thiết bị | Tất cả |
| `GET` | `/api/v1/monitoring/devices/type/{deviceType}` | Thiết bị theo loại | Tất cả |
| `GET` | `/api/v1/monitoring/devices/{deviceId}` | Chi tiết thiết bị | Tất cả |
| `PUT` | `/api/v1/monitoring/devices/{deviceId}/status` | Cập nhật trạng thái thiết bị | Tất cả |
| `GET` | `/api/v1/monitoring/summary` | Thống kê tóm tắt trạng thái thiết bị | Tất cả |
| `POST` | `/api/v1/monitoring/health-check` | Chạy kiểm tra health toàn bộ thiết bị | ADMIN, MANAGER |

**Quy tắc Nghiệp vụ:**
- Nếu thiết bị không phản hồi trong 5 phút, tự động chuyển sang trạng thái `OFFLINE`
- Khi trạng thái thiết bị thay đổi, broadcast thời gian thực qua WebSocket topic `/topic/equipment-status`
- Trực quan hóa trạng thái tại `MonitoringPage` ở frontend và `ConnectionStatusPanel` ở desktop

### 16.4 Trang Cá nhân (mypage)

Module quản lý xem/sửa hồ sơ cá nhân, đổi mật khẩu và cài đặt thông báo của người dùng.

**Cấu trúc Package:**

```
mypage/
├── controller/   MyPageController
├── dto/          MyPageResponse, ProfileUpdateRequest, PasswordChangeRequest, NotificationSettingsRequest
└── service/      MyPageService
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `GET` | `/api/v1/mypage` | Xem hồ sơ cá nhân | Tất cả |
| `PUT` | `/api/v1/mypage/profile` | Sửa hồ sơ (tên, số điện thoại, v.v.) | Tất cả |
| `PUT` | `/api/v1/mypage/password` | Đổi mật khẩu | Tất cả |
| `PUT` | `/api/v1/mypage/notifications` | Thay đổi cài đặt nhận thông báo | Tất cả |

**Quy tắc Đổi Mật khẩu:**
- Bắt buộc xác minh mật khẩu hiện tại
- Xác minh mật khẩu mới và xác nhận mật khẩu trùng khớp
- Tối thiểu 8 ký tự (Bean Validation: `@Size(min = 8)`)
- Hash bằng BCrypt trước khi lưu

### 16.5 Thông báo / Tin tức (notice)

Module quản lý thông báo hệ thống, hướng dẫn bảo trì, thông báo cập nhật, v.v. Hỗ trợ tính năng ghim lên đầu (pin) và tìm kiếm.

**Cấu trúc Package:**

```
notice/
├── controller/   NoticeController
├── domain/       Notice (Entity), NoticeCategory (Enum), NoticeRepository
├── dto/          NoticeCreateRequest, NoticeUpdateRequest, NoticeResponse
└── service/      NoticeService
```

**Các trường chính Entity:**

```java
@Entity
@Table(name = "notices")
public class Notice extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Enumerated(EnumType.STRING)
    private NoticeCategory category;  // SYSTEM, MAINTENANCE, UPDATE, GENERAL
    private Long authorId;
    private String authorName;
    private Boolean isPublished;
    private Boolean isPinned;
    private LocalDateTime publishedAt;
    private Long viewCount;
}
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `GET` | `/api/v1/notices` | Danh sách thông báo công khai (phân trang) | Tất cả |
| `GET` | `/api/v1/notices/category/{category}` | Theo danh mục | Tất cả |
| `GET` | `/api/v1/notices/pinned` | Thông báo ghim đầu trang | Tất cả |
| `GET` | `/api/v1/notices/search?keyword=` | Tìm kiếm theo từ khóa | Tất cả |
| `GET` | `/api/v1/notices/{noticeId}` | Chi tiết thông báo (tăng lượt xem) | Tất cả |
| `GET` | `/api/v1/notices/admin` | Danh sách cho quản trị viên (bao gồm chưa công khai) | ADMIN |
| `POST` | `/api/v1/notices` | Tạo thông báo | ADMIN |
| `PUT` | `/api/v1/notices/{noticeId}` | Sửa thông báo | ADMIN |
| `DELETE` | `/api/v1/notices/{noticeId}` | Xóa thông báo | ADMIN |
| `PATCH` | `/api/v1/notices/{noticeId}/publish` | Chuyển đổi công khai/ẩn | ADMIN |
| `PATCH` | `/api/v1/notices/{noticeId}/pin` | Chuyển đổi ghim/bỏ ghim | ADMIN |

**Quy tắc Nghiệp vụ:**
- Khi truy vấn danh sách, thông báo ghim (`isPinned = true`) luôn hiển thị trên cùng
- Hỗ trợ phân trang (Spring Data Pageable)
- Chức năng quản lý (tạo/sửa/xóa/công khai/ghim) chỉ dành cho ADMIN

### 16.6 Cài đặt Hệ thống (setting)

Module quản lý các giá trị cài đặt toàn hệ thống. Tất cả endpoint chỉ dành cho ADMIN.

**Cấu trúc Package:**

```
setting/
├── controller/   SystemSettingController
├── domain/       SystemSetting (Entity), SettingType (Enum), SettingCategory (Enum), SystemSettingRepository
├── dto/          SystemSettingResponse, SystemSettingUpdateRequest, BulkUpdateRequest
└── service/      SystemSettingService
```

**Các trường chính Entity:**

```java
@Entity
@Table(name = "system_settings")
public class SystemSetting extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingId;

    @Column(unique = true)
    private String settingKey;
    private String settingValue;
    @Enumerated(EnumType.STRING)
    private SettingType settingType;        // STRING, NUMBER, BOOLEAN, JSON
    @Enumerated(EnumType.STRING)
    private SettingCategory category;       // GENERAL, WEIGHING, NOTIFICATION, SECURITY
    private String description;
    private Boolean isEditable;
}
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `GET` | `/api/v1/admin/settings` | Danh sách toàn bộ cài đặt | ADMIN |
| `GET` | `/api/v1/admin/settings/category/{category}` | Cài đặt theo danh mục | ADMIN |
| `PUT` | `/api/v1/admin/settings/{settingId}` | Sửa cài đặt riêng lẻ | ADMIN |
| `PUT` | `/api/v1/admin/settings/bulk` | Sửa cài đặt hàng loạt | ADMIN |

**Quy tắc Nghiệp vụ:**
- Cài đặt có `isEditable = false` sẽ ném `BusinessException` khi cố sửa
- Khi sửa cài đặt, xác thực định dạng giá trị theo `settingType` (NUMBER -> parse số, BOOLEAN -> true/false, JSON -> JSON hợp lệ)
- API sửa hàng loạt cho phép thay đổi nhiều cài đặt trong một transaction

### 16.7 Yêu cầu Hỗ trợ / Khiếu nại (inquiry)

Module quản lý ghi nhận cuộc gọi hỗ trợ. Tiếp nhận và ghi nhận kết quả xử lý các yêu cầu liên quan đến cân/điều phối từ tài xế hoặc công ty vận tải.

**Cấu trúc Package:**

```
inquiry/
├── controller/   InquiryCallController
├── domain/       InquiryCall (Entity), InquiryType (Enum), InquiryCallRepository
├── dto/          InquiryCallCreateRequest, InquiryCallResponse
└── service/      InquiryCallService
```

**Các trường chính Entity:**

```java
@Entity
@Table(name = "inquiry_calls")
public class InquiryCall extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryCallId;

    private Long callerId;
    private String callerName;
    private String callerPhone;
    @Enumerated(EnumType.STRING)
    private InquiryType inquiryType;  // WEIGHING_ISSUE, DISPATCH_ISSUE, SYSTEM_ERROR,
                                       // GENERAL_INQUIRY, COMPLAINT, OTHER
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String content;
    private Long dispatchId;    // Điều phối liên quan (tùy chọn)
    private Long weighingId;    // Lượt cân liên quan (tùy chọn)
    private Long handlerId;     // Người xử lý
    @Column(columnDefinition = "TEXT")
    private String handlerNote; // Ghi chú xử lý
}
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `POST` | `/api/v1/inquiries/call-log` | Ghi nhận cuộc gọi hỗ trợ | Tất cả |
| `GET` | `/api/v1/inquiries/call-log` | Toàn bộ danh sách yêu cầu hỗ trợ | ADMIN, MANAGER |
| `GET` | `/api/v1/inquiries/call-log/my` | Yêu cầu hỗ trợ do tôi đăng ký | Tất cả |

### 16.8 Thống kê / Báo cáo (statistics)

Module cung cấp thống kê cân theo ngày/tháng và xuất Excel. Sử dụng thư viện Apache POI.

**Cấu trúc Package:**

```
statistics/
├── controller/   StatisticsController
├── dto/          DailyStatisticsResponse, MonthlyStatisticsResponse, StatisticsSummaryResponse
└── service/      StatisticsService
```

**API Endpoint:**

| HTTP | Đường dẫn | Mô tả | Quyền |
|------|-----------|-------|-------|
| `GET` | `/api/v1/statistics/daily` | Thống kê theo ngày | Tất cả |
| `GET` | `/api/v1/statistics/monthly` | Thống kê theo tháng | Tất cả |
| `GET` | `/api/v1/statistics/summary` | Tóm tắt thống kê (KPI) | Tất cả |
| `GET` | `/api/v1/statistics/export` | Xuất file Excel | ADMIN, MANAGER |

**Tham số Query chung:**

| Tham số | Kiểu | Mô tả |
|---------|------|-------|
| `date_from` | LocalDate | Ngày bắt đầu truy vấn |
| `date_to` | LocalDate | Ngày kết thúc truy vấn |
| `company_id` | Long | Bộ lọc công ty vận tải (tùy chọn) |
| `item_type` | String | Bộ lọc loại hàng hóa (tùy chọn) |

**Xuất Excel:**
- Sử dụng thư viện Apache POI (`poi-ooxml`)
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Tên file: `statistics_YYYYMMDD.xlsx`
- Dữ liệu thống kê được tổ chức theo từng sheet

### 16.9 Tóm tắt API Endpoint Các Module Bổ sung

Các mục bổ sung vào bảng tóm tắt API endpoint hiện có:

| Domain | Đường dẫn Gốc | Endpoint Chính |
|--------|---------------|----------------|
| Yêu thích | `/api/v1/favorites` | Danh sách, theo loại, đăng ký, xóa, toggle, thay đổi thứ tự |
| FAQ | `/api/v1/help` | Danh sách FAQ, theo danh mục, chi tiết, CRUD (ADMIN) |
| Giám sát Thiết bị | `/api/v1/monitoring` | Danh sách thiết bị, theo loại, cập nhật trạng thái, tóm tắt, health check |
| Trang Cá nhân | `/api/v1/mypage` | Xem/sửa hồ sơ, đổi mật khẩu, cài đặt thông báo |
| Thông báo | `/api/v1/notices` | Danh sách, tìm kiếm, chi tiết, ghim, CRUD (ADMIN) |
| Cài đặt Hệ thống | `/api/v1/admin/settings` | Toàn bộ/theo danh mục, sửa riêng lẻ/hàng loạt (ADMIN) |
| Yêu cầu Hỗ trợ | `/api/v1/inquiries` | Đăng ký yêu cầu, toàn bộ/của tôi |
| Thống kê | `/api/v1/statistics` | Thống kê ngày/tháng/tóm tắt, xuất Excel |

---

## 17. Các Component Frontend Mới Thêm Gần đây

### 17.1 Layout Trang Bảng (TablePageLayout.tsx)

Component layout tiêu chuẩn cho các trang có bảng dữ liệu. Cấu trúc layout 3 phần Flex gồm khu vực tìm kiếm, khu vực bảng và khu vực phân trang, đảm bảo bảng lấp đầy chính xác không gian còn lại.

```tsx
// components/TablePageLayout.tsx
<TablePageLayout>
    {/* FixedArea: Tìm kiếm/Bộ lọc (chiều cao cố định) */}
    <TablePageLayout.FixedArea>
        <SearchForm ... />
    </TablePageLayout.FixedArea>

    {/* ScrollArea: Bảng (lấp đầy không gian còn lại, cuộn) */}
    <TablePageLayout.ScrollArea>
        <SortableTable ... />
    </TablePageLayout.ScrollArea>

    {/* FixedArea: Phân trang (chiều cao cố định) */}
    <TablePageLayout.FixedArea>
        <Pagination ... />
    </TablePageLayout.FixedArea>
</TablePageLayout>
```

**Thuộc tính CSS Chính:**
- Cấp cao nhất: `height: 100%`, `display: flex`, `flexDirection: column`
- ScrollArea: `flex: 1`, `minHeight: 0` (thuộc tính then chốt ngăn phần tử con Flex tràn phần tử cha)
- FixedArea: Giữ chiều cao tự nhiên (flex-shrink: 0)

**Lý do Sử dụng:**
- Để đặt `scroll.y` của Ant Design Table thành `100%` thay vì giá trị px cố định, layout cha phải có chiều cao chính xác
- Trước đây cần hardcode như `calc(100vh - XXXpx)`, component này tự động tính toán

### 17.2 Style Cuộn Bảng (tableScroll.css)

File CSS toàn cục triển khai header cố định + body cuộn cho bảng.

```css
/* Ẩn thanh cuộn (Firefox) */
.st-fill-height .ant-table-body {
    scrollbar-width: none;
}

/* Ẩn thanh cuộn (Chrome, Safari, Edge) */
.st-fill-height .ant-table-body::-webkit-scrollbar {
    display: none;
}
```

**Class `.st-fill-height`:**
- Tự động áp dụng khi component `SortableTable` có `scroll.y` được thiết lập
- Cấu hình để cố định header bảng và chỉ cuộn body
- Ẩn thanh cuộn để giữ UI gọn gàng (cuộn bằng chuột/cảm ứng vẫn hoạt động bình thường)

### 17.3 Cải tiến MainLayout

Các tính năng sau đã được thêm vào `layouts/MainLayout.tsx`:

**Sidebar:**
- Chiều rộng 240px, có thể thu gọn/mở rộng
- Lọc từ `PAGE_REGISTRY` và chỉ hiển thị menu phù hợp với vai trò người dùng hiện tại
- Tích hợp nút yêu thích (`FavoriteButton`)

**Header:**
- Áp dụng `backdrop-filter: blur(12px)` cho hiệu ứng kính trong suốt
- Bố trí nút yêu thích, toggle theme (dark/light), menu người dùng

**Menu Ngữ cảnh Đa tab:**
- Click chuột phải vào tab hiển thị menu ngữ cảnh:
  - Đóng: Đóng tab đó
  - Đóng tất cả tab khác: Chỉ giữ tab đã chọn và tab ghim
  - Đóng tất cả tab bên phải: Đóng tất cả tab bên phải tab đã chọn
  - Đóng tất cả tab: Chỉ giữ tab ghim

**Phím tắt:**
- `Ctrl+W`: Đóng tab hiện tại
- `Ctrl+Tab`: Chuyển sang tab tiếp theo

**Lọc Menu theo Vai trò:**
- Tự động ẩn menu mà người dùng hiện tại không có quyền truy cập dựa trên cài đặt `roles` trong `PAGE_REGISTRY`
- Ví dụ: Vai trò DRIVER sẽ không thấy menu quản trị (`/admin/*`, `/master/*`) trong sidebar

### 17.4 Cải tiến SortableTable

Các tính năng sau đã được thêm vào `components/SortableTable.tsx`:

**Lưu Thứ tự Cột (localStorage):**
- Tự động lưu thứ tự cột mà người dùng thay đổi bằng kéo thả vào `localStorage`
- Key lưu trữ: `table-column-order-{tableKey}` (tableKey là định danh duy nhất theo trang)
- Tự động khôi phục thứ tự đã lưu khi truy cập lần sau

```tsx
<SortableTable
    tableKey="dispatch-table"   // Key lưu trong localStorage
    columns={columns}
    dataSource={data}
    scroll={{ y: '100%' }}
/>
```

**Loading Skeleton:**
- Hiển thị placeholder dạng hàng bằng component Ant Design Skeleton khi đang tải dữ liệu
- Cung cấp trải nghiệm loading tự nhiên hơn so với Spin loading truyền thống

**Nút Reset Thứ tự Cột:**
- Hiển thị nút reset thứ tự cột ở đầu bảng
- Click để xóa giá trị lưu trong localStorage và khôi phục thứ tự cột ban đầu

**CSS Class fill-height:**
- Khi giá trị `scroll.y` được thiết lập, tự động áp dụng CSS class `st-fill-height` cho bảng
- Liên kết với `tableScroll.css` để triển khai header cố định + body cuộn

---

## Phụ lục: Từ điển Thuật ngữ Chính

| Thuật ngữ | Mô tả |
|-----------|-------|
| **REST API** | Phương pháp thiết kế API thao tác tài nguyên bằng HTTP method (GET/POST/PUT/DELETE) |
| **SPA** | Single Page Application. Ứng dụng web thay đổi component mà không chuyển trang |
| **ORM** | Object-Relational Mapping. Công nghệ ánh xạ object với bảng DB (JPA) |
| **DTO** | Data Transfer Object. Cấu trúc dữ liệu dùng cho API request/response |
| **DI** | Dependency Injection. Mẫu nhận object từ bên ngoài thay vì tạo trực tiếp |
| **JWT** | JSON Web Token. Token xác thực do server cấp |
| **STOMP** | Giao thức nhắn tin trên WebSocket (mẫu Pub/Sub) |
| **HMR** | Hot Module Replacement. Phản ánh thay đổi code mà không cần refresh trình duyệt |
| **CORS** | Cross-Origin Resource Sharing. Cấu hình cho phép gọi API từ domain khác |
| **HikariCP** | Thư viện connection pool JDBC (cải thiện hiệu suất bằng cách tái sử dụng kết nối DB) |
| **FCM** | Firebase Cloud Messaging. Dịch vụ thông báo đẩy của Google |
| **Dirty Checking** | Tính năng JPA tự động phát hiện thay đổi Entity và tạo UPDATE query |
| **Bean** | Object được Spring quản lý (Controller, Service, Repository, v.v.) |
| **Profile** | Tính năng phân tách cấu hình theo môi trường của Spring (dev, prod, test) |
| **Interceptor** | Middleware chặn request/response để xử lý chung (dùng trong cả Axios và Spring) |
| **LPR** | License Plate Recognition. Công nghệ nhận dạng biển số xe tự động |
| **OTP** | One-Time Password. Mật khẩu bảo mật dùng một lần (dựa trên Redis, TTL 5 phút) |
| **Indicator** | Thiết bị hiển thị/truyền giá trị trọng lượng từ trạm cân (giao tiếp nối tiếp) |
| **Bảng điện tử** | Màn hình LED hiển thị OTP, hướng dẫn cân, v.v. (giao tiếp TCP) |
| **Thanh chắn** | Thanh chắn tự động tại lối vào/ra trạm cân (giao tiếp TCP) |
| **Tree-shaking** | Kỹ thuật tối ưu tự động loại bỏ code không sử dụng khi build |
| **Code Splitting** | Kỹ thuật tách JS bundle theo trang sử dụng React.lazy |
| **@dnd-kit** | Thư viện kéo thả React (dùng cho sắp xếp hàng bảng) |
| **Apache POI** | Thư viện Apache để tạo/chỉnh sửa file Excel trong Java |
| **fill-height** | Mẫu CSS giúp bảng lấp đầy chiều cao phần tử cha và triển khai header cố định + body cuộn |

---

> **Lời khuyên cuối**: Đừng cố ghi nhớ tài liệu này cùng một lúc. Hãy tham khảo khi cần trong quá trình đọc code thực tế. Cách học tốt nhất là trực tiếp sửa đổi code dự án và xác minh cách nó hoạt động.

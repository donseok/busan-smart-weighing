# Auth Module Detailed Design Document

**Version**: 1.0
**Date**: 2026-01-27
**Reference Document**: auth-basic-design.md (Basic Design Document)
**Module**: Authentication & User Management
**Status**: Draft

---

## 1. Class Diagram

### 1.1 Overall Class Relationship Diagram

```mermaid
classDiagram
    direction TB

    %% === Global Layer ===
    class ApiResponse~T~ {
        -boolean success
        -T data
        -String message
        -LocalDateTime timestamp
        +ok(T data) ApiResponse~T~$
        +ok(T data, String message) ApiResponse~T~$
        +error(ErrorCode code) ApiResponse~Void~$
        +error(ErrorCode code, String detail) ApiResponse~Void~$
    }

    class ErrorCode {
        <<enumeration>>
        AUTH_001(401, "Login ID or password does not match")
        AUTH_002(401, "Deactivated account")
        AUTH_003(423, "Account is locked")
        AUTH_004(401, "Refresh Token has expired")
        AUTH_005(401, "Invalid Refresh Token")
        AUTH_006(401, "Access Token has expired")
        AUTH_007(403, "Access denied")
        OTP_001(400, "OTP has expired or is invalid")
        OTP_002(400, "Unregistered phone number")
        OTP_003(423, "OTP verification attempt limit exceeded")
        OTP_004(400, "OTP code does not match")
        USER_001(404, "User not found")
        USER_002(409, "Login ID already registered")
        USER_003(400, "Invalid user information")
        INTERNAL_ERROR(500, "Internal server error")
        -int status
        -String message
    }

    class BusinessException {
        -ErrorCode errorCode
        -String detail
        +BusinessException(ErrorCode)
        +BusinessException(ErrorCode, String)
        +getStatus() int
    }

    class GlobalExceptionHandler {
        +handleBusinessException(BusinessException) ResponseEntity
        +handleValidationException(MethodArgumentNotValidException) ResponseEntity
        +handleAccessDeniedException(AccessDeniedException) ResponseEntity
        +handleException(Exception) ResponseEntity
    }

    class BaseEntity {
        <<abstract>>
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    BusinessException --> ErrorCode
    GlobalExceptionHandler ..> BusinessException
    GlobalExceptionHandler ..> ApiResponse

    %% === User Domain ===
    class User {
        -Long userId
        -Long companyId
        -String userName
        -String phoneNumber
        -UserRole userRole
        -String loginId
        -String passwordHash
        -boolean isActive
        -int failedLoginCount
        -LocalDateTime lockedUntil
        +authenticate(String rawPassword, PasswordEncoder encoder) boolean
        +incrementFailedLogin(int maxAttempts, Duration lockDuration) void
        +resetFailedLogin() void
        +isLocked() boolean
        +activate() void
        +deactivate() void
    }

    class UserRole {
        <<enumeration>>
        ADMIN("Administrator")
        MANAGER("Manager")
        DRIVER("Driver")
        -String description
        +includes(UserRole other) boolean
    }

    class UserRepository {
        <<interface>>
        +findByLoginId(String loginId) Optional~User~
        +findByPhoneNumber(String phoneNumber) Optional~User~
        +existsByLoginId(String loginId) boolean
    }

    User --|> BaseEntity
    User --> UserRole
    UserRepository ..> User

    %% === Auth Module ===
    class AuthController {
        -AuthService authService
        +login(LoginRequest) ResponseEntity
        +loginOtp(OtpLoginRequest) ResponseEntity
        +refresh(TokenRefreshRequest) ResponseEntity
        +logout(HttpServletRequest) ResponseEntity
    }

    class AuthService {
        -UserRepository userRepository
        -JwtTokenProvider jwtTokenProvider
        -PasswordEncoder passwordEncoder
        -RedisTemplate redisTemplate
        -AuthProperties authProperties
        +login(LoginRequest) LoginResponse
        +loginByOtp(OtpLoginRequest) LoginResponse
        +refresh(String refreshToken) TokenResponse
        +logout(String accessToken) void
    }

    class LoginRequest {
        -String loginId
        -String password
        -DeviceType deviceType
    }

    class OtpLoginRequest {
        -String phoneNumber
        -String authCode
        -DeviceType deviceType
    }

    class TokenRefreshRequest {
        -String refreshToken
    }

    class LoginResponse {
        -String accessToken
        -String refreshToken
        -String tokenType
        -long expiresIn
        -UserInfo user
    }

    class TokenResponse {
        -String accessToken
        -String tokenType
        -long expiresIn
    }

    class UserInfo {
        -Long userId
        -String userName
        -String userRole
        -String companyName
    }

    class DeviceType {
        <<enumeration>>
        WEB
        MOBILE
    }

    AuthController --> AuthService
    AuthController ..> LoginRequest
    AuthController ..> OtpLoginRequest
    AuthController ..> TokenRefreshRequest
    AuthService --> UserRepository
    AuthService --> JwtTokenProvider
    AuthService ..> LoginResponse
    AuthService ..> TokenResponse
    LoginResponse --> UserInfo

    %% === JWT ===
    class JwtTokenProvider {
        -JwtProperties jwtProperties
        -SecretKey secretKey
        +generateAccessToken(User user, DeviceType deviceType) String
        +generateRefreshToken(User user, DeviceType deviceType) String
        +validateToken(String token) boolean
        +extractClaims(String token) Claims
        +extractUserId(String token) Long
        +extractJti(String token) String
        +getRemainingExpiration(String token) long
    }

    class JwtProperties {
        -String secret
        -long accessTokenExpiration
        -long refreshTokenExpiration
        -String issuer
    }

    class JwtAuthenticationFilter {
        -JwtTokenProvider jwtTokenProvider
        -CustomUserDetailsService userDetailsService
        -RedisTemplate redisTemplate
        #doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain) void
        -resolveToken(HttpServletRequest) String
        -isBlacklisted(String jti) boolean
    }

    class UserPrincipal {
        -Long userId
        -String loginId
        -UserRole role
        -Long companyId
        +getAuthorities() Collection~GrantedAuthority~
    }

    class CustomUserDetailsService {
        -UserRepository userRepository
        +loadUserByUsername(String loginId) UserDetails
    }

    JwtAuthenticationFilter --> JwtTokenProvider
    JwtAuthenticationFilter --> CustomUserDetailsService
    JwtTokenProvider --> JwtProperties
    CustomUserDetailsService --> UserRepository
    CustomUserDetailsService ..> UserPrincipal

    %% === OTP Module ===
    class OtpController {
        -OtpService otpService
        +generate(OtpGenerateRequest) ResponseEntity
        +verify(OtpVerifyRequest) ResponseEntity
    }

    class OtpService {
        -RedisTemplate redisTemplate
        -UserRepository userRepository
        -OtpSessionRepository otpSessionRepository
        -OtpProperties otpProperties
        +generate(OtpGenerateRequest) OtpGenerateResponse
        +verify(OtpVerifyRequest) OtpVerifyResponse
        -generateOtpCode() String
        -buildRedisKey(String prefix, String key) String
    }

    class OtpGenerateRequest {
        -Long scaleId
        -Long vehicleId
        -String plateNumber
    }

    class OtpVerifyRequest {
        -String otpCode
        -String phoneNumber
    }

    class OtpGenerateResponse {
        -String otpCode
        -LocalDateTime expiresAt
        -int ttlSeconds
    }

    class OtpVerifyResponse {
        -boolean verified
        -Long vehicleId
        -String plateNumber
        -Long dispatchId
    }

    class OtpSession {
        -Long otpId
        -Long userId
        -String otpCode
        -Long vehicleId
        -String phoneNumber
        -Long scaleId
        -LocalDateTime expiresAt
        -boolean isVerified
        -int failedAttempts
    }

    class OtpSessionRepository {
        <<interface>>
        +findByOtpCodeAndExpiresAtAfter(String code, LocalDateTime now) Optional~OtpSession~
    }

    class OtpProperties {
        -int codeLength
        -int ttlSeconds
        -int maxFailedAttempts
    }

    OtpController --> OtpService
    OtpService --> UserRepository
    OtpService --> OtpSessionRepository
    OtpService --> OtpProperties
    OtpSessionRepository ..> OtpSession
    OtpSession --|> BaseEntity

    %% === Security Config ===
    class SecurityConfig {
        -JwtAuthenticationFilter jwtFilter
        -JwtAuthenticationEntryPoint entryPoint
        +securityFilterChain(HttpSecurity) SecurityFilterChain
    }

    class JwtAuthenticationEntryPoint {
        +commence(HttpServletRequest, HttpServletResponse, AuthenticationException) void
    }

    SecurityConfig --> JwtAuthenticationFilter
    SecurityConfig --> JwtAuthenticationEntryPoint
```

---

## 2. DTO Detailed Specification

### 2.1 Request DTOs

#### LoginRequest
```java
public record LoginRequest(
    @NotBlank(message = "Please enter your login ID")
    @Size(min = 3, max = 50, message = "Login ID must be 3~50 characters")
    String loginId,

    @NotBlank(message = "Please enter your password")
    @Size(min = 8, max = 100, message = "Password must be 8~100 characters")
    String password,

    @NotNull(message = "Please select a device type")
    DeviceType deviceType
) {}
```

#### OtpLoginRequest
```java
public record OtpLoginRequest(
    @NotBlank(message = "Please enter your phone number")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
             message = "Invalid phone number format (e.g., 010-1234-5678)")
    String phoneNumber,

    @NotBlank(message = "Please enter the verification code")
    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be a 6-digit number")
    String authCode,

    @NotNull(message = "Please select a device type")
    DeviceType deviceType
) {}
```

#### TokenRefreshRequest
```java
public record TokenRefreshRequest(
    @NotBlank(message = "Please provide a Refresh Token")
    String refreshToken
) {}
```

#### OtpGenerateRequest
```java
public record OtpGenerateRequest(
    @NotNull(message = "Please provide the scale ID")
    Long scaleId,

    @NotNull(message = "Please provide the vehicle ID")
    Long vehicleId,

    @NotBlank(message = "Please provide the plate number")
    @Size(max = 20)
    String plateNumber
) {}
```

#### OtpVerifyRequest
```java
public record OtpVerifyRequest(
    @NotBlank(message = "Please enter the OTP code")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit number")
    String otpCode,

    @NotBlank(message = "Please enter your phone number")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
             message = "Invalid phone number format")
    String phoneNumber
) {}
```

#### UserCreateRequest
```java
public record UserCreateRequest(
    @NotBlank @Size(min = 3, max = 50)
    String loginId,

    @NotBlank @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
             message = "Password must contain both letters and numbers")
    String password,

    @NotBlank @Size(max = 50)
    String userName,

    @NotBlank
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$")
    String phoneNumber,

    @NotNull
    UserRole userRole,

    Long companyId
) {}
```

### 2.2 Response DTOs

#### LoginResponse
```java
public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,   // Fixed as "Bearer"
    long expiresIn,     // Seconds until Access Token expiry (1800)
    UserInfo user
) {
    public record UserInfo(
        Long userId,
        String userName,
        String userRole,
        String companyName
    ) {}

    public static LoginResponse of(String accessToken, String refreshToken,
                                    long expiresIn, User user, String companyName) {
        return new LoginResponse(
            accessToken, refreshToken, "Bearer", expiresIn,
            new UserInfo(user.getUserId(), user.getUserName(),
                         user.getUserRole().name(), companyName)
        );
    }
}
```

#### TokenResponse
```java
public record TokenResponse(
    String accessToken,
    String tokenType,   // Fixed as "Bearer"
    long expiresIn      // 1800
) {
    public static TokenResponse of(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
```

#### OtpGenerateResponse
```java
public record OtpGenerateResponse(
    String otpCode,
    LocalDateTime expiresAt,
    int ttlSeconds
) {}
```

#### OtpVerifyResponse
```java
public record OtpVerifyResponse(
    boolean verified,
    Long vehicleId,
    String plateNumber,
    Long dispatchId
) {}
```

#### UserResponse
```java
public record UserResponse(
    Long userId,
    String userName,
    String phoneNumber,  // Masked: 010-****-5678
    String userRole,
    String companyName,
    boolean isActive,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user, String companyName) {
        return new UserResponse(
            user.getUserId(),
            user.getUserName(),
            MaskingUtil.maskPhone(user.getPhoneNumber()),
            user.getUserRole().name(),
            companyName,
            user.isActive(),
            user.getCreatedAt()
        );
    }
}
```

### 2.3 Common Response Wrapper

#### ApiResponse
```java
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    ErrorDetail error,
    LocalDateTime timestamp
) {
    public record ErrorDetail(String code, String message) {}

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null, LocalDateTime.now());
    }

    public static ApiResponse<Void> error(ErrorCode code) {
        return new ApiResponse<>(false, null, null,
            new ErrorDetail(code.name(), code.getMessage()), LocalDateTime.now());
    }

    public static ApiResponse<Void> error(ErrorCode code, String detail) {
        return new ApiResponse<>(false, null, null,
            new ErrorDetail(code.name(), detail), LocalDateTime.now());
    }
}
```

---

## 3. Entity Detailed Design

### 3.1 BaseEntity (Common Audit Entity)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

### 3.2 User Entity

```java
@Entity
@Table(name = "tb_user", indexes = {
    @Index(name = "idx_user_login", columnList = "login_id", unique = true),
    @Index(name = "idx_user_phone", columnList = "phone_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // === Domain Methods ===

    public boolean authenticate(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.passwordHash);
    }

    public boolean isLocked() {
        if (lockedUntil == null) return false;
        if (LocalDateTime.now().isAfter(lockedUntil)) {
            // Lock duration has elapsed → auto-unlock
            unlock();
            return false;
        }
        return true;
    }

    public void incrementFailedLogin() {
        this.failedLoginCount++;
        if (this.failedLoginCount >= MAX_FAILED_ATTEMPTS) {
            lock();
        }
    }

    public void resetFailedLogin() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    private void lock() {
        this.lockedUntil = LocalDateTime.now().plus(LOCK_DURATION);
    }

    private void unlock() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    public void activate() { this.isActive = true; }
    public void deactivate() { this.isActive = false; }

    // === Builder ===
    @Builder
    public User(Long companyId, String userName, String phoneNumber,
                UserRole userRole, String loginId, String passwordHash) {
        this.companyId = companyId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
    }
}
```

### 3.3 UserRole Enum

```java
@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("Administrator"),
    MANAGER("Manager"),
    DRIVER("Driver");

    private final String description;

    /**
     * Hierarchical permission inclusion check.
     * ADMIN includes MANAGER and DRIVER permissions.
     * MANAGER includes DRIVER permissions.
     */
    public boolean includes(UserRole other) {
        return this.ordinal() <= other.ordinal();
    }
}
```

### 3.4 OtpSession Entity

```java
@Entity
@Table(name = "tb_otp_session", indexes = {
    @Index(name = "idx_otp_code_expires", columnList = "otp_code, expires_at"),
    @Index(name = "idx_otp_phone", columnList = "phone_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OtpSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long otpId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "scale_id")
    private Long scaleId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public OtpSession(Long userId, String otpCode, Long vehicleId,
                      String phoneNumber, Long scaleId, LocalDateTime expiresAt) {
        this.userId = userId;
        this.otpCode = otpCode;
        this.vehicleId = vehicleId;
        this.phoneNumber = phoneNumber;
        this.scaleId = scaleId;
        this.expiresAt = expiresAt;
    }

    public void markVerified() { this.isVerified = true; }
    public void incrementFailedAttempts() { this.failedAttempts++; }
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isMaxAttemptsReached(int max) { return failedAttempts >= max; }
}
```

---

## 4. Service Method Signatures

### 4.1 AuthService

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * ID/PW Login.
     * - Find user → check lock status → verify password → issue tokens
     * - On failure, increment failedLoginCount; lock for 30 min upon reaching 5 attempts
     *
     * @throws BusinessException AUTH_001 Password mismatch
     * @throws BusinessException AUTH_002 Inactive account
     * @throws BusinessException AUTH_003 Account locked
     */
    @Transactional
    public LoginResponse login(LoginRequest request) { ... }

    /**
     * OTP-based login (Mobile secure login).
     * - Find user by phone number → verify auth code → issue tokens
     *
     * @throws BusinessException AUTH_001 Authentication failed
     * @throws BusinessException AUTH_002 Inactive account
     */
    @Transactional
    public LoginResponse loginByOtp(OtpLoginRequest request) { ... }

    /**
     * Access Token refresh.
     * - Validate Refresh Token → compare with Redis stored value → issue new Access Token
     *
     * @throws BusinessException AUTH_004 Expired Refresh Token
     * @throws BusinessException AUTH_005 Invalid Refresh Token
     */
    public TokenResponse refresh(String refreshToken) { ... }

    /**
     * Logout.
     * - Delete Refresh Token from Redis
     * - Add Access Token JTI to blacklist (TTL = remaining lifetime)
     */
    @Transactional
    public void logout(String accessToken) { ... }

    // === Private Helpers ===

    private void validateUserActive(User user) { ... }
    private void validateNotLocked(User user) { ... }
    private void storeRefreshToken(Long userId, DeviceType deviceType,
                                    String refreshToken) { ... }
    private String buildRefreshKey(Long userId, DeviceType deviceType) { ... }
}
```

### 4.2 OtpService

```java
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final OtpSessionRepository otpSessionRepository;
    private final OtpProperties otpProperties;
    private final ObjectMapper objectMapper;

    /**
     * OTP Generation (Weighing Station CS → Display Board).
     * - Generate 6-digit code with SecureRandom
     * - Store in Redis with 5-minute TTL
     * - Save audit log record in DB
     */
    @Transactional
    public OtpGenerateResponse generate(OtpGenerateRequest request) { ... }

    /**
     * OTP Verification (Mobile input).
     * - Look up OTP session from Redis
     * - Check failure count (invalidate if > 3 attempts)
     * - Match user/vehicle by phone number
     * - Delete Redis key on success (single-use)
     *
     * @throws BusinessException OTP_001 Expired/not found
     * @throws BusinessException OTP_002 Unregistered phone number
     * @throws BusinessException OTP_003 Failure count exceeded
     * @throws BusinessException OTP_004 Code mismatch
     */
    @Transactional
    public OtpVerifyResponse verify(OtpVerifyRequest request) { ... }

    // === Private Helpers ===

    private String generateOtpCode() { ... }  // SecureRandom 6-digit
    private String codeKey(String otpCode) { ... }  // "otp:code:{code}"
    private String scaleKey(Long scaleId) { ... }   // "otp:scale:{id}"
    private String failKey(String otpCode) { ... }  // "otp:fail:{code}"
}
```

### 4.3 UserService

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create user.
     * @throws BusinessException USER_002 Duplicate login ID
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) { ... }

    /**
     * Get user by ID.
     * @throws BusinessException USER_001 Not found
     */
    public UserResponse getUser(Long userId) { ... }

    /**
     * Get user list (paginated).
     */
    public Page<UserResponse> getUsers(Pageable pageable) { ... }

    /**
     * Toggle user active/inactive status.
     * @throws BusinessException USER_001 Not found
     */
    @Transactional
    public void toggleActive(Long userId) { ... }

    /**
     * Manual account unlock (by ADMIN).
     * @throws BusinessException USER_001 Not found
     */
    @Transactional
    public void unlockAccount(Long userId) { ... }
}
```

### 4.4 JwtTokenProvider

```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate Access Token.
     * Claims: sub(userId), login_id, role, company_id, device_type, jti
     */
    public String generateAccessToken(User user, DeviceType deviceType) { ... }

    /**
     * Generate Refresh Token.
     * Claims: sub(userId), device_type, jti
     */
    public String generateRefreshToken(User user, DeviceType deviceType) { ... }

    /**
     * Validate token.
     * - Validates signature, expiration, and format
     * - Blacklist check is performed separately in the Filter
     */
    public boolean validateToken(String token) { ... }

    /** Extract Claims from token. */
    public Claims extractClaims(String token) { ... }

    /** Extract userId (sub). */
    public Long extractUserId(String token) { ... }

    /** Extract JTI (blacklist key). */
    public String extractJti(String token) { ... }

    /** Remaining expiration time (ms). Used for calculating logout blacklist TTL. */
    public long getRemainingExpiration(String token) { ... }
}
```

---

## 5. Configuration File Details

### 5.1 application.yml

```yaml
spring:
  application:
    name: weighing-api

  # === JPA / PostgreSQL ===
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:weighing}
    username: ${DB_USERNAME:weighing}
    password: ${DB_PASSWORD:weighing}
    driver-class-name: org.postgresql.Driver
    hikari:
      minimum-idle: 10
      maximum-pool-size: 30
      connection-timeout: 5000
      idle-timeout: 300000
      max-lifetime: 600000

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: public
        format_sql: true
        jdbc:
          time_zone: Asia/Seoul

  # === Redis ===
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 3000ms
      lettuce:
        pool:
          min-idle: 5
          max-idle: 10
          max-active: 20

  # === Jackson ===
  jackson:
    time-zone: Asia/Seoul
    date-format: yyyy-MM-dd'T'HH:mm:ssXXX
    serialization:
      write-dates-as-timestamps: false
    property-naming-strategy: SNAKE_CASE

# === JWT Properties ===
jwt:
  secret: ${JWT_SECRET}                    # Base64-encoded 256bit+ secret
  access-token-expiration: 1800000         # 30 min (ms)
  refresh-token-expiration: 604800000      # 7 days (ms)
  issuer: weighing-api

# === OTP Properties ===
otp:
  code-length: 6
  ttl-seconds: 300                         # 5 min
  max-failed-attempts: 3

# === API Key (Weighing Station CS Internal Auth) ===
api:
  internal-key: ${API_INTERNAL_KEY}

# === CORS ===
cors:
  allowed-origins:
    - ${CORS_ORIGIN_WEB:http://localhost:3000}
    - ${CORS_ORIGIN_MOBILE:http://localhost:8081}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600

# === Encryption ===
encryption:
  aes-key: ${AES_SECRET_KEY}               # AES-256 Base64-encoded key

# === Actuator ===
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: when_authorized

# === Logging ===
logging:
  level:
    com.dongkuk.weighing: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

### 5.2 JwtProperties

```java
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiration;   // ms
    private long refreshTokenExpiration;  // ms
    private String issuer;
}
```

### 5.3 OtpProperties

```java
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {
    private int codeLength = 6;
    private int ttlSeconds = 300;
    private int maxFailedAttempts = 3;
}
```

### 5.4 SecurityConfig

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                        // JWT-based Stateless → CSRF not needed
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(entryPoint))
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/login/otp",
                    "/api/v1/auth/refresh",
                    "/api/v1/otp/verify",
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**"
                ).permitAll()
                // Internal (API Key) - Handled by separate filter
                .requestMatchers("/api/v1/otp/generate").permitAll()
                // Role-based
                .requestMatchers(HttpMethod.DELETE, "/api/v1/dispatches/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/master/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/master/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/dispatches")
                    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/dispatches/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/v1/gate-passes/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                // All other APIs → authentication required
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // cost factor 12 (TRD requirement)
    }
}
```

### 5.5 RedisConfig

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

### 5.6 CorsConfig

```java
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins.toArray(String[]::new))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## 6. Exception Handling Strategy

### 6.1 Exception Hierarchy

```mermaid
classDiagram
    class RuntimeException {
        <<Java>>
    }
    class BusinessException {
        -ErrorCode errorCode
        -String detail
        +getStatus() int
        +getErrorCode() ErrorCode
    }
    class AuthenticationFailedException {
        +AuthenticationFailedException()
    }
    class AccountLockedException {
        -LocalDateTime lockedUntil
        +getRemainingMinutes() long
    }
    class OtpExpiredException {
        +OtpExpiredException()
    }
    class OtpMaxAttemptsException {
        +OtpMaxAttemptsException()
    }

    RuntimeException <|-- BusinessException
    BusinessException <|-- AuthenticationFailedException
    BusinessException <|-- AccountLockedException
    BusinessException <|-- OtpExpiredException
    BusinessException <|-- OtpMaxAttemptsException
```

### 6.2 BusinessException

```java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail != null ? detail : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public int getStatus() {
        return errorCode.getStatus();
    }
}
```

### 6.3 ErrorCode Enum

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_001(401, "Login ID or password does not match"),
    AUTH_002(401, "Deactivated account. Please contact the administrator"),
    AUTH_003(423, "Account is locked"),
    AUTH_004(401, "Refresh Token has expired. Please log in again"),
    AUTH_005(401, "Invalid Refresh Token"),
    AUTH_006(401, "Access Token has expired"),
    AUTH_007(403, "Access denied"),

    // OTP
    OTP_001(400, "OTP has expired or is invalid"),
    OTP_002(400, "Unregistered phone number"),
    OTP_003(423, "OTP invalidated due to exceeding verification attempts"),
    OTP_004(400, "OTP code does not match"),

    // User
    USER_001(404, "User not found"),
    USER_002(409, "Login ID already registered"),
    USER_003(400, "Invalid user information"),

    // Common
    VALIDATION_ERROR(400, "Input validation error"),
    INTERNAL_ERROR(500, "An internal server error occurred");

    private final int status;
    private final String message;
}
```

### 6.4 GlobalExceptionHandler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {} - {}", e.getErrorCode(), e.getMessage());
        return ResponseEntity
            .status(e.getStatus())
            .body(ApiResponse.error(e.getErrorCode(),
                  e.getDetail() != null ? e.getDetail() : e.getErrorCode().getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", message);
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ErrorCode.AUTH_007));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
```

---

## 7. Database DDL

### 7.1 tb_user

```sql
CREATE TABLE tb_user (
    user_id         BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NULL,
    user_name       VARCHAR(50)     NOT NULL,
    phone_number    VARCHAR(20)     NOT NULL,
    user_role       VARCHAR(20)     NOT NULL
                    CHECK (user_role IN ('ADMIN', 'MANAGER', 'DRIVER')),
    login_id        VARCHAR(50)     NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    failed_login_count INTEGER      NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ     NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE UNIQUE INDEX idx_user_login ON tb_user (login_id);
CREATE INDEX idx_user_phone ON tb_user (phone_number);
CREATE INDEX idx_user_company ON tb_user (company_id);
CREATE INDEX idx_user_role ON tb_user (user_role);

-- Foreign Key (after tb_company is created)
-- ALTER TABLE tb_user ADD CONSTRAINT fk_user_company
--     FOREIGN KEY (company_id) REFERENCES tb_company(company_id);

-- Auto-update updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER tr_user_updated_at
    BEFORE UPDATE ON tb_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE tb_user IS 'User table';
COMMENT ON COLUMN tb_user.user_role IS 'ADMIN: Administrator, MANAGER: Manager, DRIVER: Driver';
COMMENT ON COLUMN tb_user.password_hash IS 'bcrypt(cost=12) hash value';
COMMENT ON COLUMN tb_user.failed_login_count IS 'Consecutive login failure count (locked at 5)';
COMMENT ON COLUMN tb_user.locked_until IS 'Account lock release time (NULL = not locked)';
```

### 7.2 tb_otp_session

```sql
CREATE TABLE tb_otp_session (
    otp_id          BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NULL,
    otp_code        VARCHAR(6)      NOT NULL,
    vehicle_id      BIGINT          NULL,
    phone_number    VARCHAR(20)     NOT NULL,
    scale_id        BIGINT          NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    is_verified     BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_attempts INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_otp_code_expires ON tb_otp_session (otp_code, expires_at);
CREATE INDEX idx_otp_phone ON tb_otp_session (phone_number);
CREATE INDEX idx_otp_scale ON tb_otp_session (scale_id, created_at DESC);

-- Automatic cleanup of expired data (run periodically)
-- Separate scheduler deletes records older than 90 days

COMMENT ON TABLE tb_otp_session IS 'OTP session table (also serves as audit log)';
COMMENT ON COLUMN tb_otp_session.otp_code IS '6-digit OTP code';
COMMENT ON COLUMN tb_otp_session.expires_at IS 'Expiration time (5 min after creation)';
COMMENT ON COLUMN tb_otp_session.failed_attempts IS 'Verification failure count (invalidated after 3+ failures)';
```

### 7.3 Initial Data (Seed)

```sql
-- System administrator account (password: Admin1234!)
-- bcrypt hash for 'Admin1234!' with cost 12
INSERT INTO tb_user (user_name, phone_number, user_role, login_id, password_hash)
VALUES (
    '시스템관리자',
    '010-0000-0000',
    'ADMIN',
    'admin',
    '$2a$12$LJ3MFgfFw.PAGtv.Q0n.aeF8VPx4dSmA5WVUkRrXQJQNvk7z3K5Hm'
);
```

---

## 8. Redis Key Design Details

### 8.1 Key Naming Convention

```
{domain}:{entity}:{identifier}
```

### 8.2 Complete Key List

| Key Pattern | Value Type | TTL | Purpose | CRUD |
|-------------|-----------|-----|---------|------|
| `auth:refresh:{userId}:{deviceType}` | String (SHA-256 hash) | 7 days | Refresh Token storage | SET on login, GET on refresh, DEL on logout |
| `auth:blacklist:{jti}` | String "true" | Remaining Access Token lifetime | Invalidate logged-out Access Tokens | SET on logout, GET on every request |
| `otp:code:{otpCode}` | JSON (OtpSessionData) | 5 min | OTP session data | SET on generate, GET on verify, DEL on verify success |
| `otp:scale:{scaleId}` | String (otpCode) | 5 min | Current OTP code per weighing scale | SET on generate, DEL on verify |
| `otp:fail:{otpCode}` | String (count) | 5 min | OTP verification failure count | INCR on fail, GET on verify |
| `auth:login-otp:{phoneNumber}` | String (6-digit code) | 5 min | Login verification code | SET on request, GET on verify |

### 8.3 Redis Value Structures

#### auth:refresh value
```
SHA-256(refreshToken)
```
Simple hash string. Stores hash instead of the original token to prevent token theft in case of Redis exposure.

#### otp:code value (JSON)
```json
{
  "otpId": 1,
  "scaleId": 1,
  "vehicleId": 10,
  "plateNumber": "12가3456",
  "phoneNumber": "010-1234-5678",
  "createdAt": "2026-01-27T15:00:00"
}
```

### 8.4 Redis Failure Fallback

| Feature | Fallback Strategy |
|---------|-------------------|
| Refresh Token Verification | JWT self-validation only (blacklist disabled) |
| Access Token Blacklist | Disabled (immediate invalidation on logout not possible, wait for TTL) |
| OTP Session | Direct DB (tb_otp_session) lookup |
| OTP Failure Count | Use DB failedAttempts column |

---

## 9. Controller Details

### 9.1 AuthController

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, token refresh, and logout APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "ID/PW-based login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/login/otp")
    @Operation(summary = "OTP Login", description = "Phone number + verification code login (Mobile)")
    public ResponseEntity<ApiResponse<LoginResponse>> loginOtp(
            @Valid @RequestBody OtpLoginRequest request) {
        LoginResponse response = authService.loginByOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token Refresh", description = "Reissue Access Token using Refresh Token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout current session")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.ok(null, "Logout completed"));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        throw new BusinessException(ErrorCode.AUTH_006);
    }
}
```

### 9.2 OtpController

```java
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
@Tag(name = "OTP", description = "Weighing station OTP generation/verification APIs")
public class OtpController {

    private final OtpService otpService;

    @Value("${api.internal-key}")
    private String internalKey;

    @PostMapping("/generate")
    @Operation(summary = "Generate OTP", description = "Called from the weighing station CS program (Internal)")
    public ResponseEntity<ApiResponse<OtpGenerateResponse>> generate(
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody OtpGenerateRequest request) {
        validateApiKey(apiKey);
        OtpGenerateResponse response = otpService.generate(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "OTP input verification from mobile APP")
    public ResponseEntity<ApiResponse<OtpVerifyResponse>> verify(
            @Valid @RequestBody OtpVerifyRequest request) {
        OtpVerifyResponse response = otpService.verify(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private void validateApiKey(String apiKey) {
        if (!internalKey.equals(apiKey)) {
            throw new BusinessException(ErrorCode.AUTH_007);
        }
    }
}
```

### 9.3 UserController

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create User")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(response));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get User")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "List Users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> response = userService.getUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{userId}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle User Active/Inactive")
    public ResponseEntity<ApiResponse<Void>> toggleActive(
            @PathVariable Long userId) {
        userService.toggleActive(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock Account")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(
            @PathVariable Long userId) {
        userService.unlockAccount(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
```

---

## 10. JWT Authentication Filter Details

### 10.1 JwtAuthenticationFilter

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // Check blacklist
            String jti = jwtTokenProvider.extractJti(token);
            if (isBlacklisted(jti)) {
                log.debug("Blacklisted token: {}", jti);
                filterChain.doFilter(request, response);
                return;
            }

            // Set authentication info
            Long userId = jwtTokenProvider.extractUserId(token);
            Claims claims = jwtTokenProvider.extractClaims(token);

            UserPrincipal principal = new UserPrincipal(
                userId,
                claims.get("login_id", String.class),
                UserRole.valueOf(claims.get("role", String.class)),
                claims.get("company_id", Long.class)
            );

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.toString()
                .equals(redisTemplate.opsForValue().get("auth:blacklist:" + jti));
        } catch (Exception e) {
            log.warn("Redis blacklist check failed, skipping: {}", e.getMessage());
            return false;  // Pass through on Redis failure (Fallback)
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login")
            || path.equals("/api/v1/auth/refresh")
            || path.equals("/api/v1/otp/verify")
            || path.startsWith("/actuator")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui");
    }
}
```

### 10.2 UserPrincipal

```java
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String loginId;
    private final UserRole role;
    private final Long companyId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return loginId; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
```

### 10.3 JwtAuthenticationEntryPoint

```java
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @Override
    public void commence(HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException)
            throws IOException {
        log.debug("Unauthorized access: {} {}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
            ApiResponse.error(ErrorCode.AUTH_006));
    }
}
```

---

## 11. Utility Classes

### 11.1 EncryptionUtil (AES-256)

```java
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey secretKey;

    public EncryptionUtil(@Value("${encryption.aes-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * AES-256-GCM encryption.
     * @return Base64(IV + CipherText + Tag)
     */
    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * AES-256-GCM decryption.
     */
    public String decrypt(String cipherText) {
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
```

### 11.2 MaskingUtil

```java
public final class MaskingUtil {

    private MaskingUtil() {}

    /**
     * Phone number masking: 010-1234-5678 → 010-****-5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        return phone.replaceAll("(\\d{3})-?(\\d{3,4})-?(\\d{4})",
                                "$1-****-$3");
    }

    /**
     * Plate number masking: 12가3456 → 12가****
     */
    public static String maskPlateNumber(String plate) {
        if (plate == null || plate.length() < 4) return plate;
        int visibleLength = Math.max(plate.length() - 4, 0);
        return plate.substring(0, visibleLength + 1)
             + "****".substring(0, Math.min(4, plate.length() - visibleLength - 1));
    }
}
```

---

## 12. Gradle Dependencies (build.gradle)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'
    id 'io.spring.dependency-management' version '1.1.5'
}

group = 'com.dongkuk'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'

    // OpenAPI (Swagger)
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'

    // Database
    runtimeOnly 'org.postgresql:postgresql'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Configuration Processor
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'

    // Testcontainers (Integration Tests)
    testImplementation 'org.testcontainers:testcontainers:1.19.7'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.7'
    testImplementation 'org.testcontainers:postgresql:1.19.7'

    // Embedded Redis (Unit Tests)
    testImplementation 'it.ozimov:embedded-redis:0.7.3'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

---

## 13. Testing Strategy (TDD Preparation)

### 13.1 Test Layers

| Level | Target | Tools | Scope |
|-------|--------|-------|-------|
| **Unit Test** | Domain logic, services | JUnit 5 + Mockito | User, OtpSession domain, AuthService, OtpService |
| **Slice Test** | Controllers, repositories | @WebMvcTest, @DataJpaTest | API request/response, JPA queries |
| **Integration Test** | End-to-end flows | @SpringBootTest + Testcontainers | Login-to-logout E2E |

### 13.2 Test File Structure

```
src/test/java/com/dongkuk/weighing/
├── auth/
│   ├── controller/
│   │   └── AuthControllerTest.java          # @WebMvcTest
│   └── service/
│       └── AuthServiceTest.java             # @ExtendWith(MockitoExtension)
├── user/
│   ├── domain/
│   │   ├── UserTest.java                    # Domain logic unit tests
│   │   └── UserRepositoryTest.java          # @DataJpaTest
│   └── service/
│       └── UserServiceTest.java
├── otp/
│   ├── controller/
│   │   └── OtpControllerTest.java           # @WebMvcTest
│   └── service/
│       └── OtpServiceTest.java
├── global/
│   ├── security/
│   │   └── JwtTokenProviderTest.java
│   └── util/
│       ├── EncryptionUtilTest.java
│       └── MaskingUtilTest.java
└── integration/
    ├── AuthIntegrationTest.java             # @SpringBootTest + Testcontainers
    └── OtpIntegrationTest.java
```

### 13.3 TDD Implementation Order (3-Phase Preview)

```
Phase 1: Domain Tests → Domain Implementation
  ├── UserTest                → User Entity
  ├── UserRoleTest            → UserRole Enum
  ├── OtpSessionTest          → OtpSession Entity
  ├── EncryptionUtilTest      → EncryptionUtil
  └── MaskingUtilTest         → MaskingUtil

Phase 2: Service Tests → Service Implementation
  ├── JwtTokenProviderTest    → JwtTokenProvider
  ├── AuthServiceTest         → AuthService
  ├── OtpServiceTest          → OtpService
  └── UserServiceTest         → UserService

Phase 3: Controller + Integration Tests → Controller/Config Implementation
  ├── AuthControllerTest      → AuthController + SecurityConfig
  ├── OtpControllerTest       → OtpController
  ├── UserControllerTest      → UserController
  └── AuthIntegrationTest     → End-to-end flow verification
```

### 13.4 Key Test Case List

#### AuthService Tests
| # | Test Case | Verification Points |
|---|-----------|---------------------|
| 1 | Successful login | Access/Refresh Token issuance, Redis storage, response structure |
| 2 | Non-existent loginId | AUTH_001 exception |
| 3 | Password mismatch | AUTH_001, failedLoginCount increment |
| 4 | Lock after 5 failures | AUTH_003, lockedUntil set |
| 5 | Login attempt while locked | AUTH_003 exception |
| 6 | Login after lock duration elapsed | Successful login, lock released |
| 7 | Inactive account | AUTH_002 exception |
| 8 | Successful refresh | New Access Token issued |
| 9 | Expired Refresh Token | AUTH_004 exception |
| 10 | Refresh after logout | AUTH_005 exception (not in Redis) |
| 11 | Access Token after logout | Blacklist → rejected |

#### OtpService Tests
| # | Test Case | Verification Points |
|---|-----------|---------------------|
| 1 | Successful OTP generation | 6-digit code, Redis storage, DB storage |
| 2 | Successful OTP verification | verified=true, vehicle/dispatch match |
| 3 | Expired OTP verification | OTP_001 exception |
| 4 | Unregistered phone number | OTP_002 exception |
| 5 | Invalidation after 3 failures | OTP_003, Redis key deleted |
| 6 | Wrong code input | OTP_004, failure count increment |
| 7 | Reuse attempt after successful verification | OTP_001 (deleted as single-use) |

#### User Domain Tests
| # | Test Case | Verification Points |
|---|-----------|---------------------|
| 1 | authenticate success | bcrypt match returns true |
| 2 | authenticate failure | Returns false |
| 3 | incrementFailedLogin (1~4 times) | Count increases, not locked |
| 4 | incrementFailedLogin (5th time) | lockedUntil is set |
| 5 | isLocked - while locked | Returns true |
| 6 | isLocked - after duration elapsed | Returns false, auto-reset |
| 7 | resetFailedLogin | count=0, lockedUntil=null |

---

## 14. Deployment Environment Profiles

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/weighing_dev
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
logging:
  level:
    com.dongkuk.weighing: DEBUG
    org.hibernate.SQL: DEBUG

# application-staging.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/weighing_staging
  jpa:
    hibernate:
      ddl-auto: validate
logging:
  level:
    com.dongkuk.weighing: INFO

# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/weighing
    hikari:
      minimum-idle: 10
      maximum-pool-size: 30
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
logging:
  level:
    com.dongkuk.weighing: WARN
    org.hibernate.SQL: WARN
```

---

## 15. Audit Logging Design

### 15.1 Audit Events

| Event | Recorded Items | Log Level |
|-------|---------------|-----------|
| Login Success | userId, loginId, deviceType, IP, timestamp | INFO |
| Login Failure | loginId, failure reason, failure count, IP | WARN |
| Account Lock | userId, loginId, lockedUntil | WARN |
| Logout | userId, loginId, deviceType | INFO |
| OTP Generation | scaleId, vehicleId, plateNumber | INFO |
| OTP Verification Success | otpCode (masked), phoneNumber (masked), vehicleId | INFO |
| OTP Verification Failure | otpCode (masked), phoneNumber (masked), failure count | WARN |
| Access Denied | userId, request URI, required role | WARN |

### 15.2 Log Format

```
[AUDIT] {event} | userId={} | ip={} | detail={}
```

Examples:
```
[AUDIT] LOGIN_SUCCESS | userId=1 | ip=192.168.1.100 | detail=loginId=hong, device=MOBILE
[AUDIT] LOGIN_FAILED  | userId=null | ip=192.168.1.100 | detail=loginId=hong, reason=PASSWORD_MISMATCH, attempts=3
[AUDIT] ACCOUNT_LOCKED | userId=1 | ip=192.168.1.100 | detail=loginId=hong, lockedUntil=2026-01-27T15:30:00
```

---

*This document is the Auth Module Detailed Design Document that defines all implementation details based on the Basic Design Document.*
*In the 3-phase TDD implementation, tests will be written first according to this design, followed by the code implementation.*

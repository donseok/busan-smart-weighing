# BSW 보안 감사

JWT, CORS, 권한, 입력 검증 등 보안 설정을 종합 점검합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--focus [auth|cors|injection|all]`: 점검 초점 (기본: all)
- `--severity [critical|all]`: 출력 심각도 (기본: all)

### 점검 항목

1. **인증/인가 (Authentication & Authorization)**
   - `SecurityConfig.java` 분석
     - 공개 엔드포인트 목록 확인 (/api/v1/auth/login, /swagger-ui/** 등)
     - `@PreAuthorize` 누락 컨트롤러 탐지
   - JWT 설정 검증
     - `JwtProperties`: 토큰 만료 시간 (Access 30분, Refresh 7일)
     - 시크릿 키 강도 (환경 변수 기반 여부)
     - `JwtAuthenticationFilter` 토큰 검증 로직
   - Redis 블랙리스트
     - 로그아웃 토큰 무효화 로직 확인
     - Redis 연결 실패 시 폴백 처리

2. **CORS 설정**
   - `CorsConfig.java` 분석
   - 허용 Origin 목록 (CORS_ORIGIN_WEB 환경 변수)
   - 허용 메서드/헤더 범위
   - credentials 설정

3. **입력 검증 (Injection Prevention)**
   - Controller `@Valid` 사용 여부
   - DTO의 Jakarta Validation 어노테이션
   - JPA 파라미터 바인딩 (SQL Injection 방어)
   - 프론트엔드 입력 검증 (validators.ts)
   - XSS 방어: 출력 인코딩 확인

4. **데이터 보호**
   - `EncryptionUtil.java`: AES 암호화 사용처
   - `MaskingUtil.java`: 개인정보 마스킹 적용 범위
   - 민감정보 로그 출력 여부
   - flutter_secure_storage 사용 확인 (모바일)

5. **권한 매핑 일관성**
   - 백엔드 `@PreAuthorize` ↔ 프론트엔드 `pageRegistry.ts` roles 비교
   - ADMIN 전용 엔드포인트 식별
   - DRIVER 접근 제한 엔드포인트 확인

6. **WebSocket 보안**
   - JWT 인증 후 WebSocket 연결 허용 여부
   - STOMP 메시지 권한 검증

7. **리포트 형식**
   ```
   === BSW 보안 감사 리포트 ===

   🔴 CRITICAL: {count}건
   🟡 WARNING: {count}건
   🟢 INFO: {count}건

   [상세 내역]
   🔴 [AUTH] {Controller}에 @PreAuthorize 누락
   🟡 [CORS] 와일드카드 Origin 허용
   🟢 [CRYPTO] AES-256 암호화 적용 확인
   ```

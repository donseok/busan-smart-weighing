# BSW 종합 보안 점검 (Skill)

인증, 권한, 암호화, 입력 검증, WebSocket, 배포 보안을 종합 점검합니다.

## 인자
$ARGUMENTS

## 워크플로우

### Stage 1: 인증 시스템 점검

**JWT 설정**
- `JwtTokenProvider.java`: 토큰 생성/검증 로직
- `JwtProperties`: Access 30분, Refresh 7일 설정 확인
- 시크릿 키: 환경 변수(`JWT_SECRET`) 사용 여부, 하드코딩 검사
- `JwtAuthenticationFilter`: 필터 체인 순서, 예외 처리

**Redis 블랙리스트**
- 로그아웃 시 토큰 무효화 로직
- Redis 연결 실패 시 대체 처리 (폴백 없으면 로그인/로그아웃 불가)
- TTL 설정 (토큰 만료시간과 일치 여부)

**OTP 인증**
- `OtpService`: Redis 기반 OTP 생성/검증
- OTP TTL, 자릿수 설정 (`OtpProperties`)
- 브루트포스 방어 (시도 횟수 제한)

**모바일 인증**
- `flutter_secure_storage` 토큰 저장 확인
- OTP 로그인 플로우 보안

### Stage 2: 권한 시스템 점검

**백엔드**
- `SecurityConfig.java`: 공개/보호 엔드포인트 목록
- Controller별 `@PreAuthorize` 어노테이션 전수 조사
- 역할 계층: ADMIN > MANAGER > DRIVER

**프론트엔드**
- `pageRegistry.ts`: 페이지별 roles 설정
- `AuthContext.tsx`: 인증 가드 로직
- 미인증 접근 시 리다이렉트

**일관성**
- 백엔드 `@PreAuthorize` ↔ 프론트엔드 `roles` 매핑 비교
- 불일치 항목 식별

### Stage 3: 데이터 보호 점검

- `EncryptionUtil.java`: AES 암호화 키 관리 (AES_SECRET_KEY 환경 변수)
- `MaskingUtil.java`: 개인정보 마스킹 범위 (전화번호, 차량번호 등)
- 로그 내 민감정보 출력 검사
- API 응답 내 과다 정보 노출 검사 (비밀번호, 토큰 등)

### Stage 4: 입력 검증 점검

- Controller `@Valid` / `@Validated` 사용 여부
- DTO Jakarta Validation 적용 범위
- JPA 파라미터 바인딩 (SQL Injection 방어)
- 프론트엔드 `validators.ts` 검증 규칙 충분성
- XSS 방어: HTML 인코딩, Content-Security-Policy

### Stage 5: 통신 보안 점검

- CORS 설정 (`CorsConfig.java`): 허용 Origin 범위
- WebSocket 보안: JWT 인증 후 연결 허용 여부
- HTTPS 강제 여부 (프로덕션)
- API 키 관리: `API_INTERNAL_KEY` 환경 변수

### Stage 6: 배포 보안 점검

- 환경 변수 누출 검사 (.env 파일 gitignore 여부)
- 프로덕션 디버그 모드 비활성화 확인
- Swagger UI 프로덕션 비활성화 확인
- H2 Console 프로덕션 비활성화 확인

### 리포트 형식
```
=== BSW 종합 보안 점검 리포트 ===

[점검 일시: {날짜}]

🔴 CRITICAL ({count}건)
- [AUTH-001] JWT 시크릿 키 하드코딩 발견 (경로: ...)
- [AUTHZ-001] {Controller}에 @PreAuthorize 누락

🟡 WARNING ({count}건)
- [CORS-001] 개발 Origin이 프로덕션에도 허용됨
- [DATA-001] 로그에 차량번호 평문 출력

🟢 PASS ({count}건)
- [AUTH-P01] JWT 토큰 만료 설정 적절
- [CRYPTO-P01] AES-256 암호화 적용 확인

[권장 조치 우선순위]
1. CRITICAL 항목 즉시 수정
2. WARNING 항목 다음 릴리스 전 수정
```

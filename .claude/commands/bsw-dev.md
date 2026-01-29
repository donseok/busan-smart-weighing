# BSW 개발 서버 실행

백엔드와 프론트엔드 개발 서버를 동시에 실행합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--backend` 또는 `-b`: 백엔드만 실행
- `--frontend` 또는 `-f`: 프론트엔드만 실행
- `--mobile` 또는 `-m`: 모바일 개발 서버 실행
- `--all` 또는 `-a`: 백엔드 + 프론트엔드 + 모바일 전체 실행
- 인자 없음: 백엔드 + 프론트엔드 실행

### 실행 순서

1. **사전 점검**
   - `git status`로 현재 브랜치 확인
   - 각 모듈 디렉토리 존재 여부 확인

2. **백엔드 실행** (해당 시)
   - `cd backend && ./gradlew bootRun` (백그라운드)
   - dev 프로필 (H2 + embedded Redis) 자동 적용
   - 포트 8080 기동 확인
   - Swagger UI: http://localhost:8080/swagger-ui.html

3. **프론트엔드 실행** (해당 시)
   - `cd frontend && npm install && npm run dev` (백그라운드)
   - Vite 개발 서버 포트 3000
   - API 프록시: /api → localhost:8080, /ws → localhost:8080

4. **모바일 실행** (해당 시)
   - `cd mobile && flutter pub get && flutter run`
   - Mock 모드 안내: `lib/config/api_config.dart` → `useMockData`

5. **실행 결과 요약**
   - 실행된 모듈, 포트, 접속 URL 안내

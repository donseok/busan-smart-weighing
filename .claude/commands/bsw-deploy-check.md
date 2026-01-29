# BSW 배포 전 점검 (Skill)

Railway/Vercel 배포 전 전체 시스템을 종합 점검합니다.

## 인자
$ARGUMENTS

## 워크플로우

### Stage 1: Git 상태 점검
- 현재 브랜치 (main 여부)
- 미커밋 변경사항 확인
- 원격 저장소 동기화 상태 (`git status`, `git log origin/main..HEAD`)
- 미병합 PR 확인

### Stage 2: 백엔드 점검
```bash
cd backend && ./gradlew build
```
- Gradle 빌드 성공 여부
- 테스트 전체 통과 여부
- `application-prod.yml` 환경 변수 참조 확인:
  - DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
  - REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
  - JWT_SECRET, API_INTERNAL_KEY, AES_SECRET_KEY
  - CORS_ORIGIN_WEB, FCM_ENABLED
- Entity 변경 시 마이그레이션 SQL 존재 여부 (ddl-auto=validate)
- Swagger UI 프로덕션 비활성화 확인
- H2 Console 프로덕션 비활성화 확인

### Stage 3: 프론트엔드 점검
```bash
cd frontend && npm run build
```
- TypeScript strict 빌드 성공
- 미사용 변수/파라미터 에러 해결 여부
- 번들 사이즈 확인 (이전 대비 급증 여부)
- `vercel.json` 설정:
  - SPA 라우팅 rewrites
  - API 프록시 경로 (/api → Railway backend)
  - WebSocket 프록시 경로 (/ws → Railway backend)
- 환경변수 하드코딩 여부 검사

### Stage 4: API 호환성 점검
- 백엔드 API 엔드포인트 변경사항 확인
- 프론트엔드 API 호출 경로 일치 여부
- 모바일 API 호환성 (breaking change 검사)
- API 응답 형식 일관성 (ApiResponse<T>)

### Stage 5: DB 마이그레이션 점검
- Entity 변경 감지 (git diff)
- 마이그레이션 SQL 존재/미존재 확인
- prod DB 스키마와의 호환성 예측

### Stage 6: 보안 최종 점검
- 환경 변수 파일(.env) gitignore 확인
- 시크릿 하드코딩 검사 (JWT_SECRET, AES_SECRET_KEY 등)
- 디버그 설정 비활성화 확인
- CORS Origin 프로덕션 설정 확인

### 최종 판정

```
╔══════════════════════════════════════════════╗
║         BSW 배포 전 점검 리포트              ║
╠══════════════════════════════════════════════╣
║ 항목                    │ 상태 │ 비고       ║
╠═════════════════════════╪══════╪════════════╣
║ Git 상태               │  ✅  │ main, 동기화 ║
║ 백엔드 빌드            │  ✅  │ 테스트 통과  ║
║ 프론트엔드 빌드         │  ✅  │ TS strict   ║
║ API 호환성             │  ⚠️  │ 1건 확인필요 ║
║ DB 마이그레이션         │  ❌  │ SQL 미생성   ║
║ 보안 점검              │  ✅  │ 이상 없음    ║
╠═════════════════════════╧══════╧════════════╣
║ 최종 판정: ❌ 배포 불가                      ║
║ 사유: DB 마이그레이션 SQL 미생성             ║
║ 조치: /bsw-migration-gen 실행 후 재점검      ║
╚══════════════════════════════════════════════╝
```

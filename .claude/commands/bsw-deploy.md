# BSW 배포 상태 확인 및 트리거

Railway/Vercel 배포 상태를 확인하고 배포를 트리거합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--status`: 현재 배포 상태 확인
- `--check`: 배포 전 점검만 수행
- `--trigger`: 배포 트리거 (main push)

### 배포 점검

1. **Git 상태 확인**
   - 현재 브랜치 (main 여부)
   - 미커밋 변경사항 확인
   - 원격 저장소와의 동기화 상태

2. **프론트엔드 (Vercel)**
   - `npm run build` 성공 여부 확인 (TypeScript strict)
   - `vercel.json` 프록시 설정 확인
   - SPA 라우팅 설정 확인

3. **백엔드 (Railway)**
   - `./gradlew build` 성공 여부
   - `application-prod.yml` 환경 변수 확인
   - 필수 환경 변수: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, REDIS_HOST, REDIS_PORT, JWT_SECRET

4. **배포 트리거** (--trigger 시)
   - main 브랜치 확인
   - `git push origin main`
   - Railway/Vercel 자동 배포 트리거됨
   - 배포 모니터링 URL 안내

5. **결과 리포트**
   - 배포 가능 여부 판정
   - 미충족 조건 목록
   - 환경 변수 누락 확인

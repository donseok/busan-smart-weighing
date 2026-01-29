# BSW 빌드

전체 또는 특정 모듈을 빌드합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--module [backend|frontend|mobile|desktop|all]`: 빌드 대상 (기본: all)
- `--prod`: 프로덕션 빌드
- `--skip-test`: 테스트 건너뛰기

### 빌드 순서

1. **백엔드** (`backend/`)
   ```
   cd backend && ./gradlew build
   ```
   - 프로덕션: `./gradlew build -Pprod`
   - 산출물: `build/libs/*.jar`

2. **프론트엔드** (`frontend/`)
   ```
   cd frontend && npm install && npm run build
   ```
   - TypeScript strict 모드 → 미사용 변수/파라미터 에러 발생 주의
   - 산출물: `dist/` 디렉토리
   - vendor/antd/echarts 청크 분리 확인

3. **모바일** (`mobile/`)
   ```
   cd mobile && flutter pub get && flutter build apk
   ```
   - iOS: `flutter build ios`
   - 산출물: `build/app/outputs/flutter-apk/`

4. **데스크톱** (`weighing-cs/`)
   ```
   cd weighing-cs && dotnet build
   ```
   - 배포용: `dotnet publish -c Release`
   - 산출물: `bin/Release/net8.0/`

5. **결과 리포트**
   - 각 모듈 빌드 성공/실패 상태
   - 빌드 산출물 경로 안내
   - 에러 발생 시 원인 분석 및 해결 가이드

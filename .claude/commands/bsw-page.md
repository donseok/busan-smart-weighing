# BSW 프론트엔드 페이지 스캐폴딩

새 프론트엔드 페이지를 생성하고 라우팅을 등록합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 인자 파싱
- 첫 번째 인자: 페이지명 (예: `Report`, `Inventory`)
- `--path`: 라우트 경로 (기본: `/{kebab-case}`)
- `--roles [ADMIN|MANAGER|DRIVER|ALL]`: 접근 권한 (기본: ALL)
- `--icon`: Ant Design 아이콘명
- `--category [main|master|admin]`: 메뉴 카테고리
- `--crud`: MasterCrudPage 기반 CRUD 패턴 적용

### 생성 순서

1. **페이지 컴포넌트 생성** (`frontend/src/pages/`)
   - 파일명: `{PageName}Page.tsx` (PascalCase)
   - master 카테고리: `pages/master/{PageName}Page.tsx`
   - admin 카테고리: `pages/admin/{PageName}Page.tsx`
   - CRUD 패턴 시: `MasterCrudPage` 컴포넌트 활용

2. **타입 정의** (`frontend/src/types/index.ts` 또는 별도 파일)
   - 도메인 인터페이스 정의
   - API 요청/응답 타입
   - camelCase 필드명

3. **API 함수** (`frontend/src/api/client.ts`에 추가 또는 별도 파일)
   - Axios 기반 CRUD 함수
   - 경로: `/api/v1/{kebab-case-복수형}`

4. **pageRegistry.ts 등록** (`frontend/src/config/pageRegistry.ts`)
   - key, path, label (한국어), icon, roles, component (React.lazy)
   - 정확한 기존 패턴 따르기

5. **labels.ts 업데이트** (`frontend/src/constants/labels.ts`)
   - 상태/타입별 한국어 레이블 추가 (해당 시)

6. **validators.ts 업데이트** (`frontend/src/utils/validators.ts`)
   - 폼 검증 규칙 추가 (해당 시)

7. **규칙 준수 확인**
   - TypeScript strict 모드: 미사용 변수/파라미터 불가
   - Ant Design Form + rules 배열 패턴
   - useApiCall, useCrudState 훅 활용
   - path alias: `@/*` → `src/*`

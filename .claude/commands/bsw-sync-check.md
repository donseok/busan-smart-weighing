# BSW API 계약 일관성 검증

백엔드 DTO ↔ 프론트엔드 타입 ↔ 모바일 모델 ↔ 데스크톱 모델 간 동기화 상태를 점검합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 옵션 파싱
- `--domain [도메인명]`: 특정 도메인만 검증 (기본: 전체)
- `--fix`: 불일치 자동 수정 제안
- `--report`: 상세 리포트 생성

### 검증 순서

1. **백엔드 DTO 필드 추출**
   - `backend/src/main/java/com/dongkuk/weighing/**/dto/` 하위 모든 Response DTO 스캔
   - 각 DTO의 필드명, 타입, Nullable 여부 추출
   - snake_case 변환 규칙 확인 (Jackson property-naming-strategy)

2. **프론트엔드 타입 추출**
   - `frontend/src/types/index.ts`, `frontend/src/types/weighingStation.ts` 스캔
   - TypeScript interface 필드명 (camelCase), 타입 추출
   - Optional(?) 필드 식별

3. **모바일 모델 추출**
   - `mobile/lib/models/*.dart` 스캔
   - Dart 클래스 필드명, 타입, fromJson/toJson 매핑 추출
   - camelCase 필드명 확인

4. **데스크톱 모델 추출**
   - `weighing-cs/WeighingCS/Models/*.cs` 스캔
   - C# 클래스 프로퍼티명 (PascalCase), 타입 추출
   - JsonProperty 어트리뷰트 매핑 확인

5. **교차 비교**
   - 필드명 매핑 검증:
     - Backend `dispatch_date` (snake_case)
     - Frontend `dispatchDate` (camelCase)
     - Mobile `dispatchDate` (camelCase)
     - Desktop `DispatchDate` (PascalCase)
   - 필드 누락 탐지 (한 플랫폼에만 존재하는 필드)
   - 타입 불일치 탐지 (String vs number, nullable 차이)
   - 새로 추가된 필드의 미반영 탐지

6. **리포트 생성**
   ```
   === API 계약 일관성 리포트 ===

   ✅ 일치: {count}개 도메인
   ⚠️ 불일치: {count}개 필드
   ❌ 누락: {count}개 필드

   [도메인별 상세 내역]
   - Dispatch: backend 15필드 / frontend 15필드 / mobile 12필드 (3개 누락)
   - Weighing: 타입 불일치 - grossWeight (backend: BigDecimal, frontend: number, mobile: double)

   [수정 제안] (--fix 옵션 시)
   - mobile/lib/models/dispatch.dart: 누락 필드 3개 추가 필요
   ```

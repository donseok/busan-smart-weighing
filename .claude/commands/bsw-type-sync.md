# BSW 타입 동기화 검증 (Skill)

4개 플랫폼 간 데이터 모델 일관성을 검증하고 불일치를 수정합니다.

## 인자
$ARGUMENTS

## 워크플로우

### Stage 1: 소스 수집

**백엔드 DTO** (Source of Truth)
- 경로: `backend/src/main/java/com/dongkuk/weighing/**/dto/*Response.java`
- 필드 추출: 이름, 타입, @JsonProperty, nullable
- 변환 규칙: Java → snake_case JSON (Jackson)

**프론트엔드 타입**
- 경로: `frontend/src/types/index.ts`, `frontend/src/types/weighingStation.ts`
- 필드 추출: interface 이름, 프로퍼티, 타입, optional(?)
- 변환 규칙: snake_case JSON → camelCase (Axios 인터셉터)

**모바일 모델**
- 경로: `mobile/lib/models/*.dart`
- 필드 추출: 클래스명, 필드, 타입, fromJson 키 매핑
- 변환 규칙: snake_case JSON → camelCase Dart

**데스크톱 모델**
- 경로: `weighing-cs/WeighingCS/Models/*.cs`
- 필드 추출: 클래스명, Property, 타입, [JsonProperty] 매핑
- 변환 규칙: snake_case JSON → PascalCase C#

### Stage 2: 매핑 테이블 생성

각 도메인별 교차 비교 테이블:
```
| 백엔드 DTO 필드 | JSON (snake) | 프론트 (camel) | 모바일 (camel) | 데스크톱 (Pascal) | 상태 |
|----------------|-------------|---------------|---------------|-----------------|------|
| dispatchDate   | dispatch_date | dispatchDate | dispatchDate | DispatchDate | ✅ |
| plateNumber    | plate_number | plateNumber  | - | PlateNumber | ⚠️ 모바일 누락 |
```

### Stage 3: 불일치 분류

- **🔴 필드 누락**: 한 플랫폼에 없는 필드
- **🟡 타입 불일치**: 타입 매핑이 다른 경우
- **🟠 네이밍 불일치**: 변환 규칙 위반
- **🟢 정상**: 모든 플랫폼 일치

### Stage 4: 자동 수정 (--fix 옵션)

- 누락 필드 → 해당 플랫폼 모델에 추가 코드 생성
- 타입 불일치 → 올바른 타입 매핑 제안
- 네이밍 불일치 → 규칙에 맞게 수정

### Stage 5: 리포트 출력

```
=== BSW 타입 동기화 리포트 ===

검증 도메인: 12개
총 필드 수: 187개

✅ 일치: 165개 (88.2%)
🔴 누락: 12개 (6.4%)
🟡 타입불일치: 7개 (3.7%)
🟠 네이밍불일치: 3개 (1.6%)

[도메인별 상세]
Dispatch: 15/15 ✅
Weighing: 18/20 ⚠️ (모바일 2필드 누락)
GatePass: 10/12 ⚠️ (데스크톱 2필드 누락)
...
```

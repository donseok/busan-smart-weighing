# BSW Flutter 화면 스캐폴딩

모바일 앱의 새 화면 + Provider + Service를 생성합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 인자 파싱
- 첫 번째 인자: 화면명 (예: `inventory`, `report`)
- `--type [list|detail|form|dashboard]`: 화면 유형 (기본: list)
- `--with-provider`: Provider 클래스도 생성
- `--with-service`: API Service 클래스도 생성

### 생성 순서

1. **Model 생성** (`mobile/lib/models/`)
   - `{도메인}.dart`: 도메인 모델 클래스
   - `fromJson()` / `toJson()` 직렬화
   - ApiResponse 래퍼 호환

2. **Service 생성** (`mobile/lib/services/`)
   - `{도메인}_service.dart`: Dio 기반 API 호출
   - JWT 인터셉터 자동 적용
   - Mock 데이터 지원 (`mock_{도메인}_service.dart`)

3. **Provider 생성** (`mobile/lib/providers/`)
   - `{도메인}_provider.dart`: ChangeNotifier 기반 상태 관리
   - 로딩/에러/데이터 상태 관리
   - CRUD 메서드

4. **Screen 생성** (`mobile/lib/screens/{도메인}/`)
   - list 유형: `{도메인}_list_screen.dart` (ListView + 검색 + 필터)
   - detail 유형: `{도메인}_detail_screen.dart` (상세 정보 표시)
   - form 유형: `{도메인}_form_screen.dart` (입력 폼)
   - dashboard 유형: `{도메인}_dashboard_screen.dart` (통계/차트)

5. **Widget 생성** (`mobile/lib/widgets/`)
   - 도메인 전용 위젯 (해당 시)
   - StatusBadge 스타일 재사용

6. **라우팅 등록** (`mobile/lib/app.dart`)
   - GoRouter 경로 추가
   - 인증 리다이렉트 설정

7. **Provider 등록** (`mobile/lib/main.dart`)
   - MultiProvider에 신규 Provider 추가

### 규칙
- 파일명: snake_case (`dispatch_list_screen.dart`)
- 클래스명: PascalCase (`DispatchListScreen`)
- Provider 패턴 준수 (Consumer/Selector 활용)
- 오프라인 캐시 고려 (`offline_cache_service.dart`)

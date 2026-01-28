# Busan Smart Weighing System (부산 스마트 계량 시스템)

## 1. 프로젝트 개요
**부산 스마트 계량 시스템**은 LPR(차량번호 인식), AI 검증, 모바일 앱을 활용하여 차량 계량 프로세스를 자동화하고 효율적으로 관리하는 통합 시스템입니다. 기존의 수동/반자동 계량 방식을 개선하여 무인화 및 데이터 정확성을 확보하는 것을 목표로 합니다.

## 2. 기술 스택 (Tech Stack)

### Backend
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: PostgreSQL (Supabase), H2 (Test)
- **Cache**: Redis (Embedded Redis for Dev/Test)
- **Security**: Spring Security, JWT (jjwt 0.12.5)
- **API Docs**: Swagger (SpringDoc OpenAPI 2.5.0)
- **Build Tool**: Gradle

### Frontend (Web)
- **Framework**: React 18
- **Build Tool**: Vite 7.3.1
- **Language**: TypeScript 5.9.3
- **UI Library**: Ant Design 5.29.3
- **HTTP Client**: Axios
- **State/Routing**: React Router DOM 6.30.3

### Mobile App
- **Framework**: Flutter (SDK ^3.10.4)
- **Language**: Dart
- **Key Packages**:
  - `dio`: HTTP 통신
  - `go_router`: 라우팅
  - `provider`: 상태 관리
  - `flutter_secure_storage`: 보안 저장소
  - `share_plus`: 공유 기능 (카카오톡/SMS 등)

### CS Program (Weighing Client)
- **Platform**: Windows (.NET Framework/Core 추정)
- **Role**: 현장 하드웨어 제어 (인디게이터, LPR 카메라, 차단기, 전광판) 및 로컬 계량 처리
- **Communication**: RS-232C (Serial), TCP/UDP

## 3. 주요 모듈 및 기능

### 1) LPR 차량번호 인식 시스템
- **LiDAR/Radar 연동**: 차량 진입 감지 및 촬영 트리거
- **AI 검증**: 1차 LPR 인식 결과를 AI 엔진으로 재검증하여 정확도 향상
- **OTP 보안 계량**: 인식 실패 또는 신뢰도 낮음 시 운전자 모바일 앱 OTP로 인증

### 2) 스마트 계량 웹 관리 시스템
- **배차 관리**: 품목별(부산물, 폐기물 등) 배차 등록 및 관리
- **계량 현황**: 실시간 계량 모니터링 (WebSocket), 통계 대시보드
- **전자 계량표**: 계량 완료 시 자동 생성, 카카오톡/SMS 공유
- **기준정보**: 운송사, 차량, 공통코드 관리

### 3) 계량 CS 프로그램
- **중량값 수신**: 인디게이터와 시리얼 통신으로 중량값 실시간 수신 및 안정화 판단
- **현장 제어**: 전광판 안내 문구 표시, 차단기 제어
- **오프라인 대응**: 네트워크 장애 시 로컬 캐싱 및 복구 후 동기화

### 4) 모바일 앱 (Driver App)
- **OTP 발급/입력**: 본인 인증 및 보안 계량 수행
- **전자 계량표 조회**: 내 계량 이력 및 전자 계량표 확인

## 4. 아키텍처 요약
- **Client-Server 구조**: Web/Mobile/CS 클라이언트가 REST API를 통해 Backend와 통신
- **Realtime**: WebSocket(STOMP)을 이용한 실시간 상태 동기화 (계량대 상태, 중량값 등)
- **Hardware Integration**: CS 프로그램이 Edge Node 역할을 수행하여 하드웨어 제어 및 서버 통신 중계

## 5. 프로젝트 상태
- **문서화**: 기능 명세서(FUNC-SPEC), PRD, TRD 등 상세 문서 작성 완료 (Draft)
- **개발**: Backend/Frontend/Mobile 기본 프로젝트 구조 세팅 완료

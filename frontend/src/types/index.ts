/**
 * 공통 타입 정의 모듈
 *
 * 부산 스마트 계량 시스템의 프론트엔드 전역에서 사용되는
 * API 응답, 도메인 엔티티, DTO 등의 TypeScript 인터페이스를 정의합니다.
 *
 * @module types
 */

// ─── API 공통 응답 ───

/**
 * API 공통 응답 래퍼 타입
 *
 * 모든 REST API 응답은 이 형식으로 감싸져 반환됩니다.
 *
 * @template T - 응답 데이터의 실제 타입
 * @property success - 요청 성공 여부
 * @property data - 응답 데이터 본문
 * @property message - 서버 메시지 (선택)
 * @property error - 에러 정보 (실패 시)
 * @property timestamp - 응답 생성 시각 (ISO 8601)
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  error?: { code: string; message: string };
  timestamp: string;
}

/**
 * 페이지네이션 응답 타입
 *
 * Spring Data의 Page 응답을 프론트엔드에서 사용하기 위한 인터페이스입니다.
 *
 * @template T - 페이지 내 개별 항목의 타입
 * @property content - 현재 페이지의 항목 배열
 * @property totalElements - 전체 항목 수
 * @property totalPages - 전체 페이지 수
 * @property size - 페이지당 항목 수
 * @property number - 현재 페이지 번호 (0부터 시작)
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ─── 배차 (Dispatch) ───

/**
 * 배차 데이터 타입
 *
 * 운송 차량의 배차(운송 배정) 정보를 나타냅니다.
 *
 * @property dispatchId - 배차 고유 식별자
 * @property vehicleId - 배정된 차량 ID
 * @property companyId - 운송사 ID
 * @property itemType - 품목 유형 코드 (BY_PRODUCT, WASTE 등)
 * @property itemName - 품목명
 * @property dispatchDate - 배차일 (YYYY-MM-DD)
 * @property originLocation - 출발지 (선택)
 * @property destination - 도착지 (선택)
 * @property remarks - 비고 (선택)
 * @property dispatchStatus - 배차 상태 (REGISTERED, IN_PROGRESS, COMPLETED, CANCELLED)
 * @property createdBy - 등록자 ID (선택)
 * @property createdAt - 생성일시
 * @property updatedAt - 수정일시
 */
export interface Dispatch {
  dispatchId: number;
  vehicleId: number;
  companyId: number;
  itemType: string;
  itemName: string;
  dispatchDate: string;
  originLocation?: string;
  destination?: string;
  remarks?: string;
  dispatchStatus: string;
  createdBy?: number;
  createdAt: string;
  updatedAt: string;
}

// ─── 계량 (Weighing) ───

/**
 * 계량 기록 데이터 타입
 *
 * 차량의 중량 측정 기록을 나타냅니다. 1차 계량(총중량)과
 * 2차 계량(공차중량)을 거쳐 순중량이 산출됩니다.
 *
 * @property weighingId - 계량 기록 고유 식별자
 * @property dispatchId - 연관 배차 ID
 * @property scaleId - 사용된 계량대 ID
 * @property weighingMode - 계량 방식 (LPR_AUTO, MOBILE_OTP, MANUAL, RE_WEIGH)
 * @property weighingStep - 계량 단계 (FIRST_WEIGH, SECOND_WEIGH, COMPLETED)
 * @property grossWeight - 총중량 (kg, 선택)
 * @property tareWeight - 공차중량 (kg, 선택)
 * @property netWeight - 순중량 (kg, 총중량 - 공차중량, 선택)
 * @property lprPlateNumber - LPR 인식 차량번호 (선택)
 * @property aiConfidence - AI 번호판 인식 신뢰도 (0~1, 선택)
 * @property weighingStatus - 계량 상태 (IN_PROGRESS, COMPLETED, RE_WEIGHING, ERROR)
 * @property reWeighReason - 재계량 사유 (선택)
 * @property createdAt - 생성일시
 * @property updatedAt - 수정일시
 */
export interface WeighingRecord {
  weighingId: number;
  dispatchId: number;
  scaleId: number;
  weighingMode: string;
  weighingStep: string;
  grossWeight?: number;
  tareWeight?: number;
  netWeight?: number;
  lprPlateNumber?: string;
  aiConfidence?: number;
  weighingStatus: string;
  reWeighReason?: string;
  createdAt: string;
  updatedAt: string;
}

// ─── 운송사 (Company) ───

/**
 * 운송사 데이터 타입
 *
 * 계량 시스템에 등록된 운송 회사 정보입니다.
 *
 * @property companyId - 운송사 고유 식별자
 * @property companyName - 운송사명
 * @property companyType - 회사 유형
 * @property businessNumber - 사업자등록번호 (선택)
 * @property representative - 대표자명 (선택)
 * @property phoneNumber - 연락처 (선택)
 * @property address - 주소 (선택)
 * @property isActive - 활성화 여부
 * @property createdAt - 생성일시
 */
export interface Company {
  companyId: number;
  companyName: string;
  companyType: string;
  businessNumber?: string;
  representative?: string;
  phoneNumber?: string;
  address?: string;
  isActive: boolean;
  createdAt: string;
}

// ─── 차량 (Vehicle) ───

/**
 * 차량 데이터 타입
 *
 * 계량 대상 차량의 기본 정보 및 기사 정보입니다.
 *
 * @property vehicleId - 차량 고유 식별자
 * @property plateNumber - 차량번호
 * @property vehicleType - 차종
 * @property companyId - 소속 운송사 ID (선택)
 * @property defaultTareWeight - 기본 공차중량 (kg, 선택)
 * @property maxLoadWeight - 최대 적재중량 (kg, 선택)
 * @property driverName - 기사명 (선택)
 * @property driverPhone - 기사 연락처 (선택)
 * @property isActive - 활성화 여부
 * @property createdAt - 생성일시
 */
export interface Vehicle {
  vehicleId: number;
  plateNumber: string;
  vehicleType: string;
  companyId?: number;
  defaultTareWeight?: number;
  maxLoadWeight?: number;
  driverName?: string;
  driverPhone?: string;
  isActive: boolean;
  createdAt: string;
}

// ─── 계량대 (Scale) ───

/**
 * 계량대 데이터 타입
 *
 * 물리적 계량 장비(저울)의 정보입니다.
 *
 * @property scaleId - 계량대 고유 식별자
 * @property scaleName - 계량대 이름
 * @property location - 설치 위치 (선택)
 * @property maxCapacity - 최대 계량 용량 (kg, 선택)
 * @property minCapacity - 최소 계량 용량 (kg, 선택)
 * @property scaleStatus - 계량대 상태 (IDLE, WEIGHING, ERROR 등)
 * @property isActive - 활성화 여부
 * @property createdAt - 생성일시
 */
export interface Scale {
  scaleId: number;
  scaleName: string;
  location?: string;
  maxCapacity?: number;
  minCapacity?: number;
  scaleStatus: string;
  isActive: boolean;
  createdAt: string;
}

// ─── 전자계량표 (Weighing Slip) ───

/**
 * 전자계량표 데이터 타입
 *
 * 계량 완료 후 자동 발행되는 전자 계량 전표입니다.
 * 카카오톡이나 SMS로 공유할 수 있습니다.
 *
 * @property slipId - 전표 고유 식별자
 * @property weighingId - 연관 계량 기록 ID
 * @property dispatchId - 연관 배차 ID
 * @property slipNumber - 전표번호
 * @property vehiclePlateNumber - 차량번호 (선택)
 * @property companyName - 운송사명 (선택)
 * @property itemName - 품목명 (선택)
 * @property grossWeightKg - 총중량 (kg, 선택)
 * @property tareWeightKg - 공차중량 (kg, 선택)
 * @property netWeightKg - 순중량 (kg, 선택)
 * @property sharedVia - 공유 방법 (KAKAO, SMS 등, 선택)
 * @property createdAt - 발행일시
 */
export interface WeighingSlip {
  slipId: number;
  weighingId: number;
  dispatchId: number;
  slipNumber: string;
  vehiclePlateNumber?: string;
  companyName?: string;
  itemName?: string;
  grossWeightKg?: number;
  tareWeightKg?: number;
  netWeightKg?: number;
  sharedVia?: string;
  createdAt: string;
}

// ─── 출문증 (Gate Pass) ───

/**
 * 출문증 데이터 타입
 *
 * 계량 완료 차량의 구내 출입(출문) 허가 정보입니다.
 *
 * @property gatePassId - 출문증 고유 식별자
 * @property weighingId - 연관 계량 기록 ID
 * @property dispatchId - 연관 배차 ID
 * @property passStatus - 출문 상태 (PENDING, PASSED, REJECTED)
 * @property passedAt - 출문 처리 일시 (선택)
 * @property processedBy - 처리자 ID (선택)
 * @property rejectReason - 반려 사유 (선택)
 * @property createdAt - 생성일시
 */
export interface GatePass {
  gatePassId: number;
  weighingId: number;
  dispatchId: number;
  passStatus: string;
  passedAt?: string;
  processedBy?: number;
  rejectReason?: string;
  createdAt: string;
}

// ─── 계량 통계 (Weighing Statistics) ───

/**
 * 일별 통계 데이터 타입
 *
 * @property date - 날짜 (YYYY-MM-DD)
 * @property totalCount - 해당 일자 총 계량 건수
 * @property totalNetWeightTon - 해당 일자 총 순중량 (톤)
 */
export interface DailyStatistic {
  date: string;
  totalCount: number;
  totalNetWeightTon: number;
}

/**
 * 계량 통계 종합 데이터 타입
 *
 * 대시보드에 표시되는 오늘/이번 달의 계량 현황 요약입니다.
 *
 * @property todayTotalCount - 오늘 전체 계량 건수
 * @property todayCompletedCount - 오늘 완료된 계량 건수
 * @property todayInProgressCount - 오늘 진행 중인 계량 건수
 * @property todayTotalNetWeightTon - 오늘 총 순중량 (톤)
 * @property monthTotalCount - 이번 달 전체 계량 건수
 * @property monthTotalNetWeightTon - 이번 달 총 순중량 (톤)
 * @property countByItemType - 품목 유형별 계량 건수 맵
 * @property countByWeighingMode - 계량 방식별 건수 맵
 * @property dailyStatistics - 일별 통계 배열
 */
export interface WeighingStatistics {
  todayTotalCount: number;
  todayCompletedCount: number;
  todayInProgressCount: number;
  todayTotalNetWeightTon: number;
  monthTotalCount: number;
  monthTotalNetWeightTon: number;
  countByItemType: Record<string, number>;
  countByWeighingMode: Record<string, number>;
  dailyStatistics: DailyStatistic[];
}

// ─── 인증 (Authentication) ───

/**
 * 로그인 응답 데이터 타입
 *
 * JWT 기반 인증 시 서버에서 반환하는 토큰 정보입니다.
 *
 * @property accessToken - JWT 액세스 토큰
 * @property refreshToken - JWT 리프레시 토큰
 * @property expiresIn - 토큰 만료 시간 (초)
 */
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

// ─── 알림 (Notification) ───

/**
 * 알림 항목 데이터 타입
 *
 * 사용자에게 전달되는 시스템 알림(배차 배정, 계량 완료 등)입니다.
 *
 * @property notificationId - 알림 고유 식별자
 * @property notificationType - 알림 유형
 * @property title - 알림 제목
 * @property message - 알림 메시지 본문
 * @property referenceId - 참조 엔티티 ID (선택)
 * @property isRead - 읽음 여부
 * @property createdAt - 생성일시
 */
export interface NotificationItem {
  notificationId: number;
  notificationType: string;
  title: string;
  message: string;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}

// ─── 사용자 (User) ───

/**
 * 사용자 데이터 타입
 *
 * 시스템 사용자(관리자, 담당자, 운전자)의 계정 정보입니다.
 *
 * @property userId - 사용자 고유 식별자
 * @property loginId - 로그인 ID
 * @property userName - 사용자 이름
 * @property phoneNumber - 연락처
 * @property userRole - 역할 (ADMIN: 관리자, MANAGER: 담당자, DRIVER: 운전자)
 * @property companyId - 소속 운송사 ID (선택)
 * @property companyName - 소속 운송사명 (선택)
 * @property isActive - 활성화 여부
 * @property failedLoginCount - 로그인 실패 횟수
 * @property lockedUntil - 계정 잠금 해제 시각 (선택)
 * @property createdAt - 생성일시
 * @property updatedAt - 수정일시
 */
export interface User {
  userId: number;
  loginId: string;
  userName: string;
  phoneNumber: string;
  userRole: 'ADMIN' | 'MANAGER' | 'DRIVER';
  companyId?: number;
  companyName?: string;
  isActive: boolean;
  failedLoginCount: number;
  lockedUntil?: string;
  createdAt: string;
  updatedAt: string;
}

// ─── 감사 로그 (Audit Log) ───

/**
 * 감사 로그 데이터 타입
 *
 * 시스템에서 수행된 주요 작업(생성, 수정, 삭제, 로그인 등)의 기록입니다.
 *
 * @property auditLogId - 감사 로그 고유 식별자
 * @property actorId - 수행자 사용자 ID (선택)
 * @property actorName - 수행자 이름 (선택)
 * @property actionType - 작업 유형 코드 (CREATE, UPDATE, DELETE, LOGIN 등)
 * @property actionTypeDesc - 작업 유형 한글 설명
 * @property entityType - 대상 엔티티 유형 코드 (USER, DISPATCH, WEIGHING 등)
 * @property entityTypeDesc - 대상 엔티티 유형 한글 설명
 * @property entityId - 대상 엔티티 ID (선택)
 * @property oldValue - 변경 전 값 (JSON 문자열, 선택)
 * @property newValue - 변경 후 값 (JSON 문자열, 선택)
 * @property ipAddress - 접속 IP 주소 (선택)
 * @property userAgent - 접속 브라우저 정보 (선택)
 * @property createdAt - 기록 생성일시
 */
export interface AuditLog {
  auditLogId: number;
  actorId?: number;
  actorName?: string;
  actionType: string;
  actionTypeDesc: string;
  entityType: string;
  entityTypeDesc: string;
  entityId?: number;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}

// ─── 시스템 설정 (System Setting) ───

/**
 * 시스템 설정 데이터 타입
 *
 * 관리자가 변경 가능한 시스템 환경 설정 항목입니다.
 *
 * @property settingId - 설정 고유 식별자
 * @property settingKey - 설정 키 (고유)
 * @property settingValue - 설정 값 (문자열로 저장)
 * @property settingType - 값 타입 (STRING, NUMBER, BOOLEAN, JSON)
 * @property category - 설정 카테고리 (GENERAL, WEIGHING, NOTIFICATION, SECURITY)
 * @property categoryDesc - 카테고리 한글 설명
 * @property description - 설정 설명 (선택)
 * @property isEditable - 수정 가능 여부
 * @property updatedAt - 최종 수정일시
 */
export interface SystemSetting {
  settingId: number;
  settingKey: string;
  settingValue: string;
  settingType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  category: 'GENERAL' | 'WEIGHING' | 'NOTIFICATION' | 'SECURITY';
  categoryDesc: string;
  description?: string;
  isEditable: boolean;
  updatedAt: string;
}

// ─── 공통 코드 (Common Code) ───

/**
 * 공통 코드 데이터 타입
 *
 * 시스템 전반에서 사용되는 코드 마스터 데이터입니다.
 * 품목 유형, 차종, 계량 방식 등의 드롭다운 옵션에 활용됩니다.
 *
 * @property codeId - 코드 고유 식별자
 * @property codeGroup - 코드 그룹 (ITEM_TYPE, VEHICLE_TYPE 등)
 * @property codeValue - 코드 값 (BY_PRODUCT, CARGO 등)
 * @property codeName - 코드 표시 이름 (부산물, 화물 등)
 * @property sortOrder - 정렬 순서 (선택)
 * @property isActive - 활성화 여부
 */
export interface CommonCode {
  codeId: number;
  codeGroup: string;
  codeValue: string;
  codeName: string;
  sortOrder?: number;
  isActive: boolean;
}

// ─── 대시보드 요약 (Dashboard Summary) ───

/**
 * 대시보드 요약 데이터 타입
 *
 * 메인 대시보드에 표시되는 오늘의 배차/출문/계량 현황 요약입니다.
 *
 * @property dispatchRegistered - 배차 등록(대기) 건수
 * @property dispatchInProgress - 배차 진행 중 건수
 * @property dispatchCompleted - 배차 완료 건수
 * @property dispatchCancelled - 배차 취소 건수
 * @property gatePassPending - 출문 대기 건수
 * @property gatePassPassed - 출문 통과 건수
 * @property gatePassRejected - 출문 반려 건수
 * @property weighingInProgress - 계량 진행 중 건수
 * @property weighingCompleted - 계량 완료 건수
 * @property weighingReWeighing - 재계량 건수
 */
export interface DashboardSummary {
  dispatchRegistered: number;
  dispatchInProgress: number;
  dispatchCompleted: number;
  dispatchCancelled: number;
  gatePassPending: number;
  gatePassPassed: number;
  gatePassRejected: number;
  weighingInProgress: number;
  weighingCompleted: number;
  weighingReWeighing: number;
}

// ─── 운송사별 통계 (Company Statistics) ───

/**
 * 운송사별 통계 데이터 타입
 *
 * 대시보드 분석 탭에서 운송사별 계량 현황을 표시합니다.
 *
 * @property companyId - 운송사 ID
 * @property companyName - 운송사명
 * @property weighingCount - 계량 건수
 * @property totalNetWeightTon - 총 순중량 (톤)
 */
export interface CompanyStatistics {
  companyId: number;
  companyName: string;
  weighingCount: number;
  totalNetWeightTon: number;
}

// ─── 공지사항 (Notice) ───

/**
 * 공지사항 데이터 타입
 *
 * 시스템 공지사항 게시글 정보입니다.
 *
 * @property noticeId - 공지사항 고유 식별자
 * @property title - 제목
 * @property content - 내용
 * @property category - 카테고리 (GENERAL, SYSTEM, UPDATE, MAINTENANCE)
 * @property categoryDesc - 카테고리 한글 설명
 * @property authorId - 작성자 ID
 * @property authorName - 작성자 이름
 * @property isPublished - 공개 여부
 * @property isPinned - 상단 고정 여부
 * @property viewCount - 조회수
 * @property publishedAt - 발행일시 (선택)
 * @property createdAt - 생성일시
 * @property updatedAt - 수정일시
 */
export interface Notice {
  noticeId: number;
  title: string;
  content: string;
  category: 'GENERAL' | 'SYSTEM' | 'UPDATE' | 'MAINTENANCE';
  categoryDesc: string;
  authorId: number;
  authorName: string;
  isPublished: boolean;
  isPinned: boolean;
  viewCount: number;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

// ─── 즐겨찾기 (Favorite) ───

/**
 * 즐겨찾기 데이터 타입
 *
 * 사용자가 자주 사용하는 메뉴, 배차, 차량 등을 즐겨찾기로 등록한 정보입니다.
 *
 * @property favorite_id - 즐겨찾기 고유 식별자
 * @property favorite_type - 즐겨찾기 유형 (MENU, DISPATCH, VEHICLE, COMPANY, SCALE)
 * @property favorite_type_desc - 유형 한글 설명
 * @property target_id - 대상 엔티티 ID (선택)
 * @property target_path - 대상 경로 (메뉴의 경우 URL 경로, 선택)
 * @property display_name - 표시 이름
 * @property icon - 아이콘 (선택)
 * @property sort_order - 정렬 순서 (선택)
 * @property created_at - 생성일시
 */
export interface Favorite {
  favorite_id: number;
  favorite_type: 'MENU' | 'DISPATCH' | 'VEHICLE' | 'COMPANY' | 'SCALE';
  favorite_type_desc: string;
  target_id?: number;
  target_path?: string;
  display_name: string;
  icon?: string;
  sort_order?: number;
  created_at: string;
}

// ─── 장비 모니터링 (Device Monitoring) ───

/**
 * 장비 상태 데이터 타입
 *
 * 계량 시스템에 연결된 장비(계량대, LPR 카메라, 지시기, 차단기)의
 * 연결 상태 및 기본 정보입니다.
 *
 * @property deviceId - 장비 고유 식별자
 * @property deviceCode - 장비 코드
 * @property deviceName - 장비 이름
 * @property deviceType - 장비 유형 (SCALE, LPR_CAMERA, INDICATOR, BARRIER_GATE)
 * @property deviceTypeDesc - 장비 유형 한글 설명
 * @property location - 설치 위치
 * @property connectionStatus - 연결 상태 (ONLINE, OFFLINE, ERROR)
 * @property connectionStatusDesc - 연결 상태 한글 설명
 * @property lastConnectedAt - 최종 연결 일시 (선택)
 * @property ipAddress - IP 주소 (선택)
 * @property errorMessage - 오류 메시지 (선택)
 * @property isActive - 활성화 여부
 * @property createdAt - 생성일시
 * @property updatedAt - 수정일시
 */
export interface DeviceStatus {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: 'SCALE' | 'LPR_CAMERA' | 'INDICATOR' | 'BARRIER_GATE';
  deviceTypeDesc: string;
  location: string;
  connectionStatus: 'ONLINE' | 'OFFLINE' | 'ERROR';
  connectionStatusDesc: string;
  lastConnectedAt?: string;
  ipAddress?: string;
  errorMessage?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * 장비 요약 데이터 타입
 *
 * 장비 관제 페이지 상단의 전체 장비 현황 요약입니다.
 *
 * @property totalDevices - 전체 장비 수
 * @property onlineCount - 온라인 장비 수
 * @property offlineCount - 오프라인 장비 수
 * @property errorCount - 오류 장비 수
 * @property countByTypeAndStatus - 장비 유형별, 상태별 수량 맵
 */
export interface DeviceSummary {
  totalDevices: number;
  onlineCount: number;
  offlineCount: number;
  errorCount: number;
  countByTypeAndStatus: Record<string, Record<string, number>>;
}

// ─── FAQ (자주 묻는 질문) ───

/**
 * FAQ 데이터 타입
 *
 * 이용 안내 페이지에 표시되는 자주 묻는 질문/답변입니다.
 *
 * @property faqId - FAQ 고유 식별자
 * @property question - 질문
 * @property answer - 답변
 * @property category - 카테고리 (WEIGHING, DISPATCH, ACCOUNT, SYSTEM, OTHER)
 * @property categoryDesc - 카테고리 한글 설명
 * @property sortOrder - 정렬 순서
 * @property isPublished - 공개 여부
 * @property viewCount - 조회수
 * @property createdAt - 생성일시
 * @property updatedAt - 수정일시
 */
export interface Faq {
  faqId: number;
  question: string;
  answer: string;
  category: 'WEIGHING' | 'DISPATCH' | 'ACCOUNT' | 'SYSTEM' | 'OTHER';
  categoryDesc: string;
  sortOrder: number;
  isPublished: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

// ─── 마이페이지 (My Profile) ───

/**
 * 내 프로필 데이터 타입
 *
 * 마이페이지에서 표시되는 현재 로그인 사용자의 프로필 정보입니다.
 *
 * @property userId - 사용자 고유 식별자
 * @property loginId - 로그인 ID
 * @property userName - 사용자 이름
 * @property phoneNumber - 연락처
 * @property email - 이메일 (선택)
 * @property userRole - 역할 (ADMIN, MANAGER, DRIVER)
 * @property userRoleDesc - 역할 한글 설명
 * @property companyId - 소속 운송사 ID (선택)
 * @property companyName - 소속 운송사명 (선택)
 * @property pushEnabled - 푸시 알림 활성화 여부
 * @property emailEnabled - 이메일 알림 활성화 여부
 * @property createdAt - 가입일시
 * @property lastLoginAt - 최종 로그인 일시 (선택)
 */
export interface MyProfile {
  userId: number;
  loginId: string;
  userName: string;
  phoneNumber: string;
  email?: string;
  userRole: 'ADMIN' | 'MANAGER' | 'DRIVER';
  userRoleDesc: string;
  companyId?: number;
  companyName?: string;
  pushEnabled: boolean;
  emailEnabled: boolean;
  createdAt: string;
  lastLoginAt?: string;
}

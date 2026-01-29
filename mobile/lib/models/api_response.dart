/// API 응답 래퍼 클래스
///
/// 백엔드 서버의 표준 응답 형식을 정의합니다.
/// 성공/실패 여부, 데이터, 에러 정보를 포함합니다.

/// 제네릭 API 응답 모델
///
/// [T] 응답 데이터의 타입
/// 모든 API 응답은 이 클래스로 래핑되어 일관된 에러 처리가 가능합니다.
class ApiResponse<T> {
  /// 요청 성공 여부
  final bool success;

  /// 응답 데이터 (성공 시)
  final T? data;

  /// 서버 메시지 (선택)
  final String? message;

  /// 에러 정보 (실패 시)
  final ApiError? error;

  /// 서버 응답 타임스탬프
  final String? timestamp;

  ApiResponse({
    required this.success,
    this.data,
    this.message,
    this.error,
    this.timestamp,
  });

  /// JSON 맵에서 [ApiResponse] 객체를 생성합니다.
  ///
  /// [json] 서버 응답 JSON
  /// [fromData] 데이터 필드를 타입 [T]로 변환하는 함수 (선택)
  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T Function(dynamic)? fromData,
  ) {
    return ApiResponse<T>(
      success: json['success'] as bool? ?? false,
      data: json['data'] != null && fromData != null
          ? fromData(json['data'])
          : json['data'] as T?,
      message: json['message'] as String?,
      error: json['error'] != null
          ? ApiError.fromJson(json['error'] as Map<String, dynamic>)
          : null,
      timestamp: json['timestamp'] as String?,
    );
  }
}

/// API 에러 정보 모델
///
/// 에러 코드와 사용자 친화적인 메시지를 포함합니다.
class ApiError {
  /// 에러 코드 (예: 'UNAUTHORIZED', 'NOT_FOUND')
  final String code;

  /// 사용자에게 표시할 에러 메시지
  final String message;

  ApiError({
    required this.code,
    required this.message,
  });

  /// JSON 맵에서 [ApiError] 객체를 생성합니다.
  factory ApiError.fromJson(Map<String, dynamic> json) {
    return ApiError(
      code: json['code'] as String? ?? 'UNKNOWN',
      message: json['message'] as String? ?? '알 수 없는 오류가 발생했습니다.',
    );
  }
}

/// 페이지네이션 데이터 모델
///
/// 서버의 페이지네이션 응답을 표현합니다.
/// [T] 목록 항목의 타입
class PaginatedData<T> {
  /// 현재 페이지의 데이터 목록
  final List<T> content;

  /// 전체 항목 수
  final int totalElements;

  /// 전체 페이지 수
  final int totalPages;

  /// 현재 페이지 번호 (0부터 시작)
  final int currentPage;

  /// 페이지당 항목 수
  final int pageSize;

  PaginatedData({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.currentPage,
    required this.pageSize,
  });

  /// JSON 맵에서 [PaginatedData] 객체를 생성합니다.
  ///
  /// [json] 서버 응답 JSON
  /// [fromItem] 각 항목을 타입 [T]로 변환하는 함수
  factory PaginatedData.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromItem,
  ) {
    final rawContent = json['content'] as List<dynamic>? ?? [];
    return PaginatedData<T>(
      content: rawContent
          .map((e) => fromItem(e as Map<String, dynamic>))
          .toList(),
      totalElements: json['totalElements'] as int? ?? 0,
      totalPages: json['totalPages'] as int? ?? 0,
      currentPage: json['currentPage'] as int? ?? 0,
      pageSize: json['pageSize'] as int? ?? 20,
    );
  }
}

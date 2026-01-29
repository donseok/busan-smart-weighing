/// 배차/계량/계량표 상태 관리 Provider
///
/// 배차(Dispatch) 목록/상세, 계량(Weighing) 기록, 계량표(Slip) 조회,
/// OTP 인증, 계량표 공유 등 핵심 업무 데이터의 상태를 관리하는 [ChangeNotifier]입니다.
/// Mock API와 실제 API를 모두 지원하여 백엔드 없이도 전체 플로우를 테스트할 수 있습니다.
import 'package:flutter/foundation.dart';
import '../config/api_config.dart';
import '../models/dispatch.dart';
import '../models/weighing_record.dart';
import '../models/weighing_slip.dart';
import '../models/api_response.dart';
import '../services/api_service.dart';
import '../services/mock_api_service.dart';
import '../services/offline_cache_service.dart';

/// 배차/계량/계량표 상태 관리 Provider
///
/// [ApiService] 또는 [MockApiService]를 내부적으로 사용하며,
/// 두 가지 생성자를 통해 실제/Mock 모드를 선택합니다.
///
/// 주요 기능:
/// - 배차 목록/상세 조회
/// - 계량 기록 조회 (기간별 필터링)
/// - OTP 인증 요청
/// - 계량표 목록/상세 조회 및 공유
class DispatchProvider extends ChangeNotifier {
  /// 실제 API 서비스 (Mock 모드가 아닐 때 사용)
  final ApiService? _apiService;

  /// Mock API 서비스 (백엔드 없이 테스트 시 사용)
  final MockApiService? _mockApiService;

  /// Mock 모드 여부 (MockApiService가 주입되었는지 확인)
  bool get _useMock => _mockApiService != null;

  /// 배차 목록
  List<Dispatch> _dispatches = [];

  /// 선택된 배차 (상세 화면용)
  Dispatch? _selectedDispatch;

  /// 계량 기록 목록
  List<WeighingRecord> _weighingRecords = [];

  /// 계량표 목록
  List<WeighingSlip> _slips = [];

  /// 선택된 계량표 (상세 화면용)
  WeighingSlip? _selectedSlip;

  /// 로딩 상태
  bool _isLoading = false;

  /// 오류 메시지
  String? _errorMessage;

  /// 오프라인 모드 여부 (캐시 데이터 사용 중)
  bool _isOfflineMode = false;

  // ── 페이지네이션 ──

  /// 현재 페이지 번호 (0-based)
  int _currentPage = 0;

  /// 추가 데이터 존재 여부
  bool _hasMore = true;

  /// 페이지당 항목 수
  static const int _pageSize = 20;

  /// 실제 API를 사용하는 기본 생성자
  DispatchProvider(ApiService apiService)
      : _apiService = apiService,
        _mockApiService = null;

  /// Mock API를 사용하는 테스트용 생성자
  DispatchProvider.mock(MockApiService mockService)
      : _apiService = null,
        _mockApiService = mockService;

  /// 배차 목록
  List<Dispatch> get dispatches => _dispatches;

  /// 선택된 배차
  Dispatch? get selectedDispatch => _selectedDispatch;

  /// 계량 기록 목록
  List<WeighingRecord> get weighingRecords => _weighingRecords;

  /// 계량표 목록
  List<WeighingSlip> get slips => _slips;

  /// 선택된 계량표
  WeighingSlip? get selectedSlip => _selectedSlip;

  /// 로딩 중 여부
  bool get isLoading => _isLoading;

  /// 오류 메시지 (없으면 null)
  String? get errorMessage => _errorMessage;

  /// 추가 페이지 데이터 존재 여부
  bool get hasMore => _hasMore;

  /// 오프라인 모드 여부 (캐시 데이터를 표시 중인지)
  bool get isOfflineMode => _isOfflineMode;

  // ── 내부 API 호출 헬퍼 ──

  /// GET 요청 헬퍼 (Mock/실제 API 자동 분기)
  Future<ApiResponse<T>> _get<T>(
    String url, {
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? fromData,
  }) {
    if (_useMock) {
      return _mockApiService!.get<T>(url, queryParameters: queryParameters, fromData: fromData);
    }
    return _apiService!.get<T>(url, queryParameters: queryParameters, fromData: fromData);
  }

  /// POST 요청 헬퍼 (Mock/실제 API 자동 분기)
  Future<ApiResponse<T>> _post<T>(
    String url, {
    dynamic data,
    T Function(dynamic)? fromData,
  }) {
    if (_useMock) {
      return _mockApiService!.post<T>(url, data: data, fromData: fromData);
    }
    return _apiService!.post<T>(url, data: data, fromData: fromData);
  }

  // ── 배차(Dispatch) 관련 ──

  /// 배차 목록 조회
  ///
  /// [isManager]가 true이면 전체 배차를, false이면 내 배차만 조회합니다.
  /// 오늘 날짜 기준으로 필터링됩니다.
  Future<void> fetchDispatches({bool isManager = false}) async {
    _isLoading = true;
    _errorMessage = null;
    _isOfflineMode = false;
    _currentPage = 0;
    _hasMore = true;
    notifyListeners();

    try {
      final url =
          isManager ? ApiConfig.dispatchesUrl : ApiConfig.myDispatchesUrl;
      final response = await _get<List<Dispatch>>(
        url,
        queryParameters: {
          'date': DateTime.now().toIso8601String().split('T')[0],
          'page': 0,
          'size': _pageSize,
        },
        fromData: (data) {
          // 단순 배열 응답 처리
          if (data is List) {
            return data
                .map((e) => Dispatch.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          // 페이징 응답 (content 필드) 처리
          if (data is Map<String, dynamic> && data.containsKey('content')) {
            return (data['content'] as List)
                .map((e) => Dispatch.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          return <Dispatch>[];
        },
      );

      if (response.success && response.data != null) {
        _dispatches = response.data!;
        _hasMore = _dispatches.length >= _pageSize;
        // 성공 시 캐시에 저장
        try {
          final cacheData = _dispatches.map((d) => d.toJson()).toList();
          await OfflineCacheService.save('dispatches', cacheData);
        } catch (_) {
          // 캐시 저장 실패는 무시
        }
      } else {
        _errorMessage = response.error?.message ?? '배차 목록을 불러올 수 없습니다.';
      }
    } catch (e) {
      // 오프라인 폴백: 캐시된 데이터 로드 시도
      try {
        final cached = await OfflineCacheService.load('dispatches');
        if (cached != null && cached is List) {
          _dispatches = cached
              .map((e) => Dispatch.fromJson(Map<String, dynamic>.from(e as Map)))
              .toList();
          _isOfflineMode = true;
          _errorMessage = null;
        } else {
          _errorMessage = '배차 목록 조회 중 오류가 발생했습니다.';
        }
      } catch (_) {
        _errorMessage = '배차 목록 조회 중 오류가 발생했습니다.';
      }
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 배차 목록 추가 페이지 조회 (무한 스크롤)
  ///
  /// [isManager]가 true이면 전체 배차를, false이면 내 배차만 조회합니다.
  /// [date]를 지정하면 해당 날짜 기준으로 필터링합니다.
  /// 더 이상 로드할 데이터가 없거나 이미 로딩 중이면 요청을 무시합니다.
  Future<void> fetchMoreDispatches({
    bool isManager = false,
    String? date,
  }) async {
    if (!_hasMore || _isLoading) return;

    _isLoading = true;
    _currentPage++;
    notifyListeners();

    try {
      final url =
          isManager ? ApiConfig.dispatchesUrl : ApiConfig.myDispatchesUrl;
      final response = await _get<List<Dispatch>>(
        url,
        queryParameters: {
          'date': date ?? DateTime.now().toIso8601String().split('T')[0],
          'page': _currentPage,
          'size': _pageSize,
        },
        fromData: (data) {
          if (data is List) {
            return data
                .map((e) => Dispatch.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          if (data is Map<String, dynamic> && data.containsKey('content')) {
            return (data['content'] as List)
                .map((e) => Dispatch.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          return <Dispatch>[];
        },
      );

      if (response.success && response.data != null) {
        final newItems = response.data!;
        _dispatches = [..._dispatches, ...newItems];
        _hasMore = newItems.length >= _pageSize;
      } else {
        _currentPage--;
      }
    } catch (e) {
      _currentPage--;
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 페이지네이션 상태 초기화
  void resetPagination() {
    _currentPage = 0;
    _hasMore = true;
  }

  /// 배차 상세 조회
  ///
  /// [id]에 해당하는 배차의 상세 정보를 불러와 [selectedDispatch]에 저장합니다.
  Future<void> fetchDispatchDetail(String id) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final response = await _get<Dispatch>(
        ApiConfig.dispatchDetailUrl(id),
        fromData: (data) =>
            Dispatch.fromJson(data as Map<String, dynamic>),
      );

      if (response.success && response.data != null) {
        _selectedDispatch = response.data;
      } else {
        _errorMessage = response.error?.message ?? '배차 상세를 불러올 수 없습니다.';
      }
    } catch (e) {
      _errorMessage = '배차 상세 조회 중 오류가 발생했습니다.';
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 배차 선택 (목록에서 상세로 이동 시)
  void selectDispatch(Dispatch dispatch) {
    _selectedDispatch = dispatch;
    notifyListeners();
  }

  // ── 계량 기록(Weighing Record) 관련 ──

  /// 계량 기록 조회
  ///
  /// [dispatchId], [startDate], [endDate] 파라미터로 필터링할 수 있습니다.
  /// 날짜 형식은 'yyyy-MM-dd'입니다.
  Future<void> fetchWeighingRecords({
    String? dispatchId,
    String? startDate,
    String? endDate,
  }) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final queryParams = <String, dynamic>{};
      if (dispatchId != null) queryParams['dispatchId'] = dispatchId;
      if (startDate != null) queryParams['startDate'] = startDate;
      if (endDate != null) queryParams['endDate'] = endDate;

      final response = await _get<List<WeighingRecord>>(
        ApiConfig.weighingsUrl,
        queryParameters: queryParams,
        fromData: (data) {
          // 단순 배열 응답 처리
          if (data is List) {
            return data
                .map(
                    (e) => WeighingRecord.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          // 페이징 응답 (content 필드) 처리
          if (data is Map<String, dynamic> && data.containsKey('content')) {
            return (data['content'] as List)
                .map(
                    (e) => WeighingRecord.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          return <WeighingRecord>[];
        },
      );

      if (response.success && response.data != null) {
        _weighingRecords = response.data!;
      } else {
        _errorMessage = response.error?.message ?? '계량 기록을 불러올 수 없습니다.';
      }
    } catch (e) {
      _errorMessage = '계량 기록 조회 중 오류가 발생했습니다.';
    }

    _isLoading = false;
    notifyListeners();
  }

  // ── OTP 인증 ──

  /// OTP 인증 요청
  ///
  /// [otp] 코드와 [dispatchId]를 서버로 전송하여 검증합니다.
  /// 성공 시 `ApiResponse<bool>(data: true)`를 반환합니다.
  Future<ApiResponse<bool>> verifyOtp({
    required String otp,
    required String dispatchId,
  }) async {
    try {
      final response = await _post<bool>(
        ApiConfig.otpVerifyUrl,
        data: {
          'otp': otp,
          'dispatchId': dispatchId,
        },
        fromData: (data) {
          if (data is bool) return data;
          if (data is Map<String, dynamic>) return data['verified'] as bool? ?? false;
          return false;
        },
      );
      return response;
    } catch (e) {
      return ApiResponse<bool>(
        success: false,
        error: ApiError(code: 'OTP_ERROR', message: 'OTP 인증 중 오류가 발생했습니다.'),
      );
    }
  }

  // ── 계량표(Slip) 관련 ──

  /// 계량표 목록 조회
  ///
  /// [startDate]~[endDate] 범위로 필터링합니다.
  /// 날짜 형식은 'yyyy-MM-dd'입니다.
  Future<void> fetchSlips({
    String? startDate,
    String? endDate,
  }) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final queryParams = <String, dynamic>{};
      if (startDate != null) queryParams['startDate'] = startDate;
      if (endDate != null) queryParams['endDate'] = endDate;

      final response = await _get<List<WeighingSlip>>(
        ApiConfig.slipsUrl,
        queryParameters: queryParams,
        fromData: (data) {
          // 단순 배열 응답 처리
          if (data is List) {
            return data
                .map(
                    (e) => WeighingSlip.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          // 페이징 응답 (content 필드) 처리
          if (data is Map<String, dynamic> && data.containsKey('content')) {
            return (data['content'] as List)
                .map(
                    (e) => WeighingSlip.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          return <WeighingSlip>[];
        },
      );

      if (response.success && response.data != null) {
        _slips = response.data!;
      } else {
        _errorMessage = response.error?.message ?? '계량표 목록을 불러올 수 없습니다.';
      }
    } catch (e) {
      _errorMessage = '계량표 목록 조회 중 오류가 발생했습니다.';
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 계량표 상세 조회
  ///
  /// [id]에 해당하는 계량표의 상세 정보를 불러와 [selectedSlip]에 저장합니다.
  Future<void> fetchSlipDetail(String id) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final response = await _get<WeighingSlip>(
        ApiConfig.slipDetailUrl(id),
        fromData: (data) =>
            WeighingSlip.fromJson(data as Map<String, dynamic>),
      );

      if (response.success && response.data != null) {
        _selectedSlip = response.data;
      } else {
        _errorMessage = response.error?.message ?? '계량표를 불러올 수 없습니다.';
      }
    } catch (e) {
      _errorMessage = '계량표 조회 중 오류가 발생했습니다.';
    }

    _isLoading = false;
    notifyListeners();
  }

  /// 계량표 공유
  ///
  /// [slipId]의 계량표를 [shareType] 방식으로 공유합니다.
  /// [shareType]은 'KAKAO' 또는 'SMS'이며, SMS의 경우 [phoneNumber]가 필요합니다.
  Future<ApiResponse<bool>> shareSlip({
    required String slipId,
    required String shareType, // 'KAKAO' or 'SMS'
    String? phoneNumber,
  }) async {
    try {
      final response = await _post<bool>(
        ApiConfig.slipShareUrl(slipId),
        data: {
          'shareType': shareType,
          if (phoneNumber != null) 'phoneNumber': phoneNumber,
        },
        fromData: (data) => true,
      );
      return response;
    } catch (e) {
      return ApiResponse<bool>(
        success: false,
        error: ApiError(code: 'SHARE_ERROR', message: '공유 중 오류가 발생했습니다.'),
      );
    }
  }

  /// 오류 상태 초기화
  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  /// 선택 상태 초기화 (배차/계량표)
  void clearSelection() {
    _selectedDispatch = null;
    _selectedSlip = null;
    notifyListeners();
  }
}

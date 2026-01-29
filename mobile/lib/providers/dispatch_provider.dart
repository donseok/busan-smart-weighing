import 'package:flutter/foundation.dart';
import '../config/api_config.dart';
import '../models/dispatch.dart';
import '../models/weighing_record.dart';
import '../models/weighing_slip.dart';
import '../models/api_response.dart';
import '../services/api_service.dart';
import '../services/mock_api_service.dart';

class DispatchProvider extends ChangeNotifier {
  final ApiService? _apiService;
  final MockApiService? _mockApiService;
  bool get _useMock => _mockApiService != null;

  List<Dispatch> _dispatches = [];
  Dispatch? _selectedDispatch;
  List<WeighingRecord> _weighingRecords = [];
  List<WeighingSlip> _slips = [];
  WeighingSlip? _selectedSlip;

  bool _isLoading = false;
  String? _errorMessage;

  DispatchProvider(ApiService apiService)
      : _apiService = apiService,
        _mockApiService = null;

  DispatchProvider.mock(MockApiService mockService)
      : _apiService = null,
        _mockApiService = mockService;

  List<Dispatch> get dispatches => _dispatches;
  Dispatch? get selectedDispatch => _selectedDispatch;
  List<WeighingRecord> get weighingRecords => _weighingRecords;
  List<WeighingSlip> get slips => _slips;
  WeighingSlip? get selectedSlip => _selectedSlip;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  // ── 내부 API 호출 헬퍼 ──

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

  // ---- Dispatches ----

  Future<void> fetchDispatches({bool isManager = false}) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final url =
          isManager ? ApiConfig.dispatchesUrl : ApiConfig.myDispatchesUrl;
      final response = await _get<List<Dispatch>>(
        url,
        queryParameters: {'date': DateTime.now().toIso8601String().split('T')[0]},
        fromData: (data) {
          if (data is List) {
            return data
                .map((e) => Dispatch.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          // Handle paginated response
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
      } else {
        _errorMessage = response.error?.message ?? '배차 목록을 불러올 수 없습니다.';
      }
    } catch (e) {
      _errorMessage = '배차 목록 조회 중 오류가 발생했습니다.';
    }

    _isLoading = false;
    notifyListeners();
  }

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

  void selectDispatch(Dispatch dispatch) {
    _selectedDispatch = dispatch;
    notifyListeners();
  }

  // ---- Weighing Records ----

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
          if (data is List) {
            return data
                .map(
                    (e) => WeighingRecord.fromJson(e as Map<String, dynamic>))
                .toList();
          }
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

  // ---- OTP ----

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

  // ---- Slips ----

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
          if (data is List) {
            return data
                .map(
                    (e) => WeighingSlip.fromJson(e as Map<String, dynamic>))
                .toList();
          }
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

  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  void clearSelection() {
    _selectedDispatch = null;
    _selectedSlip = null;
    notifyListeners();
  }
}

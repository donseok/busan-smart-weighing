import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

/// 오프라인 캐시 서비스
///
/// API 응답을 로컬에 캐시하여 오프라인 시에도 데이터를 표시합니다.
/// SharedPreferences를 사용한 간단한 키-값 캐시 구현입니다.
class OfflineCacheService {
  static const String _prefix = 'cache_';
  static const Duration _defaultExpiry = Duration(hours: 1);

  /// 데이터를 캐시에 저장
  static Future<void> save(String key, dynamic data) async {
    final prefs = await SharedPreferences.getInstance();
    final cacheEntry = {
      'data': data,
      'timestamp': DateTime.now().toIso8601String(),
    };
    await prefs.setString('$_prefix$key', jsonEncode(cacheEntry));
  }

  /// 캐시에서 데이터 로드 (만료 확인 포함)
  static Future<dynamic> load(String key, {Duration? expiry}) async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString('$_prefix$key');
    if (raw == null) return null;

    try {
      final entry = jsonDecode(raw) as Map<String, dynamic>;
      final timestamp = DateTime.parse(entry['timestamp'] as String);
      final maxAge = expiry ?? _defaultExpiry;

      if (DateTime.now().difference(timestamp) > maxAge) {
        // Cache expired
        await prefs.remove('$_prefix$key');
        return null;
      }

      return entry['data'];
    } catch (_) {
      return null;
    }
  }

  /// 특정 캐시 항목 삭제
  static Future<void> remove(String key) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('$_prefix$key');
  }

  /// 모든 캐시 삭제
  static Future<void> clearAll() async {
    final prefs = await SharedPreferences.getInstance();
    final keys = prefs.getKeys().where((k) => k.startsWith(_prefix));
    for (final key in keys) {
      await prefs.remove(key);
    }
  }

  /// 캐시 존재 여부 확인
  static Future<bool> has(String key) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.containsKey('$_prefix$key');
  }
}

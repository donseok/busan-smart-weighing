import 'package:flutter/material.dart';

/// 앱 전역 컬러 시스템
///
/// 홈 화면, 로그인 화면 등에서 공통으로 사용하는 색상 상수입니다.
/// Tailwind CSS Slate 팔레트 기반의 다크 테마 컬러입니다.
class AppColors {
  AppColors._();

  // ── 배경 ──
  static const backgroundDark = Color(0xFF0B1120);
  static const navyDeep = Color(0xFF0F172A);
  static const surface = Color(0xFF1E293B);
  static const surfaceLight = Color(0xFF334155);

  // ── 주요 색상 ──
  static const primary = Color(0xFF06B6D4);
  static const green = Color(0xFF10B981);
  static const amber = Color(0xFFF59E0B);
  static const blue = Color(0xFF3B82F6);
  static const errorRose = Color(0xFFF43F5E);

  // ── 텍스트 / 보조 ──
  static const white = Color(0xFFF8FAFC);
  static const slate = Color(0xFF94A3B8);
  static const slateLight = Color(0xFFCBD5E1);
  static const slateFooter = Color(0xFF475569);
}

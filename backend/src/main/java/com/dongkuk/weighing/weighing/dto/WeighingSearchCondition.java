package com.dongkuk.weighing.weighing.dto;

import com.dongkuk.weighing.weighing.domain.WeighingMode;
import com.dongkuk.weighing.weighing.domain.WeighingStatus;

import java.time.LocalDate;

/**
 * 계량 기록 검색 조건 DTO
 *
 * 계량 기록 목록 조회 시 필터링 조건을 담는 객체입니다.
 * 날짜 범위, 계량 모드, 상태, 차량 번호 등으로 검색할 수 있습니다.
 *
 * @param dateFrom 검색 시작 날짜 (이 날짜 이후, 선택)
 * @param dateTo 검색 종료 날짜 (이 날짜 이전, 선택)
 * @param weighingMode 계량 모드 필터 (자동/수동 등, 선택)
 * @param status 계량 상태 필터 (진행중/완료 등, 선택)
 * @param lprPlateNumber 차량 번호 검색어 (부분 일치, 선택)
 *
 * @author 시스템
 * @since 1.0
 */
public record WeighingSearchCondition(
    LocalDate dateFrom,
    LocalDate dateTo,
    WeighingMode weighingMode,
    WeighingStatus status,
    String lprPlateNumber
) {}

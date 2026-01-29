package com.dongkuk.weighing.dispatch.dto;

import com.dongkuk.weighing.dispatch.domain.DispatchStatus;
import com.dongkuk.weighing.dispatch.domain.ItemType;

import java.time.LocalDate;

/**
 * 배차 검색 조건 DTO
 *
 * 배차 목록 조회 시 필터링 조건을 담는 객체입니다.
 * 날짜 범위, 품목 유형, 배차 상태 등으로 검색할 수 있습니다.
 *
 * @param dateFrom 검색 시작 날짜 (이 날짜 이후, 선택)
 * @param dateTo 검색 종료 날짜 (이 날짜 이전, 선택)
 * @param itemType 품목 유형 필터 (선택)
 * @param status 배차 상태 필터 (대기/진행중/완료 등, 선택)
 *
 * @author 시스템
 * @since 1.0
 */
public record DispatchSearchCondition(
    LocalDate dateFrom,
    LocalDate dateTo,
    ItemType itemType,
    DispatchStatus status
) {}

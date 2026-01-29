package com.dongkuk.weighing.dispatch.dto;

import com.dongkuk.weighing.dispatch.domain.ItemType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 배차 생성 요청 DTO
 *
 * 새로운 배차 정보를 등록하기 위한 요청 데이터를 담는 객체입니다.
 * 차량, 거래처, 품목, 배차 일자, 출발지/목적지 등의 정보를 포함합니다.
 *
 * @param vehicleId 차량 고유 식별자 (필수)
 * @param companyId 거래처(업체) 고유 식별자 (필수)
 * @param itemType 품목 유형 (필수)
 * @param itemName 품목명 (필수, 최대 100자)
 * @param dispatchDate 배차 예정 날짜 (필수, 현재 또는 미래 날짜)
 * @param originLocation 출발지 (선택, 최대 100자)
 * @param destination 목적지 (선택, 최대 100자)
 * @param remarks 비고 사항 (선택)
 *
 * @author 시스템
 * @since 1.0
 */
public record DispatchCreateRequest(
    @NotNull
    Long vehicleId,

    @NotNull
    Long companyId,

    @NotNull
    ItemType itemType,

    @NotBlank @Size(max = 100)
    String itemName,

    @NotNull @FutureOrPresent
    LocalDate dispatchDate,

    @Size(max = 100)
    String originLocation,

    @Size(max = 100)
    String destination,

    String remarks
) {}

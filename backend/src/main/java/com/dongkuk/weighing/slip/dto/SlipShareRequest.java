package com.dongkuk.weighing.slip.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 전자계량표 공유 요청 DTO
 *
 * 전자계량표를 외부로 공유(전송)하기 위한 요청 객체입니다.
 * 공유 방식(이메일, SMS, 인쇄 등)을 지정합니다.
 *
 * @param type 공유 방식 (예: EMAIL, SMS, PRINT 등, 필수)
 *
 * @author 시스템
 * @since 1.0
 */
public record SlipShareRequest(
    @NotBlank
    String type
) {}

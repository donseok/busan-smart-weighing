package com.dongkuk.weighing.global.util;

import com.dongkuk.weighing.global.common.util.MaskingUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingUtilTest {

    @Nested
    @DisplayName("전화번호 마스킹")
    class MaskPhone {

        @Test
        @DisplayName("010-1234-5678 → 010-****-5678")
        void shouldMaskMiddleDigits() {
            assertThat(MaskingUtil.maskPhone("010-1234-5678"))
                    .isEqualTo("010-****-5678");
        }

        @Test
        @DisplayName("01012345678 → 010-****-5678 (하이픈 없는 경우)")
        void shouldMaskWithoutHyphens() {
            assertThat(MaskingUtil.maskPhone("01012345678"))
                    .isEqualTo("010-****-5678");
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void shouldReturnNullForNullInput() {
            assertThat(MaskingUtil.maskPhone(null)).isNull();
        }

        @Test
        @DisplayName("짧은 문자열은 그대로 반환")
        void shouldReturnShortStringAsIs() {
            assertThat(MaskingUtil.maskPhone("010")).isEqualTo("010");
        }
    }

    @Nested
    @DisplayName("차량번호 마스킹")
    class MaskPlateNumber {

        @Test
        @DisplayName("12가3456 → 12가****")
        void shouldMaskLastFourChars() {
            assertThat(MaskingUtil.maskPlateNumber("12가3456"))
                    .isEqualTo("12가****");
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void shouldReturnNullForNullInput() {
            assertThat(MaskingUtil.maskPlateNumber(null)).isNull();
        }
    }
}

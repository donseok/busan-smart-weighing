package com.dongkuk.weighing.global.common.util;

/**
 * 개인정보 마스킹 유틸리티
 *
 * 전화번호, 차량번호 등 개인정보를 마스킹 처리하여
 * 로그나 화면에 노출 시 민감 정보를 보호한다.
 * 인스턴스 생성을 방지하는 유틸리티 클래스이다.
 *
 * @author 시스템
 * @since 1.0
 */
public final class MaskingUtil {

    /** 인스턴스 생성 방지 */
    private MaskingUtil() {}

    /**
     * 전화번호 마스킹: 010-1234-5678 → 010-****-5678
     * 중간 자릿수를 '*'로 대체하여 개인 식별을 방지한다.
     *
     * @param phone 원본 전화번호
     * @return 마스킹된 전화번호 (null이거나 10자 미만이면 원본 반환)
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        return phone.replaceAll("(\\d{3})-?(\\d{3,4})-?(\\d{4})", "$1-****-$3");
    }

    /**
     * 차량번호 마스킹: 12가3456 → 12가*****
     * 앞 3자만 노출하고 나머지를 '*'로 대체한다.
     *
     * @param plate 원본 차량번호
     * @return 마스킹된 차량번호 (null이거나 4자 미만이면 원본 반환)
     */
    public static String maskPlateNumber(String plate) {
        if (plate == null || plate.length() < 4) return plate;
        String visible = plate.substring(0, 3);
        String masked = "*".repeat(plate.length() - 3);
        return visible + masked;
    }
}

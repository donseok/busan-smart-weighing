package com.dongkuk.weighing.global.common.util;

public final class MaskingUtil {

    private MaskingUtil() {}

    /**
     * 전화번호 마스킹: 010-1234-5678 → 010-****-5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        return phone.replaceAll("(\\d{3})-?(\\d{3,4})-?(\\d{4})", "$1-****-$3");
    }

    /**
     * 차량번호 마스킹: 12가3456 → 12가*****
     */
    public static String maskPlateNumber(String plate) {
        if (plate == null || plate.length() < 4) return plate;
        String visible = plate.substring(0, 3);
        String masked = "*".repeat(plate.length() - 3);
        return visible + masked;
    }
}

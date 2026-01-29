package com.dongkuk.weighing.global.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM 암호화/복호화 유틸리티
 *
 * 민감한 데이터(개인정보 등)를 AES-256-GCM 알고리즘으로 암호화하고 복호화한다.
 * GCM 모드는 기밀성과 무결성을 동시에 보장하며,
 * 매 암호화마다 랜덤 IV(초기화 벡터)를 생성하여 동일 평문도 다른 암호문을 생성한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Component
public class EncryptionUtil {

    /** 암호화 알고리즘 */
    private static final String ALGORITHM = "AES";

    /** 암호화 변환 방식 (AES/GCM/NoPadding) */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /** GCM 인증 태그 길이 (비트) */
    private static final int GCM_TAG_LENGTH = 128;

    /** 초기화 벡터(IV) 길이 (바이트) - GCM 권장 12바이트 */
    private static final int IV_LENGTH = 12;

    /** AES 비밀 키 */
    private final SecretKey secretKey;

    /**
     * Base64 인코딩된 AES 키로 EncryptionUtil을 초기화한다.
     *
     * @param base64Key Base64 인코딩된 AES-256 키
     */
    public EncryptionUtil(@Value("${encryption.aes-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * AES-256-GCM 암호화.
     * 랜덤 IV를 생성하고, IV + 암호문 + 인증태그를 결합하여 Base64로 인코딩한다.
     *
     * @param plainText 평문 텍스트
     * @return Base64(IV + CipherText + Tag)
     */
    public String encrypt(String plainText) {
        try {
            // 암호학적으로 안전한 랜덤 IV 생성
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // AES-GCM 암호화 수행
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV와 암호문을 하나의 바이트 배열로 결합
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * AES-256-GCM 복호화.
     * Base64 디코딩 후 IV와 암호문을 분리하여 원본 평문을 복원한다.
     *
     * @param cipherText Base64 인코딩된 암호문
     * @return 복호화된 평문 텍스트
     */
    public String decrypt(String cipherText) {
        try {
            // Base64 디코딩 후 IV와 암호문 분리
            byte[] combined = Base64.getDecoder().decode(cipherText);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            // AES-GCM 복호화 수행
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}

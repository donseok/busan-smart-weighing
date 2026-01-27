package com.dongkuk.weighing.global.util;

import com.dongkuk.weighing.global.common.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        // AES-256 requires 32 bytes key → Base64 encoded
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) (i + 1);
        }
        String base64Key = Base64.getEncoder().encodeToString(key);
        encryptionUtil = new EncryptionUtil(base64Key);
    }

    @Test
    @DisplayName("암호화 후 복호화하면 원본과 동일하다")
    void encryptAndDecrypt() {
        String plainText = "010-1234-5678";

        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("같은 평문이라도 매번 다른 암호문이 생성된다 (IV 랜덤)")
    void encryptProducesDifferentCiphertext() {
        String plainText = "010-1234-5678";

        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = encryptionUtil.encrypt(plainText);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("암호화된 결과는 Base64 형식이다")
    void encryptedResultIsBase64() {
        String encrypted = encryptionUtil.encrypt("test");

        assertThat(encrypted).matches("^[A-Za-z0-9+/]+=*$");
    }

    @Test
    @DisplayName("빈 문자열도 암호화/복호화 가능하다")
    void encryptEmptyString() {
        String encrypted = encryptionUtil.encrypt("");
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertThat(decrypted).isEmpty();
    }

    @Test
    @DisplayName("한글 문자열도 암호화/복호화 가능하다")
    void encryptKoreanText() {
        String plainText = "부산공장 스마트 계량 시스템";

        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("잘못된 암호문을 복호화하면 예외가 발생한다")
    void decryptInvalidCiphertext() {
        assertThatThrownBy(() -> encryptionUtil.decrypt("invalid-base64!@#"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("변조된 암호문을 복호화하면 예외가 발생한다")
    void decryptTamperedCiphertext() {
        String encrypted = encryptionUtil.encrypt("010-1234-5678");
        // tamper the ciphertext by modifying a character
        char[] chars = encrypted.toCharArray();
        chars[20] = (chars[20] == 'A') ? 'B' : 'A';
        String tampered = new String(chars);

        assertThatThrownBy(() -> encryptionUtil.decrypt(tampered))
                .isInstanceOf(RuntimeException.class);
    }
}

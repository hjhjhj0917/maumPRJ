package com.example.maum.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

// 메서드는 전부 static(기존 호출부 전부 EncryptUtil.encAES128BCBC(...) 형태로 쓰고 있어 그대로 유지)이지만,
// key만은 소스코드 상수가 아니라 application-secret.yaml에서 주입받아야 해서 @Component로 등록해
// Spring이 기동 시점에 setKey를 한 번 호출하게 함(정적 필드를 인스턴스 세터로 채우는 방식)
@Component
public class EncryptUtil {

    private static final int IV_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static String key;

    @Value("${secure.encrypt.aes.key}")
    public void setKey(String key) {
        EncryptUtil.key = key;
    }

    public static String encAES128BCBC(String str) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        // CBC는 매 암호화마다 새 IV를 써야 같은 평문이 매번 다른 암호문으로 나옴(고정 IV 재사용 시
        // 같은 평문이 항상 같은 암호문이 되어 DB에서 평문을 몰라도 중복 여부가 드러나는 문제가 있었음) —
        // 복호화 때 다시 꺼내 써야 하므로 암호문 앞에 IV를 그대로 붙여서 저장함
        byte[] ivBytes = new byte[IV_LENGTH];
        SECURE_RANDOM.nextBytes(ivBytes);

        return encryptWithIv(str, ivBytes);
    }

    // ★ 즐겨찾기 이후 추가/수정
    // 이메일처럼 DB에서 "=" 로 찾아야 하는(아이디/비밀번호 찾기, 가입 중복 확인, UNIQUE 제약) 값은
    // 랜덤 IV를 쓰면 같은 이메일도 매번 다른 암호문이 되어 조회가 항상 실패함 —
    // 그래서 IV를 평문+키로 만든 HMAC에서 뽑아, 같은 평문이면 항상 같은 암호문이 나오게 함.
    // 복호화 형식(IV + 암호문)은 동일하므로 decAES128BCBC를 그대로 쓰면 됨
    public static String encAES128BCBCDeterministic(String str) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] ivBytes = Arrays.copyOf(mac.doFinal(str.getBytes(StandardCharsets.UTF_8)), IV_LENGTH);

        return encryptWithIv(str, ivBytes);
    }

    private static String encryptWithIv(String str, byte[] ivBytes) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        byte[] textBytes = str.getBytes(StandardCharsets.UTF_8);

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec((key.getBytes(StandardCharsets.UTF_8)), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

        byte[] encrypted = cipher.doFinal(textBytes);

        byte[] result = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(ivBytes, 0, result, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    public static String decAES128BCBC(String str) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        byte[] decoded = Base64.getDecoder().decode(str);

        byte[] ivBytes = Arrays.copyOfRange(decoded, 0, IV_LENGTH);
        byte[] textBytes = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec((key.getBytes(StandardCharsets.UTF_8)), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return new String(cipher.doFinal(textBytes), StandardCharsets.UTF_8);
    }
}

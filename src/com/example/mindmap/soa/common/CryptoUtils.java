package com.example.mindmap.soa.common;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoUtils {
    private CryptoUtils() {}

    private static final SecureRandom RNG = new SecureRandom();
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private static byte[] deriveKeyBytes(String sharedSecret) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return sha.digest(sharedSecret.getBytes(StandardCharsets.UTF_8)); // 32 bytes => AES-256
        } catch (Exception e) {
            throw new RuntimeException("Cannot derive key", e);
        }
    }

    private static SecretKeySpec key() {
        return new SecretKeySpec(deriveKeyBytes(SoaConfig.SHARED_SECRET), "AES");
    }

    public static String randomIvB64() {
        byte[] iv = new byte[IV_BYTES];
        RNG.nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    public static String encryptToB64(String ivB64, String plainText) {
        try {
            byte[] iv = Base64.getDecoder().decode(ivB64);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ct);
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    public static String decryptFromB64(String ivB64, String cipherTextB64) {
        try {
            byte[] iv = Base64.getDecoder().decode(ivB64);
            byte[] ct = Base64.getDecoder().decode(cipherTextB64);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed", e);
        }
    }
}

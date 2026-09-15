package pierro.lmsPlatform.Security.Crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final SecretKeySpec secretKey = loadKey();

    private static SecretKeySpec loadKey() {
        String keyBase64 = System.getenv("APP_ENCRYPTION_KEY");

        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new RuntimeException("Missing APP_ENCRYPTION_KEY environment variable");
        }

        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);

        if (keyBytes.length != 32) {
            throw new RuntimeException("APP_ENCRYPTION_KEY must be 256-bit AES key");
        }

        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plainText) {
        try {
            if (plainText == null) {
                return null;
            }

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            if (encryptedText == null) {
                return null;
            }

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedBytes);

            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decrypt failed", e);
        }
    }
}

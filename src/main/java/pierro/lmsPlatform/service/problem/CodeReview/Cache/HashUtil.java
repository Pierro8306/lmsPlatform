package pierro.lmsPlatform.service.problem.CodeReview.Cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class HashUtil {
    private HashUtil() {}

    public static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    // Chuẩn hóa code cho đúng định dạng
    public static String normalize(String code) {
        if (code == null) return "\n";
        String s = code.replace("\r\n", "\n").replace('\r', '\n').replaceAll("(?m)[ \\t]+$", "");
        return s.stripTrailing() + "\n";
    }
}
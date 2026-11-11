package pierro.lmsPlatform.security.rateLimit;

import org.springframework.stereotype.Service;
import pierro.lmsPlatform.security.exception.TooManyRequestsException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chống dò mật khẩu bằng 2 lớp:
 *  - theo (username + IP): {@value #MAX_FAILS_PER_KEY} lần sai => khoá 15 phút;
 *  - theo riêng IP: {@value #MAX_FAILS_PER_IP} lần sai (với bất kỳ username nào) => khoá IP 15 phút,
 *    chặn kiểu thử nhiều username khác nhau từ một IP.
 *
 * Mọi entry đều tự hết hạn (cửa sổ đếm 15 phút) và bị dọn khi map lớn, nên không rò bộ nhớ;
 * ngoài ra có trần cứng {@value #HARD_LIMIT} entry theo username.
 *
 * Lưu trong RAM nên chỉ phù hợp 1 instance; nhiều instance hãy chuyển sang Redis / Bucket4j.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_FAILS_PER_KEY = 5;
    private static final int MAX_FAILS_PER_IP = 50;
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);
    private static final Duration FAIL_WINDOW = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 10_000;
    private static final int HARD_LIMIT = 200_000;

    private record Attempt(int fails, Instant lockedUntil, Instant lastFailAt) {
        boolean isLocked(Instant now) {
            return lockedUntil != null && now.isBefore(lockedUntil);
        }

        /** Đã hết hạn khoá, hoặc (chưa khoá) lần sai cuối đã ngoài cửa sổ đếm => có thể bỏ. */
        boolean isStale(Instant now) {
            Instant until = lockedUntil != null ? lockedUntil : lastFailAt.plus(FAIL_WINDOW);
            return now.isAfter(until);
        }
    }

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public static String key(String username, String ip) {
        return (username == null ? "" : username.trim().toLowerCase()) + "|" + ip;
    }

    /** Key theo IP. Không chứa '|' nên không bao giờ trùng với key (username|ip). */
    private static String ipKey(String key) {
        return "ip:" + key.substring(key.lastIndexOf('|') + 1);
    }

    public void checkAllowed(String key) {
        Instant now = Instant.now();
        if (isLocked(key, now) || isLocked(ipKey(key), now)) {
            throw new TooManyRequestsException("Đăng nhập sai quá nhiều lần, vui lòng thử lại sau 15 phút");
        }
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        if (attempts.size() > CLEANUP_THRESHOLD) {
            attempts.entrySet().removeIf(e -> e.getValue().isStale(now));
        }
        // Trần cứng: khi bị spam username ngẫu nhiên, ngừng ghi key theo username mới (key theo IP vẫn đếm)
        if (attempts.size() < HARD_LIMIT || attempts.containsKey(key)) {
            fail(key, MAX_FAILS_PER_KEY, now);
        }
        fail(ipKey(key), MAX_FAILS_PER_IP, now);
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private boolean isLocked(String k, Instant now) {
        Attempt a = attempts.get(k);
        if (a == null) return false;
        if (a.isLocked(now)) return true;
        if (a.isStale(now)) attempts.remove(k, a);
        return false;
    }

    private void fail(String k, int maxFails, Instant now) {
        attempts.compute(k, (kk, old) -> {
            int fails = (old == null || old.isStale(now)) ? 1 : old.fails() + 1;
            return fails >= maxFails
                    ? new Attempt(0, now.plus(LOCK_TIME), now)
                    : new Attempt(fails, null, now);
        });
    }
}

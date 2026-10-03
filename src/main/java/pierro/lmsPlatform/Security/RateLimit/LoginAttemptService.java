package pierro.lmsPlatform.Security.RateLimit;

import org.springframework.stereotype.Service;
import pierro.lmsPlatform.Security.Exception.TooManyRequestsException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Khoá tạm (username + IP) sau N lần đăng nhập sai. Lưu trong RAM nên chỉ phù hợp 1 instance;
 * nếu chạy nhiều instance hãy chuyển sang Redis / Bucket4j.
 */
@Service
public class LoginAttemptService {
    private static final int MAX_FAILS = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 10_000;

    private record Attempt(int fails, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public static String key(String username, String ip) {
        return (username == null ? "" : username.trim().toLowerCase()) + "|" + ip;
    }

    public void checkAllowed(String key) {
        Attempt a = attempts.get(key);
        if (a != null && a.lockedUntil() != null) {
            if (Instant.now().isBefore(a.lockedUntil())) {
                throw new TooManyRequestsException("Đăng nhập sai quá nhiều lần, vui lòng thử lại sau 15 phút");
            }
            attempts.remove(key, a);
        }
    }

    public void recordFailure(String key) {
        if (attempts.size() > CLEANUP_THRESHOLD) {
            Instant now = Instant.now();
            attempts.entrySet().removeIf(e -> e.getValue().lockedUntil() != null
                    && now.isAfter(e.getValue().lockedUntil()));
        }
        attempts.compute(key, (k, old) -> {
            int fails = (old == null ? 0 : old.fails()) + 1;
            return fails >= MAX_FAILS
                    ? new Attempt(0, Instant.now().plus(LOCK_TIME))
                    : new Attempt(fails, null);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }
}

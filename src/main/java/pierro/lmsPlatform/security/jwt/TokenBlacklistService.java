package pierro.lmsPlatform.security.jwt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Quản lý danh sách đen (Blacklist) cho JWT token khi người dùng đăng xuất trên một thiết bị cụ thể.
 * Token bị đưa vào blacklist sẽ bị từ chối ngay trong JwtFilter / refresh service cho đến khi hết hạn (TTL).
 *
 * Mặc định sử dụng Guava Cache (in-memory) với thời gian tự hủy 7 ngày (tương ứng thời hạn tối đa của refresh token).
 * Trong kiến trúc nhiều node/server (multi-instance), có thể chuyển sang dùng Redis (RedisTemplate)
 * tương tự LoginAttemptService.
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklist = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofDays(7))
            .maximumSize(200_000)
            .build();

    public void blacklistToken(String token) {
        if (token != null && !token.isBlank()) {
            blacklist.put(token.trim(), Boolean.TRUE);
            log.debug("Token blacklisted successfully");
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return blacklist.getIfPresent(token.trim()) != null;
    }

    /** Xóa toàn bộ blacklist (dùng cho integration tests nếu cần). */
    public void clear() {
        blacklist.invalidateAll();
    }
}

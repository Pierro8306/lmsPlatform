package pierro.lmsPlatform.service.problem.CodeReview.Cache;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import pierro.lmsPlatform.entity.cache.AnalysisCache;
import pierro.lmsPlatform.repository.cache.AnalysisCacheRepository;

@Service
@RequiredArgsConstructor
public class AnalysisCacheService {
    private static final Logger log = LoggerFactory.getLogger(AnalysisCacheService.class);

    private final AnalysisCacheRepository repository;
    private final ObjectMapper objectMapper;

    /** Các lần gọi LLM đang chạy (cùng JVM): cùng key thì chỉ gọi một lần, các luồng khác chờ kết quả.
     * CompletableFuture đại diện cho 1 kết quả sẽ được trả về trong tương lai
     * ConcurrentHashMap sẽ là 1 cái hộp để chứa những kết quả đó ứng key tương ứng của nó
     */
    private final ConcurrentHashMap<String, CompletableFuture<String>> inflight = new ConcurrentHashMap<>();

    /** Đọc cache. Payload hỏng hoặc không đọc được (đổi cấu trúc record) thì coi như miss. */
    public <T> Optional<T> find(String key, Class<T> type) {

         if (key == null || key.isBlank()) {
        throw new IllegalArgumentException("Cache key must not be null or blank");
    }


        Optional<AnalysisCache> found = repository.findById(key);
        if (found.isEmpty()) return Optional.empty();// cache miss

        AnalysisCache e = found.get();
        T value;
        try {
            value = read(e.getPayload(), type);
        } catch (IllegalStateException ex) {
            log.warn("Cache payload không đọc được, bỏ qua (key={})", key, ex);
            return Optional.empty();
        }
        touch(e);
        return Optional.of(value);
    }

    /** Lưu cache. Key đã tồn tại thì giữ nguyên (không ghi đè, không reset hitCount). */
    public void put(String key, Object value) {
        try {
            if (repository.existsById(key)) return;
            AnalysisCache e = new AnalysisCache();
            e.setCacheKey(key);
            e.setPayload(json(value));
            repository.save(e);
        } catch (DataAccessException ex) {
            // Luồng tiến trình khác vừa lưu cùng key, hoặc DB lỗi tạm: cache chỉ là tối ưu nên không làm hỏng kết quả
            log.warn("Không lưu được cache (key={})", key, ex);
        }
    }

    /**
     * Thứ tự: DB -> luồng khác đang gọi cùng key -> tự gọi loader rồi lưu DB.
     * Loader ném RuntimeException thì không lưu gì và các luồng đang chờ cũng nhận lỗi.
     */
    public <T> T computeIfAbsent(String key, Class<T> type, Supplier<T> loader) {
        Optional<T> hit = find(key, type);
        if (hit.isPresent()) return hit.get();

        CompletableFuture<String> mine = new CompletableFuture<>();
        CompletableFuture<String> other = inflight.putIfAbsent(key, mine);// nếu key chưa có thì đẩy mine vào, other=null hoặc key đã có thì trả về future đang tồn tại
        if (other != null) {
            return read(other.join(), type); // chờ future của thread phía trước hoàn thành rồi lấy kết quả
        }

        try {
            T result = loader.get();        // thực hiện công việc cần xử lý 
            put(key, result);
            mine.complete(json(result));    // thông báo kết quả cho các future đang đợi
            return result;
        } catch (RuntimeException e) {
            mine.completeExceptionally(e);
            throw e;
        } finally {
            inflight.remove(key, mine);
        }
    }

    private void touch(AnalysisCache e) {
        try {
            e.setHitCount(e.getHitCount() + 1);
            e.setLastHitAt(LocalDateTime.now());
            repository.save(e);
        } catch (DataAccessException ex) {
            log.debug("Không cập nhật được hitCount (key={})", e.getCacheKey(), ex);   // thống kê, không quan trọng
        }
    }
    
    // chuyển java object thành json
    private String json(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // chuyển json thành java object
    private <T> T read(String s, Class<T> t) {
        try {
            return objectMapper.readValue(s, t);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
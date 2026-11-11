
package pierro.lmsPlatform.Service.Judge;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pierro.lmsPlatform.DTO.Request.Judge.JudgeRequest;
import pierro.lmsPlatform.DTO.Response.Judge.JudgeReport;
import pierro.lmsPlatform.Entity.Problem.Verdict;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hàng đợi chấm bài: tối đa `judge.workers` bài chấm đồng thời (mỗi bài 1 container tại 1 thời điểm).
 * Hàng đợi đầy (500) thì từ chối để không sập máy.
 *
 * Mọi future trả về đều được đảm bảo HOÀN THÀNH (kể cả khi app shutdown): bài đang chấm bị interrupt,
 * bài còn trong hàng đợi được trả kết quả SE thay vì treo vĩnh viễn.
 */
@Component
public class JudgeQueue {
    private static final Logger log = LoggerFactory.getLogger(JudgeQueue.class);
    private static final int QUEUE_CAPACITY = 500;

    private final JudgeService judgeService;
    private final ThreadPoolExecutor executor;
    /** Các future chưa hoàn thành - để shutdown() có thể hoàn thành nốt chúng. */
    private final Set<CompletableFuture<JudgeReport>> pending = ConcurrentHashMap.newKeySet();

    public JudgeQueue(JudgeService judgeService, @Value("${judge.workers:2}") int workers) {
        if (workers < 1) throw new IllegalArgumentException("judge.workers must be >= 1, got " + workers);
        this.judgeService = judgeService;
        AtomicInteger n = new AtomicInteger();
        this.executor = new ThreadPoolExecutor(workers, workers, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                r -> new Thread(r, "judge-worker-" + n.incrementAndGet()));
    }

    public CompletableFuture<JudgeReport> submit(JudgeRequest request) {
        CompletableFuture<JudgeReport> future = new CompletableFuture<>();
        pending.add(future);
        future.whenComplete((r, e) -> pending.remove(future));
        try {
            executor.execute(() -> {
                if (future.isDone()) return;                       // đã bị huỷ / hoàn thành sớm
                try {
                    future.complete(judgeService.judge(request));
                } catch (Throwable t) {                            // kể cả InterruptedException bọc ngoài, Error...
                    log.error("Judge worker failed", t);
                    future.complete(JudgeReport.failure(Verdict.SE, "Internal judge error"));
                }
            });
        } catch (RejectedExecutionException e) {
            future.complete(JudgeReport.failure(Verdict.SE, executor.isShutdown()
                    ? "Judge is shutting down, try again later"
                    : "Judge queue is full, try again later"));
        }
        return future;
    }

    @PreDestroy
    void shutdown() {
        // interrupt các worker đang chấm -> DockerSandbox sẽ `docker kill` container tương ứng
        executor.shutdownNow();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Các task chưa kịp chạy bị shutdownNow() vứt đi -> hoàn thành future của chúng để không ai chờ mãi.
        for (CompletableFuture<JudgeReport> f : pending) {
            f.complete(JudgeReport.failure(Verdict.SE, "Judge shut down before this submission was judged"));
        }
    }
}

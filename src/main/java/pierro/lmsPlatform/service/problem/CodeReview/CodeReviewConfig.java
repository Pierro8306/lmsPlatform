package pierro.lmsPlatform.service.problem.CodeReview;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Tạo 2 thread pool để xử lý LLM và xử lí phân tích tĩnh
@Configuration
public class CodeReviewConfig {
    private static ThreadFactory named(String p) {
        AtomicInteger n = new AtomicInteger();    //đếm sô lượng thread
        return r -> { Thread t = new Thread(r, p + n.incrementAndGet()); t.setDaemon(true); return t; };
    }

    @Bean(destroyMethod = "shutdownNow")
    ExecutorService crLlmPool(@Value("${codereview.llm.concurrency:3}") int n) {
        return Executors.newFixedThreadPool(n, named("cr-llm-"));
    }

    @Bean(destroyMethod = "shutdownNow")
    ExecutorService crStaticPool() { return Executors.newFixedThreadPool(2, named("cr-static-")); }
}

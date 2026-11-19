package pierro.lmsPlatform.dto.response.problem;

import java.time.LocalDateTime;
import java.util.List;

public record StudentProblemDetail(String id,
                                   String title,
                                   String content,
                                   String difficulty,
                                   int timeLimitMs,
                                   int memoryLimitKb,
                                   LocalDateTime startAt,
                                   LocalDateTime deadline,
                                   boolean open,
                                   Integer maxAttempts,
                                   List<SampleTest> samples) {
    public record SampleTest(  // dùng để cho học sinh xem đc 2 trường input và output
            String input,
            String expectedOutput
    ) {}
}

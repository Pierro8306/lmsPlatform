package pierro.lmsPlatform.dto.response.problem;

import java.time.LocalDateTime;
import java.util.List;

public record ProblemResponse(String id,
                              String title,
                              String slug,
                              String content,
                              String difficulty,
                              int timeLimitMs,
                              int memoryLimitKb,
                              boolean published,
                              LocalDateTime startAt,
                              LocalDateTime deadline,
                              boolean allowLate,
                              Integer maxAttempts,
                              List<String> groupIds,
                              int testCaseCount) {
}

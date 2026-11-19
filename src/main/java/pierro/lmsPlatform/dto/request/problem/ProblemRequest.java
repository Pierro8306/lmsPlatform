package pierro.lmsPlatform.dto.request.problem;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public record ProblemRequest(@NotBlank
                             String title,
                             @NotBlank
                             @Pattern(regexp = "[a-z0-9-]+")
                             String slug,
                             String content,
                             String difficulty,
                             @Min(50)
                             @Max(30000)
                             int timeLimitMs,
                             @Min(16384)
                             @Max(2097152)
                             int memoryLimitKb,
                             boolean published,
                             LocalDateTime startAt,
                             LocalDateTime deadline,
                             Boolean allowLate,
                             @Min(1)
                             Integer maxAttempts,
                             List<String> groupIds) {
}

package pierro.lmsPlatform.DTO.Response.Quiz;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** Dòng trong danh sách bài thi của ADMIN. status: UPCOMING | OPEN | CLOSED (theo giờ server). */
public record AdminQuizResponse(
        String id,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalTime timeLimit,
        int maxAttempts,
        String userGroup,
        String status,
        int questionCount,
        int attemptCount) {
}

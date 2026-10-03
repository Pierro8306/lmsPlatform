package pierro.lmsPlatform.DTO.Response.Quiz;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** Một lượt làm bài trong bảng điểm (ADMIN). points = số câu đúng, percentage = 0–100. */
public record AdminAttemptResponse(
        String attemptId,
        String userName,
        String status,
        double points,
        int totalQuestions,
        double percentage,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        LocalTime useTime) {
}

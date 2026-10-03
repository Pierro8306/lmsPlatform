package pierro.lmsPlatform.DTO.Response.Quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartQuizResponse {
    private String attemptId;
    private LocalDateTime startedAt;
    /** Hạn chót nộp bài do server tính: min(startedAt + timeLimit, quiz.endTime). */
    private LocalDateTime deadline;
    private List<QuestionResponse> questions;
}

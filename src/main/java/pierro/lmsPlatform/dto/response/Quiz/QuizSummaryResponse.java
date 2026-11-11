package pierro.lmsPlatform.dto.response.Quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSummaryResponse {
    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalTime timeLimit;
    private int maxAttempts;
    private int attemptsUsed;
    private int questionCount;
}

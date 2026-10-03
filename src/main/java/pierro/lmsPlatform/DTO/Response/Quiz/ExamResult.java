package pierro.lmsPlatform.DTO.Response.Quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pierro.lmsPlatform.DTO.Request.Quiz.QuizAnswer;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResult {
    private String attemptId;
    private String status;
    /** Số câu đúng. */
    private double points;
    private int totalQuestions;
    /** Phần trăm (0–100). */
    private double percentage;
    private LocalDateTime submittedAt;
    /** Đáp án đúng thật sự — chỉ có dữ liệu sau khi bài thi đã đóng (endTime). */
    private List<QuizAnswer> correct;
}

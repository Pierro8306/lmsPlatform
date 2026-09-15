package pierro.lmsPlatform.DTO.Response.Quiz;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.DTO.Request.Quiz.QuizAnswer;

import java.util.List;
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamResult {
    private double points;
    private List<QuizAnswer> correct;
}

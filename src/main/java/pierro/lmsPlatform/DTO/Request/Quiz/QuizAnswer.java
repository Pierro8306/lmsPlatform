package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAnswer {
    private Long id_question;
    private Long id_answer;
}

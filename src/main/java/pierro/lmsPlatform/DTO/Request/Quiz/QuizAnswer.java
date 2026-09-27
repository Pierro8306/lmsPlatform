package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAnswer {
    private String id_question;
    private String id_answer;
}

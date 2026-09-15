package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuizAnswer {
    private Long id_question;
    private Long id_answer;
}

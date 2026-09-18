package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.Data;

@Data
public class Choice {
    private String answerText;
    private boolean isCorrect;
}

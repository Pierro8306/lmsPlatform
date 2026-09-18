package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.Data;
import lombok.Getter;

@Data
public class Choice {
    private String answerText;
    private boolean isCorrect;
}

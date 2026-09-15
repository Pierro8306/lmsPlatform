package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.Getter;

@Getter
public class Choice {
    private String answerText;
    private boolean isCorrect;
}

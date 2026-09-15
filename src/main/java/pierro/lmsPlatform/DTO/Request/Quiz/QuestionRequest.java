package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.Getter;

import java.util.List;

@Getter
public class QuestionRequest {
    private String questionContent;
    private List<Choice> choices;
}

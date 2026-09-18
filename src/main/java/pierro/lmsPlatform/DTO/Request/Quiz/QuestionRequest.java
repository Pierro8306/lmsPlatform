package pierro.lmsPlatform.DTO.Request.Quiz;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class QuestionRequest {
    private String questionContent;
    private String subject;
    private List<Choice> choices;
}

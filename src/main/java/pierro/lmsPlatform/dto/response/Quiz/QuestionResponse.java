package pierro.lmsPlatform.dto.response.Quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {
    private String id;
    private String content;
    private String subject;
    private List<StudentChoiceResponse> choices;
}

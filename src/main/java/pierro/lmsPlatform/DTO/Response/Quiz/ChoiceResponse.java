package pierro.lmsPlatform.DTO.Response.Quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChoiceResponse {
    private String content;
    private boolean isCorrect;
}

package pierro.lmsPlatform.dto.request.Quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReplaceQuestionsRequest {
    @NotEmpty(message = "Bài thi cần ít nhất 1 câu hỏi")
    private List<@NotNull(message = "Câu hỏi không được null") @Valid QuestionRequest> questions;
}

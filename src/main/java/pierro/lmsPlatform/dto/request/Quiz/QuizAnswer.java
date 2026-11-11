package pierro.lmsPlatform.dto.request.Quiz;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Một lựa chọn của học viên. Câu nhiều đáp án đúng: gửi nhiều phần tử cùng id_question. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswer {
    @NotBlank
    private String id_question;
    @NotBlank
    private String id_answer;
}

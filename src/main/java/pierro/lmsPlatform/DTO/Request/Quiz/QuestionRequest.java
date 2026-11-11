package pierro.lmsPlatform.DTO.Request.Quiz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 2000, message = "Nội dung câu hỏi tối đa 2000 ký tự")
    private String questionContent;

    @Size(max = 255, message = "Chủ đề tối đa 255 ký tự")
    private String subject;

    @NotNull(message = "Câu hỏi cần có danh sách đáp án")
    @Size(min = 2, message = "Mỗi câu hỏi cần ít nhất 2 đáp án")
    private List<@NotNull(message = "Đáp án không được null") @Valid Choice> choices;

    /** @JsonIgnore để không lọt vào JSON schema gửi cho AI cũng như JSON trả về client. */
    @JsonIgnore
    @AssertTrue(message = "Mỗi câu hỏi cần ít nhất 1 đáp án đúng")
    public boolean isCorrectChoicePresent() {
        return choices == null || choices.stream().anyMatch(c -> c != null && c.isCorrect());
    }
}


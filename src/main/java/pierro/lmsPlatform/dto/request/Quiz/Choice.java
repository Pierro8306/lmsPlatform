package pierro.lmsPlatform.dto.request.Quiz;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Choice {
    @NotBlank(message = "Nội dung đáp án không được để trống")
    @Size(max = 1000, message = "Đáp án tối đa 1000 ký tự")
    private String answerText;

    // JSON vẫn dùng key "isCorrect" (khớp prompt AI + frontend); Java dùng isCorrect()/setCorrect()
    @JsonProperty("isCorrect")
    private boolean correct;
}


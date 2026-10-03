package pierro.lmsPlatform.DTO.Request.Quiz;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Choice {
    @NotBlank(message = "Nội dung đáp án không được để trống")
    private String answerText;

    // JSON vẫn dùng key "isCorrect" (khớp prompt AI + frontend); Java dùng isCorrect()/setCorrect()
    @JsonProperty("isCorrect")
    private boolean correct;
}

package pierro.lmsPlatform.DTO.Response.Quiz;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Tên trường khớp với request (answerText / isCorrect) để frontend sửa rồi gửi lại nguyên dạng. */
public record AdminChoiceResponse(
        String id,
        String answerText,
        @JsonProperty("isCorrect") boolean correct) {
}

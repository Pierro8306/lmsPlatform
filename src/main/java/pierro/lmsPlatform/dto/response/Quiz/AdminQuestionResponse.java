package pierro.lmsPlatform.dto.response.Quiz;

import java.util.List;

public record AdminQuestionResponse(
        String id,
        String questionContent,
        String subject,
        List<AdminChoiceResponse> choices) {
}

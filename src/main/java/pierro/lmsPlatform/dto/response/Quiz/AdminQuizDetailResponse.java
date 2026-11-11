package pierro.lmsPlatform.dto.response.Quiz;

import java.util.List;

public record AdminQuizDetailResponse(
        AdminQuizResponse quiz,
        List<AdminQuestionResponse> questions) {
}

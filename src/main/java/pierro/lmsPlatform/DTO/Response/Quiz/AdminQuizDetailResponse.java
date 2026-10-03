package pierro.lmsPlatform.DTO.Response.Quiz;

import java.util.List;

public record AdminQuizDetailResponse(
        AdminQuizResponse quiz,
        List<AdminQuestionResponse> questions) {
}

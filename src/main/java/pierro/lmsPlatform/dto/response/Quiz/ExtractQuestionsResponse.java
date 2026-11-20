package pierro.lmsPlatform.dto.response.Quiz;

import pierro.lmsPlatform.dto.request.Quiz.QuestionRequest;

import java.util.List;

public record ExtractQuestionsResponse(
        List<QuestionRequest> questions,
        int droppedCount
) {}

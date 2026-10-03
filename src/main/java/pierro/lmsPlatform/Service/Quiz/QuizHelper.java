package pierro.lmsPlatform.Service.Quiz;

import org.springframework.util.StringUtils;
import pierro.lmsPlatform.DTO.Request.Quiz.Choice;
import pierro.lmsPlatform.DTO.Request.Quiz.QuestionRequest;
import pierro.lmsPlatform.Entity.Quiz.Question;
import pierro.lmsPlatform.Entity.Quiz.QuestionChoice;
import pierro.lmsPlatform.Security.Exception.BadRequestException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class QuizHelper {
    private QuizHelper() {}

    public static void validateSchedule(LocalDateTime start, LocalDateTime end, LocalTime timeLimit) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("Thời gian đóng phải sau thời gian mở");
        }
        if (timeLimit.equals(LocalTime.MIDNIGHT)) {
            throw new BadRequestException("Thời gian làm bài phải lớn hơn 0");
        }
    }

    public static Question buildQuestion(QuestionRequest qr) {
        Question question = new Question();
        question.setContent(qr.getQuestionContent().trim());
        question.setSubject(StringUtils.hasText(qr.getSubject()) ? qr.getSubject().trim() : "General");
        for (Choice c : qr.getChoices()) {
            QuestionChoice choice = new QuestionChoice();
            choice.setContent(c.getAnswerText().trim());
            choice.setCorrect(c.isCorrect());
            choice.setQuestion(question);
            question.getQuestionChoices().add(choice);
        }
        return question;
    }

    public static double percentage(double points, int total) {
        return total == 0 ? 0 : Math.round(points * 10_000.0 / total) / 100.0;
    }

    public static Map<Long, Long> toCountMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
    }
}
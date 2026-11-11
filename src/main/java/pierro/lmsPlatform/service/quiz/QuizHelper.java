package pierro.lmsPlatform.service.quiz;
import pierro.lmsPlatform.entity.quiz.Attempt;
import pierro.lmsPlatform.entity.quiz.AttemptStatus;
import pierro.lmsPlatform.entity.quiz.Quiz;
import java.time.Duration;

import org.springframework.util.StringUtils;
import pierro.lmsPlatform.dto.request.Quiz.Choice;
import pierro.lmsPlatform.dto.request.Quiz.QuestionRequest;
import pierro.lmsPlatform.entity.quiz.Question;
import pierro.lmsPlatform.entity.quiz.QuestionChoice;
import pierro.lmsPlatform.security.exception.BadRequestException;

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

    /** Cho phép trễ tối đa từng này giây (độ trễ mạng) sau hạn chót. */
    public static final long GRACE_SECONDS = 15;

    /** Hạn chót nộp bài do server tính: min(startedAt + timeLimit, quiz.endTime). */
    public static LocalDateTime deadlineOf(Attempt attempt, Quiz quiz) {
        Duration limit = Duration.between(LocalTime.MIDNIGHT, quiz.getTimeLimit());
        LocalDateTime byLimit = attempt.getStartedAt().plus(limit);
        return byLimit.isBefore(quiz.getEndTime()) ? byLimit : quiz.getEndTime();
    }

    public static boolean isPastDeadline(Attempt attempt, Quiz quiz, LocalDateTime now) {
        return now.isAfter(deadlineOf(attempt, quiz).plusSeconds(GRACE_SECONDS));
    }

    public static void expire(Attempt attempt, LocalDateTime now) {
        attempt.setStatus(AttemptStatus.EXPIRED);
        attempt.setSubmittedAt(now);
        attempt.setPoints(0.0);
    }
}

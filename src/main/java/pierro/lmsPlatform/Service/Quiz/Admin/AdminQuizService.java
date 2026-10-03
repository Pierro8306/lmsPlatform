package pierro.lmsPlatform.Service.Quiz.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pierro.lmsPlatform.DTO.Request.Quiz.CreateQuizRequest;
import pierro.lmsPlatform.DTO.Request.Quiz.ReplaceQuestionsRequest;
import pierro.lmsPlatform.DTO.Request.Quiz.UpdateQuizRequest;
import pierro.lmsPlatform.DTO.Response.Quiz.*;
import pierro.lmsPlatform.Entity.Auth.UserGroup;
import pierro.lmsPlatform.Entity.Quiz.Attempt;
import pierro.lmsPlatform.Entity.Quiz.Question;
import pierro.lmsPlatform.Entity.Quiz.Quiz;
import pierro.lmsPlatform.Repository.Auth.UserGroupRepository;
import pierro.lmsPlatform.Repository.Quiz.AttemptRepository;
import pierro.lmsPlatform.Repository.Quiz.QuestionRepository;
import pierro.lmsPlatform.Repository.Quiz.QuizRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;
import pierro.lmsPlatform.Security.Exception.ConflictException;
import pierro.lmsPlatform.Security.Exception.ResourceNotFoundException;
import pierro.lmsPlatform.Service.Quiz.QuizHelper;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminQuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;
    private final UserGroupRepository userGroupRepository;
    private final IdObfuscator idObfuscator;

    @Transactional
    public AdminQuizResponse createQuiz(CreateQuizRequest req) {
        QuizHelper.validateSchedule(req.getStartTime(), req.getEndTime(), req.getTimeLimit());

        Quiz quiz = new Quiz();
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setStartTime(req.getStartTime());
        quiz.setEndTime(req.getEndTime());
        quiz.setTimeLimit(req.getTimeLimit());
        quiz.setMaxAttempts(req.getMaxAttempts() == null ? 1 : req.getMaxAttempts());
        quiz.setUserGroup(findGroup(req.getUserGroup()));
        req.getQuestions().forEach(qr -> quiz.getQuestions().add(QuizHelper.buildQuestion(qr)));
        Quiz saved = quizRepository.save(quiz);
        return toResponse(saved, saved.getQuestions().size(), 0, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<AdminQuizResponse> list() {
        List<Quiz> quizzes = quizRepository.findAllByOrderByStartTimeDesc();
        if (quizzes.isEmpty()) return List.of();

        List<Long> ids = quizzes.stream().map(Quiz::getId).toList();
        Map<Long, Long> questionCounts = QuizHelper.toCountMap(quizRepository.countQuestionsByQuizIds(ids));
        Map<Long, Long> attemptCounts = QuizHelper.toCountMap(attemptRepository.countByQuizIds(ids));
        LocalDateTime now = LocalDateTime.now();

        return quizzes.stream().map(q -> toResponse(q,
                questionCounts.getOrDefault(q.getId(), 0L).intValue(),
                attemptCounts.getOrDefault(q.getId(), 0L).intValue(),
                now)).toList();
    }

    @Transactional(readOnly = true)
    public AdminQuizDetailResponse detail(String quizIdH) {
        Quiz quiz = findQuiz(quizIdH);
        List<AdminQuestionResponse> questions = quiz.getQuestions().stream()
                .sorted(Comparator.comparing(Question::getId))
                .map(q -> new AdminQuestionResponse(
                        idObfuscator.encode(q.getId()),
                        q.getContent(),
                        q.getSubject(),
                        q.getQuestionChoices().stream()
                                .map(c -> new AdminChoiceResponse(idObfuscator.encode(c.getId()), c.getContent(), c.isCorrect()))
                                .toList()))
                .toList();
        int attempts = (int) attemptRepository.countByQuiz_Id(quiz.getId());
        return new AdminQuizDetailResponse(toResponse(quiz, questions.size(), attempts, LocalDateTime.now()), questions);
    }

    @Transactional
    public void update(String quizIdH, UpdateQuizRequest req) {
        QuizHelper.validateSchedule(req.getStartTime(), req.getEndTime(), req.getTimeLimit());
        Quiz quiz = findQuiz(quizIdH);
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setStartTime(req.getStartTime());
        quiz.setEndTime(req.getEndTime());
        quiz.setTimeLimit(req.getTimeLimit());
        if (req.getMaxAttempts() != null) quiz.setMaxAttempts(req.getMaxAttempts());
        quiz.setUserGroup(findGroup(req.getUserGroup()));
    }

    @Transactional
    public void replaceQuestions(String quizIdH, ReplaceQuestionsRequest req) {
        Quiz quiz = findQuiz(quizIdH);
        if (attemptRepository.countByQuiz_Id(quiz.getId()) > 0) {
            throw new ConflictException("Bài thi đã có học viên làm bài, không thể thay đổi câu hỏi");
        }
        List<Question> old = new ArrayList<>(quiz.getQuestions());
        quiz.getQuestions().clear();
        req.getQuestions().forEach(qr -> quiz.getQuestions().add(QuizHelper.buildQuestion(qr)));
        quizRepository.flush();
        questionRepository.deleteAll(old);
    }

    @Transactional
    public void delete(String quizIdH, boolean force) {
        Quiz quiz = findQuiz(quizIdH);
        List<Attempt> attempts = attemptRepository.findAllByQuiz_Id(quiz.getId());
        if (!attempts.isEmpty()) {
            if (!force) {
                throw new ConflictException("Bài thi đã có " + attempts.size()
                        + " lượt làm bài. Gọi lại với force=true nếu muốn xoá cả kết quả");
            }
            attemptRepository.deleteAll(attempts);
            attemptRepository.flush();
        }
        List<Question> questions = new ArrayList<>(quiz.getQuestions());
        quizRepository.delete(quiz);
        quizRepository.flush();
        questionRepository.deleteAll(questions);
    }

    @Transactional(readOnly = true)
    public List<AdminAttemptResponse> results(String quizIdH) {
        Quiz quiz = findQuiz(quizIdH);
        int quizTotal = quiz.getQuestions().size();
        return attemptRepository.findByQuiz_IdOrderByStartedAtDesc(quiz.getId()).stream().map(a -> {
            int total = a.getTotalQuestions() == null ? quizTotal : a.getTotalQuestions();
            double points = a.getPoints() == null ? 0 : a.getPoints();
            return new AdminAttemptResponse(
                    idObfuscator.encode(a.getId()),
                    a.getUser().getUsername(),
                    a.getStatus().name(),
                    points,
                    total,
                    QuizHelper.percentage(points, total),
                    a.getStartedAt(),
                    a.getSubmittedAt(),
                    a.getUseTime());
        }).toList();
    }

    private AdminQuizResponse toResponse(Quiz q, int questionCount, int attemptCount, LocalDateTime now) {
        String status = now.isBefore(q.getStartTime()) ? "UPCOMING"
                : now.isBefore(q.getEndTime()) ? "OPEN" : "CLOSED";
        return new AdminQuizResponse(
                idObfuscator.encode(q.getId()),
                q.getTitle(),
                q.getDescription(),
                q.getStartTime(),
                q.getEndTime(),
                q.getTimeLimit(),
                q.getMaxAttempts(),
                q.getUserGroup() == null ? null : q.getUserGroup().getName(),
                status,
                questionCount,
                attemptCount);
    }

    private Quiz findQuiz(String quizIdH) {
        return quizRepository.findById(idObfuscator.decode(quizIdH))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi"));
    }

    private UserGroup findGroup(String name) {
        if (!StringUtils.hasText(name)) return null;
        return userGroupRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm: " + name));
    }
}

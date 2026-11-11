package pierro.lmsPlatform.service.quiz.Student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.Quiz.QuizAnswer;
import pierro.lmsPlatform.dto.response.Quiz.ExamResult;
import pierro.lmsPlatform.dto.response.Quiz.QuestionResponse;
import pierro.lmsPlatform.dto.response.Quiz.QuizSummaryResponse;
import pierro.lmsPlatform.dto.response.Quiz.StartQuizResponse;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.auth.UserGroup;
import pierro.lmsPlatform.entity.quiz.*;
import pierro.lmsPlatform.mapper.ChoiceMapper;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.repository.quiz.AttemptRepository;
import pierro.lmsPlatform.repository.quiz.QuizRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.*;
import pierro.lmsPlatform.service.quiz.QuizHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
        import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {


    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;
    private final ChoiceMapper choiceMapper;
    private final IdObfuscator idObfuscator;

    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> getAvailableQuizzes(Long userId) {
        User user = findUser(userId);
        List<Quiz> quizzes = user.getUserGroup() == null
                ? quizRepository.findByUserGroupIsNullOrderByStartTimeDesc()
                : quizRepository.findAvailableForGroup(user.getUserGroup().getId());
        if (quizzes.isEmpty()) return List.of();

        List<Long> quizIds = quizzes.stream().map(Quiz::getId).toList();
        Map<Long, Long> attemptsUsed = QuizHelper.toCountMap(attemptRepository.countByUserForQuizzes(userId, quizIds));
        Map<Long, Long> questionCounts = QuizHelper.toCountMap(quizRepository.countQuestionsByQuizIds(quizIds));

        return quizzes.stream().map(q -> new QuizSummaryResponse(
                idObfuscator.encode(q.getId()),
                q.getTitle(),
                q.getDescription(),
                q.getStartTime(),
                q.getEndTime(),
                q.getTimeLimit(),
                q.getMaxAttempts(),
                attemptsUsed.getOrDefault(q.getId(), 0L).intValue(),
                questionCounts.getOrDefault(q.getId(), 0L).intValue())).toList();
    }

    @Transactional(noRollbackFor = ApiException.class)
    public StartQuizResponse startQuiz(String quizIdH, Long userId) {
        User user = lockUser(userId);
        Quiz quiz = lockQuizShared(idObfuscator.decode(quizIdH));
        assertCanAccess(quiz, user);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(quiz.getStartTime())) throw new BadRequestException("Bài thi chưa mở");
        if (!now.isBefore(quiz.getEndTime())) throw new BadRequestException("Bài thi đã kết thúc");

        Attempt attempt = findOpenAttempt(quiz.getId(), userId).orElse(null);
        if (attempt != null && isPastDeadline(attempt, quiz, now)) {
            expire(attempt, now);
            attempt = null;
        }

        if (attempt == null) {
            long used = attemptRepository.countByQuiz_IdAndUser_Id(quiz.getId(), userId);
            if (used >= quiz.getMaxAttempts()) {
                throw new ConflictException("Bạn đã dùng hết số lần làm bài (" + quiz.getMaxAttempts() + ")");
            }
            attempt = new Attempt();
            attempt.setUser(user);
            attempt.setQuiz(quiz);
            attempt.setStartedAt(now);
            attempt.setDateTime(now);
            attempt.setStatus(AttemptStatus.IN_PROGRESS);
            attempt.setTotalQuestions(quiz.getQuestions().size());
            attempt = attemptRepository.save(attempt);
        }

        return new StartQuizResponse(
                idObfuscator.encode(attempt.getId()),
                attempt.getStartedAt(),
                deadlineOf(attempt, quiz),
                buildQuestionsForStudent(quiz));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public ExamResult submitQuiz(String quizIdH, List<QuizAnswer> quizAnswers, Long userId) {
        Quiz quiz = findQuiz(idObfuscator.decode(quizIdH));
        lockUser(userId);
        Attempt attempt = findOpenAttempt(quiz.getId(), userId)
                .orElseThrow(() -> new BadRequestException("Bạn chưa bắt đầu bài thi hoặc đã nộp bài rồi"));

        LocalDateTime now = LocalDateTime.now();
        if (isPastDeadline(attempt, quiz, now)) {
            expire(attempt, now);
            throw new BadRequestException("Đã quá thời gian làm bài, bài làm không được ghi nhận");
        }

        Map<Long, Question> questionsById = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        Map<Long, Map<Long, QuestionChoice>> choicesByQuestion = new HashMap<>();
        questionsById.forEach((id, q) -> choicesByQuestion.put(id,
                q.getQuestionChoices().stream().collect(Collectors.toMap(QuestionChoice::getId, c -> c))));

        Map<Long, Set<Long>> selected = new HashMap<>();
        List<Answer> answers = new ArrayList<>();

        for (QuizAnswer qa : quizAnswers == null ? List.<QuizAnswer>of() : quizAnswers) {
            if (qa == null) {
                throw new BadRequestException("Dữ liệu câu trả lời không hợp lệ");
            }
            Long questionId = idObfuscator.decode(qa.getId_question());
            Long choiceId = idObfuscator.decode(qa.getId_answer());

            Question question = questionsById.get(questionId);
            if (question == null) {
                throw new BadRequestException("Có câu hỏi không thuộc bài thi này");
            }
            QuestionChoice choice = choicesByQuestion.get(questionId).get(choiceId);
            if (choice == null) {
                throw new BadRequestException("Có đáp án không thuộc câu hỏi tương ứng");
            }

            if (selected.computeIfAbsent(questionId, k -> new HashSet<>()).add(choiceId)) {
                Answer answer = new Answer();
                answer.setAttempt(attempt);
                answer.setQuestion(question);
                answer.setQuestionChoice(choice);
                answers.add(answer);
            }
        }

        int correctCount = 0;
        for (Question q : questionsById.values()) {
            Set<Long> correctIds = q.getQuestionChoices().stream()
                    .filter(QuestionChoice::isCorrect)
                    .map(QuestionChoice::getId)
                    .collect(Collectors.toSet());
            if (!correctIds.isEmpty() && correctIds.equals(selected.getOrDefault(q.getId(), Set.of()))) {
                correctCount++;
            }
        }

        long usedSeconds = Math.max(0, Duration.between(attempt.getStartedAt(), now).getSeconds());
        attempt.getAnswers().addAll(answers);
        attempt.setPoints((double) correctCount);
        attempt.setTotalQuestions(questionsById.size());
        attempt.setSubmittedAt(now);
        attempt.setUseTime(LocalTime.ofSecondOfDay(Math.min(usedSeconds, 86_399)));
        attempt.setStatus(AttemptStatus.SUBMITTED);

        return toResult(attempt, quiz, revealedCorrectAnswers(quiz, now));
    }

    @Transactional(readOnly = true)
    public List<ExamResult> getExamResult(String quizIdH, Long userId) {
        Quiz quiz = findQuiz(idObfuscator.decode(quizIdH));
        List<QuizAnswer> correct = revealedCorrectAnswers(quiz, LocalDateTime.now());
        return attemptRepository.findAllByQuiz_IdAndUser_IdOrderByStartedAtAsc(quiz.getId(), userId).stream()
                .filter(a -> a.getStatus() == AttemptStatus.SUBMITTED)
                .map(a -> toResult(a, quiz, correct))
                .toList();
    }

    private ExamResult toResult(Attempt attempt, Quiz quiz, List<QuizAnswer> correct) {
        int total = attempt.getTotalQuestions() == null ? quiz.getQuestions().size() : attempt.getTotalQuestions();
        double points = attempt.getPoints() == null ? 0 : attempt.getPoints();
        double percentage = QuizHelper.percentage(points, total);

        return ExamResult.builder()
                .attemptId(idObfuscator.encode(attempt.getId()))
                .status(attempt.getStatus().name())
                .points(points)
                .totalQuestions(total)
                .percentage(percentage)
                .submittedAt(attempt.getSubmittedAt())
                .correct(correct)
                .build();
    }

    private List<QuizAnswer> revealedCorrectAnswers(Quiz quiz, LocalDateTime now) {
        if (now.isBefore(quiz.getEndTime())) return List.of();
        return quiz.getQuestions().stream()
                .flatMap(q -> q.getQuestionChoices().stream()
                        .filter(QuestionChoice::isCorrect)
                        .map(c -> new QuizAnswer(idObfuscator.encode(q.getId()), idObfuscator.encode(c.getId()))))
                .toList();
    }

    private List<QuestionResponse> buildQuestionsForStudent(Quiz quiz) {
        List<Question> questions = new ArrayList<>(quiz.getQuestions());
        Collections.shuffle(questions);
        return questions.stream().map(q -> {
            List<QuestionChoice> choices = new ArrayList<>(q.getQuestionChoices());
            Collections.shuffle(choices);
            return new QuestionResponse(
                    idObfuscator.encode(q.getId()),
                    q.getContent(),
                    q.getSubject(),
                    choiceMapper.toStudentDtoList(choices));
        }).toList();
    }

    private Optional<Attempt> findOpenAttempt(Long quizId, Long userId) {
        return attemptRepository.findFirstByQuiz_IdAndUser_IdAndStatusOrderByStartedAtDesc(
                quizId, userId, AttemptStatus.IN_PROGRESS);
    }

    private LocalDateTime deadlineOf(Attempt attempt, Quiz quiz) {
        return QuizHelper.deadlineOf(attempt, quiz);
    }

    private boolean isPastDeadline(Attempt attempt, Quiz quiz, LocalDateTime now) {
        return QuizHelper.isPastDeadline(attempt, quiz, now);
    }

    private void expire(Attempt attempt, LocalDateTime now) {
        QuizHelper.expire(attempt, now);
    }

    private void assertCanAccess(Quiz quiz, User user) {
        UserGroup required = quiz.getUserGroup();
        if (required == null) return;
        if (user.getUserGroup() == null || !required.getId().equals(user.getUserGroup().getId())) {
            throw new ForbiddenException("Bài thi này không dành cho nhóm của bạn");
        }
    }

    /** Khoá chia sẻ dòng quiz: admin (FOR UPDATE) không thể thay câu hỏi / xoá quiz giữa chừng lúc tạo attempt. */
    private Quiz lockQuizShared(Long id) {
        return quizRepository.lockSharedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi"));
    }

    private Quiz findQuiz(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi"));
    }

    private User findUser(Long id) {
        return userRepository.findWithRoleAndGroupById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private User lockUser(Long id) {
        return userRepository.lockById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }
}


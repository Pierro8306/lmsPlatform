package pierro.lmsPlatform.service.quiz.Admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pierro.lmsPlatform.dto.request.Quiz.Choice;
import pierro.lmsPlatform.dto.request.Quiz.QuestionRequest;
import pierro.lmsPlatform.dto.request.Quiz.QuizAnswer;
import pierro.lmsPlatform.dto.request.Quiz.ReplaceQuestionsRequest;
import pierro.lmsPlatform.dto.response.Quiz.ExamResult;
import pierro.lmsPlatform.entity.auth.Role;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.quiz.*;
import pierro.lmsPlatform.mapper.ChoiceMapper;
import pierro.lmsPlatform.repository.auth.PasswordResetTokenRepository;
import pierro.lmsPlatform.repository.auth.RoleRepository;
import pierro.lmsPlatform.repository.auth.UserGroupRepository;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.repository.quiz.AttemptRepository;
import pierro.lmsPlatform.repository.quiz.QuestionRepository;
import pierro.lmsPlatform.repository.quiz.QuizRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.service.quiz.Admin.AdminQuizService;
import pierro.lmsPlatform.service.quiz.Student.QuizService;
import pierro.lmsPlatform.service.user.Admin.UserManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizAndAdminBusinessTest {

    @Mock private QuizRepository quizRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private AttemptRepository attemptRepository;
    @Mock private UserGroupRepository userGroupRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ChoiceMapper choiceMapper;

    private IdObfuscator idObfuscator;
    private AdminQuizService adminQuizService;
    private QuizService quizService;
    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        idObfuscator = new IdObfuscator("test-salt-business");
        adminQuizService = new AdminQuizService(quizRepository, questionRepository, attemptRepository, userGroupRepository, idObfuscator);
        quizService = new QuizService(userRepository, quizRepository, attemptRepository, choiceMapper, idObfuscator);
        userManagementService = new UserManagementService(userRepository, userGroupRepository, passwordEncoder, roleRepository, passwordResetTokenRepository, idObfuscator);
    }

    @Test
    @DisplayName("AdminQuizService: cannot replace questions when quiz already has attempts")
    void testReplaceQuestionsBlockedWhenAttemptsExist() {
        Quiz quiz = new Quiz();
        quiz.setId(1L);
        String quizHash = idObfuscator.encode(1L);

        when(quizRepository.lockForUpdateById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.countByQuiz_Id(1L)).thenReturn(1L);

        Choice c1 = new Choice(); c1.setAnswerText("A"); c1.setCorrect(true);
        Choice c2 = new Choice(); c2.setAnswerText("B"); c2.setCorrect(false);
        QuestionRequest qr = new QuestionRequest();
        qr.setQuestionContent("New Q?");
        qr.setChoices(List.of(c1, c2));

        ReplaceQuestionsRequest req = new ReplaceQuestionsRequest();
        req.setQuestions(List.of(qr));

        assertThrows(ConflictException.class, () -> adminQuizService.replaceQuestions(quizHash, req));
    }

    @Test
    @DisplayName("QuizService: hides correct answers before quiz endTime and reveals after endTime")
    void testCorrectAnswersHiddenBeforeEndTime() {
        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setStartTime(LocalDateTime.now().minusHours(1));
        quiz.setEndTime(LocalDateTime.now().plusHours(1)); // Chưa kết thúc
        quiz.setTimeLimit(LocalTime.of(0, 30));

        Question q = new Question();
        q.setId(10L);
        QuestionChoice c = new QuestionChoice();
        c.setId(100L);
        c.setCorrect(true);
        c.setQuestion(q);
        q.getQuestionChoices().add(c);
        quiz.getQuestions().add(q);

        Attempt attempt = new Attempt();
        attempt.setId(50L);
        attempt.setQuiz(quiz);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(5));
        attempt.setStatus(AttemptStatus.IN_PROGRESS);

        User user = new User();
        user.setId(5L);

        String quizHash = idObfuscator.encode(1L);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(userRepository.lockById(5L)).thenReturn(Optional.of(user));
        when(attemptRepository.findFirstByQuiz_IdAndUser_IdAndStatusOrderByStartedAtDesc(1L, 5L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(attempt));

        List<QuizAnswer> answers = List.of(new QuizAnswer(idObfuscator.encode(10L), idObfuscator.encode(100L)));
        ExamResult resultBeforeEnd = quizService.submitQuiz(quizHash, answers, 5L);

        assertEquals(1.0, resultBeforeEnd.getPoints());
        assertTrue(resultBeforeEnd.getCorrect().isEmpty(), "Correct answers must be hidden before endTime");

        // Khi quiz đã qua endTime -> xem kết quả phải hiện đáp án đúng
        quiz.setEndTime(LocalDateTime.now().minusMinutes(1));
        when(attemptRepository.findAllByQuiz_IdAndUser_IdOrderByStartedAtAsc(1L, 5L))
                .thenReturn(List.of(attempt));

        List<ExamResult> resultsAfterEnd = quizService.getExamResult(quizHash, 5L);
        assertEquals(1, resultsAfterEnd.get(0).getCorrect().size());
    }

    @Test
    @DisplayName("UserManagementService: prevents deleting the last ADMIN account")
    void testCannotDeleteLastAdmin() {
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        User lastAdmin = new User();
        lastAdmin.setId(2L);
        lastAdmin.setRole(adminRole);

        String targetHash = idObfuscator.encode(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(lastAdmin));
        when(userRepository.lockAllByRoleName("ADMIN")).thenReturn(List.of(lastAdmin));

        // Admin id=1 cố xóa admin cuối cùng id=2
        assertThrows(ConflictException.class, () -> userManagementService.deleteUser(targetHash, 1L));
        verify(userRepository, never()).delete(any());
    }
}
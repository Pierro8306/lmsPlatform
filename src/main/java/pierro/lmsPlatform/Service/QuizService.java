package pierro.lmsPlatform.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Quiz.Choice;
import pierro.lmsPlatform.DTO.Request.Quiz.QuestionRequest;
import pierro.lmsPlatform.DTO.Request.Quiz.QuizAnswer;
import pierro.lmsPlatform.DTO.Response.Quiz.ExamResult;
import pierro.lmsPlatform.DTO.Response.Quiz.QuestionResponse;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Quiz.*;
import pierro.lmsPlatform.Mapper.ChoiceMapper;
import pierro.lmsPlatform.Repository.*;
import pierro.lmsPlatform.Repository.Quiz.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class QuizService {
    QuestionRepository questionRepository;
    QuestionChoiceRepository questionChoiceRepository;
    UserRepository userRepository;
    QuizRepository quizRepository;
    AnswerRepository answerRepository;
    AttemptRepository attemptRepository;
    ChoiceMapper choiceMapper;

    @Transactional
    private Question createQuestion(QuestionRequest questionRequest) {
        Question question = new Question();
        question.setContent(questionRequest.getQuestionContent());
        questionRepository.save(question);

        List<QuestionChoice> questionChoices = new ArrayList<>();
        for (Choice choiceDto : questionRequest.getChoices()) {
            QuestionChoice choice = new QuestionChoice();

            choice.setContent(choiceDto.getAnswerText());
            choice.setCorrect(choiceDto.isCorrect());

            choice.setQuestion(question);
            questionChoices.add(choice);
        }
        questionChoiceRepository.saveAll(questionChoices);
        return question;
    }
    @Transactional
    public void createQuiz(List<QuestionRequest> questionRequests, String title, String description, LocalDateTime startDate, LocalDateTime endDate, LocalTime timeLimit) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setStartTime(startDate);
        quiz.setEndTime(endDate);
        quiz.setTimeLimit(timeLimit);
        for (int i = 0; i < questionRequests.size(); i++) {
            Question question = createQuestion(questionRequests.get(i));
            quiz.getQuestions().add(question);
        }
        quizRepository.save(quiz);
    }

    public QuestionResponse getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        List<QuestionChoice> choices = questionChoiceRepository.findByQuestionId(question.getId());

        Collections.shuffle(choices);
        return new QuestionResponse(question.getContent(),question.getSubject(), choiceMapper.entityToDTO(choices));
    }

    public List<QuestionChoice> getCorrectChoices(List<Answer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyList();
        }
        List<QuestionChoice> correctChoices = new ArrayList<>();
        for (Answer answer : answers) {
            if (answer.getQuestionChoice() != null && answer.getQuestionChoice().isCorrect()) {
                correctChoices.add(answer.getQuestionChoice());
            }
        }
        return correctChoices;
    }

    public int countCorrectAnswer(List<QuizAnswer> quizAnswers){
        if (quizAnswers == null || quizAnswers.isEmpty()) {
            return 0;
        }
        return quizAnswers.size();
    }
    @Transactional
    public void handleAnswer(List<QuizAnswer> quizAnswers, LocalTime localTime, Long quizId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Quiz"));

        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setDateTime(LocalDateTime.now());
        attempt.setUseTime(localTime);

        attempt = attemptRepository.save(attempt);

        List<Answer> answers = new ArrayList<>();
        int correctCount = 0;

        if (quizAnswers != null) {
            for (QuizAnswer qa : quizAnswers) {
                Answer answer = new Answer();
                answer.setAttempt(attempt);

                Question question = questionRepository.findById(qa.getId_question())
                        .orElse(null);
                QuestionChoice choice = questionChoiceRepository.findById(qa.getId_answer())
                        .orElse(null);

                answer.setQuestion(question);
                answer.setQuestionChoice(choice);

                if (choice != null && choice.isCorrect()) {
                    correctCount++;
                }

                answers.add(answer);
            }
            answerRepository.saveAll(answers);
        }

        attempt.setPoints((double) correctCount);
        attempt.setAnswers((Set<Answer>) answers);
        attemptRepository.save(attempt);
    }
    public List<ExamResult> getExamResult(Long quizId, Long userId) {
        List<ExamResult> examResults = new ArrayList<>();
        List<Attempt> attempts = attemptRepository.findAllByQuiz_IdAndUser_Id(quizId, userId);

        for (Attempt attempt : attempts) {
            List<Answer> answerList = new ArrayList<>(attempt.getAnswers());

            List<QuestionChoice> correctChoices = getCorrectChoices(answerList);

            List<QuizAnswer> quizAnswers = new ArrayList<>();
            for (QuestionChoice choice : correctChoices) {
                quizAnswers.add(new QuizAnswer(choice.getQuestion().getId(), choice.getId()));
            }

            examResults.add(new ExamResult(attempt.getPoints(), quizAnswers));
        }

        return examResults;
    }
}

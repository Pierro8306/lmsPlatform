package pierro.lmsPlatform.Service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
@RequiredArgsConstructor
public class QuizService {
    private final QuestionRepository questionRepository;
    private final QuestionChoiceRepository questionChoiceRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final AnswerRepository answerRepository;
    private final AttemptRepository attemptRepository;
    private final ChoiceMapper choiceMapper;
    private final ChatClient chatClient;

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
        for (QuestionRequest questionRequest : questionRequests) {
            Question question = createQuestion(questionRequest);
            quiz.getQuestions().add(question);
        }
        quizRepository.save(quiz);
    }

    public QuestionResponse getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

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
                .orElseThrow(() -> new RuntimeException("User not found"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

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
        attempt.setAnswers(new HashSet<>(answers));
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
    public List<QuestionRequest> extractQuestions(MultipartFile file) throws Exception {

        String pdfText = "";
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfText = stripper.getText(document);
        }

        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<QuestionRequest>>() {
        });

        String prompt = """
                Bạn là hệ thống trích xuất dữ liệu đề thi tự động. Hãy đọc văn bản sau và bóc tách thành danh sách câu hỏi trắc nghiệm.
                
                QUY TẮC BÓC TÁCH NGHIÊM NGẶT:
                1. 'questionContent': Chỉ lấy nội dung câu hỏi, KHÔNG kèm số thứ tự (Ví dụ: "Câu 1: " -> loại bỏ "Câu 1: ").
                2. 'answerText': Chỉ lấy nội dung của đáp án, TUYỆT ĐỐI KHÔNG kèm các ký tự A, B, C, D ở đầu. (Ví dụ: "A. Hà Nội" -> chỉ lấy "Hà Nội").
                3. 'isCorrect': Dựa vào ký hiệu trong đề (bôi đậm, dấu sao) HOẶC đối chiếu với bảng đáp án ở cuối đề thi. Tự động xác định đáp án đúng và gán 'isCorrect' = true, các đáp án sai gán 'isCorrect' = false.
                
                TRẢ VỀ ĐÚNG ĐỊNH DẠNG JSON SAU, không giải thích gì thêm:
                {format}
                
                VĂN BẢN ĐỀ THI:
                {text}
                """;

        String finalPdfText = pdfText;
        String response = chatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("format", converter.getFormat())
                        .param("text", finalPdfText))
                .call()
                .content();

        assert response != null;
        String cleanJson = response.replace("```json", "").replace("```", "").trim();
        return converter.convert(cleanJson);
    }
}

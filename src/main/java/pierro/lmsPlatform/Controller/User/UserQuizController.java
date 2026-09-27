package pierro.lmsPlatform.Controller.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Quiz.QuizAnswer;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.QuizService;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/user/quizzes")
@RequiredArgsConstructor
public class UserQuizController {
    private final QuizService quizService;

    @GetMapping("/questions/{questionId}")
    public ResponseEntity<ResponseObject> getQuestionById(@PathVariable String questionId) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy chi tiết câu hỏi thành công", quizService.getQuestionById(questionId)));
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<ResponseObject> submitQuiz(
            @PathVariable String quizId,
            @RequestParam LocalTime timeUsed,
            @RequestBody List<QuizAnswer> answers) {

        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        quizService.handleAnswer(answers, timeUsed, quizId, currentUserId);

        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Nộp bài kiểm tra thành công", null));
    }

    @GetMapping("/{quizId}/results")
    public ResponseEntity<ResponseObject> getExamResult(@PathVariable String quizId) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy kết quả kiểm tra thành công", quizService.getExamResult(quizId, currentUserId)));
    }
}
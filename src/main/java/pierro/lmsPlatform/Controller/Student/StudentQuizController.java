package pierro.lmsPlatform.Controller.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Quiz.QuizAnswer;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.Quiz.Student.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/student/quizzes")
@RequiredArgsConstructor
public class StudentQuizController {
    private final QuizService quizService;

    /** Danh sách bài thi dành cho user (theo nhóm). */
    @GetMapping
    public ResponseEntity<ResponseObject> getAvailableQuizzes(@AuthenticationPrincipal Long userId) {
        return ResponseObject.ok("Lấy danh sách bài thi thành công",
                quizService.getAvailableQuizzes(userId));
    }

    /** Bắt đầu / tiếp tục làm bài: server bắt đầu tính giờ và trả về câu hỏi (không có đáp án đúng). */
    @PostMapping("/{quizId}/start")
    public ResponseEntity<ResponseObject> startQuiz(@PathVariable String quizId,
                                                    @AuthenticationPrincipal Long userId) {
        return ResponseObject.ok("Bắt đầu làm bài",
                quizService.startQuiz(quizId, userId));
    }

    /** Nộp bài: không còn nhận timeUsed từ client — thời gian do server tính. */
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<ResponseObject> submitQuiz(@PathVariable String quizId,
                                                     @AuthenticationPrincipal Long userId,
                                                     @RequestBody List<QuizAnswer> answers) {
        return ResponseObject.ok("Nộp bài kiểm tra thành công",
                quizService.submitQuiz(quizId, answers, userId));
    }

    @GetMapping("/{quizId}/results")
    public ResponseEntity<ResponseObject> getExamResult(@PathVariable String quizId,
                                                      @AuthenticationPrincipal Long userId) {
        return ResponseObject.ok("Lấy kết quả kiểm tra thành công",
                quizService.getExamResult(quizId, userId));
    }
}

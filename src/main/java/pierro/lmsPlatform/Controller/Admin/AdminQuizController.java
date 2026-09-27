package pierro.lmsPlatform.Controller.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pierro.lmsPlatform.DTO.Request.Quiz.QuestionRequest;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.QuizService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {
    private final QuizService quizService;

    @PostMapping("/extract-questions")
    public ResponseEntity<ResponseObject> extractQuestions(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Trích xuất câu hỏi từ file PDF thành công", quizService.extractQuestions(file)));
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createQuiz(
            @RequestBody List<QuestionRequest> questionRequests,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam LocalTime timeLimit) {
        quizService.createQuiz(questionRequests, title, description, startDate, endDate, timeLimit);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tạo bài kiểm tra thành công", null));
    }
}
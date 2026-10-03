package pierro.lmsPlatform.Controller.Admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pierro.lmsPlatform.DTO.Request.Quiz.CreateQuizRequest;
import pierro.lmsPlatform.DTO.Request.Quiz.ReplaceQuestionsRequest;
import pierro.lmsPlatform.DTO.Request.Quiz.UpdateQuizRequest;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.Quiz.Admin.AdminQuizService;
import pierro.lmsPlatform.Service.Quiz.Admin.QuizAiExtractorService;

@RestController
@RequestMapping("/api/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {
    private final AdminQuizService adminQuizService;
    private  final QuizAiExtractorService quizAiExtractorService;

    @PostMapping("/extract-questions")
    public ResponseEntity<ResponseObject> extractQuestions(@RequestParam("file") MultipartFile file) {
        return ResponseObject.ok("Trích xuất câu hỏi từ file PDF thành công", quizAiExtractorService.extractQuestions(file));
    }

    /** Body JSON: title, description, startTime, endTime, timeLimit("HH:mm:ss"), maxAttempts, userGroup, questions[]. Trả về bài kiểm tra vừa tạo (có id). */
    @PostMapping
    public ResponseEntity<ResponseObject> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        return ResponseObject.ok("Tạo bài kiểm tra thành công", adminQuizService.createQuiz(request));
    }

    @GetMapping
    public ResponseEntity<ResponseObject> listQuizzes() {
        return ResponseObject.ok("Lấy danh sách bài kiểm tra thành công", adminQuizService.list());
    }

    /** Chi tiết kèm câu hỏi và đáp án đúng (để admin xem / sửa). */
    @GetMapping("/{quizId}")
    public ResponseEntity<ResponseObject> getQuiz(@PathVariable String quizId) {
        return ResponseObject.ok("Lấy chi tiết bài kiểm tra thành công", adminQuizService.detail(quizId));
    }

    /** Sửa thông tin chung (không gồm câu hỏi). */
    @PutMapping("/{quizId}")
    public ResponseEntity<ResponseObject> updateQuiz(@PathVariable String quizId,
                                                     @Valid @RequestBody UpdateQuizRequest request) {
        adminQuizService.update(quizId, request);
        return ResponseObject.ok("Cập nhật bài kiểm tra thành công");
    }

    /** Thay toàn bộ câu hỏi — chỉ khi chưa có ai làm bài (409 nếu đã có). */
    @PutMapping("/{quizId}/questions")
    public ResponseEntity<ResponseObject> replaceQuestions(@PathVariable String quizId,
                                                           @Valid @RequestBody ReplaceQuestionsRequest request) {
        adminQuizService.replaceQuestions(quizId, request);
        return ResponseObject.ok("Cập nhật câu hỏi thành công");
    }

    /** Đã có lượt làm bài thì cần ?force=true (xoá cả kết quả). */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ResponseObject> deleteQuiz(@PathVariable String quizId,
                                                     @RequestParam(defaultValue = "false") boolean force) {
        adminQuizService.delete(quizId, force);
        return ResponseObject.ok("Xoá bài kiểm tra thành công");
    }

    @GetMapping("/{quizId}/results")
    public ResponseEntity<ResponseObject> getResults(@PathVariable String quizId) {
        return ResponseObject.ok("Lấy bảng điểm thành công", adminQuizService.results(quizId));
    }
}

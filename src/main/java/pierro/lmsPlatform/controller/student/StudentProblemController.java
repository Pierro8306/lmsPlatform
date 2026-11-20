package pierro.lmsPlatform.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.request.Judge.JudgeRequest;
import pierro.lmsPlatform.dto.request.Judge.SubmitDto;
import pierro.lmsPlatform.dto.response.Judge.JudgeReport;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.entity.problem.Problem;
import pierro.lmsPlatform.service.judge.JudgeQueue;
import pierro.lmsPlatform.service.problem.StudentProblemService;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/student/problems")
@RequiredArgsConstructor
public class StudentProblemController {
    private final StudentProblemService studentProblemService;
    private final JudgeQueue queue;

    // Lấy danh sách bài tập được giao
    @GetMapping
    public ResponseEntity<ResponseObject> list(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseObject.ok(
                "Lấy danh sách bài tập thành công",
                studentProblemService.list(userId)
        );
    }

    // Xem chi tiết bài tập
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> detail(
            @AuthenticationPrincipal Long userId,
            @PathVariable String id
    ) {
        return ResponseObject.ok(
                "Lấy chi tiết bài tập thành công",
                studentProblemService.detail(userId, id)
        );
    }

    // Nộp bài và chấm code
    @PostMapping("/submit")
    public CompletableFuture<JudgeReport> submitCode(
            @AuthenticationPrincipal Long userId,
            @RequestBody SubmitDto submitDto
    ) {
        Problem problem = studentProblemService.validateCanSubmit(userId, submitDto.idProblem());
        JudgeRequest judgeRequest = studentProblemService.createJudgeRequest(problem, submitDto);
        return queue.submit(judgeRequest).thenApply(report -> {
            studentProblemService.saveSubmission(report, submitDto, userId);
            return report;
        });
    }

    // Lấy danh sách lịch sử nộp bài của sinh viên cho bài tập
    @GetMapping("/{id}/submissions")
    public ResponseEntity<ResponseObject> listSubmissions(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") String id
    ) {
        return ResponseObject.ok(
                "Lấy danh sách nộp bài thành công",
                studentProblemService.listSubmissions(userId, id)
        );
    }

    // Xem chi tiết một bài nộp của sinh viên
    @GetMapping("/{id}/submissions/{submissionId}")
    public ResponseEntity<ResponseObject> getSubmissionDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("id") String id,
            @PathVariable("submissionId") String submissionId
    ) {
        return ResponseObject.ok(
                "Lấy chi tiết nộp bài thành công",
                studentProblemService.getSubmissionDetail(userId, id, submissionId)
        );
    }
}

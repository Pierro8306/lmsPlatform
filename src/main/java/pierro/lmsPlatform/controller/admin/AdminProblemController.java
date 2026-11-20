package pierro.lmsPlatform.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.request.problem.ProblemRequest;
import pierro.lmsPlatform.dto.request.problem.TestCaseItemRequest;
import pierro.lmsPlatform.dto.request.problem.TestCaseRequest;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.problem.AdminProblemService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/problems")
@RequiredArgsConstructor
public class AdminProblemController {
    private final AdminProblemService service;

    // Lấy danh sách bài tập
    @GetMapping
    public ResponseEntity<ResponseObject> list() {
        return ResponseObject.ok(
                "Lấy danh sách bài tập thành công",
                service.list()
        );
    }

    // Lấy chi tiết một bài tập
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> get(
            @PathVariable String id
    ) {
        return ResponseObject.ok(
                "Lấy chi tiết bài tập thành công",
                service.get(id)
        );
    }

    // Tạo bài tập mới
    @PostMapping
    public ResponseEntity<ResponseObject> create(
            @Valid @RequestBody ProblemRequest request
    ) {
        return ResponseObject.ok(
                "Tạo bài tập thành công",
                service.createProblem(request)
        );
    }

    // Cập nhật bài tập
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> update(
            @PathVariable String id,
            @Valid @RequestBody ProblemRequest request
    ) {
        return ResponseObject.ok(
                "Cập nhật bài tập thành công",
                service.update(id, request)
        );
    }

    // Xóa bài tập
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> delete(
            @PathVariable String id
    ) {
        service.delete(id);

        return ResponseObject.ok("Xoá bài tập thành công");
    }

    // Lấy danh sách test case của bài tập
    @GetMapping("/{id}/tests")
    public ResponseEntity<ResponseObject> tests(
            @PathVariable String id
    ) {
        return ResponseObject.ok(
                "Lấy test case thành công",
                service.listTests(id)
        );
    }

    // Thay thế/đồng bộ toàn bộ test case (xóa test case thừa mồ côi)
    @PutMapping("/{id}/tests")
    public ResponseEntity<ResponseObject> replaceTests(
            @PathVariable String id,
            @Valid @RequestBody List<TestCaseItemRequest> requests
    ) {
        return ResponseObject.ok(
                "Đồng bộ test case thành công",
                service.replaceTests(id, requests)
        );
    }

    // Thêm test case
    @PostMapping("/{id}/tests")
    public ResponseEntity<ResponseObject> addTest(
            @PathVariable String id,
            @Valid @RequestBody TestCaseRequest request
    ) {
        return ResponseObject.ok(
                "Thêm test case thành công",
                service.addTest(id, request)
        );
    }

    // Cập nhật test case
    @PutMapping("/{id}/tests/{tcId}")
    public ResponseEntity<ResponseObject> updateTest(
            @PathVariable String id,
            @PathVariable String tcId,
            @Valid @RequestBody TestCaseRequest request
    ) {
        return ResponseObject.ok(
                "Cập nhật test case thành công",
                service.updateTest(id, tcId, request)
        );
    }

    // Xóa test case
    @DeleteMapping("/{id}/tests/{tcId}")
    public ResponseEntity<ResponseObject> deleteTest(
            @PathVariable String id,
            @PathVariable String tcId
    ) {
        service.deleteTest(id, tcId);

        return ResponseObject.ok("Xoá test case thành công");
    }

    // Lấy danh sách lịch sử nộp bài của sinh viên cho bài tập (ADMIN)
    @GetMapping("/{id}/submissions")
    public ResponseEntity<ResponseObject> listSubmissions(
            @PathVariable String id
    ) {
        return ResponseObject.ok(
                "Lấy danh sách bài nộp thành công",
                service.listSubmissions(id)
        );
    }

    // Lấy chi tiết một bài nộp (ADMIN)
    @GetMapping("/{id}/submissions/{submissionId}")
    public ResponseEntity<ResponseObject> getSubmissionDetail(
            @PathVariable String id,
            @PathVariable String submissionId
    ) {
        return ResponseObject.ok(
                "Lấy chi tiết bài nộp thành công",
                service.getSubmissionDetail(id, submissionId)
        );
    }
}

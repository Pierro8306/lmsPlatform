package pierro.lmsPlatform.controller.admin.problem;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.problem.AdminProblemService;
import pierro.lmsPlatform.dto.request.problem.ProblemRequest;
import pierro.lmsPlatform.dto.request.problem.TestCaseRequest;

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

}

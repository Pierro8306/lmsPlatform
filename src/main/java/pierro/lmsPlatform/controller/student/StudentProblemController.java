package pierro.lmsPlatform.controller.student;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.problem.StudentProblemService;

@RestController
@RequestMapping("/api/student/problems")
@RequiredArgsConstructor
public class StudentProblemController {
    private final StudentProblemService service;

    // Lấy danh sách bài tập được giao
    @GetMapping
    public ResponseEntity<ResponseObject> list(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseObject.ok(
                "Lấy danh sách bài tập thành công",
                service.list(userId)
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
                service.detail(userId, id)
        );
    }
}

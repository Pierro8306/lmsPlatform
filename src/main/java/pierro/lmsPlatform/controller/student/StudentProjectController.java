package pierro.lmsPlatform.controller.student;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.request.Project.ProjectSubmitRequest;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.project.Student.ProjectSubmissionService;

@RestController
@RequestMapping("/api/student/projects")
@RequiredArgsConstructor
public class StudentProjectController {
    private final ProjectSubmissionService projectService;

    @GetMapping
    public ResponseEntity<ResponseObject> listProjects() {
        return ResponseObject.ok("Lấy danh sách bài tập dự án thành công", projectService.listProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ResponseObject> getProject(@PathVariable String projectId) {
        return ResponseObject.ok("Lấy chi tiết bài tập dự án thành công", projectService.getProject(projectId));
    }

    @PostMapping("/submit")
    public ResponseEntity<ResponseObject> submitProject(@AuthenticationPrincipal Long userId,
                                                     @Valid @RequestBody ProjectSubmitRequest request) {
        projectService.submit(userId, request);
        return ResponseObject.ok("Nộp bài thành công");
    }

    @GetMapping("/{projectId}/submissions/me")
    public ResponseEntity<ResponseObject> getMySubmission(@PathVariable String projectId,
                                                       @AuthenticationPrincipal Long userId) {
        return ResponseObject.ok("Lấy bài nộp của tôi thành công",
                projectService.getMySubmission(projectId, userId));
    }
}

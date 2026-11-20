package pierro.lmsPlatform.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.request.Project.GradeSubmissionRequest;
import pierro.lmsPlatform.dto.request.Project.ProjectDto;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.project.Admin.AdminProjectService;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {
    private final AdminProjectService projectService;

    @GetMapping
    public ResponseEntity<ResponseObject> listProjects() {
        return ResponseObject.ok("Lấy danh sách bài tập dự án thành công", projectService.listProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ResponseObject> getProject(@PathVariable String projectId) {
        return ResponseObject.ok("Lấy chi tiết bài tập dự án thành công", projectService.getProject(projectId));
    }

    /** Trả về bài tập vừa tạo (có id mã hoá để student nộp bài, admin chấm bài). */
    @PostMapping
    public ResponseEntity<ResponseObject> createProject(@Valid @RequestBody ProjectDto projectDto) {
        return ResponseObject.ok("Tạo bài tập dự án thành công", projectService.createProject(projectDto));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ResponseObject> updateProject(@PathVariable String projectId,
                                                        @Valid @RequestBody ProjectDto projectDto) {
        return ResponseObject.ok("Cập nhật bài tập dự án thành công", projectService.updateProject(projectId, projectDto));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ResponseObject> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseObject.ok("Xoá bài tập dự án thành công");
    }

    @GetMapping("/{projectId}/submissions")
    public ResponseEntity<ResponseObject> getAllSubmissions(@PathVariable String projectId) {
        return ResponseObject.ok("Lấy danh sách bài nộp thành công",
                projectService.getAllSubmissionsOfProject(projectId));
    }

    /** Body JSON: { "score": 8.5, "feedback": "..." } (trước đây là query param). */
    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<ResponseObject> gradeSubmission(@PathVariable String submissionId,
                                                          @Valid @RequestBody GradeSubmissionRequest request) {
        projectService.gradeSubmission(submissionId, request);
        return ResponseObject.ok("Chấm điểm thành công");
    }
}

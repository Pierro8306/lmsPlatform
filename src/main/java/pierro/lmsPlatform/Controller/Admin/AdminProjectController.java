package pierro.lmsPlatform.Controller.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Project.ProjectDto;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.ProjectService;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ResponseObject> createProject(@RequestBody ProjectDto projectDto) {
        projectService.createProject(projectDto);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tạo bài tập dự án thành công", null));
    }

    @GetMapping("/{projectId}/submissions")
    public ResponseEntity<ResponseObject> getAllSubmissions(@PathVariable String projectId) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy danh sách bài nộp thành công", projectService.getAllSubmissionsOfProject(projectId)));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<ResponseObject> gradeSubmission(
            @PathVariable String submissionId,
            @RequestParam String score,
            @RequestParam String feedback) {
        projectService.gradeSubmission(submissionId, score, feedback);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Chấm điểm thành công", null));
    }
}
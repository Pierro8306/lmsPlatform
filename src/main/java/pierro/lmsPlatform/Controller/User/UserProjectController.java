package pierro.lmsPlatform.Controller.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Project.ProjectSubmitRequest;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.ProjectService;

import java.util.Objects;

@RestController
@RequestMapping("/api/user/projects")
@RequiredArgsConstructor
public class UserProjectController {

    private final ProjectService projectService;

    @PostMapping("/submit")
    public ResponseEntity<ResponseObject> submitProject(@Valid @RequestBody ProjectSubmitRequest request) {
        Long currentUserId = (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        projectService.submit(currentUserId, request);

        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Nộp bài thành công", null));
    }

    @GetMapping("/{projectId}/submissions/me")
    public ResponseEntity<ResponseObject> getMySubmission(@PathVariable String projectId) {
        Long currentUserId = (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Object submissionInfo = projectService.getMySubmission(projectId, currentUserId);

        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy bài nộp của tôi thành công", submissionInfo));
    }
}
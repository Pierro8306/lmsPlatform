package pierro.lmsPlatform.Service.Project.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Project.GradeSubmissionRequest;
import pierro.lmsPlatform.DTO.Request.Project.ProjectDto;
import pierro.lmsPlatform.DTO.Response.Project.ProjectResponse;
import pierro.lmsPlatform.DTO.Response.Project.SubmissionDto;
import pierro.lmsPlatform.Entity.Project.Project;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;
import pierro.lmsPlatform.Entity.Project.SubmissionStatus;
import pierro.lmsPlatform.Mapper.ProjectMapper;
import pierro.lmsPlatform.Mapper.SubmissionMapper;
import pierro.lmsPlatform.Repository.Project.ProjectRepository;
import pierro.lmsPlatform.Repository.Project.ProjectSubmissionRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;
import pierro.lmsPlatform.Security.Exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectSubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;
    private final ProjectMapper projectMapper;
    private final IdObfuscator idObfuscator;

    @Transactional
    public ProjectResponse createProject(ProjectDto dto) {
        Project project = new Project();
        project.setTitle(dto.getTitle().trim());
        project.setDescription(dto.getDescription() == null ? null : dto.getDescription().trim());
        project.setDeadline(dto.getDeadline());
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectMapper.toResponses(projectRepository.findAllByOrderByDeadlineDesc());
    }

    @Transactional(readOnly = true)
    public List<SubmissionDto> getAllSubmissionsOfProject(String projectIdH) {
        Long projectId = idObfuscator.decode(projectIdH);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài tập");
        }
        return submissionMapper.listEntityToListDTO(
                submissionRepository.findByProjectIdOrderBySubmittedAtDesc(projectId));
    }

    @Transactional
    public void gradeSubmission(String submissionIdH, GradeSubmissionRequest request) {
        Long submissionId = idObfuscator.decode(submissionIdH);
        ProjectSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp"));

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback() == null ? null : request.getFeedback().trim());
        submission.setStatus(SubmissionStatus.GRADED);
    }
}

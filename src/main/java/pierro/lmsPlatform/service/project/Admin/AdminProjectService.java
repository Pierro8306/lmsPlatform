package pierro.lmsPlatform.service.project.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.Project.GradeSubmissionRequest;
import pierro.lmsPlatform.dto.request.Project.ProjectDto;
import pierro.lmsPlatform.dto.response.Project.ProjectResponse;
import pierro.lmsPlatform.dto.response.Project.SubmissionDto;
import pierro.lmsPlatform.entity.project.Project;
import pierro.lmsPlatform.entity.project.ProjectSubmission;
import pierro.lmsPlatform.entity.project.SubmissionStatus;
import pierro.lmsPlatform.mapper.ProjectMapper;
import pierro.lmsPlatform.mapper.SubmissionMapper;
import pierro.lmsPlatform.repository.project.ProjectRepository;
import pierro.lmsPlatform.repository.project.ProjectSubmissionRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

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

package pierro.lmsPlatform.Service.Project.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Project.ProjectSubmitRequest;
import pierro.lmsPlatform.DTO.Response.Project.ProjectResponse;
import pierro.lmsPlatform.DTO.Response.Project.SubmissionDto;
import pierro.lmsPlatform.Entity.Project.Project;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;
import pierro.lmsPlatform.Entity.Project.SubmissionStatus;
import pierro.lmsPlatform.Mapper.ProjectMapper;
import pierro.lmsPlatform.Mapper.SubmissionMapper;
import pierro.lmsPlatform.Repository.Auth.UserRepository;
import pierro.lmsPlatform.Repository.Project.ProjectRepository;
import pierro.lmsPlatform.Repository.Project.ProjectSubmissionRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;
import pierro.lmsPlatform.Security.Exception.BadRequestException;
import pierro.lmsPlatform.Security.Exception.ConflictException;
import pierro.lmsPlatform.Security.Exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectSubmissionService {
    private final ProjectRepository projectRepository;
    private final ProjectSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final SubmissionMapper submissionMapper;
    private final ProjectMapper projectMapper;
    private final IdObfuscator idObfuscator;

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectMapper.toResponses(projectRepository.findAllByOrderByDeadlineDesc());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(String projectIdH) {
        Long projectId = idObfuscator.decode(projectIdH);
        return projectMapper.toResponse(projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập")));
    }

    @Transactional
    public void submit(Long currentUserId, ProjectSubmitRequest request) {
        Long projectId = idObfuscator.decode(request.getProjectId());
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));

        LocalDateTime now = LocalDateTime.now();
        if (project.getDeadline() != null && now.isAfter(project.getDeadline())) {
            throw new BadRequestException("Đã quá hạn nộp bài, bạn không thể nộp bài nữa");
        }

        ProjectSubmission submission = submissionRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElse(null);

        if (submission == null) {
            submission = new ProjectSubmission();
            submission.setProject(project);
            submission.setUser(userRepository.getReferenceById(currentUserId));
        } else if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new ConflictException("Bài nộp đã được chấm điểm, không thể nộp lại");
        }

        submission.setGithub_url(request.getGithubUrl().trim());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(now);

        try {
            submissionRepository.saveAndFlush(submission);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Bài nộp đang được xử lý, vui lòng thử lại");
        }
    }

    @Transactional(readOnly = true)
    public SubmissionDto getMySubmission(String projectIdH, Long currentUserId) {
        Long projectId = idObfuscator.decode(projectIdH);
        ProjectSubmission submission = submissionRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa nộp bài cho bài tập này"));
        return submissionMapper.entityToDTO(submission);
    }
}

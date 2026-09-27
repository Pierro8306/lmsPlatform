package pierro.lmsPlatform.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Project.ProjectDto;
import pierro.lmsPlatform.DTO.Request.Project.ProjectSubmitRequest;
import pierro.lmsPlatform.DTO.Response.Project.SubmissionDto;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Project.Project;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;
import pierro.lmsPlatform.Mapper.SubmissionMapper;
import pierro.lmsPlatform.Repository.Project.ProjectRepository;
import pierro.lmsPlatform.Repository.Project.ProjectSubmissionRepository;
import pierro.lmsPlatform.Repository.Auth.UserRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectSubmissionRepository projectSubmissionRepository;
    private final UserRepository userRepository;
    private final SubmissionMapper submissionMapper;
    private final IdObfuscator idObfuscator;

    public void createProject(ProjectDto projectDto) {
        Project project = new Project();
        project.setTitle(projectDto.getTitle());
        project.setDescription(projectDto.getDescription());
        project.setDeadline(projectDto.getDeadline());
        projectRepository.save(project);
    }

    public List<SubmissionDto> getAllSubmissionsOfProject(String projectIdH) {
        Long projectId = idObfuscator.decode(projectIdH);
        List<ProjectSubmission> projectSubmissions = projectSubmissionRepository.findByProjectId(projectId);
        return submissionMapper.listEntityToListDTO(projectSubmissions);
    }

    public void submit(Long currentUserId, ProjectSubmitRequest request) {
        Long projectId = idObfuscator.decode(request.getProjectId());

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài tập"));

        if (LocalDateTime.now().isAfter(project.getDeadline())) {
            throw new RuntimeException("Đã quá hạn nộp bài. Bạn không thể nộp bài nữa!");
        }

        ProjectSubmission submission = projectSubmissionRepository
                .findByProjectIdAndUserId(projectId, user.getId())
                .orElse(new ProjectSubmission());

        submission.setGithub_url(request.getGithubUrl());
        submission.setProject(project);
        submission.setUser(user);
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(LocalDateTime.now());

        projectSubmissionRepository.save(submission);
    }

    public SubmissionDto getMySubmission(String projectIdH, Long currentUserId) {
        Long projectId = idObfuscator.decode(projectIdH);

        return submissionMapper.entityToDTO(
                projectSubmissionRepository.findByProjectIdAndUserId(projectId, currentUserId)
                        .orElseThrow(() -> new RuntimeException("Submission not found")));
    }

    public void gradeSubmission(String submissionIdH, String score, String feedback) {
        Long submissionId = idObfuscator.decode(submissionIdH);
        ProjectSubmission submission = projectSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài nộp"));

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus("GRADED");

        projectSubmissionRepository.save(submission);
    }
}
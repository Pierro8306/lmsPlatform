package pierro.lmsPlatform.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.ProjectDto;
import pierro.lmsPlatform.DTO.Response.Project.SubmissionDto;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Project.Project;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;
import pierro.lmsPlatform.Mapper.SubmissionMapper;
import pierro.lmsPlatform.Repository.Project.ProjectRepository;
import pierro.lmsPlatform.Repository.Project.ProjectSubmissionRepository;
import pierro.lmsPlatform.Repository.UserRepository;

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

    public void createProject(ProjectDto projectDto) {
        Project project = new Project();
        project.setTitle(projectDto.getTitle());
        project.setDescription(projectDto.getDescription());
        project.setDeadline(projectDto.getDeadline());
        projectRepository.save(project);
    }
    public List<SubmissionDto> getAllSubmissionsOfProject(Long projectId){
        List<ProjectSubmission> projectSubmissions = projectSubmissionRepository.findByProjectId(projectId);
        return submissionMapper.listEntityToListDTO(projectSubmissions);
    }

    public void submitProject(Long projectId, Long userId, String githubUrl) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài tập"));

        if (LocalDateTime.now().isAfter(project.getDeadline())) {
            throw new RuntimeException("Đã quá hạn nộp bài. Bạn không thể nộp bài nữa!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProjectSubmission submission = projectSubmissionRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElse(new ProjectSubmission());

        submission.setGithub_url(githubUrl);
        submission.setProject(project);
        submission.setUser(user);
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(LocalDateTime.now());

        projectSubmissionRepository.save(submission);
    }
    public SubmissionDto getMySubmission(Long projectId, Long userId){
        return submissionMapper.entityToDTO(
                projectSubmissionRepository.findByProjectIdAndUserId(
                        projectId,
                        userId
                ).orElseThrow(() -> new RuntimeException("Submission not found")));

    }
    public void gradeSubmission(Long submissionId, String score, String feedback) {
        ProjectSubmission submission = projectSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài nộp"));

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus("GRADED");

        projectSubmissionRepository.save(submission);
    }

}

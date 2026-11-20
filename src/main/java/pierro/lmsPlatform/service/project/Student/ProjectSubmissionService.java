package pierro.lmsPlatform.service.project.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.Project.ProjectSubmitRequest;
import pierro.lmsPlatform.dto.response.Project.ProjectResponse;
import pierro.lmsPlatform.dto.response.Project.SubmissionDto;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.project.Project;
import pierro.lmsPlatform.entity.project.ProjectSubmission;
import pierro.lmsPlatform.entity.project.SubmissionStatus;
import pierro.lmsPlatform.mapper.ProjectMapper;
import pierro.lmsPlatform.mapper.SubmissionMapper;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.repository.project.ProjectRepository;
import pierro.lmsPlatform.repository.project.ProjectSubmissionRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ForbiddenException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

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
    public List<ProjectResponse> listProjects(Long userId) {
        if (userId == null) {
            return listProjects();
        }
        User user = findUser(userId);
        List<Project> projects = user.getUserGroup() == null
                ? projectRepository.findByUserGroupIsNullOrderByDeadlineDesc()
                : projectRepository.findAvailableForGroup(user.getUserGroup().getId());
        return projectMapper.toResponses(projects);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(String projectIdH) {
        return getProject(projectIdH, null);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(String projectIdH, Long userId) {
        Long projectId = idObfuscator.decode(projectIdH);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));
        if (userId != null) {
            assertCanAccess(project, findUser(userId));
        }
        return projectMapper.toResponse(project);
    }

    @Transactional
    public void submit(Long currentUserId, ProjectSubmitRequest request) {
        Long projectId = idObfuscator.decode(request.getProjectId());
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));

        User user = findUser(currentUserId);
        assertCanAccess(project, user);

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
            submission.setUser(user);
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
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập"));

        if (currentUserId != null) {
            assertCanAccess(project, findUser(currentUserId));
        }

        ProjectSubmission submission = submissionRepository
                .findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa nộp bài cho bài tập này"));
        return submissionMapper.entityToDTO(submission);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }

    private void assertCanAccess(Project project, User user) {
        if (project.getUserGroup() == null) {
            return;
        }
        if (user.getUserGroup() == null || !user.getUserGroup().getId().equals(project.getUserGroup().getId())) {
            throw new ForbiddenException("Bạn không thuộc nhóm được giao bài tập dự án này");
        }
    }
}

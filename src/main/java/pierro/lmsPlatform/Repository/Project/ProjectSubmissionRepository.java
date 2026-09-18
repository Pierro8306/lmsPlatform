package pierro.lmsPlatform.Repository.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Project.ProjectSubmission;

import java.util.List;
import java.util.Optional;

public interface ProjectSubmissionRepository extends JpaRepository<ProjectSubmission,Long> {
    List<ProjectSubmission> findByProjectId(Long projectId);
    Optional<ProjectSubmission> findByProjectIdAndUserId(Long projectId, Long userId);
}

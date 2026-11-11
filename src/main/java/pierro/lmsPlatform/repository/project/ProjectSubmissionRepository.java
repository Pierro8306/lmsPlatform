package pierro.lmsPlatform.repository.project;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.project.ProjectSubmission;

import java.util.List;
import java.util.Optional;

public interface ProjectSubmissionRepository extends JpaRepository<ProjectSubmission, Long> {

    /** Nạp luôn user để mapper lấy username mà không bị N+1. */
    @EntityGraph(attributePaths = "user")
    List<ProjectSubmission> findByProjectIdOrderBySubmittedAtDesc(Long projectId);

    @EntityGraph(attributePaths = "user")
    Optional<ProjectSubmission> findByProjectIdAndUserId(Long projectId, Long userId);

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<ProjectSubmission> findById(Long id);
}
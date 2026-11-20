package pierro.lmsPlatform.repository.problem;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.problem.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // Đếm số lần một học viên đã nộp một bài tập
    long countByProblemIdAndUserId(Long problemId, Long userId);

    @EntityGraph(attributePaths = {"problem", "user"})
    List<Submission> findByProblemIdAndUserIdOrderByCreatedAtDesc(Long problemId, Long userId);

    @EntityGraph(attributePaths = {"problem", "user"})
    List<Submission> findByProblemIdOrderByCreatedAtDesc(Long problemId);

    @EntityGraph(attributePaths = {"problem", "user", "testResults", "testResults.testCase"})
    Optional<Submission> findByIdAndProblemId(Long id, Long problemId);

    @EntityGraph(attributePaths = {"problem", "user", "testResults", "testResults.testCase"})
    Optional<Submission> findByIdAndUserId(Long id, Long userId);
}


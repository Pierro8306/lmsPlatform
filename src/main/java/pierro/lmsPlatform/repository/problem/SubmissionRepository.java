package pierro.lmsPlatform.repository.problem;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.problem.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // Đếm số lần một học viên đã nộp một bài tập
    long countByProblemIdAndUserId(Long problemId, Long userId);
}

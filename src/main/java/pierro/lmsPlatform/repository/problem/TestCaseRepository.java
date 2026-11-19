package pierro.lmsPlatform.repository.problem;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.problem.TestCase;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    // Lấy tất cả TestCase của một bài tập
    List<TestCase> findByProblemId(Long problemId);

    // Tìm TestCase theo ID và ID bài tập
    Optional<TestCase> findByIdAndProblemId(Long id, Long problemId);
}

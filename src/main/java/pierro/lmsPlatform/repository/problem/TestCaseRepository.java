package pierro.lmsPlatform.repository.problem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.entity.problem.TestCase;

import java.util.List;
import java.util.Optional;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    // Lấy tất cả TestCase của một bài tập
    List<TestCase> findByProblemId(Long problemId);

    // Tìm TestCase theo ID và ID bài tập
    Optional<TestCase> findByIdAndProblemId(Long id, Long problemId);

    @Query("SELECT t.problem.id, COUNT(t) FROM TestCase t WHERE t.problem.id IN :problemIds GROUP BY t.problem.id")
    List<Object[]> countByProblemIds(@Param("problemIds") List<Long> problemIds);

    long countByProblemId(Long problemId);
}

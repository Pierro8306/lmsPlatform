package pierro.lmsPlatform.repository.problem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import pierro.lmsPlatform.entity.problem.Problem;

public interface ProblemRepository extends JpaRepository<Problem,Long> {
    // Kiểm tra slug đã tồn tại chưa
    boolean existsBySlug(String slug);

    // Tìm bài tập theo slug
    Optional<Problem> findBySlug(String slug);

    // Lấy các bài tập đã công khai thuộc một nhóm học viên
    List<Problem> findByPublishedTrueAndGroups_Id(Long groupId);
}

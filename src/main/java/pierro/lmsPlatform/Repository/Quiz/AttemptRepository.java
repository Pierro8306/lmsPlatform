package pierro.lmsPlatform.Repository.Quiz;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.Entity.Quiz.Attempt;
import pierro.lmsPlatform.Entity.Quiz.AttemptStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findAllByQuiz_IdAndUser_IdOrderByStartedAtAsc(Long quizId, Long userId);

    Optional<Attempt> findFirstByQuiz_IdAndUser_IdAndStatusOrderByStartedAtDesc(
            Long quizId, Long userId, AttemptStatus status);

    long countByQuiz_IdAndUser_Id(Long quizId, Long userId);

    long countByQuiz_Id(Long quizId);

    List<Attempt> findAllByQuiz_Id(Long quizId);

    /** Bảng điểm: nạp sẵn user để không N+1 khi lấy username. */
    @EntityGraph(attributePaths = "user")
    List<Attempt> findByQuiz_IdOrderByStartedAtDesc(Long quizId);

    /** Mỗi dòng: [quizId, tổng số lượt làm của mọi học viên]. */
    @Query("select a.quiz.id, count(a) from Attempt a where a.quiz.id in :quizIds group by a.quiz.id")
    List<Object[]> countByQuizIds(@Param("quizIds") Collection<Long> quizIds);

    /** Mỗi dòng: [quizId, số lượt đã làm của user]. */
    @Query("select a.quiz.id, count(a) from Attempt a where a.user.id = :userId and a.quiz.id in :quizIds group by a.quiz.id")
    List<Object[]> countByUserForQuizzes(@Param("userId") Long userId, @Param("quizIds") Collection<Long> quizIds);

    /** Các lượt đang làm dở, nạp sẵn quiz để tính hạn chót (dùng cho job dọn attempt quá hạn). */
    @EntityGraph(attributePaths = "quiz")
    List<Attempt> findByStatus(AttemptStatus status);
}

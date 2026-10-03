package pierro.lmsPlatform.Repository.Quiz;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.Entity.Quiz.Quiz;

import java.util.Collection;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByUserGroupIsNullOrderByStartTimeDesc();

    /** Danh sách cho ADMIN: nạp sẵn nhóm để lấy tên mà không N+1. */
    @EntityGraph(attributePaths = "userGroup")
    List<Quiz> findAllByOrderByStartTimeDesc();

    /** Mỗi dòng: [quizId, số câu hỏi]. Tránh load cả collection chỉ để gọi size(). */
    @Query("select q.id, count(qs) from Quiz q join q.questions qs where q.id in :ids group by q.id")
    List<Object[]> countQuestionsByQuizIds(@Param("ids") Collection<Long> ids);

    @Query("select q from Quiz q where q.userGroup is null or q.userGroup.id = :groupId order by q.startTime desc")
    List<Quiz> findAvailableForGroup(@Param("groupId") Long groupId);
}

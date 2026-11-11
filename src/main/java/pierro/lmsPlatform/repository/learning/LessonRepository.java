package pierro.lmsPlatform.repository.learning;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.learning.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson,Long> {

    /** Nạp luôn course để map courseId/courseTitle mà không bị N+1. */
    @EntityGraph(attributePaths = "course")
    List<Lesson> findAllByOrderByIdDesc();

    /** Bài học chưa gắn khoá học nào (để admin lấy ID gọi addLesson). */
    @EntityGraph(attributePaths = "course")
    List<Lesson> findByCourseIsNullOrderByIdDesc();

    /** Gỡ toàn bộ bài học khỏi khoá học (gọi trước khi xoá khoá học để bài học không bị xoá theo). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Lesson l set l.course = null where l.course.id = :courseId")
    int detachFromCourse(@Param("courseId") Long courseId);
}

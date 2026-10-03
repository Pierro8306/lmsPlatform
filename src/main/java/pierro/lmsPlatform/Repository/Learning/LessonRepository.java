package pierro.lmsPlatform.Repository.Learning;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Learning.Lesson;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson,Long> {

    /** Nạp luôn course để map courseId/courseTitle mà không bị N+1. */
    @EntityGraph(attributePaths = "course")
    List<Lesson> findAllByOrderByIdDesc();

    /** Bài học chưa gắn khoá học nào (để admin lấy ID gọi addLesson). */
    @EntityGraph(attributePaths = "course")
    List<Lesson> findByCourseIsNullOrderByIdDesc();
}

package pierro.lmsPlatform.repository.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.entity.learning.LessonProgress;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<LessonProgress> findByUserIdAndLesson_Course_Id(Long userId, Long courseId);

    @Query("select lp.lesson.id from LessonProgress lp where lp.user.id = :userId and lp.lesson.course.id = :courseId and lp.completed = true")
    List<Long> findCompletedLessonIdsByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("select lp.lesson.course.id, count(lp) from LessonProgress lp where lp.user.id = :userId and lp.completed = true and lp.lesson.course.id in :courseIds group by lp.lesson.course.id")
    List<Object[]> countCompletedByUserForCourses(@Param("userId") Long userId, @Param("courseIds") Collection<Long> courseIds);

    void deleteByLesson_Id(Long lessonId);
}

package pierro.lmsPlatform.repository.learning;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.entity.learning.Course;

import java.util.Collection;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = "userGroup")
    List<Course> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "userGroup")
    List<Course> findByUserGroupIsNullOrderByIdDesc();

    @EntityGraph(attributePaths = "userGroup")
    @Query("select c from Course c where c.userGroup is null or c.userGroup.id = :groupId order by c.id desc")
    List<Course> findAvailableForGroup(@Param("groupId") Long groupId);

    @Query("select c.id, count(l) from Course c join c.lessons l where c.id in :courseIds group by c.id")
    List<Object[]> countLessonsByCourseIds(@Param("courseIds") Collection<Long> courseIds);
}

package pierro.lmsPlatform.repository.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.learning.Course;

public interface CourseRepository extends JpaRepository<Course,Long> {
}

package pierro.lmsPlatform.Repository.Learning;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Learning.Course;

public interface CourseRepository extends JpaRepository<Course,Long> {
}

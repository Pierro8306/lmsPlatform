package pierro.lmsPlatform.Repository.Learning;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Learning.Lesson;

public interface LessonRepository extends JpaRepository<Lesson,Long> {
}

package pierro.lmsPlatform.Repository.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Quiz.Quiz;

public interface QuizRepository extends JpaRepository<Quiz,Long> {
}

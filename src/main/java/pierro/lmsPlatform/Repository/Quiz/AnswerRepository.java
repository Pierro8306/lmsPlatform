package pierro.lmsPlatform.Repository.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Quiz.Answer;

public interface AnswerRepository extends JpaRepository<Answer,Long> {
}

package pierro.lmsPlatform.Repository.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Quiz.Question;

public interface QuestionRepository extends JpaRepository<Question,Long> {

}

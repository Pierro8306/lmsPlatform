package pierro.lmsPlatform.repository.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.quiz.Question;

public interface QuestionRepository extends JpaRepository<Question,Long> {

}

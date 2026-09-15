package pierro.lmsPlatform.Repository.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.Entity.Quiz.QuestionChoice;

import java.util.List;

public interface QuestionChoiceRepository extends JpaRepository<QuestionChoice,Long> {
    List<QuestionChoice> findByQuestionId(Long questionId);
    @Query("SELECT a FROM QuestionChoices a WHERE a.id_question IN :questionIds AND a.isCorrect = true")
    List<QuestionChoice> findCorrectAnswersByQuestionIds(@Param("questionIds") List<Long> questionIds);
}

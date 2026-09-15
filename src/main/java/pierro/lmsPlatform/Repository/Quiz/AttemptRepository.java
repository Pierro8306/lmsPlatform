package pierro.lmsPlatform.Repository.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Quiz.Attempt;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt,Long> {
    List<Attempt> findAllByQuiz_IdAndUser_Id(Long quizId, Long userId);
}

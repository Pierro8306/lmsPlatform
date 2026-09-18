package pierro.lmsPlatform.Entity.Quiz;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "id_attempt")
    private Attempt attempt;
    @ManyToOne
    @JoinColumn(name = "id_question")
    private Question question;
    @ManyToOne
    @JoinColumn(name = "id_choice")
    private QuestionChoice questionChoice;
}

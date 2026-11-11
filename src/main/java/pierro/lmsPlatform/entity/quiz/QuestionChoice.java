package pierro.lmsPlatform.entity.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "QuestionChoices")
public class QuestionChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question")
    private Question question;

    // Lombok: boolean `correct` => isCorrect()/setCorrect(); giữ tên cột cũ "is_correct"
    @Column(name = "is_correct", nullable = false)
    private boolean correct;
}


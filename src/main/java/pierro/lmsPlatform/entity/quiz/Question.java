package pierro.lmsPlatform.entity.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.entity.BaseEntity;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Questions")
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // length 2000: mặc định 255 ký tự không đủ cho đề dài
    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String subject;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<QuestionChoice> questionChoices = new LinkedHashSet<>();
}

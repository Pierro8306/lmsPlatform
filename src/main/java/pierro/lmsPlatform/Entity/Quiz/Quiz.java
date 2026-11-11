package pierro.lmsPlatform.Entity.Quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.Entity.Auth.UserGroup;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Thời gian được làm bài (dạng HH:mm:ss). */
    @Column(nullable = false)
    private LocalTime timeLimit;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Số lần làm tối đa cho mỗi học viên. */
    @Column(name = "max_attempts", nullable = false, columnDefinition = "INT DEFAULT 1")
    private int maxAttempts = 1;

    /** null = mở cho tất cả; có giá trị = chỉ nhóm đó được làm. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_group")
    private UserGroup userGroup;

    @ManyToMany(cascade = CascadeType.PERSIST)   // lưu câu hỏi cùng quiz, không cần questionRepository
    @JoinTable(
            name = "quiz_question",
            joinColumns = @JoinColumn(name = "id_quiz"),
            inverseJoinColumns = @JoinColumn(name = "id_question")
    )
    private Set<Question> questions = new HashSet<>();
}


package pierro.lmsPlatform.entity.learning;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.entity.BaseEntity;
import pierro.lmsPlatform.entity.auth.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "lesson_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_lesson"}))
public class LessonProgress extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_lesson")
    private Lesson lesson;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime completedAt;
}

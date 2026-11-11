package pierro.lmsPlatform.entity.quiz;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.entity.auth.User;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Attempts")
public class Attempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Thời điểm SERVER ghi nhận lúc bắt đầu làm bài (không tin client). */
    private LocalDateTime startedAt;

    /** Thời điểm nộp bài (server). */
    private LocalDateTime submittedAt;

    /** Giữ cột cũ: thời điểm tạo attempt. */
    private LocalDateTime dateTime;

    /** Thời gian đã dùng, do server tính. */
    private LocalTime useTime;

    private Double points;

    private Integer totalQuestions;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AttemptStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_quiz")
    private Quiz quiz;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answers = new HashSet<>();
}

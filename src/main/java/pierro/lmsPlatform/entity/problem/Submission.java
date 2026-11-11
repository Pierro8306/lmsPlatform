package pierro.lmsPlatform.entity.problem;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pierro.lmsPlatform.entity.auth.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "submissions", indexes = @Index(name = "idx_submissions_problem", columnList = "id_problem"))
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_problem")
    private Problem problem;

    @Column(nullable = false)
    private String language;

    @Column(name = "source_code", columnDefinition = "TEXT", nullable = false)
    private String sourceCode;

    /** null = chưa chấm xong. */
    @Enumerated(EnumType.STRING)
    private Verdict status;

    @Column(name = "exec_time_ms")
    private Integer execTimeMs;

    @Column(name = "memory_used_kb")
    private Integer memoryUsedKb;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SubmissionTestResult> testResults = new HashSet<>();
}

package pierro.lmsPlatform.entity.problem;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "submission_test_results",
        indexes = @Index(name = "idx_str_submission", columnList = "id_submission"))
public class SubmissionTestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_submission")
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_test_case")
    private TestCase testCase;

    @Enumerated(EnumType.STRING)
    private Verdict status;

    @Column(name = "exec_time_ms")
    private Integer execTimeMs;

    @Column(name = "memory_used_kb")
    private Integer memoryUsedKb;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}

package pierro.lmsPlatform.Entity.Problem;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "problems")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String difficulty;

    @Column(name = "time_limit_ms", nullable = false)
    private int timeLimitMs = 1000;

    @Column(name = "memory_limit_kb", nullable = false)
    private int memoryLimitKb = 256 * 1024;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Submission> submissions = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TestCase> testCases = new HashSet<>();
}

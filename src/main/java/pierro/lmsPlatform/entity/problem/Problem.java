package pierro.lmsPlatform.entity.problem;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.entity.auth.UserGroup;

import java.time.LocalDateTime;
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

    @Column(name = "start_at")
    private LocalDateTime startAt;          // null = mở ngay

    private LocalDateTime deadline;         // null = không giới hạn

    @Column(name = "allow_late")
    private Boolean allowLate = false;      // dùng Boolean (nullable) để ddl-auto=update không lỗi với dữ liệu cũ

    @Column(name = "max_attempts")
    private Integer maxAttempts;            // null = không giới hạn

    @ManyToMany
    @JoinTable(name = "problem_groups",
            joinColumns = @JoinColumn(name = "id_problem"),
            inverseJoinColumns = @JoinColumn(name = "id_user_group"))
    private Set<UserGroup> groups = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Submission> submissions = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TestCase> testCases = new HashSet<>();

}

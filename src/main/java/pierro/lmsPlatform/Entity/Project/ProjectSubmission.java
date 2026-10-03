package pierro.lmsPlatform.Entity.Project;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.Entity.Auth.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ProjectSubmissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_project"}))
public class ProjectSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String github_url;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SubmissionStatus status;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    @Column(length = 2000)
    private String feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project")
    private Project project;
}
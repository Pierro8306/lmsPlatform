package pierro.lmsPlatform.Entity.Project;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.Entity.Auth.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ProjectSubmissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "project_id"})
        })
public class ProjectSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String github_url;
    private LocalDateTime submittedAt;
    private String status;
    private String score;
    private String feedback;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project")
    private Project project;
}

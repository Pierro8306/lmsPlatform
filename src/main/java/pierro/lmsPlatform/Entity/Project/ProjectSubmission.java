package pierro.lmsPlatform.Entity.Project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Quiz.Attempt;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ProjectSubmissions")
public class ProjectSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String github_url;
    private String status;
    private String score;
    private String feedback;
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
    @ManyToOne
    @JoinColumn(name = "id_project")
    private Project project;
}

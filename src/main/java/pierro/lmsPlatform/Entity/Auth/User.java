package pierro.lmsPlatform.Entity.Auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_group")
    private UserGroup userGroup;

    @Column(unique = true, nullable = false,columnDefinition = "VARCHAR(100)")
    private String username;
    @Email
    @Column(unique = true, length = 255)
    private String email;
    @Column(nullable = false)
    private String password;

    @Column(name = "token_version", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int tokenVersion = 0;

}


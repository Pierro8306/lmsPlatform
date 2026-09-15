package pierro.lmsPlatform.Entity.Auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_role")
    private Role role;

    @Column(unique = true, nullable = false,columnDefinition = "VARCHAR(100)")
    private String username;
    @Email
    private String email;
    @Column(nullable = false,columnDefinition = "VARCHAR(10)")
    private String password;

}

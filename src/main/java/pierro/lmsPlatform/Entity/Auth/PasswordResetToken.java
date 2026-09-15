package pierro.lmsPlatform.Entity.Auth;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@Table(name = "PasswordResetTokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id")
    private User user;

    private String token;
    private LocalDate expiration;
    private LocalDate creationDate;
    private LocalDate usedDate;
}

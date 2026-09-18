package pierro.lmsPlatform.Entity.Quiz;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pierro.lmsPlatform.Entity.Auth.User;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Attempts")
public class Attempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private LocalDateTime dateTime;
    private LocalTime useTime;
    private Double points;
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
    @ManyToOne
    @JoinColumn(name = "id_quiz")
    private Quiz quiz;
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answers = new HashSet<>();
}

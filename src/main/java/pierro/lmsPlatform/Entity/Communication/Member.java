package pierro.lmsPlatform.Entity.Communication;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.Entity.Auth.User;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@Table(name = "Members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "id_event")
    private Event event;
    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
}

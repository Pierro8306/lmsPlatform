package pierro.lmsPlatform.Entity.Communication;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pierro.lmsPlatform.Entity.Auth.User;

@Entity
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

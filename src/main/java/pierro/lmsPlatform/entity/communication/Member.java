package pierro.lmsPlatform.entity.communication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.entity.auth.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_event", "id_user"}))
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_event")
    private Event event;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;
}

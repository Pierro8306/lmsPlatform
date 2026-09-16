package pierro.lmsPlatform.Repository.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Communication.Event;
import pierro.lmsPlatform.Entity.Communication.Member;

public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByEventAndUser(Event event, User user);
    Member findByEventIdAndUserId(Long id_event, Long user_id);
}

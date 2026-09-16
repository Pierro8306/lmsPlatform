package pierro.lmsPlatform.Repository.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Communication.Event;

public interface EventRepository extends JpaRepository<Event,Long> {
}

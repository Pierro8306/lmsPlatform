package pierro.lmsPlatform.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.communication.Event;

public interface EventRepository extends JpaRepository<Event,Long> {
}

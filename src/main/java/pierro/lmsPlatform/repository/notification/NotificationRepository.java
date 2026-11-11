package pierro.lmsPlatform.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.communication.Notification;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}

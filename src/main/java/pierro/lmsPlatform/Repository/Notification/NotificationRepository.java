package pierro.lmsPlatform.Repository.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Communication.Notification;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}

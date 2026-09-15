package pierro.lmsPlatform.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Auth.User;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);
}

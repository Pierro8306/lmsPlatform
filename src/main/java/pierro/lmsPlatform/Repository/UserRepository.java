package pierro.lmsPlatform.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Auth.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
}

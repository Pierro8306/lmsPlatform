package pierro.lmsPlatform.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.auth.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String name);
}

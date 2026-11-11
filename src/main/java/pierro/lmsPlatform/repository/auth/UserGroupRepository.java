package pierro.lmsPlatform.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.auth.UserGroup;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup,Long> {

    Optional<UserGroup> findByName(String name);
}

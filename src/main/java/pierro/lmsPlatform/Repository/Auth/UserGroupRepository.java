package pierro.lmsPlatform.Repository.Auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Auth.UserGroup;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup,Long> {

    Optional<UserGroup> findByName(String name);
}

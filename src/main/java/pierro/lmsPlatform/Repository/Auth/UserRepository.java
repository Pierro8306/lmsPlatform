package pierro.lmsPlatform.Repository.Auth;

import org.springframework.data.jpa.repository.EntityGraph;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pierro.lmsPlatform.Entity.Auth.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role", "userGroup"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"role", "userGroup"})
    Optional<User> findWithRoleAndGroupById(Long id);

    @Override
    @EntityGraph(attributePaths = {"role", "userGroup"})
    List<User> findAll();                       // tránh N+1 khi map sang DTO

    @EntityGraph(attributePaths = {"role", "userGroup"})
    List<User> findByRole_Name(String roleName);

    /** Khoá dòng user (SELECT ... FOR UPDATE) để tuần tự hoá các request start/submit bài thi đồng thời. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> lockById(@Param("id") Long id);

    /**
     * Khoá (SELECT ... FOR UPDATE) toàn bộ user thuộc một role, theo thứ tự id cố định.
     * Dùng để tuần tự hoá các thao tác xoá / hạ quyền admin đồng thời, tránh hai admin
     * cùng xoá/hạ quyền lẫn nhau rồi hệ thống không còn admin nào.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.role.name = :roleName order by u.id")
    List<User> lockAllByRoleName(@Param("roleName") String roleName);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findFirstByEmailIgnoreCase(String email);

    /** JwtFilter dùng: user còn tồn tại không + role hiện tại + phiên bản token trong DB. */
    @Query("select u.role.name as roleName, u.tokenVersion as tokenVersion from User u where u.id = :id")
    Optional<UserAuthInfo> findAuthInfoById(@Param("id") Long id);
}

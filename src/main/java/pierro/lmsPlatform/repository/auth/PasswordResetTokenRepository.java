package pierro.lmsPlatform.repository.auth;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.entity.auth.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String tokenHash);

    boolean existsByUser_IdAndCreatedAtAfter(Long userId, LocalDateTime time);

    void deleteByUser_Id(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime time);

    /** SELECT ... FOR UPDATE: hai request dùng chung 1 token cùng lúc sẽ được xử lý tuần tự, chỉ một request thành công. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t where t.token = :token")
    Optional<PasswordResetToken> lockByToken(@Param("token") String tokenHash);
}

package pierro.lmsPlatform.Repository.Auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pierro.lmsPlatform.Entity.Auth.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String tokenHash);

    boolean existsByUser_IdAndCreatedAtAfter(Long userId, LocalDateTime time);

    void deleteByUser_Id(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime time);
}

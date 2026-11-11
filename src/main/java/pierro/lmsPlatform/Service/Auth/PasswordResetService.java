package pierro.lmsPlatform.Service.Auth;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Auth.ResetPasswordRequest;
import pierro.lmsPlatform.Entity.Auth.PasswordResetToken;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Repository.Auth.PasswordResetTokenRepository;
import pierro.lmsPlatform.Repository.Auth.UserRepository;
import pierro.lmsPlatform.Security.Exception.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {
    /** Không gửi thêm mail nếu user vừa yêu cầu trong khoảng này (chống spam hộp thư). */
    private static final Duration COOLDOWN = Duration.ofMinutes(1);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final long expirationMinutes;
    private final String frontendUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                MailService mailService,
                                @Value("${app.password-reset.expiration-minutes:30}") long expirationMinutes,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.expirationMinutes = expirationMinutes;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
    }

    @Transactional
    public void requestReset(String email) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByExpiresAtBefore(now);   // dọn token hết hạn

        userRepository.findFirstByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            if (tokenRepository.existsByUser_IdAndCreatedAtAfter(user.getId(), now.minus(COOLDOWN))) {
                return;
            }
            tokenRepository.deleteByUser_Id(user.getId());   // mỗi user chỉ có 1 token còn hiệu lực

            String rawToken = newRawToken();
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken(sha256(rawToken));
            token.setCreatedAt(now);
            token.setExpiresAt(now.plusMinutes(expirationMinutes));
            tokenRepository.save(token);

            final String to = user.getEmail();
            final String link = frontendUrl + "/reset-password?token=" + rawToken;
            runAfterCommit(() -> mailService.sendPasswordReset(to, link, expirationMinutes));
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken token = tokenRepository.lockByToken(sha256(request.getToken().trim()))
                .filter(t -> t.getUsedAt() == null && t.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new BadRequestException("Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);   // đăng xuất mọi thiết bị
        token.setUsedAt(now);
    }

    /** Chỉ gửi mail khi transaction đã commit: tránh gửi link cho token chưa tồn tại / đã bị rollback. */
    private static void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private static String newRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}


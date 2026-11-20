package pierro.lmsPlatform.service.auth;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.Auth.ChangePasswordRequest;
import pierro.lmsPlatform.dto.request.Auth.LoginRequest;
import pierro.lmsPlatform.dto.response.Auth.LoginResponse;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;
import pierro.lmsPlatform.security.exception.UnauthorizedException;
import pierro.lmsPlatform.security.jwt.JwtService;
import pierro.lmsPlatform.security.jwt.TokenBlacklistService;
import pierro.lmsPlatform.security.rateLimit.LoginAttemptService;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    /** Hash giả để thời gian phản hồi giống nhau dù username có tồn tại hay không. */
    private String dummyHash;

    @PostConstruct
    void initDummyHash() {
        dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest request, String clientIp) {
        String key = LoginAttemptService.key(request.getUsername(), clientIp);
        loginAttemptService.checkAllowed(key);

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        String hash = user != null ? user.getPassword() : dummyHash;
        boolean ok = passwordEncoder.matches(request.getPassword(), hash) && user != null;

        if (!ok) {
            loginAttemptService.recordFailure(key);
            throw new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu");
        }
        loginAttemptService.recordSuccess(key);
        return toLoginResponse(user);
    }

    /** Đổi refresh token lấy cặp token mới. Refresh token chỉ hợp lệ khi còn đúng phiên bản trong DB và không nằm trong blacklist. */
    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken) {
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new UnauthorizedException("Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
        }
        JwtService.TokenInfo info = jwtService.parse(refreshToken, JwtService.TokenType.REFRESH)
                .orElseThrow(() -> new UnauthorizedException("Phiên đăng nhập không hợp lệ hoặc đã hết hạn"));
        User user = userRepository.findWithRoleAndGroupById(info.userId())
                .filter(u -> u.getTokenVersion() == info.version())
                .orElseThrow(() -> new UnauthorizedException("Phiên đăng nhập không hợp lệ hoặc đã hết hạn"));
        return toLoginResponse(user);
    }

    /**
     * Đăng xuất trên thiết bị hiện tại: đưa token (access và/hoặc refresh) vào blacklist.
     * Không tăng tokenVersion để các thiết bị khác không bị văng.
     */
    public void logout(Long userId, String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            tokenBlacklistService.blacklistToken(accessToken);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenBlacklistService.blacklistToken(refreshToken);
        }
    }

    public void logout(Long userId, String accessToken) {
        logout(userId, accessToken, null);
    }

    /**
     * Giữ lại hàm logout(userId) để tương thích ngược. Mặc định kích hoạt đăng xuất tất cả thiết bị.
     */
    @Transactional
    public void logout(Long userId) {
        logoutAll(userId);
    }

    /**
     * Buộc đăng xuất trên tất cả mọi thiết bị (tăng tokenVersion trong DB).
     */
    @Transactional
    public void logoutAll(Long userId) {
        userRepository.findById(userId).ifPresent(this::revokeTokens);
    }

    /**
     * Đổi mật khẩu và đăng xuất các thiết bị khác. Trả về cặp token mới để phiên hiện tại
     * tiếp tục dùng được.
     */
    @Transactional
    public LoginResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không đúng");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu cũ");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        revokeTokens(user);
        // user đang được quản lý trong transaction nên không cần save()
        return toLoginResponse(user);
    }

    private void revokeTokens(User user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    private LoginResponse toLoginResponse(User user) {
        return new LoginResponse(user.getRole().getName(),
                jwtService.createAccessToken(user),
                jwtService.createRefreshToken(user));
    }
}

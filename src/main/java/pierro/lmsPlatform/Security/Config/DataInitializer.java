package pierro.lmsPlatform.Security.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pierro.lmsPlatform.Entity.Auth.Role;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Repository.Auth.RoleRepository;
import pierro.lmsPlatform.Repository.Auth.UserRepository;

/**
 * Khởi tạo dữ liệu nền.
 *  - Role ADMIN / STUDENT: luôn tạo nếu thiếu.
 *  - Tài khoản mặc định: CHỈ tạo khi cấu hình mật khẩu qua biến môi trường / properties
 *    (app.seed.admin-password, app.seed.student-password). Không có mật khẩu nào được hard-code trong mã nguồn.
 *    Production: chỉ đặt APP_SEED_ADMIN_PASSWORD cho lần chạy đầu tiên, tạo xong thì bỏ biến đi và đổi mật khẩu.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private static final int MIN_SEED_PASSWORD_LENGTH = 8;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    @Value("${app.seed.student-password:}")
    private String studentPassword;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> newRole("ADMIN"));
            Role memberRole = roleRepository.findByName("STUDENT")
                    .orElseGet(() -> newRole("STUDENT"));

            seedUser("admin", "admin@lms.local", adminPassword, adminRole);
            seedUser("student", "student@lms.local", studentPassword, memberRole);

            if (userRepository.findByRole_Name("ADMIN").isEmpty()) {
                log.warn("Hệ thống chưa có tài khoản ADMIN nào. Đặt app.seed.admin-password "
                        + "(biến môi trường APP_SEED_ADMIN_PASSWORD) rồi khởi động lại để tạo tài khoản admin đầu tiên.");
            }
        };
    }

    private Role newRole(String name) {
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    private void seedUser(String username, String email, String rawPassword, Role role) {
        if (rawPassword == null || rawPassword.isBlank()) return;          // không cấu hình => không tạo
        if (userRepository.existsByUsername(username)) return;
        if (rawPassword.length() < MIN_SEED_PASSWORD_LENGTH) {
            log.warn("Bỏ qua tạo tài khoản '{}': mật khẩu seed phải từ {} ký tự", username, MIN_SEED_PASSWORD_LENGTH);
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
        log.info("Đã tạo tài khoản mặc định '{}' (hãy đổi mật khẩu sau lần đăng nhập đầu tiên)", username);
    }
}

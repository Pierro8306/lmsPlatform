package pierro.lmsPlatform.Security.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pierro.lmsPlatform.Entity.Auth.Role;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Repository.Auth.RoleRepository;
import pierro.lmsPlatform.Repository.Auth.UserRepository;


@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // 1. Tạo 2 Role mặc định nếu chưa tồn tại
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ADMIN");
                        return roleRepository.save(role);
                    });

            Role memberRole = roleRepository.findByName("STUDENT")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("STUDENT");
                        return roleRepository.save(role);
                    });

            // 2. Tạo tài khoản Admin mặc định
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@lms.local");
                admin.setPassword(passwordEncoder.encode("Admin@123"));

                admin.setRole(adminRole);

                userRepository.save(admin);
                System.out.println(">>> Đã tạo Admin mặc định: admin / Admin@123");
            }

            // 3. Tạo tài khoản Member/User mặc định
            if (!userRepository.existsByUsername("student")) {
                User user = new User();
                user.setUsername("student");
                user.setEmail("student@lms.local");
                user.setPassword(passwordEncoder.encode("Student@123"));

                user.setRole(memberRole);

                userRepository.save(user);
                System.out.println(">>> Đã tạo User mặc định: student / Student@123");
            }
        };
    }
}
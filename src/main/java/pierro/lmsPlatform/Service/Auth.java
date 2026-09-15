package pierro.lmsPlatform.Service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import pierro.lmsPlatform.DTO.Request.ChangePasswordRequest;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Repository.UserRepository;


public class Auth {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;

    @Transactional
    public boolean changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return true;
    }
}

package pierro.lmsPlatform.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pierro.lmsPlatform.DTO.Request.Auth.ChangePasswordRequest;
import pierro.lmsPlatform.DTO.Request.Auth.LoginRequest;
import pierro.lmsPlatform.DTO.Response.Auth.LoginResponse;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Repository.Auth.UserRepository;
import pierro.lmsPlatform.Security.Jwt.JwtService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    public LoginResponse authenticate(LoginRequest loginRequest) {
        var user = userRepository.findByUsername(loginRequest.getUsername());
        if (user.isEmpty()) {
            return null;
        }
        if (passwordEncoder.matches(
                loginRequest.getPassword(),
                user.get().getPassword()))
        {
            System.out.println(user.get().getUsername()+" "+user.get().getRole().getName());
            return new LoginResponse(
                    user.get().getRole().getName(),
                    jwtService.createToken(user.get())
            );
        }
        return null;
    }
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return;
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}

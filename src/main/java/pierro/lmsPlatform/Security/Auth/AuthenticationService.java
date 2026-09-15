package pierro.lmsPlatform.Security.Auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pierro.lmsPlatform.DTO.Request.Auth.LoginRequest;
import pierro.lmsPlatform.DTO.Response.Auth.LoginResponse;
import pierro.lmsPlatform.Repository.UserRepository;
import pierro.lmsPlatform.Security.Jwt.JwtService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    JwtService jwtService;
    PasswordEncoder passwordEncoder;
    LoginRequest loginRequest;
    public LoginResponse authenticate(LoginRequest loginRequest) {
        var user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
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
}

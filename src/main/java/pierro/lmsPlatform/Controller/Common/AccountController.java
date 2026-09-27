package pierro.lmsPlatform.Controller.Common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pierro.lmsPlatform.DTO.Request.Auth.ChangePasswordRequest;
import pierro.lmsPlatform.Service.AuthenticationService;

@RestController
@RequestMapping("/api/user")
public class AccountController {

    private final AuthenticationService authService;

    public AccountController(AuthenticationService authService) { this.authService = authService; }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;

        Long currentUserId = (Long) authentication.getPrincipal();

        authService.changePassword(currentUserId, request);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

}

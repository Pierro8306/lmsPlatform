package pierro.lmsPlatform.Controller.Auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pierro.lmsPlatform.DTO.Request.Auth.ForgotPasswordRequest;
import pierro.lmsPlatform.DTO.Request.Auth.LoginRequest;
import pierro.lmsPlatform.DTO.Request.Auth.RefreshTokenRequest;
import pierro.lmsPlatform.DTO.Request.Auth.ResetPasswordRequest;
import pierro.lmsPlatform.DTO.Response.Auth.LoginResponse;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.Auth.AuthenticationService;
import pierro.lmsPlatform.Service.Auth.PasswordResetService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@Valid @RequestBody LoginRequest loginRequest,
                                                HttpServletRequest http) {
        // Chạy sau reverse proxy: bật server.forward-headers-strategy=framework để getRemoteAddr() là IP thật
        LoginResponse auth = authenticationService.authenticate(loginRequest, http.getRemoteAddr());
        return ResponseObject.ok("Đăng nhập thành công", auth);
    }

    /** Đổi refresh token lấy access token + refresh token mới. */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseObject> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseObject.ok("Làm mới phiên đăng nhập thành công",
                authenticationService.refresh(request.getRefreshToken()));
    }

    /** Luôn trả cùng một thông báo dù email có tồn tại hay không. */
    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseObject> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseObject.ok("Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseObject> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseObject.ok("Đặt lại mật khẩu thành công, vui lòng đăng nhập lại");
    }
}

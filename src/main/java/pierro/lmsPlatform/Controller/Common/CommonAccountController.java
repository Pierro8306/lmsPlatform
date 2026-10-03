package pierro.lmsPlatform.Controller.Common;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pierro.lmsPlatform.DTO.Request.Auth.ChangePasswordRequest;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.Auth.AuthenticationService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class CommonAccountController {
    private final AuthenticationService authService;

    @PostMapping("/change-password")
    public ResponseEntity<ResponseObject> changePassword(@AuthenticationPrincipal Long userId,
                                                       @Valid @RequestBody ChangePasswordRequest request) {
        // Trả về cặp token mới: các thiết bị khác bị đăng xuất, phiên hiện tại vẫn dùng tiếp được
        return ResponseObject.ok("Đổi mật khẩu thành công", authService.changePassword(userId, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseObject.ok("Đăng xuất thành công");
    }
}

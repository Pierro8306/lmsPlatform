package pierro.lmsPlatform.Controller.Common;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pierro.lmsPlatform.DTO.Request.Auth.UpdateProfileRequest;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.User.Student.ProfileService;

@RestController
@RequestMapping("/api/common/profile")
@RequiredArgsConstructor
public class CommonProfileController {
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ResponseObject> getProfile(@AuthenticationPrincipal Long userId) {
        return ResponseObject.ok("Lấy thông tin cá nhân thành công", profileService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<ResponseObject> updateProfile(@AuthenticationPrincipal Long userId,
                                                        @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseObject.ok("Cập nhật thông tin cá nhân thành công",
                profileService.updateProfile(userId, request));
    }
}
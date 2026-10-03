package pierro.lmsPlatform.Controller.Admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Auth.CreateUserDto;
import pierro.lmsPlatform.DTO.Request.Auth.UpdateUserRequest;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.User.Admin.UserManagementService;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {
    private final UserManagementService accountService;

    @PostMapping
    public ResponseEntity<ResponseObject> createAccount(@Valid @RequestBody CreateUserDto dto) {
        accountService.createUser(dto);
        return ResponseObject.ok("Tạo tài khoản thành công");
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseObject> getAllAccounts() {
        return ResponseObject.ok("Lấy dữ liệu thành công", accountService.getAllUser());
    }

    @GetMapping("/getByRole")
    public ResponseEntity<ResponseObject> getAccountsByRole(@RequestParam String role) {
        return ResponseObject.ok("Lấy dữ liệu thành công", accountService.getByRole(role));
    }

    @DeleteMapping
    public ResponseEntity<ResponseObject> deleteUser(@RequestParam("IdUser") String userId,
                                                     @AuthenticationPrincipal Long adminId) {
        accountService.deleteUser(userId, adminId);
        return ResponseObject.ok("Xoá user thành công");
    }

    @PutMapping
    public ResponseEntity<ResponseObject> updateUser(@Valid @RequestBody UpdateUserRequest request,
                                                     @AuthenticationPrincipal Long adminId) {
        accountService.updateUser(request, adminId);
        return ResponseObject.ok("Cập nhật thành công");
    }
}

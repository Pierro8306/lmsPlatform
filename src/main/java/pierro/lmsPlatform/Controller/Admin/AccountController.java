package pierro.lmsPlatform.Controller.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Auth.CreateUserDto;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.DTO.Response.UserResposeDto;
import pierro.lmsPlatform.Service.AccountService;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    @PostMapping()
    public ResponseEntity<ResponseObject> createAccount(@RequestParam CreateUserDto createUserDto) {
        accountService.createUser(createUserDto);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tạo thông báo thành công", null));
    }
    @GetMapping("/getAll")
    public ResponseEntity<ResponseObject> getAllAccounts() {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy dữ liệu thành công",accountService.getAllUser()));
    }
    @GetMapping("/getByRole")
    public ResponseEntity<ResponseObject> getAccountsByRole(@RequestParam String role) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy dữ liệu thành công",accountService.getByRole(role)));
    }
    @DeleteMapping
    public ResponseEntity<ResponseObject> deleteUser(@RequestParam String IdUser) {
        accountService.deleteUser(IdUser);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Xóa user thành công",null));
    }
    @PutMapping
    public ResponseEntity<ResponseObject> updateUser(@RequestParam UserResposeDto userResposeDto) {
        accountService.updateUser(userResposeDto);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "update thành công",null));
    }
}

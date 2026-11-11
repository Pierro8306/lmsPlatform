package pierro.lmsPlatform.dto.request.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(max = 72)
    private String password;
}

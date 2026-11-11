package pierro.lmsPlatform.dto.request.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "ID không được để trống")
    private String id;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Role không được để trống")
    private String role;

    /** Tuỳ chọn: để trống = bỏ khỏi nhóm. */
    private String userGroup;
}

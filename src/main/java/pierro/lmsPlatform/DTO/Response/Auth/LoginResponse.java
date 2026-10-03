package pierro.lmsPlatform.DTO.Response.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String role;
    /** Access token (ngắn hạn) — gửi kèm header Authorization: Bearer. */
    private String token;
    /** Refresh token (dài hạn) — chỉ dùng để gọi POST /api/auth/refresh. */
    private String refreshToken;
}

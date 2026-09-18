package pierro.lmsPlatform.DTO.Response.Auth;

import lombok.Data;


@Data
public class LoginResponse {
    private String role;
    private String token;
}

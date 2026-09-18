package pierro.lmsPlatform.DTO.Request.Auth;

import lombok.Data;

@Data

public class LoginRequest {
    private String username;
    private String password;
}

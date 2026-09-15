package pierro.lmsPlatform.DTO.Response.Auth;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String role;
    private String token;
}

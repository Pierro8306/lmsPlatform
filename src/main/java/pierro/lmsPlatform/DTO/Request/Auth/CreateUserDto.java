package pierro.lmsPlatform.DTO.Request.Auth;

import lombok.Data;
import pierro.lmsPlatform.Entity.Auth.Role;
import pierro.lmsPlatform.Entity.Auth.UserGroup;

@Data
public class CreateUserDto {
    private String role;
    private String userGroup;
    private String username;
    private String email;
    private String password;
}

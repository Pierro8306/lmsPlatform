package pierro.lmsPlatform.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pierro.lmsPlatform.Entity.Auth.UserGroup;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResposeDto {
    private String id;
    private String role;
    private UserGroup userGroup;
    private String username;
    private String email;
}

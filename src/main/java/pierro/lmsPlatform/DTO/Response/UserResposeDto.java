package pierro.lmsPlatform.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResposeDto {
    private String id;
    private String role;
    private String userGroup;   // tên nhóm, không phải entity
    private String username;
    private String email;
}

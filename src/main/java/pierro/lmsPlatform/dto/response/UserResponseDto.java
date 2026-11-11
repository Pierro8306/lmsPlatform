package pierro.lmsPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String id;
    private String role;
    private String userGroup;   // tên nhóm, không phải entity
    private String username;
    private String email;
}

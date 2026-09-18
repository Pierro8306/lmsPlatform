package pierro.lmsPlatform.DTO.Request.Auth;

import lombok.Data;
import lombok.Getter;

@Data

public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}

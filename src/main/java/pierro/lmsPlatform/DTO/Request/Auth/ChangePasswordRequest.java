package pierro.lmsPlatform.DTO.Request.Auth;

import lombok.Getter;

@Getter

public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}

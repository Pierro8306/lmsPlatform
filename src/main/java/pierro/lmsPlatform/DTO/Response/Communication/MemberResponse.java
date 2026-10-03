package pierro.lmsPlatform.DTO.Response.Communication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Chỉ lộ thông tin tối thiểu của người tham gia — không bao giờ có password hash. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private String userId;
    private String username;
    private String email;
}

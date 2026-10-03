package pierro.lmsPlatform.DTO.Response.Project;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class SubmissionDto {
    private String id;
    private String userName;
    private String github_url;
    private LocalDateTime submittedAt;
    private String status;
    private String score;
    private String feedback;
}

package pierro.lmsPlatform.DTO.Request.Project;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectDto {
    private String projectId;
    private String title;
    private String description;
    private LocalDateTime deadline;
}

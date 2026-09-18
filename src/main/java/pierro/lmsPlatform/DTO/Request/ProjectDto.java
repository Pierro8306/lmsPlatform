package pierro.lmsPlatform.DTO.Request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectDto {
    private String title;
    private String description;
    private LocalDateTime deadline;
}

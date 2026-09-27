package pierro.lmsPlatform.DTO.Request.Project;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectSubmitRequest {

    @NotBlank(message = "ID bài tập không được để trống")
    private String projectId;

    @NotBlank(message = "Đường dẫn Github không được để trống")
    private String githubUrl;
}

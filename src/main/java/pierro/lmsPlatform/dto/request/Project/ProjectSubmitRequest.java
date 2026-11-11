package pierro.lmsPlatform.dto.request.Project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectSubmitRequest {
    @NotBlank(message = "ID bài tập không được để trống")
    private String projectId;

    @NotBlank(message = "Đường dẫn Github không được để trống")
    @Size(max = 255, message = "Đường dẫn Github tối đa 255 ký tự")
    @Pattern(regexp = "^https://github\\.com/[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(/\\S*)?$",
            message = "Đường dẫn phải có dạng https://github.com/<user>/<repo>")
    private String githubUrl;
}
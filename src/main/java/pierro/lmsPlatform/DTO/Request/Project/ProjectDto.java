package pierro.lmsPlatform.DTO.Request.Project;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectDto {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    @NotNull(message = "Hạn nộp không được để trống")
    @Future(message = "Hạn nộp phải ở tương lai")
    private LocalDateTime deadline;
}
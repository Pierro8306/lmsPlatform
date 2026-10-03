package pierro.lmsPlatform.DTO.Request.Learning;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCourseDto {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;
}

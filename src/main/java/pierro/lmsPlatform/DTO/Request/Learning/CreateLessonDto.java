package pierro.lmsPlatform.DTO.Request.Learning;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLessonDto {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String content;
    private String video_url;
    private String attachment_urls;
}

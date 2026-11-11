package pierro.lmsPlatform.dto.request.Learning;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLessonDto {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;
    @Size(max = 20000, message = "Nội dung tối đa 20000 ký tự")
    private String content;
    @Size(max = 1000, message = "Đường dẫn video tối đa 1000 ký tự")
    private String video_url;
    @Size(max = 5000, message = "Danh sách tệp đính kèm tối đa 5000 ký tự")
    private String attachment_urls;
}


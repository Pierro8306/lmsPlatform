package pierro.lmsPlatform.DTO.Request.Learning;

import lombok.Data;

@Data
public class CreateLessonDto {
    private String title;
    private String content;
    private String video_url;
    private String attachment_urls;
}

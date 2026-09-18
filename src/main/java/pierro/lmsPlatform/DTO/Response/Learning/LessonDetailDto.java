package pierro.lmsPlatform.DTO.Response.Learning;

import lombok.Data;

@Data
public class LessonDetailDto {
    private String title;
    private String content;
    private String video_url;
    private String attachment_urls;
}

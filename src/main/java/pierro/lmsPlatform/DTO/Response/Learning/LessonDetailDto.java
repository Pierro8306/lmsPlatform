package pierro.lmsPlatform.DTO.Response.Learning;

import lombok.Data;

@Data
public class LessonDetailDto {
    private String id;
    private String title;
    private String content;
    private String video_url;
    private String attachment_urls;
}
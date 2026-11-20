package pierro.lmsPlatform.dto.response.Learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDetailDto {
    private String id;
    private String title;
    private String content;
    private String video_url;
    private String attachment_urls;
    private boolean completed;
    private LocalDateTime completedAt;
}

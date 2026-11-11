package pierro.lmsPlatform.dto.response.Learning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dòng trong danh sách bài học của ADMIN. courseId/courseTitle = null nếu bài học chưa gắn khoá học. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLessonSummaryDto {
    private String id;
    private String title;
    private String courseId;
    private String courseTitle;
}

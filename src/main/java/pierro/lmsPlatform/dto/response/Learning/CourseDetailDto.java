package pierro.lmsPlatform.dto.response.Learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDto {
    private String id;
    private String title;
    private String description;
    private String userGroup;
    private int totalLessons;
    private int completedLessons;
    private double progressPercent;
    private List<LessonDetailDto> lessons;
}

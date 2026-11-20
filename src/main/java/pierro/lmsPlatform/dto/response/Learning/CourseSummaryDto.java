package pierro.lmsPlatform.dto.response.Learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryDto {
    private String id;
    private String title;
    private String description;
    private String userGroup;
    private int totalLessons;
    private int completedLessons;
    private double progressPercent;
}

package pierro.lmsPlatform.DTO.Response.Learning;

import lombok.Data;

import java.util.List;

@Data
public class CourseDetailDto {
    private String id;
    private String title;
    private String description;
    private List<LessonDetailDto>  lessons;
}

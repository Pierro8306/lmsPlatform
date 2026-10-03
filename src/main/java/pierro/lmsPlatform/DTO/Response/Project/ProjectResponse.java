package pierro.lmsPlatform.DTO.Response.Project;

import lombok.Data;

import java.time.LocalDateTime;

/** Bài tập dự án trả về cho client (id đã mã hoá, dùng để nộp bài / chấm điểm). */
@Data
public class ProjectResponse {
    private String id;
    private String title;
    private String description;
    private LocalDateTime deadline;
}

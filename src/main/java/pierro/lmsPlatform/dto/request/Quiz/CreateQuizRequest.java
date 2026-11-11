package pierro.lmsPlatform.dto.request.Quiz;
import jakarta.validation.constraints.Size;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateQuizRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String description;

    @NotNull(message = "Thời gian mở bài thi không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian đóng bài thi không được để trống")
    private LocalDateTime endTime;

    /** Thời gian làm bài, dạng "HH:mm:ss" (ví dụ "00:45:00"). */
    @NotNull(message = "Thời gian làm bài không được để trống")
    private LocalTime timeLimit;

    @Min(value = 1, message = "Số lần làm tối thiểu là 1")
    private Integer maxAttempts = 1;

    /** Tuỳ chọn: tên nhóm được phép làm. Để trống = tất cả. */
    private String userGroup;

    @NotEmpty(message = "Bài thi cần ít nhất 1 câu hỏi")
    private List<@NotNull(message = "Câu hỏi không được null") @Valid QuestionRequest> questions;
}


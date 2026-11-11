package pierro.lmsPlatform.dto.request.Quiz;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** Cập nhật thông tin chung của bài thi (không gồm câu hỏi — dùng API thay câu hỏi riêng). */
@Data
public class UpdateQuizRequest {
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

    /** Dạng "HH:mm:ss" (ví dụ "00:45:00"). */
    @NotNull(message = "Thời gian làm bài không được để trống")
    private LocalTime timeLimit;

    /** Bỏ trống = giữ nguyên. */
    @Min(value = 1, message = "Số lần làm tối thiểu là 1")
    private Integer maxAttempts;

    /** Tên nhóm được phép làm. Để trống = tất cả. */
    private String userGroup;
}


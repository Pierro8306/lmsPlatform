package pierro.lmsPlatform.dto.request.Project;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeSubmissionRequest {
    @NotNull(message = "Điểm không được để trống")
    @DecimalMin(value = "0.0", message = "Điểm tối thiểu là 0")
    @DecimalMax(value = "10.0", message = "Điểm tối đa là 10")
    @Digits(integer = 2, fraction = 2, message = "Điểm tối đa 2 chữ số thập phân")
    private BigDecimal score;

    @Size(max = 2000, message = "Nhận xét tối đa 2000 ký tự")
    private String feedback;
}
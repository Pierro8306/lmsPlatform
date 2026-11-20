package pierro.lmsPlatform.dto.request.problem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO đại diện cho một test case khi tạo/sửa hàng loạt.
 * id: null nếu là test case mới, chuỗi mã hóa nếu là test case đã có sẵn cần cập nhật.
 */
public record TestCaseItemRequest(
        String id,
        String inputData,
        @NotNull(message = "Kết quả mong đợi không được để trống")
        String expectedOutput,
        boolean hidden,
        @PositiveOrZero(message = "Hệ số điểm phải >= 0")
        double scoreWeight) {
}

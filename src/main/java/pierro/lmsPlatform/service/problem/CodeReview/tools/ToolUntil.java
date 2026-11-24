package pierro.lmsPlatform.service.problem.CodeReview.tools;

import java.util.Locale;

import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Severity;

// 2 công cụ phục vụ chuẩn hóa dữ liệu đầu ra trước khi chuyển thành dạng Finding 
final class ToolUtil {
    // Chuẩn hóa đường dẫn file (loại bỏ /src/ ở đầu)
    static String rel(String p) { return p.startsWith("/src/") ? p.substring(5) : p; }
    // Chuẩn hóa severity 
    static Severity sev(String s) {
        return switch (s == null ? "" : s.toUpperCase(Locale.ROOT)) {
            case "HIGH", "ERROR" -> Severity.HIGH;
            case "MEDIUM", "WARNING" -> Severity.MEDIUM;
            case "LOW", "STYLE", "PERFORMANCE", "PORTABILITY" -> Severity.LOW;
            default -> Severity.INFO;
        };
    }
}
package pierro.lmsPlatform.service.problem.CodeReview.model;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import pierro.lmsPlatform.service.problem.CodeReview.Cache.HashUtil;
// Class định nghĩa các kiểu gì liệu dùng trong code review
public final class ReviewModels {
    private ReviewModels() {
    }

    public enum TargetType {
        PROBLEM, PROJECT;
    }

    // Thứ tự tăng dần theo mức độ để sort/so sánh đúng
    public enum Severity {
        INFO, LOW, MEDIUM, HIGH;
    }

    public record SourceFile(String path, Lang lang, String content, String hash) {
        public static SourceFile of(String path, Lang lang, String raw) {
            String c = HashUtil.normalize(raw);
            return new SourceFile(path, lang, c, HashUtil.sha256(c));
        }
    }

    // Input của code review
    public record SourceBundle(Path workDir, List<SourceFile> listSourceFiles, String context) {}

    // ---- Output của LLM ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LlmIssue(      // vấn đề MỚI mà tool tĩnh chưa phát hiện
        int line,
        String category,
        String severity,
        String message,
        String suggestion) {}

    // Phán quyết của LLM cho từng finding tĩnh (Sx trong prompt)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StaticVerdict(
        String id,               // "S1", "S2", ...
        String verdict,          // CONFIRMED | FALSE_POSITIVE
        String explanation,      // giải thích dễ hiểu cho sinh viên
        String suggestion) {}    // gợi ý sửa

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FileReview(
        String summary,
        String complexity,
        List<LlmIssue> issues,
        List<StaticVerdict> verdicts) {

        // Chuẩn hóa null (LLM quên trả trường, hoặc đọc cache cũ)
        public FileReview {
            issues = issues == null ? List.of() : issues;
            verdicts = verdicts == null ? List.of() : verdicts;
        }
    }

    // Output sau khi gộp tool tĩnh + LLM
    public record Finding(String file, int line, Severity severity, String category,
                          String source, String ruleId, String message,
                          String suggestion, List<String> confirmedBy) {}

    public record FileSummary(String path, String summary, String complexity) {}

    // Output cuối của CodeReview
    public record ReviewReport(
        String bundleHash,
        String summary,
        List<FileSummary> listFileSummaries,
        List<Finding> listFindings,
        List<String> listWaring,
        boolean complete) {}
}
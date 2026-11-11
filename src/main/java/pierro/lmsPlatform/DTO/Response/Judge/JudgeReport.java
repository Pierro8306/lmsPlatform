package pierro.lmsPlatform.DTO.Response.Judge;

import pierro.lmsPlatform.Entity.Problem.Verdict;

import java.util.List;

public record JudgeReport(
        Verdict verdict,
        int maxTimeMs,
        int maxMemoryKb,
        double score,
        double maxScore,
        String message,
        List<TestResult> results) {

    public record TestResult(long testCaseId, Verdict verdict, int timeMs, int memoryKb, String message) {}

    public static JudgeReport failure(Verdict verdict, String message) {
        return failure(verdict, message, 0);
    }

    /** Giữ nguyên maxScore của bài để phía gọi không bị chia cho 0 khi tính phần trăm. */
    public static JudgeReport failure(Verdict verdict, String message, double maxScore) {
        return new JudgeReport(verdict, 0, 0, 0, maxScore, message, List.of());
    }
}

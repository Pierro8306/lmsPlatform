package pierro.lmsPlatform.dto.request.Judge;

import java.util.List;

/**
 * @param language       "cpp" | "python" | "java"
 * @param timeLimitMs    giới hạn thời gian chạy mỗi test (ms)
 * @param memoryLimitKb  giới hạn bộ nhớ mỗi test (KB)
 * @param stopOnFirstFail dừng ngay khi gặp test đầu tiên không AC
 * @param epsilon        0 = so khớp chính xác (chuẩn hóa dòng); >0 = so số thực với sai số
 */
public record JudgeRequest(
        String sourceCode,
        String language,
        int timeLimitMs,
        int memoryLimitKb,
        List<TestCaseData> testCases,
        boolean stopOnFirstFail,
        double epsilon) {

    public record TestCaseData(long id, String input, String expectedOutput,
                               double scoreWeight, boolean hidden) {}
}

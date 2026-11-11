package pierro.lmsPlatform.service.judge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pierro.lmsPlatform.dto.request.Judge.JudgeRequest;
import pierro.lmsPlatform.dto.request.Judge.JudgeRequest.TestCaseData;
import pierro.lmsPlatform.dto.request.Judge.LanguageConfig;
import pierro.lmsPlatform.dto.response.Judge.JudgeReport;
import pierro.lmsPlatform.dto.response.Judge.JudgeReport.TestResult;
import pierro.lmsPlatform.entity.problem.Verdict;
import pierro.lmsPlatform.service.judge.DockerSandbox.RunResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Chấm 1 bài nộp: validate -> compile 1 lần -> chạy lần lượt từng test -> tổng hợp kết quả. */
@Service
public class JudgeService {
    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    static final int MAX_SOURCE_CHARS = 256 * 1024;
    static final int MAX_TEST_CASES = 200;
    static final int MIN_TIME_LIMIT_MS = 50;
    static final int MAX_TIME_LIMIT_MS = 30_000;
    static final int MIN_MEMORY_KB = 16 * 1024;
    static final int MAX_MEMORY_KB = 2 * 1024 * 1024;
    /** Tổng thời gian tối đa cho 1 bài nộp (compile + mọi test), tránh chiếm worker quá lâu. */
    static final long MAX_TOTAL_MS = 5 * 60_000L;

    private final DockerSandbox sandbox;
    private final OutputChecker checker;

    public JudgeService(DockerSandbox sandbox, OutputChecker checker) {
        this.sandbox = sandbox;
        this.checker = checker;
    }

    public JudgeReport judge(JudgeRequest req) {
        Optional<LanguageConfig> langOpt = LanguageConfig.find(req.language());
        if (langOpt.isEmpty())
            return JudgeReport.failure(Verdict.SE, "Unsupported language: " + req.language());
        String invalid = validate(req);
        if (invalid != null)
            return JudgeReport.failure(Verdict.SE, invalid);

        double total = 0;
        for (JudgeRequest.TestCaseData tc : req.testCases()) total += tc.scoreWeight();

        LanguageConfig lang = langOpt.get();
        long deadline = System.nanoTime() + MAX_TOTAL_MS * 1_000_000L;
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("judge_src_");
            Fs.openDir(workDir);
            Path src = workDir.resolve(lang.sourceFile());
            Files.writeString(src, req.sourceCode() == null ? "" : req.sourceCode(), StandardCharsets.UTF_8);
            Fs.openFile(src);

            // 1) Compile một lần
            DockerSandbox.CompileResult cr = sandbox.compile(workDir, lang);
            if (!cr.ok()) return JudgeReport.failure(cr.failVerdict(), cr.message(), total);

            // 2) Chạy từng test
            List<TestResult> results = new ArrayList<>();
            Verdict overall = Verdict.AC;
            int maxTime = 0, maxMem = 0;
            double earned = 0;

            for (JudgeRequest.TestCaseData tc : req.testCases()) {
                if (Thread.currentThread().isInterrupted()) {   // shutdown/huỷ: dừng ngay, không spawn thêm container
                    results.add(new TestResult(tc.id(), Verdict.SE, 0, 0, "Judge interrupted"));
                    if (overall == Verdict.AC) overall = Verdict.SE;
                    break;
                }
                if (System.nanoTime() > deadline) {
                    results.add(new TestResult(tc.id(), Verdict.SE, 0, 0, "Judge deadline exceeded"));
                    if (overall == Verdict.AC) overall = Verdict.SE;
                    break;
                }
                RunResult rr = sandbox.run(workDir, lang, tc.input(), req.timeLimitMs(), req.memoryLimitKb());
                TestResult tr = evaluate(tc, rr, req.epsilon());
                results.add(tr);

                maxTime = Math.max(maxTime, tr.timeMs());
                maxMem = Math.max(maxMem, tr.memoryKb());

                if (tr.verdict() == Verdict.AC) {
                    earned += tc.scoreWeight();
                } else {
                    if (overall == Verdict.AC) overall = tr.verdict();
                    if (req.stopOnFirstFail()) break;
                }
            }
            return new JudgeReport(overall, maxTime, maxMem, earned, total, "", results);

        } catch (Exception e) {
            log.error("Judge failed", e);
            // không trả e.getMessage() ra ngoài (có thể lộ đường dẫn nội bộ)
            return JudgeReport.failure(Verdict.SE, "Internal judge error", total);
        } finally {
            Fs.deleteQuietly(workDir);
        }
    }

    /** @return thông báo lỗi, hoặc null nếu hợp lệ. */
    private static String validate(JudgeRequest req) {
        if (req.testCases() == null || req.testCases().isEmpty()) return "Problem has no test cases";
        if (req.testCases().size() > MAX_TEST_CASES) return "Too many test cases (max " + MAX_TEST_CASES + ")";
        if (req.sourceCode() != null && req.sourceCode().length() > MAX_SOURCE_CHARS)
            return "Source code too large (max " + MAX_SOURCE_CHARS + " chars)";
        if (req.timeLimitMs() < MIN_TIME_LIMIT_MS || req.timeLimitMs() > MAX_TIME_LIMIT_MS)
            return "Invalid time limit (" + MIN_TIME_LIMIT_MS + ".." + MAX_TIME_LIMIT_MS + " ms)";
        if (req.memoryLimitKb() < MIN_MEMORY_KB || req.memoryLimitKb() > MAX_MEMORY_KB)
            return "Invalid memory limit (" + MIN_MEMORY_KB + ".." + MAX_MEMORY_KB + " KB)";
        if (!(req.epsilon() >= 0)) return "Invalid epsilon"; // chặn cả NaN
        for (TestCaseData tc : req.testCases()) {
            if (tc == null) return "Null test case";
            if (!(tc.scoreWeight() >= 0)) return "Invalid score weight on test " + tc.id();
        }
        return null;
    }

    private TestResult evaluate(TestCaseData tc, RunResult rr, double epsilon) {
        Verdict v;
        String msg = "";
        switch (rr.status()) {
            case TLE -> v = Verdict.TLE;
            case MLE -> v = Verdict.MLE;
            case OLE -> v = Verdict.OLE;
            case RTE -> {
                v = Verdict.RTE;
                msg = tc.hidden() ? "Exit code " + rr.exitCode() : shorten(rr.stderr(), 500);
            }
            case SYSTEM_ERROR -> {
                v = Verdict.SE;
                msg = shorten(rr.stderr(), 500);
            }
            default -> { // OK
                if (checker.check(rr.stdout(), tc.expectedOutput(), epsilon)) {
                    v = Verdict.AC;
                } else {
                    v = Verdict.WA;
                    if (!tc.hidden()) // không lộ đáp án của test ẩn
                        msg = "Expected: " + shorten(tc.expectedOutput(), 200)
                            + " | Got: " + shorten(rr.stdout(), 200);
                }
            }
        }
        return new TestResult(tc.id(), v, rr.timeMs(), rr.memoryKb(), msg);
    }

    private static String shorten(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}

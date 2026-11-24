package pierro.lmsPlatform.service.problem.CodeReview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import pierro.lmsPlatform.service.problem.CodeReview.Cache.AnalysisCacheService;
import pierro.lmsPlatform.service.problem.CodeReview.Cache.HashUtil;
import pierro.lmsPlatform.service.problem.CodeReview.LLM.LlmReviewer;
import pierro.lmsPlatform.service.problem.CodeReview.model.Lang;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.FileReview;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.FileSummary;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Finding;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.LlmIssue;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.ReviewReport;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Severity;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceBundle;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceFile;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.StaticVerdict;
import pierro.lmsPlatform.service.problem.CodeReview.tools.SkippedException;
import pierro.lmsPlatform.service.problem.CodeReview.tools.StaticAnalyzer;

/**
 * Pipeline: cache báo cáo -> phân tích tĩnh -> LLM thẩm định (cache từng file) -> merge -> cache báo cáo.
 */
@Service
public class CodeReviewService {
    private static final Logger log = LoggerFactory.getLogger(CodeReviewService.class);

    /** Tăng khi đổi image tool / ruleset: cache báo cáo cũ tự vô hiệu. */
    static final String TOOLS_VERSION = "1";

    private static final int MAX_FILES_LLM = 15;
    private static final int MAX_TOTAL_CHARS = 15000;
    private static final int MAX_STATIC_PER_FILE = 20;   // trần số finding tĩnh đưa vào prompt mỗi file
    private static final int MAX_LLM_ISSUES = 8;

    private final AnalysisCacheService cache;
    private final LlmReviewer llm;
    private final List<StaticAnalyzer> analyzers;
    private final ExecutorService llmPool;
    private final ExecutorService staticPool;

    // Có 2 bean ExecutorService nên phải chỉ rõ tên bean (khớp với CodeReviewConfig)
    public CodeReviewService(AnalysisCacheService cache,
                             LlmReviewer llm,
                             List<StaticAnalyzer> analyzers,
                             @Qualifier("crLlmPool") ExecutorService llmPool,
                             @Qualifier("crStaticPool") ExecutorService staticPool) {
        this.cache = cache;
        this.llm = llm;
        this.analyzers = analyzers;
        this.llmPool = llmPool;
        this.staticPool = staticPool;
    }

    private record StaticOutcome(List<Finding> findings, List<String> warnings, boolean ok) {}

    public ReviewReport analyze(SourceBundle b) {
        // Key báo cáo gồm mọi thứ ảnh hưởng kết quả: tool, prompt, model, source, context
        String key = HashUtil.sha256(String.join("\n", "report", TOOLS_VERSION,
                LlmReviewer.PROMPT_VERSION, llm.model(), bundleHash(b), ctxHash(b)));

        // Trúng cache: không chạy tool, không gọi LLM
        Optional<ReviewReport> hit = cache.find(key, ReviewReport.class);
        if (hit.isPresent()) return hit.get();

        List<String> warnings = new ArrayList<>();

        // Phân tích tĩnh 
        StaticOutcome st = runStatic(b);
        warnings.addAll(st.warnings());
        boolean complete = st.ok();

        // Gom finding theo file, sắp xếp nghiêm trọng nhất lên đầu
        Map<String, List<Finding>> byFile = st.findings().stream()
                .collect(Collectors.groupingBy(Finding::file, HashMap::new, Collectors.toList()));
        byFile.replaceAll((path, list) -> sortFindings(list));

        // Chọn file gửi LLM
        List<SourceFile> toReview = pickFiles(b, byFile, warnings);

        // LLM thẩm định từng file, chọn các finding . Sx trong prompt = phần tử x-1 của danh sách promptList
        Map<String, List<Finding>> promptList = new HashMap<>();
        for (SourceFile f : toReview) {
            List<Finding> all = byFile.getOrDefault(f.path(), List.of());
            promptList.put(f.path(), all.subList(0, Math.min(MAX_STATIC_PER_FILE, all.size())));
        }

        List<CompletableFuture<LlmReviewer.Reviewed>> jobs = toReview.stream()
                .map(f -> CompletableFuture.supplyAsync(
                        () -> llm.review(f, promptList.get(f.path()), b.context()), llmPool))
                .toList();
        List<LlmReviewer.Reviewed> reviewed = jobs.stream().map(CompletableFuture::join).toList();

        // Merge
        List<Finding> findings = new ArrayList<>();
        List<FileSummary> summaries = new ArrayList<>();
        Set<String> handledFiles = new HashSet<>();   // file đã được LLM xử lý xong

        for (var r : reviewed) {
            SourceFile f = r.file();
            if (r.error() != null || r.review() == null) {
                complete = false;
                warnings.add(    "LLM lỗi tạm thời: " + f.path() + " (" + r.error() + ")");
//                warnings.add("LLM lỗi tạm thời: " + f.path());
                continue;   // finding tĩnh của file này sẽ được giữ nguyên ở bước dưới
            }
            handledFiles.add(f.path());
            FileReview fr = r.review();
            summaries.add(new FileSummary(f.path(), fr.summary(), fr.complexity()));

            List<Finding> sf = promptList.get(f.path());
            Set<Integer> judged = new HashSet<>();

            // Finding tĩnh đã được LLM thẩm định
            for (StaticVerdict v : fr.verdicts()) {
                int idx = parseId(v.id());
                if (idx < 0 || idx >= sf.size() || !judged.add(idx)) continue;   // id sai hoặc trùng
                if ("FALSE_POSITIVE".equalsIgnoreCase(v.verdict())) continue;     // tool báo nhầm -> bỏ
                Finding s = sf.get(idx);
                findings.add(new Finding(s.file(), s.line(), s.severity(), s.category(),
                        s.source(), s.ruleId(),
                        blank(v.explanation()) ? s.message() : v.explanation(),
                        blank(v.suggestion()) ? s.suggestion() : v.suggestion(),
                        List.of("LLM")));
            }
            // Finding tĩnh mà LLM không nhắc tới: giữ nguyên
            for (int i = 0; i < sf.size(); i++) {
                if (!judged.contains(i)) findings.add(sf.get(i));
            }
            // Finding tĩnh vượt trần prompt: giữ nguyên
            List<Finding> all = byFile.getOrDefault(f.path(), List.of());
            if (all.size() > sf.size()) findings.addAll(all.subList(sf.size(), all.size()));

            // Vấn đề MỚI do LLM phát hiện
            int lineCount = f.content().split("\n", -1).length;
            for (LlmIssue i : fr.issues().stream().limit(MAX_LLM_ISSUES).toList()) {
                int line = (i.line() >= 1 && i.line() <= lineCount) ? i.line() : 0;   // LLM bịa dòng thì về 0
                findings.add(new Finding(f.path(), line, parseSev(i.severity()), normCat(i.category()),
                        "LLM", "", i.message(), i.suggestion(), List.of()));
            }
        }

        // File không qua LLM (vượt giới hạn hoặc LLM lỗi, hoặc path không khớp): giữ finding tĩnh
        byFile.forEach((path, list) -> {
            if (!handledFiles.contains(path)) findings.addAll(list);
        });

        findings.sort(findingOrder());

        long high = findings.stream().filter(x -> x.severity() == Severity.HIGH).count();
        long med = findings.stream().filter(x -> x.severity() == Severity.MEDIUM).count();
        int nFiles = b.listSourceFiles().size();
        String summary = (nFiles == 1 && !summaries.isEmpty())
                ? summaries.get(0).summary()
                : nFiles + " file, " + findings.size() + " vấn đề (" + high + " HIGH, " + med + " MEDIUM)";

        ReviewReport report = new ReviewReport(bundleHash(b), summary, summaries, findings, warnings, complete);

        // Báo cáo thiếu (lỗi tạm) thì KHÔNG cache. Cache từng file của LLM vẫn còn nên lần sau không tốn lại
        if (complete) cache.put(key, report);
        return report;
    }

    //  phân tích tĩnh

    private StaticOutcome runStatic(SourceBundle b) {
        Set<Lang> langs = b.listSourceFiles().stream().map(SourceFile::lang).collect(Collectors.toSet());

        List<CompletableFuture<StaticOutcome>> jobs = analyzers.stream()
                .filter(a -> !Collections.disjoint(a.languages(), langs))
                .map(a -> CompletableFuture.supplyAsync(() -> runOne(a, b), staticPool))
                .toList();

        List<Finding> out = new ArrayList<>();
        List<String> warn = new ArrayList<>();
        boolean ok = true;
        for (var j : jobs) {
            StaticOutcome o = j.join();
            out.addAll(o.findings());
            warn.addAll(o.warnings());
            ok &= o.ok();
        }
        return new StaticOutcome(out, warn, ok);
    }

    private StaticOutcome runOne(StaticAnalyzer a, SourceBundle b) {
        try {
            return new StaticOutcome(a.analyze(b.workDir(), b.listSourceFiles()), List.of(), true);
        } catch (SkippedException e) {   // bỏ qua có chủ đích: kết quả xác định, vẫn cache được
            return new StaticOutcome(List.of(), List.of(a.name() + ": " + e.getMessage()), true);
        } catch (Exception e) {          // lỗi tạm thời: báo cáo không được cache
            log.warn("{} failed", a.name(), e);
            return new StaticOutcome(List.of(), List.of(a.name() + ": lỗi tạm thời"), false);
        }
    }

    // chọn file cho LLM

    // Ưu tiên file có nhiều finding MEDIUM/HIGH, sau đó tới file lớn; dừng khi chạm giới hạn chi phí. */
    private List<SourceFile> pickFiles(SourceBundle b, Map<String, List<Finding>> byFile, List<String> warnings) {
        Comparator<SourceFile> order = Comparator
                .comparingLong((SourceFile f) -> urgency(byFile.get(f.path())))
                .thenComparingInt(f -> f.content().length())
                .reversed();

        List<SourceFile> picked = new ArrayList<>();
        int total = 0;
        for (SourceFile f : b.listSourceFiles().stream().sorted(order).toList()) {
            if (picked.size() >= MAX_FILES_LLM || total + f.content().length() > MAX_TOTAL_CHARS) {
                warnings.add("LLM bỏ qua (vượt giới hạn): " + f.path());
                continue;
            }
            picked.add(f);
            total += f.content().length();
        }
        return picked;
    }

    private static long urgency(List<Finding> list) {
        if (list == null) return 0;
        return list.stream().filter(x -> x.severity().compareTo(Severity.MEDIUM) >= 0).count();
    }



    /** Hash tổng của toàn bộ source file (path + hash từng file). */
    private String bundleHash(SourceBundle b) {
        StringBuilder sb = new StringBuilder();
        b.listSourceFiles().stream().sorted(Comparator.comparing(SourceFile::path))
                .forEach(f -> sb.append('\n').append(f.path()).append(':').append(f.hash()));
        return HashUtil.sha256(sb.toString());
    }

    private String ctxHash(SourceBundle b) {
        return HashUtil.sha256(b.context() == null ? "" : b.context());
    }

    //  tiện ích

    private static Comparator<Finding> findingOrder() {
        return Comparator.comparing((Finding x) -> x.severity()).reversed()
                .thenComparing((Finding x) -> x.file())
                .thenComparingInt((Finding x) -> x.line());
    }

    private static List<Finding> sortFindings(List<Finding> l) {
        List<Finding> copy = new ArrayList<>(l);
        copy.sort(findingOrder());
        return copy;
    }

    /** "S3" -> 2; sai định dạng -> -1. */
    private static int parseId(String id) {
        try {
            String s = id.trim().toUpperCase(Locale.ROOT);
            return s.startsWith("S") ? Integer.parseInt(s.substring(1)) - 1 : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static Severity parseSev(String s) {
        try {
            return Severity.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Severity.LOW;
        }
    }

    private static String normCat(String c) {
        Set<String> ok = Set.of("SECURITY", "CLEAN_CODE", "COMPLEXITY", "BUG", "PERFORMANCE");
        String u = c == null ? "" : c.trim().toUpperCase(Locale.ROOT);
        return ok.contains(u) ? u : "CLEAN_CODE";
    }
}
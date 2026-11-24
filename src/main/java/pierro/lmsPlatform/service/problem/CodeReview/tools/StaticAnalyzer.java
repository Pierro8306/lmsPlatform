package pierro.lmsPlatform.service.problem.CodeReview.tools;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import pierro.lmsPlatform.service.problem.CodeReview.model.Lang;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Finding;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceFile;

//interface chung cho các công cụ tĩnh phân tích (static analyzer)
public interface StaticAnalyzer {
    Set<Lang> languages();

    String name();

    /**
     * @throws SkippedException khi ppt bỏ qua có chủ đích
     * Mọi exception khác coi là lỗi tạm thời
     * -> báo cáo không được cache.
     */
    List<Finding> analyze(
        Path dir,
        List<SourceFile> files
    ) throws Exception;
}
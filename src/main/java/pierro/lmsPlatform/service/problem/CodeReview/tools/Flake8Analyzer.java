package pierro.lmsPlatform.service.problem.CodeReview.tools;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pierro.lmsPlatform.service.problem.CodeReview.model.Lang;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Finding;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Severity;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceFile;

@Component
@RequiredArgsConstructor
public class Flake8Analyzer implements StaticAnalyzer {
    private final ToolContainer docker;

    public Set<Lang> languages() { return Set.of(Lang.PYTHON); }
    public String name() { return "flake8"; }

    public List<Finding> analyze(Path dir, List<SourceFile> files) throws Exception {
        var r = docker.run(dir, List.of("flake8", "--isolated", "--max-line-length=120",
                "--format=%(path)s|%(row)d|%(code)s|%(text)s", "/src"), 60_000);
        if (r.timedOut()) throw new IllegalStateException("flake8 timeout");

        List<Finding> out = new ArrayList<>();
        for (String line : r.stdout().split("\n")) {
            String[] p = line.split("\\|", 4);
            if (p.length < 4) continue;

            int row;
            try {
                row = Integer.parseInt(p[1].trim());
            } catch (NumberFormatException e) {
                continue;   // dòng không đúng định dạng thì bỏ qua, không làm hỏng cả analyzer
            }

            String code = p[2].trim();
            boolean bug = code.startsWith("E9") || code.startsWith("F");
            Severity s = (code.startsWith("E9") || code.equals("F821") || code.startsWith("F63") || code.startsWith("F7"))
                    ? Severity.HIGH : bug ? Severity.MEDIUM : Severity.LOW;
            out.add(new Finding(ToolUtil.rel(p[0]), row, s,
                    bug ? "BUG" : "STYLE", "flake8", code, p[3], "", List.of()));
        }
        return out;
    }
}
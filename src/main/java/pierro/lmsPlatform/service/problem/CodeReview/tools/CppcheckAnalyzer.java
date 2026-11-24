package pierro.lmsPlatform.service.problem.CodeReview.tools;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pierro.lmsPlatform.service.problem.CodeReview.model.Lang;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Finding;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceFile;

@Component
@RequiredArgsConstructor 
public class CppcheckAnalyzer implements StaticAnalyzer {
    private final ToolContainer docker;
    
    public Set<Lang> languages() { return Set.of(Lang.C, Lang.CPP); }
    public String name() { return "cppcheck"; }

    public List<Finding> analyze(Path dir, List<SourceFile> files) throws Exception {
        var r = docker.run(dir, List.of("cppcheck", "--enable=warning,style,performance,portability",
                "--inconclusive", "--quiet",
                "--template={file}|{line}|{severity}|{id}|{message}", "/src"), 90_000);
        if (r.timedOut()) throw new IllegalStateException("cppcheck timeout");
        List<Finding> out = new ArrayList<>();
        for (String line : r.stderr().split("\n")) {
            String[] p = line.split("\\|", 5);
            if (p.length < 5) continue;
            int ln;
            try { ln = Integer.parseInt(p[1].trim()); } catch (NumberFormatException e) { continue; }
            String sev = p[2].trim();
            String cat = switch (sev) {
                case "error", "warning" -> "BUG";
                case "performance" -> "PERFORMANCE";
                default -> "STYLE";
            };
            out.add(new Finding(ToolUtil.rel(p[0]), ln, ToolUtil.sev(sev), cat, "cppcheck", p[3], p[4], "", List.of()));
        }
        return out;
    }
}

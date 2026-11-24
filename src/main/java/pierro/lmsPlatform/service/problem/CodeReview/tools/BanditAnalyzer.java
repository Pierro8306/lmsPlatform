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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor 
public class BanditAnalyzer implements StaticAnalyzer {
    private final ToolContainer docker; private final ObjectMapper om;

    public Set<Lang> languages() { return Set.of(Lang.PYTHON); }
    public String name() { return "bandit"; }

    public List<Finding> analyze(Path dir, List<SourceFile> files) throws Exception {
        var r = docker.run(dir, List.of("bandit", "-r", "/src", "-f", "json", "-q"), 60_000);
        if (r.timedOut() || r.stdout().isBlank()) throw new IllegalStateException("bandit failed: " + r.stderr());
        List<Finding> out = new ArrayList<>();
        for (JsonNode n : om.readTree(r.stdout()).path("results")) {     // exit=1 khi có issue là bình thường
            out.add(new Finding(ToolUtil.rel(n.path("filename").asString()), n.path("line_number").asInt(),
                    ToolUtil.sev(n.path("issue_severity").asString()), "SECURITY", "bandit",
                    n.path("test_id").asString(), n.path("issue_text").asString(), "", List.of()));
        }
        return out;
    }
}

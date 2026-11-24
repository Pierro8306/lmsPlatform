package pierro.lmsPlatform.service.problem.CodeReview.tools;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import java.io.StringReader;

import lombok.RequiredArgsConstructor;
import pierro.lmsPlatform.service.problem.CodeReview.model.Lang;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Finding;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.Severity;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceFile;

@Component
@RequiredArgsConstructor 
public class SpotBugsAnalyzer implements StaticAnalyzer {
private static final String SCRIPT =
        "set -eu; "+ "find /src -name '*.java' > /tmp/srcs.txt; "+ "mkdir -p /tmp/out; "+ "if ! javac -proc:none -nowarn -encoding UTF-8 "
      + "-d /tmp/out @/tmp/srcs.txt > /tmp/javac.log 2>&1; then "+ "cat /tmp/javac.log >&2; exit 10; fi; "+ "if ! spotbugs -textui -xml:withMessages "
      + "-output /tmp/sb.xml /tmp/out > /tmp/sb.log 2>&1; then "+ "cat /tmp/sb.log >&2; exit 11; fi; "+ "test -s /tmp/sb.xml; "
      + "cat /tmp/sb.xml";

    private final ToolContainer docker;

    public Set<Lang> languages() { return Set.of(Lang.JAVA); }
    public String name() { return "spotbugs"; }

    public List<Finding> analyze(Path dir, List<SourceFile> files) throws Exception {
        var r = docker.run(dir, List.of("sh", "-c", SCRIPT), 120_000);
        if (r.timedOut()) throw new IllegalStateException("spotbugs timeout");
        String xml = r.stdout();

    if (xml == null || xml.isBlank()) {
        throw new SkippedException(
            "SpotBugs không tạo được XML"
        );
    }

    String trimmed = xml.stripLeading();

    if (!trimmed.startsWith("<?xml")
            && !trimmed.startsWith("<BugCollection")) {
        throw new IllegalStateException(
            "SpotBugs output không phải XML: "
            + trimmed.substring(0, Math.min(300, trimmed.length()))
        );
    }

    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();

    f.setFeature(
        "http://apache.org/xml/features/disallow-doctype-decl",
        true
    );

    Document doc = f.newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));

        List<Finding> out = new ArrayList<>();
        NodeList bugs = doc.getElementsByTagName("BugInstance");
        for (int i = 0; i < bugs.getLength(); i++) {
            Element b = (Element) bugs.item(i);
            Element sl = child(b, "SourceLine");
            String sp = sl == null ? "" : sl.getAttribute("sourcepath");
            int line = (sl == null || sl.getAttribute("start").isEmpty()) ? 0 : Integer.parseInt(sl.getAttribute("start"));
            String path = files.stream().map(SourceFile::path).filter(p -> !sp.isEmpty() && (p.endsWith(sp) || sp.endsWith(p)))
                    .findFirst().orElse(sp);
            Severity sev = switch (b.getAttribute("priority")) { case "1" -> Severity.HIGH; case "2" -> Severity.MEDIUM; default -> Severity.LOW; };
            String c = b.getAttribute("category");
            String cat = ("SECURITY".equals(c) || "MALICIOUS_CODE".equals(c)) ? "SECURITY"
                       : "PERFORMANCE".equals(c) ? "PERFORMANCE" : "STYLE".equals(c) ? "STYLE" : "BUG";
            Element msg = child(b, "LongMessage");
            out.add(new Finding(path, line, sev, cat, "spotbugs", b.getAttribute("type"),
                    msg == null ? b.getAttribute("type") : msg.getTextContent(), "", List.of()));
        }
        return out;
    }

    private static Element child(Element e, String tag) {
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling())
            if (n instanceof Element c && c.getTagName().equals(tag)) return c;
        return null;
    }
}

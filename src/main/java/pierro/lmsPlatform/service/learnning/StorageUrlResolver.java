package pierro.lmsPlatform.service.learnning;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the object paths stored in Lesson.video_url / Lesson.attachment_urls into
 * public URLs under CLOUD_STORAGE_URL. Pure string work: never touches the network or file bytes.
 */
@Component
public class StorageUrlResolver {

    /** Separators allowed inside Lesson.attachment_urls (kept as-is in the output). */
    private static final Pattern SEPARATOR = Pattern.compile("\\s*[,;\\r\\n]+\\s*");

    private final String baseUrl;

    public StorageUrlResolver(@Value("${CLOUD_STORAGE_URL:}") String baseUrl) {
        String b = baseUrl == null ? "" : baseUrl.trim();
        while (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        this.baseUrl = b;
    }

    /** One stored value -> public URL. Absolute http(s) URLs (YouTube, external CDN...) pass through. */
    public String resolve(String stored) {
        if (!StringUtils.hasText(stored)) return stored;
        String v = stored.trim();
        String lower = v.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) return v;
        if (baseUrl.isEmpty()) return v;          // storage not configured: leave untouched

        StringBuilder path = new StringBuilder();
        for (String seg : v.split("/")) {
            if (seg.isEmpty() || seg.equals(".") || seg.equals("..")) continue;
            if (path.length() > 0) path.append('/');
            path.append(URLEncoder.encode(seg, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return baseUrl + "/" + path;
    }

    /** A separated list (comma, semicolon or newline) -> same list with every item resolved. */
    public String resolveList(String stored) {
        if (!StringUtils.hasText(stored)) return stored;
        StringBuilder out = new StringBuilder();
        Matcher m = SEPARATOR.matcher(stored);
        int last = 0;
        while (m.find()) {
            out.append(resolve(stored.substring(last, m.start())));
            out.append(m.group());
            last = m.end();
        }
        out.append(resolve(stored.substring(last)));
        return out.toString();
    }
}

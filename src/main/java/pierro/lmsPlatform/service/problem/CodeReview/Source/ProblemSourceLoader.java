package pierro.lmsPlatform.service.problem.CodeReview.Source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pierro.lmsPlatform.service.problem.CodeReview.model.Lang;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceBundle;
import pierro.lmsPlatform.service.problem.CodeReview.model.ReviewModels.SourceFile;

/**
 * Đóng gói source của một bài nộp thành thư mục tạm để các tool tĩnh đọc.
 * Nơi gọi load() PHẢI gọi cleanup(bundle) trong finally khi xong việc.
 */
@Component
@RequiredArgsConstructor
public class ProblemSourceLoader {
    private static final Logger log = LoggerFactory.getLogger(ProblemSourceLoader.class);

    private static final String TMP_PREFIX = "cr_src_";
    private static final int MAX_STATEMENT_CHARS = 4000;
    private static final Pattern JAVA_CLASS =
            Pattern.compile("public\\s+(?:final\\s+)?class\\s+(\\w+)"); //nhận diện tên class java

    private final TargetReader reader;

    public SourceBundle load(String submissionId) throws IOException {
        var info = reader.problem(submissionId);// cần hỏi tại sao không dùng info
        Lang lang = Lang.fromName(info.language())
                .orElseThrow(() -> new IllegalArgumentException("Không hỗ trợ ngôn ngữ này"));

        String name = lang.defaultFile();
        if (lang == Lang.JAVA) {
            Matcher m = JAVA_CLASS.matcher(info.sourceCode() == null ? "" : info.sourceCode());
            if (m.find()) name = m.group(1) + ".java";
        }

        SourceFile sf = SourceFile.of(name, lang, info.sourceCode());

        Path dir = Files.createTempDirectory(TMP_PREFIX);
        try {
            Path file = dir.resolve(name);
            Files.writeString(file, sf.content(), StandardCharsets.UTF_8);
            makeReadableByContainer(dir, file);                            // cấp quyền cho container đọc thư mục và file
        } catch (IOException | RuntimeException e) {
            deleteQuietly(dir);   // xóa file nếu lỗi giữa chừng, không để lại thư mục rác
            throw e;
        }

        String stmt = info.statement() == null ? "" : info.statement();
        if (stmt.length() > MAX_STATEMENT_CHARS) {
            stmt = stmt.substring(0, MAX_STATEMENT_CHARS);
        }
        return new SourceBundle(dir, List.of(sf), "Bài tập: " + info.title() + "\n" + stmt);
    }

    /** Xóa thư mục tạm của bundle. An toàn khi gọi nhiều lần hoặc với null. */
    public void cleanup(SourceBundle bundle) {
        if (bundle != null) deleteQuietly(bundle.workDir());
    }

    /**
     * Container chạy bằng root nhưng đã --cap-drop ALL (mất quyền bỏ qua phân quyền file),
     * nên thư mục 700 của user khác sẽ không đọc được trên Linux. Cấp quyền đọc cho "other".
     */
    private static void makeReadableByContainer(Path dir, Path file) throws IOException {
        try {
            Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwxr-xr-x"));
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-r--r--"));
        } catch (UnsupportedOperationException e) {
            // Windows không có POSIX permission; Docker Desktop tự xử lý việc chia sẻ file
        }
    }

    // xóa thư mục
    private static void deleteQuietly(Path dir) {
        if (dir == null || dir.getFileName() == null
                || !dir.getFileName().toString().startsWith(TMP_PREFIX)) {
            return;   // chỉ xóa thư mục do chính loader này tạo
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    log.warn("Không xóa được {}", p, e);
                }
            });
        } catch (IOException e) {
            log.warn("Không dọn được thư mục tạm {}", dir, e);
        }
    }


    // Có thẻ viết hàm đóng gói nhiều file nếu phát triển thêm
}
package pierro.lmsPlatform.service.judge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

final class Fs {
    private static final Logger log = LoggerFactory.getLogger(Fs.class);

    private Fs() {}

    /**
     * Cho "others" đọc + duyệt thư mục (r-x), KHÔNG cho ghi.
     * Container ghi vào thư mục này bằng root + CAP_DAC_OVERRIDE; user (nobody) chỉ đọc được.
     * (no-op trên Windows)
     */
    static void openDir(Path p) {
        File f = p.toFile();
        f.setReadable(true, false);
        f.setExecutable(true, false);
    }

    static void openFile(Path p) {
        p.toFile().setReadable(true, false);
    }

    /** Xóa đệ quy: file con trước, thư mục cha sau. Không ném lỗi, nhưng log cảnh báo nếu xóa thất bại. */
    static void deleteQuietly(Path root) {
        if (root == null || !Files.exists(root, LinkOption.NOFOLLOW_LINKS)) return;
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try { Files.deleteIfExists(file); }
                    catch (IOException e) { log.warn("Cannot delete {}: {}", file, e.getMessage()); }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.warn("Cannot visit {}: {}", file, exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    try { Files.deleteIfExists(dir); }
                    catch (IOException e) { log.warn("Cannot delete dir {}: {}", dir, e.getMessage()); }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Cleanup of {} failed: {}", root, e.getMessage());
        }
    }
}

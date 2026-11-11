package pierro.lmsPlatform.Service.Judge;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pierro.lmsPlatform.DTO.Request.Judge.LanguageConfig;
import pierro.lmsPlatform.Entity.Problem.Verdict;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chạy code trong Docker.
 *  - compile(): 1 container (root, giới hạn tài nguyên), ghi binary vào workDir.
 *  - run():     1 container MỚI cho mỗi test.
 *
 * Mô hình đặc quyền khi run():
 *  - Script giám sát chạy bằng ROOT trong container (chỉ có cap SETUID/SETGID/DAC_OVERRIDE/KILL).
 *  - Chương trình của user được hạ quyền xuống uid 65534 bằng `setpriv`, và bọc trong `timeout -s KILL`
 *    (do root chạy) nên user KHÔNG thể kill timeout, KHÔNG thể ghi vào /io (thư mục do host sở hữu, mode 755),
 *    KHÔNG thể truy cập /proc/1 => không thể giả mạo metadata (JUDGE_META) hay tạo symlink/file rác ở /io.
 *  - Đọc file kết quả trên host luôn dùng NOFOLLOW_LINKS (phòng thủ nhiều lớp).
 *
 * Thời gian/bộ nhớ đo BÊN TRONG container:
 *  - thời gian: date +%s%N trước/sau chương trình (wall-clock, gồm cả JVM startup với Java)
 *  - bộ nhớ:    cgroup memory.peak (v2, kernel >= 5.19) hoặc memory.max_usage_in_bytes (v1) -> chỉ là ước lượng
 */
@Component
public class DockerSandbox {
    private static final Logger log = LoggerFactory.getLogger(DockerSandbox.class);

    static final int COMPILE_TIMEOUT_MS = 20_000;
    static final int HARD_MARGIN_MS = 500;               // timeout trong container = TL + margin
    static final int START_GRACE_MS = 3_000;             // dư cho docker khởi động (timeout phía host, phòng thủ lớp 2)
    static final int MAX_OUTPUT_BYTES = 8 * 1024 * 1024;
    static final int MAX_MSG_BYTES = 4 * 1024;
    static final long MAX_COMPILE_FILE_BYTES = 64L * 1024 * 1024;
    static final long PULL_TIMEOUT_MS = 10 * 60_000L;     // pull image lần đầu có thể rất lâu
    private static final String META_PREFIX = "JUDGE_META ";
    private static final String LABEL_KEY = "judge.instance";

    /**
     * Định danh instance, gắn vào label của mọi container để cleanupOrphans chỉ dọn container CỦA MÌNH.
     * Nếu chạy nhiều instance trên cùng 1 Docker host, đặt judge.instance-id khác nhau cho từng instance.
     */
    @Value("${judge.instance-id:default}")
    private String instanceId = "default";

    /** Các image đã xác nhận có sẵn local (tránh `docker run` tự pull và ăn vào compile/run timeout). */
    private final Set<String> readyImages = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Object> pullLocks = new ConcurrentHashMap<>();

    public record CompileResult(boolean ok, Verdict failVerdict, String message) {}

    public enum RunStatus { OK, TLE, MLE, RTE, OLE, SYSTEM_ERROR }

    public record RunResult(RunStatus status, String stdout, String stderr,
                            int exitCode, int timeMs, int memoryKb) {
        static RunResult of(RunStatus s, String stderr, int exit, int t, int m) {
            return new RunResult(s, "", stderr, exit, t, m);
        }
    }

    // ------------------------------------------------------------------ STARTUP

    @PostConstruct
    void init() {
        cleanupOrphans();
        warmUpImages();
    }

    /** Pull trước image của mọi ngôn ngữ ở thread nền (không chặn việc khởi động app). */
    private void warmUpImages() {
        Thread t = new Thread(() -> {
            for (LanguageConfig l : LanguageConfig.all()) {
                try {
                    ensureImage(l.image());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    log.warn("Docker not available, skip image warm-up: {}", e.getMessage());
                    return;
                }
            }
        }, "judge-image-warmup");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Dọn các container còn sót lại từ lần chạy trước (app crash, kill -9...).
     * Chỉ dọn container có label của instance này VÀ tên bắt đầu bằng "judge_"
     * (filter name là regex/chuỗi con nên phải neo ^ để không đụng container khác).
     */
    void cleanupOrphans() {
        try {
            ProcessRunner.Result r = ProcessRunner.run(
                    List.of("docker", "ps", "-aq",
                            "--filter", "label=" + LABEL_KEY + "=" + safeInstanceId(),
                            "--filter", "name=^judge_"),
                    10_000, MAX_MSG_BYTES, null);
            if (r.timedOut() || r.exitCode() != 0) {
                log.warn("Cannot list orphan containers: {}", r.stderr());
                return;
            }
            List<String> ids = r.stdout().lines().map(String::trim).filter(s -> !s.isEmpty()).toList();
            if (ids.isEmpty()) return;
            List<String> cmd = new ArrayList<>(List.of("docker", "rm", "-f"));
            cmd.addAll(ids);
            ProcessRunner.run(cmd, 30_000, MAX_MSG_BYTES, null);
            log.info("Removed {} orphan judge container(s)", ids.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.warn("Docker not available at startup: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------ COMPILE

    public CompileResult compile(Path workDir, LanguageConfig lang) {
        if (!lang.needsCompile()) return new CompileResult(true, null, "");

        String name = newName();
        List<String> cmd = baseCmd(name, 768, 256, 2);
        cmd.addAll(List.of(
                "--cap-add", "DAC_OVERRIDE",                       // root ghi được vào workDir (host sở hữu, 755)
                "--read-only",
                "--tmpfs", "/tmp:rw,noexec,nosuid,size=256m",
                "-e", "HOME=/tmp",
                "--ulimit", "fsize=" + MAX_COMPILE_FILE_BYTES,     // chống tạo file khổng lồ (.incbin, template bomb...)
                "--mount", mount(workDir, "/app", false), "-w", "/app",
                lang.image(), "sh", "-c", lang.compileCmd()));
        try {
            // Đảm bảo image có sẵn: nếu không, `docker run` sẽ pull và làm compile timeout oan (CE)
            if (!ensureImage(lang.image())) return new CompileResult(false, Verdict.SE, "Judge system error");

            ProcessRunner.Result r = ProcessRunner.run(cmd, COMPILE_TIMEOUT_MS, MAX_MSG_BYTES, () -> kill(name));
            if (r.timedOut()) {
                // daemon treo/quá tải thì là lỗi hệ thống, không được quy cho người nộp bài
                if (!daemonResponsive()) {
                    log.error("Docker daemon unresponsive during compile");
                    return new CompileResult(false, Verdict.SE, "Judge system error");
                }
                return new CompileResult(false, Verdict.CE, "Compile time limit exceeded");
            }
            if (r.exitCode() == 0) return new CompileResult(true, null, "");
            if (r.exitCode() == 125) { // lỗi của docker daemon/CLI
                log.error("Docker error on compile: {}", r.stderr());
                return new CompileResult(false, Verdict.SE, "Judge system error");
            }
            String msg = (r.stderr() + "\n" + r.stdout()).replace("/app/", "").trim();
            if (msg.isEmpty() && r.exitCode() == 137) msg = "Compiler was killed (out of memory?)";
            return new CompileResult(false, Verdict.CE, trim(msg));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            kill(name);
            return new CompileResult(false, Verdict.SE, "Interrupted");
        } catch (IOException e) {
            log.error("Cannot start docker", e);
            return new CompileResult(false, Verdict.SE, "Judge system error");
        }
    }

    // ------------------------------------------------------------------ RUN

    public RunResult run(Path workDir, LanguageConfig lang, String input, int timeLimitMs, int memLimitKb) {
        String name = newName();
        Path ioDir = null;
        try {
            if (!ensureImage(lang.image()))
                return RunResult.of(RunStatus.SYSTEM_ERROR, "Judge system error", -1, 0, 0);

            ioDir = Files.createTempDirectory("judge_io_");
            Fs.openDir(ioDir);                       // others: r-x (user KHÔNG ghi được vào /io)
            Path inFile = ioDir.resolve("input.txt");
            Files.writeString(inFile, input == null ? "" : input, StandardCharsets.UTF_8);
            Fs.openFile(inFile);

            int heapMb = Math.max(16, (memLimitKb + 1023) / 1024);
            int containerMb = heapMb + lang.memoryOverheadMb();
            String runCmd = lang.runCmd().replace("{MEM_MB}", String.valueOf(heapMb));
            int hardMs = timeLimitMs + HARD_MARGIN_MS;

            List<String> cmd = baseCmd(name, containerMb, lang.pidsLimit(), 1);
            cmd.addAll(List.of(
                    "--read-only",
                    "--tmpfs", "/tmp:rw,noexec,nosuid,size=16m",
                    // script giám sát chạy root; cần: hạ quyền (SETUID/SETGID), ghi /io (DAC_OVERRIDE), kill (KILL)
                    "--cap-add", "SETUID", "--cap-add", "SETGID",
                    "--cap-add", "DAC_OVERRIDE", "--cap-add", "KILL",
                    "-e", "HOME=/tmp",
                    "-e", "PYTHONDONTWRITEBYTECODE=1",
                    "--ulimit", "core=0",
                    "--ulimit", "fsize=" + MAX_OUTPUT_BYTES,                    // chống ghi output vô hạn
                    "--ulimit", "stack=" + (containerMb * 1024L * 1024L),       // stack = giới hạn bộ nhớ
                    "--mount", mount(workDir, "/app", true),
                    "--mount", mount(ioDir, "/io", false),
                    "-w", "/app",
                    lang.image(), "sh", "-c", script(runCmd, hardMs)));

            ProcessRunner.Result r = ProcessRunner.run(cmd, (long) hardMs + START_GRACE_MS, MAX_MSG_BYTES, () -> kill(name));

            // Quá hạn phía host: timeout trong container (do root chạy, user không né được) đáng lẽ đã kết thúc
            // chương trình từ lâu. Đây là sự cố hạ tầng (docker chậm/treo), KHÔNG phải TLE của người nộp bài.
            if (r.timedOut()) {
                log.error("docker run exceeded host-side timeout ({} ms) - infrastructure problem", hardMs + START_GRACE_MS);
                return RunResult.of(RunStatus.SYSTEM_ERROR, "Judge system error", -1, 0, 0);
            }

            Meta meta = parseMeta(r.stdout());
            if (meta == null) {
                if (r.exitCode() == 137) return RunResult.of(RunStatus.MLE, "", 137, 0, 0);
                log.warn("No metadata from container (exit {}): {}", r.exitCode(), r.stderr());
                return RunResult.of(RunStatus.SYSTEM_ERROR, "Judge system error", r.exitCode(), 0, 0);
            }

            Path outFile = ioDir.resolve("stdout.txt");
            long outSize = regularFileSize(outFile);
            String stdout = readCapped(outFile, MAX_OUTPUT_BYTES);
            String stderr = readCapped(ioDir.resolve("stderr.txt"), MAX_MSG_BYTES);

            RunStatus status;
            if (meta.exit == 153 || outSize >= MAX_OUTPUT_BYTES) status = RunStatus.OLE;     // SIGXFSZ
            else if (meta.oomKills > 0) status = RunStatus.MLE;                              // cgroup xác nhận có OOM-kill
            else if (meta.exit == 137)                                                         // SIGKILL: do timeout hoặc OOM killer
                status = meta.timeMs >= hardMs - 1 ? RunStatus.TLE : RunStatus.MLE;
            else if (meta.timeMs > timeLimitMs) status = RunStatus.TLE;
            else if (meta.exit != 0) status = looksLikeOom(stderr) ? RunStatus.MLE : RunStatus.RTE;
            else status = RunStatus.OK;

            return new RunResult(status, stdout, stderr, meta.exit, meta.timeMs, meta.memoryKb);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            kill(name);
            return RunResult.of(RunStatus.SYSTEM_ERROR, "Interrupted", -1, 0, 0);
        } catch (IOException e) {
            log.error("Sandbox IO error", e);
            return RunResult.of(RunStatus.SYSTEM_ERROR, "Judge system error", -1, 0, 0);
        } finally {
            Fs.deleteQuietly(ioDir);
        }
    }

    // ------------------------------------------------------------------ helpers

    private String safeInstanceId() {
        String id = instanceId == null || instanceId.isBlank() ? "default" : instanceId;
        return id.replaceAll("[^A-Za-z0-9_.-]", "_");
    }

    /** @return true nếu image đã có local (pull nếu thiếu); false nếu không thể có image. */
    private boolean ensureImage(String image) throws IOException, InterruptedException {
        if (readyImages.contains(image)) return true;
        synchronized (pullLocks.computeIfAbsent(image, k -> new Object())) {
            if (readyImages.contains(image)) return true;
            ProcessRunner.Result r = ProcessRunner.run(
                    List.of("docker", "image", "inspect", image), 15_000, MAX_MSG_BYTES, null);
            if (r.timedOut() || r.exitCode() != 0) {
                log.info("Pulling image {}", image);
                r = ProcessRunner.run(List.of("docker", "pull", image), PULL_TIMEOUT_MS, MAX_MSG_BYTES, null);
                if (r.timedOut() || r.exitCode() != 0) {
                    log.error("Cannot pull image {}: {}", image, r.stderr());
                    return false;
                }
            }
            readyImages.add(image);
            return true;
        }
    }

    /** Docker daemon còn phản hồi không (dùng để phân biệt lỗi hạ tầng với lỗi của người nộp bài). */
    private boolean daemonResponsive() throws InterruptedException {
        try {
            ProcessRunner.Result r = ProcessRunner.run(
                    List.of("docker", "version", "--format", "{{.Server.Version}}"), 5_000, 1024, null);
            return !r.timedOut() && r.exitCode() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    /** Các cờ bảo mật dùng chung cho cả compile và run. */
    private List<String> baseCmd(String name, int memMb, int pids, int cpus) {
        return new ArrayList<>(List.of(
                "docker", "run", "--rm", "--name", name,
                "--network", "none",
                "--label", LABEL_KEY + "=" + safeInstanceId(),
                "--memory", memMb + "m", "--memory-swap", memMb + "m",   // cấm dùng swap
                "--cpus", String.valueOf(cpus),
                "--pids-limit", String.valueOf(pids),                   // chống fork bomb
                "--cap-drop", "ALL",
                "--security-opt", "no-new-privileges",
                "--ulimit", "nofile=256"));
    }

    /**
     * Script chạy (bằng root) trong container:
     *  - timeout -s KILL: do root chạy => user không kill/né được
     *  - setpriv: hạ chương trình user xuống nobody
     *  - metadata chỉ root in ra stdout của container; stdout của user đã chuyển hướng vào file
     * runCmd lấy từ LanguageConfig (tin cậy), không chứa dữ liệu của user.
     */
    private static String script(String runCmd, int hardMs) {
        String t = String.format(Locale.ROOT, "%.3f", hardMs / 1000.0);
        return "S=$(date +%s%N)\n"
             + "timeout -s KILL " + t + " setpriv --reuid=65534 --regid=65534 --clear-groups "
             + runCmd + " < /io/input.txt > /io/stdout.txt 2> /io/stderr.txt\n"
             + "C=$?\n"
             + "E=$(date +%s%N)\n"
             + "P=$(cat /sys/fs/cgroup/memory.peak 2>/dev/null || cat /sys/fs/cgroup/memory/memory.max_usage_in_bytes 2>/dev/null || echo 0)\n"
             + "O=$(sed -n 's/^oom_kill //p' /sys/fs/cgroup/memory.events /sys/fs/cgroup/memory/memory.oom_control 2>/dev/null | head -n 1)\n"
             + "echo \"" + META_PREFIX + "$C $(( (E-S)/1000000 )) $(( P/1024 )) ${O:-0}\"\n";
    }

    /** oomKills: số lần OOM-kill do cgroup ghi nhận (0 nếu kernel không cung cấp). */
    record Meta(int exit, int timeMs, int memoryKb, int oomKills) {}

    /** Lấy dòng meta CUỐI CÙNG (do script in ra sau cùng). */
    static Meta parseMeta(String dockerStdout) {
        Meta found = null;
        for (String line : dockerStdout.split("\\R")) {
            if (!line.startsWith(META_PREFIX)) continue;
            try {
                String[] p = line.substring(META_PREFIX.length()).trim().split("\\s+");
                found = new Meta(Integer.parseInt(p[0]),
                        (int) Math.min(Integer.MAX_VALUE, Long.parseLong(p[1])),
                        (int) Math.min(Integer.MAX_VALUE, Long.parseLong(p[2])),
                        p.length > 3 ? (int) Math.min(Integer.MAX_VALUE, Long.parseLong(p[3])) : 0);
            } catch (RuntimeException e) {
                found = null;
            }
        }
        return found;
    }

    private static boolean looksLikeOom(String stderr) {
        return stderr.contains("bad_alloc") || stderr.contains("OutOfMemoryError")
                || stderr.contains("MemoryError") || stderr.contains("Cannot allocate memory");
    }

    /**
     * `docker kill` container. Phải xóa cờ interrupt TẠM THỜI: nếu cờ đang bật (ta vừa bị interrupt),
     * Process.waitFor() trong ProcessRunner sẽ ném InterruptedException ngay và `docker kill` bị
     * destroyForcibly trước khi kịp chạy -> container không bao giờ bị kill.
     */
    private void kill(String name) {
        boolean interrupted = Thread.interrupted();
        try {
            ProcessRunner.run(List.of("docker", "kill", name), 5000, 1024, null);
        } catch (InterruptedException e) {
            interrupted = true;
        } catch (IOException e) {
            log.warn("docker kill {} failed: {}", name, e.getMessage());
        } finally {
            if (interrupted) Thread.currentThread().interrupt();   // trả lại cờ cho phía gọi
        }
    }

    private static String mount(Path p, String target, boolean readOnly) {
        String src = p.toAbsolutePath().toString().replace("\\", "/");
        return "type=bind,src=" + src + ",dst=" + target + (readOnly ? ",readonly" : "");
    }

    private static String newName() {
        return "judge_" + UUID.randomUUID().toString().replace("-", "");
    }

    /** Kích thước file THƯỜNG (không theo symlink); 0 nếu không tồn tại / không phải file thường. */
    private static long regularFileSize(Path p) throws IOException {
        if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS)) return 0;
        BasicFileAttributes a = Files.readAttributes(p, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        return a.isRegularFile() ? a.size() : 0;
    }

    /** Chỉ đọc file thường, KHÔNG theo symlink (chống đọc file tùy ý của host). */
    private static String readCapped(Path p, int max) throws IOException {
        if (!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) return "";
        try (InputStream in = Files.newInputStream(p, LinkOption.NOFOLLOW_LINKS)) {
            return new String(in.readNBytes(max), StandardCharsets.UTF_8);
        }
    }

    private static String trim(String s) {
        s = s == null ? "" : s.trim();
        return s.length() > MAX_MSG_BYTES ? s.substring(0, MAX_MSG_BYTES) + "\n...(truncated)" : s;
    }
}
